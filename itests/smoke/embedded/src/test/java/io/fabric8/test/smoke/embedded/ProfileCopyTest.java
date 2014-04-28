/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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

import io.fabric8.api.Constants;
import io.fabric8.api.Profile;
import io.fabric8.api.ProfileBuilder;
import io.fabric8.api.ProfileItem;
import io.fabric8.api.ProfileManager;
import io.fabric8.api.ProfileManagerLocator;
import io.fabric8.test.embedded.support.EmbeddedTestSupport;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link Profile} copy.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Apr-2014
 */
public class ProfileCopyTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedTestSupport.beforeClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        EmbeddedTestSupport.afterClass();
    }

    @Test
    public void testProfileCopy() throws Exception {

        ProfileManager prfManager = ProfileManagerLocator.getProfileManager();
        Profile profileA = prfManager.getDefaultProfile();
        Assert.assertEquals(Constants.DEFAULT_PROFILE_VERSION, profileA.getVersion());
        Set<ProfileItem> itemsA = profileA.getProfileItems(null);

        ProfileBuilder builder = ProfileBuilder.Factory.createFrom(profileA);
        Profile profileB = builder.build();
        Assert.assertEquals(Constants.DEFAULT_PROFILE_VERSION, profileB.getVersion());
        Set<ProfileItem> itemsB = profileB.getProfileItems(null);

        Assert.assertEquals(profileA.getIdentity(), profileB.getIdentity());
        Assert.assertEquals(profileA.getAttributes(), profileB.getAttributes());
        Assert.assertEquals(itemsA, itemsB);
    }
}