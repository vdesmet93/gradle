/*
 * Copyright 2007-2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.core.publish.PublishOptions;
import org.gradle.api.artifacts.PublishInstruction;
import org.gradle.util.WrapUtil;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class DefaultPublishOptionsFactoryTest {
    private DefaultPublishOptionsFactory publishOptionsFactory;
    private static final String TEST_CONF = "conf1";

    @Before
    public void setUp() {
        publishOptionsFactory = new DefaultPublishOptionsFactory();
    }

    @Test
    public void testCreatePublishOptionsWithUploadModuleDescriptorTrue() {
        File testIvyFile = new File("testFile");
        PublishInstruction publishInstruction = new PublishInstruction();
        publishInstruction.getModuleDescriptor().setPublish(true);
        PublishOptions publishOptions = publishOptionsFactory.createPublishOptions(WrapUtil.toSet(TEST_CONF), publishInstruction, testIvyFile);
        assertThat(publishOptions.getSrcIvyPattern(), equalTo(testIvyFile.getAbsolutePath()));
        checkCommonValues(publishOptions);
    }

    @Test
    public void testCreatePublishOptionsWithUploadModuleDescriptorFalse() {
        PublishInstruction publishInstruction = new PublishInstruction();
        publishInstruction.getModuleDescriptor().setPublish(false);
        PublishOptions publishOptions = publishOptionsFactory.createPublishOptions(WrapUtil.toSet(TEST_CONF), publishInstruction, null);
        assertThat(publishOptions.getSrcIvyPattern(), equalTo(null));
        checkCommonValues(publishOptions);
    }

    private void checkCommonValues(PublishOptions publishOptions) {
        assertThat(publishOptions.getConfs(), equalTo(WrapUtil.toArray(TEST_CONF)));
        assertThat(publishOptions.isOverwrite(), equalTo(true));
    }
}
