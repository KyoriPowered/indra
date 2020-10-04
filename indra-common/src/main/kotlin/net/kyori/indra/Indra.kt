/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@file:JvmName("Indra")
package net.kyori.indra

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaToolChain
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

internal const val EXTENSION_NAME = "indra"
internal const val PUBLICATION_NAME = "maven"
internal val SOURCE_FILES = listOf(
  "**/*.groovy",
  "**/*.java",
  "**/*.kt",
  "**/*.scala"
)

fun extension(project: Project): IndraExtension = project.extensions.findByType(IndraExtension::class)
  ?: project.extensions.create(EXTENSION_NAME, IndraExtension::class)

fun version(tc: JavaToolChain): JavaVersion = JavaVersion.toVersion(tc.version)
fun versionNumber(version: JavaVersion): Int = version.ordinal + 1
fun versionString(version: JavaVersion): String = when(version) {
  JavaVersion.VERSION_1_9 -> "9"
  JavaVersion.VERSION_1_10 -> "10"
  else -> version.toString()
}

/**
 * Link to the API documentation for a specific java version.
 */
fun jdkApiDocs(version: JavaVersion): String = if(version.isJava11Compatible) {
  "https://docs.oracle.com/en/java/javase/${version.majorVersion}/docs/api"
} else {
  "https://docs.oracle.com/javase/${version.majorVersion}/docs/api"
}

fun grgit(project: Project): Grgit? {
  return project.extensions.findByType(Grgit::class)
}

/**
 * Find a tag, if any, that corresponds with the current checked out commit
 */
fun headTag(project: Project): Tag? {
  val grgit = grgit(project) ?: return null
  val headCommit = grgit.head()
  return grgit.tag.list().find { it.commit == headCommit }
}
