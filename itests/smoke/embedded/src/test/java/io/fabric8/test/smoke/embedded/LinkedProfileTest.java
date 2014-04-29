/*
 * #%L
 * Fabric8 :: Testsuite :: Smoke :: Embedded
 * %%
 * Copyright (C) 2014 Red Hat
 * %%
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
 * #L%
 */
package io.fabric8.test.smoke.embedded;

import io.fabric8.api.ConfigurationProfileItem;
import io.fabric8.api.ConfigurationProfileItemBuilder;
import io.fabric8.api.LinkedProfile;
import io.fabric8.api.LinkedProfileVersion;
import io.fabric8.api.Profile;
import io.fabric8.api.ProfileBuilder;
import io.fabric8.api.ProfileManager;
import io.fabric8.api.ProfileManagerLocator;
import io.fabric8.api.ProfileVersionBuilder;
import io.fabric8.test.embedded.support.EmbeddedTestSupport;

import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.resource.Version;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link LinkedProfile} functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public class LinkedProfileTest {

    Version version = Version.parseVersion("2.0");
    String identityA = "A";
    String identityB = "B";
    String identityC = "C";

    Map<String, Object>  configA = new HashMap<>();
    Map<String, Object>  configB = new HashMap<>();
    Map<String, Object>  configC = new HashMap<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedTestSupport.beforeClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        EmbeddedTestSupport.afterClass();
    }

    @Before
    public void setUp() {
        configA.put("keyA", "aaa");
        configA.put("keyB", "aaa");
        configA.put("keyC", "aaa");

        configB.put("keyA", "aaa");
        configB.put("keyB", "bbb");
        configB.put("keyC", "bbb");

        configC.put("keyA", "aaa");
        configC.put("keyB", "bbb");
        configC.put("keyC", "ccc");
    }

    @Test
    public void testLinkedProfile() {

        ProfileVersionBuilder versionBuilder = ProfileVersionBuilder.Factory.create(version);
        ProfileBuilder builderA = versionBuilder.getProfileBuilder(identityA);
        ConfigurationProfileItemBuilder itemBuilder = builderA.getProfileItemBuilder("confItem", ConfigurationProfileItemBuilder.class);
        builderA.addProfileItem(itemBuilder.setConfiguration(configA).build());
        itemBuilder = builderA.getProfileItemBuilder("confItemA", ConfigurationProfileItemBuilder.class);
        builderA.addProfileItem(itemBuilder.setConfiguration(configA).build());
        Profile profileA = builderA.build();
        versionBuilder.addProfile(profileA);

        ProfileBuilder builderB = versionBuilder.getProfileBuilder(identityB);
        itemBuilder = builderB.getProfileItemBuilder("confItem", ConfigurationProfileItemBuilder.class);
        builderB.addProfileItem(itemBuilder.setConfiguration(configB).build());
        itemBuilder = builderB.getProfileItemBuilder("confItemB", ConfigurationProfileItemBuilder.class);
        builderB.addProfileItem(itemBuilder.setConfiguration(configB).build());
        builderB.addParentProfile(profileA.getIdentity());
        Profile profileB = builderB.build();
        versionBuilder.addProfile(profileB);

        ProfileBuilder builderC = versionBuilder.getProfileBuilder(identityC);
        itemBuilder = builderC.getProfileItemBuilder("confItem", ConfigurationProfileItemBuilder.class);
        builderC.addProfileItem(itemBuilder.setConfiguration(configC).build());
        itemBuilder = builderC.getProfileItemBuilder("confItemC", ConfigurationProfileItemBuilder.class);
        builderC.addProfileItem(itemBuilder.setConfiguration(configC).build());
        builderC.addParentProfile(profileB.getIdentity());
        Profile profileC = builderC.build();
        versionBuilder.addProfile(profileC);

        LinkedProfileVersion linkedVersion = versionBuilder.build();

        ProfileManager prfManager = ProfileManagerLocator.getProfileManager();
        prfManager.addProfileVersion(linkedVersion);

        profileA = prfManager.getLinkedProfile(version, identityA);
        Assert.assertTrue("No attributes", profileA.getAttributes().isEmpty());
        Assert.assertTrue("No parents", profileA.getParents().isEmpty());
        Assert.assertEquals(2, profileA.getProfileItems(null).size());
        Assert.assertEquals(configA, profileA.getProfileItem("confItem", ConfigurationProfileItem.class).getConfiguration());
        Assert.assertEquals(configA, profileA.getProfileItem("confItemA", ConfigurationProfileItem.class).getConfiguration());

        profileB = prfManager.getLinkedProfile(version, identityB);
        Assert.assertTrue("No attributes", profileB.getAttributes().isEmpty());
        Assert.assertEquals(1, profileB.getParents().size());
        Assert.assertEquals(identityA, profileB.getParents().iterator().next());
        Assert.assertEquals(2, profileB.getProfileItems(null).size());
        Assert.assertEquals(configB, profileB.getProfileItem("confItem", ConfigurationProfileItem.class).getConfiguration());
        Assert.assertEquals(configB, profileB.getProfileItem("confItemB", ConfigurationProfileItem.class).getConfiguration());

        profileC = prfManager.getLinkedProfile(version, identityC);
        Assert.assertTrue("No attributes", profileC.getAttributes().isEmpty());
        Assert.assertEquals(1, profileC.getParents().size());
        Assert.assertEquals(identityB, profileC.getParents().iterator().next());
        Assert.assertEquals(2, profileC.getProfileItems(null).size());
        Assert.assertEquals(configC, profileC.getProfileItem("confItem", ConfigurationProfileItem.class).getConfiguration());
        Assert.assertEquals(configC, profileC.getProfileItem("confItemC", ConfigurationProfileItem.class).getConfiguration());

        prfManager.removeProfileVersion(version);
    }
}
