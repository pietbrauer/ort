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

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Provides information for forward and backward compatibility to tools processing the SPDX document.
 * Cardinality: Mandatory, one.
 */
data class SpdxDocument(
    /**
     * Identifier for the document.
     * Cardinality: mandatory, one.
     */
    @JsonProperty("SPDXID")
    val id: String,

    /**
     * Name given to the document by its creator.
     * Cardinality: mandatory, one.
     */
     val name: String,

    /**
     * Namespace for the document as a unique absolute Uniform Resource Identifier (URI).
     * Cardinality: mandatory, one.
     */
//    @JsonProperty("DocumentNamespace")
//    val namespace: String,

    // FIXME Implement ExternalDocumentRef per
    // https://spdx.org/spdx-specification-21-web-version#h.h430e9ypa0j9
    /**
     * External SPDX documents referenced within this document.
     * Cardinality: Optional, one or many.
     */
    /*
    @JsonProperty("ExternalDocumentRef")
    val externalDocumentRef: String?,
    */

    /**
     * External SPDX documents referenced within this document.
     * Cardinality: Optional, one or many.
     */
    @JsonProperty("licenseListVersion")
    val licenseListVersion: String?,

    /**
     * Information on the creation of this document.
     */
//    val creationInfo: SpdxCreationInfo,

    /**
     * Comment from document creators to document consumers.
     * Cardinality: Optional, one.
     */
    @JsonProperty("comment")
    val creatorComment: String? = null,

    /**
     * Comment from document creators to document consumers.
     * Cardinality: Optional, one.
     */
    val documentDescribes: SpdxDocumentDescribes
) {
    /**
     * Reference number to applicable SPDX version, used to determine how to parse and interpret the document.
     * Cardinality: mandatory, one.
     */
    @JsonProperty("SPDXVersion")
    val version: String = "2.2"

    /**
     * License of this document which is per the SPDX specification must be CC0-1.0.
     * Cardinality: mandatory, one.
     */
    @JsonProperty("dataLicense")
    val license: String = "CC0-1.0"
}
