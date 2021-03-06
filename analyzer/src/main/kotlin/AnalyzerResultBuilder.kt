/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
 * Copyright (C) 2020 Bosch.IO GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.analyzer

import org.ossreviewtoolkit.model.AnalyzerResult
import org.ossreviewtoolkit.model.CuratedPackage
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.OrtIssue
import org.ossreviewtoolkit.model.Project
import org.ossreviewtoolkit.model.ProjectAnalyzerResult
import org.ossreviewtoolkit.model.createAndLogIssue
import org.ossreviewtoolkit.utils.log

class AnalyzerResultBuilder(private val curationProvider: PackageCurationProvider = PackageCurationProvider.EMPTY) {
    private val projects = sortedSetOf<Project>()
    private val packages = sortedSetOf<CuratedPackage>()
    private val issues = sortedMapOf<Identifier, List<OrtIssue>>()

    fun build() = AnalyzerResult(projects, packages, issues)

    fun addResult(projectAnalyzerResult: ProjectAnalyzerResult): AnalyzerResultBuilder {
        // TODO: It might be, e.g. in the case of PIP "requirements.txt" projects, that different projects with
        //       the same ID exist. We need to decide how to handle that case.
        val existingProject = projects.find { it.id == projectAnalyzerResult.project.id }

        if (existingProject != null) {
            val existingDefinitionFileUrl = existingProject.let {
                "${it.vcsProcessed.url}/${it.definitionFilePath}"
            }
            val incomingDefinitionFileUrl = projectAnalyzerResult.project.let {
                "${it.vcsProcessed.url}/${it.definitionFilePath}"
            }

            val issue = createAndLogIssue(
                source = "analyzer",
                message = "Multiple projects with the same id '${existingProject.id.toCoordinates()}' " +
                        "found. Not adding the project defined in '$incomingDefinitionFileUrl' to the " +
                        "analyzer results as it duplicates the project defined in " +
                        "'$existingDefinitionFileUrl'."
            )

            val projectIssues = issues.getOrDefault(existingProject.id, emptyList())
            issues[existingProject.id] = projectIssues + issue
        } else {
            projects += projectAnalyzerResult.project

            packages += projectAnalyzerResult.packages.map { pkg ->
                val curations = curationProvider.getCurationsFor(pkg.id)
                curations.fold(pkg.toCuratedPackage()) { cur, packageCuration ->
                    log.debug {
                        "Applying curation '$packageCuration' to package '${pkg.id.toCoordinates()}'."
                    }

                    packageCuration.apply(cur)
                }
            }

            if (projectAnalyzerResult.issues.isNotEmpty()) {
                issues[projectAnalyzerResult.project.id] = projectAnalyzerResult.issues
            }
        }

        return this
    }
}
