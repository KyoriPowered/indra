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
package net.kyori.indra

import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject
import kotlin.math.max

/**
 * Options configuring the toolchain versioning
 */
open class JavaToolchainVersions @Inject constructor(objects: ObjectFactory, providers: ProviderFactory) {

  /**
   * The target Java version to compile for.
   *
   * Default: 8
   */
  val target: Property<Int> = objects.property(Int::class).convention(8)

  /**
   * The minimum toolchain version to use when building.
   *
   * Default: 11
   */
  val minimumToolchain: Property<Int> = objects.property(Int::class).convention(11)

  /**
   * Whether to strictly apply toolchain versions.
   *
   * If this is set to false, java toolchains will only be overridden for
   * the Gradle JVM's version is less than the target version,
   * and cross-version testing will not be performed.
   */
  val strictVersions: Property<Boolean> = objects.property<Boolean>()
    .convention(
      providers.gradleProperty("strictMultireleaseVersions")
        .orElse(providers.environmentVariable("CI")) // set by GH Actions and Travis
        .map(String::toBoolean)
        .orElse(false)
    )

  /**
   * Toolchains that should be used to execute tests
   */
  val testWith: SetProperty<Int> = objects.setProperty(Int::class)

  /**
   * If preview features should be enabled. This will be applied to all compile and JavaExec tasks
   */
  val enablePreviewFeatures: Property<Boolean> = objects.property(Boolean::class).convention(false)

  /** The version that should be used to run compile tasks, taking into account strict version and current JVM */
  internal val actualVersion: Provider<Int> = this.strictVersions.map { strict ->
    val running = versionNumber(JavaVersion.current())
    target.finalizeValue()
    minimumToolchain.finalizeValue()
    val minimum = max(minimumToolchain.get(), target.get()) // If target > minimum toolchain, the target is our new minimum
    if(strict || running < minimum) {
      minimum
    } else {
      running
    }
  }

  init {
    testWith.add(target)
  }

  /**
   * Add versions that should be tested with, when strict versions are enabled.
   */
  fun testWith(vararg testVersions: Int) {
    testWith.addAll(testVersions.toList())
  }

}
