package org.ossreviewtoolkit.spdx.model

/**
 * [SpdxExternalDocumentReference]s are used ny [SpdxDocument]s to list all external documents which are referenced from
 * that particular [SpdxDocument], see also
 * https://github.com/spdx/spdx-spec/blob/master/chapters/2-document-creation-information.md#26-external-document-references-
 */
data class SpdxExternalDocumentReference(
    /**
     * The identifier referencing the external [SpdxDocument] in the format: "DocumentRef-${id-string}"
     */
    val externalDocumentId: String = "",

    /**
     * The checksum corresponding to the external [SpdxDocument] referenced by this [SpdxExternalDocumentReference].
     */
    val checksum: SpdxChecksum,

    /**
     * The SPDX Document URI of the external [SpdxDocument] referenced by this [SpdxExternalDocumentReference].
     */
    val spdxDocument: String
) {
    init {
        require(externalDocumentId.isNotBlank()) { "The 'externalDocumentId' must not be blank." }

        require(spdxDocument.isNotBlank()) { "The 'spdxDocument' must not be blank." }
    }
}
