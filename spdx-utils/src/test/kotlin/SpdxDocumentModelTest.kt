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

package org.ossreviewtoolkit.spdx

import com.fasterxml.jackson.databind.ObjectMapper

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

import org.ossreviewtoolkit.spdx.model.SpdxDocument

private fun format(value: String, mapper: ObjectMapper): String =
    mapper.readTree(value).let { node ->
        mapper.writeValueAsString(node)
    }

private fun formatYaml(yaml: String): String = format(yaml, SpdxModelSerializer.createYamlMapper())

private fun formatJson(json: String): String = format(json, SpdxModelSerializer.createJsonMapper())

/**
 * This test uses the following test assets copied from the SPDX 2.2.1 specification examples.
 *
 * 1. https://github.com/spdx/spdx-spec/blob/development/v2.2.1/examples/SPDXYAMLExample-2.2.spdx.yaml#L3-L392.
 * 2. https://github.com/spdx/spdx-spec/blob/development/v2.2.1/examples/SPDXJSONExample-v2.2.spdx.json#L3-L279.
 *
 * The only changes made to above files are:
 *
 * 1. Fixed https://github.com/spdx/spdx-spec/issues/409
 * 2. removed the ranges property in the "*-no-ranges*" files as ranges cannot be implemented due to unclarities in
 *    the specification.
 */
class SpdxDocumentModelTest : WordSpec( {
    "The official YAML example from the SPDX specification version 2.2" should {
        "be deserializable" {
            val yaml = SpdxDocumentModelTest::class.java.getResource("/SPDXYAMLExample-2.2.spdx.yaml").readText()

            SpdxModelSerializer.fromYaml<SpdxDocument>(yaml)
        }
    }

    "The official YAML example without ranges from the SPDX specification version 2.2" should {
        "have idempotent (de)-serialization" {
            val yaml = SpdxDocumentModelTest::class.java.getResource("/SPDXYAMLExample-2.2-no-ranges.spdx.yaml").readText()

            val document = SpdxModelSerializer.fromYaml<SpdxDocument>(yaml)
            val deserializedYaml = SpdxModelSerializer.toYaml(document)

            deserializedYaml shouldBe formatYaml(yaml)
        }
    }

    "The official JSON example from the SPDX specification version 2.2" should {
        "be deserializable" {
            val json = SpdxDocumentModelTest::class.java.getResource("/SPDXJSONExample-v2.2.spdx.json").readText()

            SpdxModelSerializer.fromJson<SpdxDocument>(json)
        }
    }

    "The official JSON example without ranges from the SPDX specification version 2.2" should {
        "have idempotent (de-)serialization" {
            val json = SpdxDocumentModelTest::class.java.getResource("/SPDXJSONExample-v2.2.spdx-no-ranges.json").readText()

            val document = SpdxModelSerializer.fromJson<SpdxDocument>(json)
            val deserializedJson = SpdxModelSerializer.toJson(document)

            deserializedJson shouldBe formatJson(json)
        }
    }
})
