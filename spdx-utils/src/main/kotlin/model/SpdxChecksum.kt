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

import com.fasterxml.jackson.annotation.JsonRootName

/**
 * Checksum to be able to verify provided [SpdxDocument], [SpdxPackage] or [SpdxFile] against actual files.
 */
@JsonRootName(value = "Checksum")
data class SpdxChecksum(
    /**
     * Type of the [SpdxAnnotation]
     * Cardinality: Mandatory, one.
     */
    val algorithm: SpdxChecksumAlgorithm,

    /**
     * Value of the [SpdxChecksum].
     * Cardinality: Mandatory, one.
     */
    val value: String
) : Comparable<SpdxChecksum> {
    /**
     * A comparison function to sort [SpdxChecksum]s.
     */
    override fun compareTo(other: SpdxChecksum) =
        compareValuesBy(
            this,
            other,
            compareBy(SpdxChecksum::value)
                .thenBy(SpdxChecksum::algorithm)
        ) { it }
}
