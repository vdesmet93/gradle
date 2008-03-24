/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.gradle.util

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * @author Hans Dockter
 */
class GradleVersionTest extends GroovyTestCase {
    static final String TEST_VERSION = '1.0.0-RC1'
    static final String TEST_BUILDTIME = 'sometime'
    void testGradleVersion() {
        GradleVersion gradleVersion = new GradleVersion()
        assertEquals(TEST_VERSION, gradleVersion.version)
        assertEquals(TEST_BUILDTIME, gradleVersion.buildTime)
    }

    void testPrettyPrint() {
        String expectedText = """Gradle $TEST_VERSION
Gradle buildtime: $TEST_BUILDTIME
Groovy $InvokerHelper.version
JVM ${System.getProperty("java.vm.version")}
JVM Vendor: ${System.getProperty("java.vm.vendor")}
OS Name: ${System.getProperty("os.name")}
"""
    }
}