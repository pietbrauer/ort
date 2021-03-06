/*
 * Copyright (C) 2021 Bosch.IO GmbH
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

package org.ossreviewtoolkit.model

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

import java.util.SortedSet

class DependencyGraphTest : WordSpec({
    "createScopes()" should {
        "construct a simple tree with scopes" {
            val ids = listOf(
                id("org.apache.commons", "commons-lang3", "3.11"),
                id("org.apache.commons", "commons-collections4", "4.4"),
                id(group = "org.junit", artifact = "junit", version = "5")
            )
            val fragments =
                setOf(
                    DependencyReference(0),
                    DependencyReference(1),
                    DependencyReference(2)
                )
            val scopeMap = mapOf(
                "scope1" to listOf(RootDependencyIndex(0), RootDependencyIndex(1)),
                "scope2" to listOf(RootDependencyIndex(1), RootDependencyIndex(2))
            )

            val graph = DependencyGraph(ids, fragments, scopeMap)
            val scopes = graph.createScopes()

            scopes.map { it.name } should containExactly("scope1", "scope2")
            scopeDependencies(scopes, "scope1") shouldBe "${pkgId(ids[1])}${pkgId(ids[0])}"
            scopeDependencies(scopes, "scope2") shouldBe "${pkgId(ids[1])}${pkgId(ids[2])}"
        }

        "construct a tree with multiple levels" {
            val ids = listOf(
                id("org.apache.commons", "commons-lang3", "3.11"),
                id("org.apache.commons", "commons-collections4", "4.4"),
                id("org.apache.commons", "commons-configuration2", "2.8"),
                id("org.apache.commons", "commons-csv", "1.5")
            )
            val refLang = DependencyReference(0)
            val refCollections = DependencyReference(1)
            val refConfig = DependencyReference(2, dependencies = sortedSetOf(refLang, refCollections))
            val refCsv = DependencyReference(3, dependencies = sortedSetOf(refConfig))
            val fragments = setOf(refCsv)
            val scopeMap = mapOf("s" to listOf(RootDependencyIndex(3)))
            val graph = DependencyGraph(ids, fragments, scopeMap)
            val scopes = graph.createScopes()

            scopeDependencies(scopes, "s") shouldBe "${pkgId(ids[3])}<${pkgId(ids[2])}<${pkgId(ids[1])}" +
                    "${pkgId(ids[0])}>>"
        }

        "construct scopes from different fragments" {
            val ids = listOf(
                id("org.apache.commons", "commons-lang3", "3.11"),
                id("org.apache.commons", "commons-collections4", "4.4"),
                id("org.apache.commons", "commons-configuration2", "2.8"),
                id("org.apache.commons", "commons-logging", "1.3")
            )
            val refLogging = DependencyReference(3)
            val refLang = DependencyReference(0)
            val refCollections1 = DependencyReference(1)
            val refCollections2 = DependencyReference(1, fragment = 1, dependencies = sortedSetOf(refLogging))
            val refConfig1 = DependencyReference(2, dependencies = sortedSetOf(refLang, refCollections1))
            val refConfig2 =
                DependencyReference(2, fragment = 1, dependencies = sortedSetOf(refLang, refCollections2))
            val fragments = setOf(refConfig1, refConfig2)
            val scopeMap = mapOf(
                "s1" to listOf(RootDependencyIndex(2)),
                "s2" to listOf(RootDependencyIndex(2, fragment = 1))
            )

            val graph = DependencyGraph(ids, fragments, scopeMap)
            val scopes = graph.createScopes()

            scopeDependencies(scopes, "s1") shouldBe "${pkgId(ids[2])}<${pkgId(ids[1])}${pkgId(ids[0])}>"
            scopeDependencies(scopes, "s2") shouldBe "${pkgId(ids[2])}<${pkgId(ids[1])}<${pkgId(ids[3])}>" +
                    "${pkgId(ids[0])}>"
        }

        "deal with attributes of package references" {
            val ids = listOf(
                id("org.apache.commons", "commons-lang3", "3.10"),
                id("org.apache.commons", "commons-collections4", "4.4")
            )
            val issue = OrtIssue(source = "analyzer", message = "Could not analyze :-(")
            val refLang = DependencyReference(0, linkage = PackageLinkage.PROJECT_DYNAMIC)
            val refCol = DependencyReference(1, issues = listOf(issue), dependencies = sortedSetOf(refLang))
            val trees = setOf(refCol)
            val scopeMap = mapOf("s" to listOf(RootDependencyIndex(1)))

            val graph = DependencyGraph(ids, trees, scopeMap)
            val scopes = graph.createScopes()
            val scope = scopes.first()

            scope.shouldNotBeNull()
            scope.dependencies shouldHaveSize 1
            val pkgRefCol = scope.dependencies.first()
            pkgRefCol.issues should containExactly(issue)
            pkgRefCol.dependencies shouldHaveSize 1

            val pkgRefLang = pkgRefCol.dependencies.first()
            pkgRefLang.linkage shouldBe PackageLinkage.PROJECT_DYNAMIC
        }
    }
})

/** The name of the dependency manager used by tests. */
private const val MANAGER_NAME = "TestManager"

/**
 * Create an identifier string with the given [group], [artifact] ID and [version].
 */
private fun id(group: String, artifact: String, version: String): String = "$MANAGER_NAME:$group:$artifact:$version"

/**
 * Create an [Identifier] based on the given [id] string.
 */
private fun pkgId(id: String): Identifier = Identifier(id)

/**
 * Output the dependency tree of the given scope as a string.
 */
private fun scopeDependencies(scopes: SortedSet<Scope>, name: String): String = buildString {
    scopes.find { it.name == name }?.let { scope ->
        scope.dependencies.forEach { dumpDependencies(it) }
    }
}

/**
 * Transform a dependency tree structure starting at [ref] to a string.
 */
private fun StringBuilder.dumpDependencies(ref: PackageReference) {
    append(ref.id)
    if (ref.dependencies.isNotEmpty()) {
        append('<')
        ref.dependencies.forEach { dumpDependencies(it) }
        append('>')
    }
}
