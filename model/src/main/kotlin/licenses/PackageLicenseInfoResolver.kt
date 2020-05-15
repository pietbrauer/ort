/*
 * Copyright (C) 2017-2020 HERE Europe B.V.
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

package org.ossreviewtoolkit.model.licenses

import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.utils.FindingsMatcher
import org.ossreviewtoolkit.spdx.SpdxExpression
import org.ossreviewtoolkit.spdx.SpdxSingleLicenseExpression

class PackageLicenseInfoResolver(val provider: PackageLicenseInfoProvider) {
    private val licenseInfo: MutableMap<Identifier, ResolvedPackageLicenseInfo> = mutableMapOf()

    /**
     * Get the [PackageLicenseInfo] for the project or package identified by [id].
     * TODO: Add options to filter output, e.g. "filter excluded findings".
     */
    fun resolveLicenseInfo(id: Identifier) = licenseInfo.getOrPut(id) { createLicenseInfo(id) }

    private fun createLicenseInfo(id: Identifier): ResolvedPackageLicenseInfo {
        val licenseInfo = provider.get(id)

        val concludedLicenses = licenseInfo.concludedLicenseInfo.concludedLicense?.decompose().orEmpty()
        val declaredLicenses = licenseInfo.declaredLicenseInfo.processed.spdxExpression?.decompose().orEmpty()
        val detectedLicenses = licenseInfo.detectedLicenseInfo.findings.flatMap { (_, findings) ->
            findings.licenses
                .mapTo(mutableSetOf()) { it.license }
                .map { SpdxExpression.parse(it) }
                .filter { it is SpdxSingleLicenseExpression }
                .map { it as SpdxSingleLicenseExpression }
        }

        val allLicenses = concludedLicenses + declaredLicenses + detectedLicenses
        val resolvedLicenses = allLicenses.map { resolveLicense(it, licenseInfo) }

        return ResolvedPackageLicenseInfo(id, resolvedLicenses)
    }

    private fun resolveLicense(
        license: SpdxSingleLicenseExpression,
        licenseInfo: PackageLicenseInfo
    ): ResolvedLicenseInfo {
        val sources = mutableSetOf<LicenseSource>()

        // Handle concluded license.
        if (license in licenseInfo.concludedLicenseInfo.concludedLicense?.decompose().orEmpty()) {
            sources += LicenseSource.CONCLUDED
        }

        // Handle declared license.
        if (license in licenseInfo.declaredLicenseInfo.processed.spdxExpression?.decompose().orEmpty()) {
            sources += LicenseSource.DECLARED
        }
        val originalDeclaredLicense =
            licenseInfo.declaredLicenseInfo.processed.mapped.entries.find { it.value == license }?.key

        // Handle detected license.
        val locations = mutableSetOf<ResolvedTextLocation>()
        val copyrights = mutableSetOf<ResolvedCopyrightFinding>()

        licenseInfo.detectedLicenseInfo.findings.forEach { (provenance, findings) ->
            // TODO: Process copyright statements.
            // TODO: Filter copyright garbage.
            val matchedFindings = FindingsMatcher().match(findings.licenses, findings.copyrights)
            matchedFindings.find { it.license == license.toString() }?.let { licenseFindings ->
                licenseFindings.locations.mapTo(locations) {
                    // TODO: Apply license finding curations.
                    // TODO: Apply path excludes.
                    ResolvedTextLocation(provenance, it.path, it.startLine, it.endLine, emptyList(), emptyList())
                }
            }
        }

        return ResolvedLicenseInfo(license, sources, originalDeclaredLicense, locations, copyrights)
    }
}
