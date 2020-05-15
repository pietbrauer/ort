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
import org.ossreviewtoolkit.model.Provenance
import org.ossreviewtoolkit.model.config.LicenseFindingCuration
import org.ossreviewtoolkit.model.config.PathExclude
import org.ossreviewtoolkit.spdx.SpdxSingleLicenseExpression

/**
 * Resolved license information about a package (or project).
 */
data class ResolvedPackageLicenseInfo(
    /**
     * The identifier of the package (or project).
     */
    val id: Identifier,

    /**
     * The lost of [ResolvedLicenseInfo]s for this package (or project).
     */
    val licenses: List<ResolvedLicenseInfo>
)

/**
 * Resolved information for a single license.
 */
data class ResolvedLicenseInfo(
    /**
     * The license.
     */
    val license: SpdxSingleLicenseExpression,

    /**
     * The sources where this license was found.
     */
    val sources: Set<LicenseSource>,

    /**
     * The original declared license, if this license is a processed declared license, or null.
     */
    val originalDeclaredLicense: String?,

    /**
     * All text locations where this license was found.
     */
    val locations: Set<ResolvedTextLocation>,

    /**
     * All copyrights there were associated to this license.
     */
    val copyrights: Set<ResolvedCopyrightFinding>
)

/**
 * A resolved text location.
 */
data class ResolvedTextLocation(
    /**
     * The provenance of the file.
     */
    val provenance: Provenance,

    /**
     * The path of the file.
     */
    val path: String,

    /**
     * The start line of the text location.
     */
    val startLine: Int,

    /**
     * The end line of the text location.
     */
    val endLine: Int,

    /**
     * All [LicenseFindingCuration]s that were applied while resolving the text location.
     */
    val appliedCurations: List<LicenseFindingCuration>,

    /**
     * All matching [PathExclude]s matching this [path].
     */
    val matchingPathExcludes: List<PathExclude>
)

/**
 * A resolved copyright finding.
 */
data class ResolvedCopyrightFinding(
    /**
     * The resolved copyright statement.
     */
    val statement: String,

    /**
     * The original copyright statement, if this statement was processed.
     */
    val originalStatements: List<String>,

    /**
     * All text locations where this copyright was found.
     */
    val locations: Set<ResolvedTextLocation>
)
