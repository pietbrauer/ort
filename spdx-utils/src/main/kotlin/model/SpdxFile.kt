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

package org.ossreviewtoolkit.spdx.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Describes a software file
 */
data class SpdxFile(
    /**
     * Name as given by package originator.
     * Cardinality: Mandatory, one.
     */
    val name: String,

    /**
     * Identifier for the package.
     * Cardinality: Mandatory, one.
     */
    @JsonProperty("SPDXID")
    val id: String,

//    /**
//     */
//    @JsonProperty("fileTypes")
//    val type: String? = null,
//
//    /**
//     */
//    @JsonProperty("fileChecksum")
//    val checksum: String? = null,
//
//    /**
//     */
//    @JsonProperty("fileComment")
//    val comment: String? = null,
//
//    /**
//     */
//    @JsonProperty("fileContributors")
//    val fileContributors: String? = null,

//    /**
//     */
//    val licenseConcluded: String? = null,

    val licenseInfoFromFiles: List<String>,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val licenseComments: String? = null
) : Comparable<SpdxFile> {
    companion object {
        /**
         * A constant for a [SpdxPackage] where all properties are empty.
         */
        @JvmField
        val EMPTY = SpdxFile(
            name = "",
            id = "",
            licenseInfoFromFiles = emptyList()
        )
    }

    /**
     * A comparison function to sort files by their SPDX id.
     */
    override fun compareTo(other: SpdxFile) = id.compareTo(other.id)
}
