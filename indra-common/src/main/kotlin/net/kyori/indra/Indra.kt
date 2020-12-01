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

@Deprecated(message = "This method uses components of Gradle that have been deprecated.")
fun version(tc: JavaToolChain): JavaVersion = JavaVersion.toVersion(tc.version)
@Deprecated(message = "This method has moved - use the one in the 'util' package.", replaceWith = ReplaceWith(imports = arrayOf("net.kyori.indra.util.versionNumber"), expression = "versionNumber(version)"))
fun versionNumber(version: JavaVersion): Int = net.kyori.indra.util.versionNumber(version)
@Deprecated(message = "This method has moved - use the one in the 'util' package.", replaceWith = ReplaceWith(imports = arrayOf("net.kyori.indra.util.versionString"), expression = "versionString(version)"))
fun versionString(version: JavaVersion): String = net.kyori.indra.util.versionString(version)
@Deprecated(message = "This method has moved - use the one in the 'util' package.", replaceWith = ReplaceWith(imports = arrayOf("net.kyori.indra.util.versionString"), expression = "versionString(version)"))
fun versionString(version: Int): String = net.kyori.indra.util.versionString(version)

/**
 * Link to the API documentation for a specific java version.
 */
@Deprecated("Use the variant with an integer version instead", replaceWith = ReplaceWith("jdkApiDocs(versionNumber(version))"))
fun jdkApiDocs(version: JavaVersion): String = if(version.isJava11Compatible) {
  "https://docs.oracle.com/en/java/javase/${version.majorVersion}/docs/api"
} else {
  "https://docs.oracle.com/javase/${version.majorVersion}/docs/api"
}

/**
 * Link to the API documentation for a specific java version.
 */
@Deprecated("No replacement.")
fun jdkApiDocs(javaVersion: Int): String = if(javaVersion >= 11) {
  "https://docs.oracle.com/en/java/javase/$javaVersion/docs/api"
} else {
  "https://docs.oracle.com/javase/$javaVersion/docs/api"
}

@Deprecated(message = "This method has moved - use the one in the 'util' package.", replaceWith = ReplaceWith(imports = arrayOf("net.kyori.indra.util.grgit"), expression = "grgit(project)"))
fun grgit(project: Project): Grgit? = net.kyori.indra.util.grgit(project)

/**
 * Find a tag, if any, that corresponds with the current checked out commit
 */
@Deprecated(message = "This method has moved - use the one in the 'util' package.", replaceWith = ReplaceWith(imports = arrayOf("net.kyori.indra.util.headTag"), expression = "headTag(project)"))
fun headTag(project: Project): Tag? = net.kyori.indra.util.headTag(project)
