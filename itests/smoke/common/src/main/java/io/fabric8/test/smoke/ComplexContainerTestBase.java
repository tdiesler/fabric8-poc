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
package io.fabric8.test.smoke;

import static io.fabric8.api.Constants.DEFAULT_PROFILE_VERSION;
import io.fabric8.api.ConfigurationProfileItemBuilder;
import io.fabric8.api.Constants;
import io.fabric8.api.Container;
import io.fabric8.api.Container.State;
import io.fabric8.api.ContainerIdentity;
import io.fabric8.api.ContainerManager;
import io.fabric8.api.ContainerManagerLocator;
import io.fabric8.api.CreateOptions;
import io.fabric8.api.Profile;
import io.fabric8.api.ProfileBuilder;
import io.fabric8.api.ProfileIdentity;
import io.fabric8.api.ProfileManager;
import io.fabric8.api.ProfileManagerLocator;
import io.fabric8.api.ProfileVersion;
import io.fabric8.api.ProfileVersionBuilder;
import io.fabric8.api.ProvisionEvent;
import io.fabric8.api.ProvisionEvent.EventType;
import io.fabric8.api.ProvisionEventListener;
import io.fabric8.spi.DefaultContainerBuilder;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.gravia.resource.Version;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test container/profile functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public abstract class ComplexContainerTestBase  {

    @Before
    public void preConditions() {
        PrePostConditions.assertPreConditions();
    }

    @After
    public void postConditions() {
        PrePostConditions.assertPostConditions();
    }

    @Test
    public void testContainersAndProfiles() throws Exception {

        ContainerManager cntManager = ContainerManagerLocator.getContainerManager();
        ProfileManager prfManager = ProfileManagerLocator.getProfileManager();

        // Create parent container
        DefaultContainerBuilder builder = DefaultContainerBuilder.create();
        CreateOptions options = builder.addIdentityPrefix("cntA").build();
        Container cntParent = cntManager.createContainer(options);

        // Verify parent identity
        ContainerIdentity idParent = cntParent.getIdentity();
        Assert.assertTrue(idParent.getSymbolicName().startsWith("cntA#"));

        // Start the parent container
        cntParent = cntManager.startContainer(idParent, null);
        Assert.assertSame(State.STARTED, cntParent.getState());
        Assert.assertEquals(DEFAULT_PROFILE_VERSION, cntParent.getProfileVersion());

        // Verify that the parent has the default profile assigned
        Assert.assertEquals(Constants.DEFAULT_PROFILE_VERSION, cntParent.getProfileVersion());
        Assert.assertEquals(1, cntParent.getProfileIdentities().size());
        Assert.assertTrue(cntParent.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));

        // Build a new profile version
        Version version20 = Version.parseVersion("2.0");
        ProfileVersionBuilder pvbuilder = ProfileVersionBuilder.Factory.create();
        ProfileVersion profVersion20 = pvbuilder.addIdentity(version20).build();

        // Verify that the version cannot be set
        // because it is not registered with the {@link ProfileManager}
        try {
            cntManager.setProfileVersion(idParent, version20, null);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        prfManager.addProfileVersion(profVersion20);

        // Verify that the version can still not be set
        // because the contaier uses the default profile
        // which is not yet part of profile 2.0
        try {
            cntManager.setProfileVersion(idParent, version20, null);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Build a new profile and associated it with 2.0
        ProfileBuilder profileBuilder = ProfileBuilder.Factory.create();
        Profile default20 = profileBuilder.addIdentity(ProfileIdentity.create("default")).build();
        prfManager.addProfile(version20, default20);

        // Setup the provision listener
        final CountDownLatch latchA = new CountDownLatch(1);
        ProvisionEventListener listener = new ProvisionEventListener() {
            @Override
            public void processEvent(ProvisionEvent event) {
                String symbolicName = event.getProfile().getIdentity().getSymbolicName();
                if (event.getType() == EventType.PROVISIONED && "default".equals(symbolicName)) {
                    latchA.countDown();
                }
            }
        };

        // Switch the container to version 2.0
        cntParent = cntManager.setProfileVersion(idParent, version20, listener);
        Assert.assertTrue("ProvisionEvent received", latchA.await(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(version20, cntParent.getProfileVersion());
        Assert.assertTrue(cntParent.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));
        Assert.assertEquals(1, cntParent.getProfileIdentities().size());

        // Create profile foo
        profileBuilder = ProfileBuilder.Factory.create();
        profileBuilder = profileBuilder.addIdentity(ProfileIdentity.create("foo"));
        ConfigurationProfileItemBuilder configBuilder = profileBuilder.getProfileItemBuilder(Container.CONTAINER_SERVICE_PID, ConfigurationProfileItemBuilder.class);
        configBuilder.setConfiguration(Collections.singletonMap(Container.CNFKEY_CONFIG_TOKEN, (Object) "bar"));
        profileBuilder.addProfileItem(configBuilder.build());
        Profile fooProfile = profileBuilder.build();

        // Verify that the profile cannot be added
        // because the profile version does not yet exist
        try {
            cntManager.addProfiles(idParent, Collections.singleton(fooProfile.getIdentity()), null);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Add profile foo to 2.0
        prfManager.addProfile(version20, fooProfile);
        Assert.assertEquals(2, prfManager.getProfileIdentities(version20).size());

        // Verify that the profile cannot be added again
        try {
            prfManager.addProfile(version20, fooProfile);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Setup the provision listener
        final CountDownLatch latchB = new CountDownLatch(1);
        listener = new ProvisionEventListener() {
            @Override
            public void processEvent(ProvisionEvent event) {
                String symbolicName = event.getProfile().getIdentity().getSymbolicName();
                if (event.getType() == EventType.PROVISIONED && "foo".equals(symbolicName)) {
                    latchB.countDown();
                }
            }
        };

        // Add profile foo to parent container
        cntParent = cntManager.addProfiles(idParent, Collections.singleton(fooProfile.getIdentity()), listener);
        Assert.assertTrue("ProvisionEvent received", latchB.await(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(version20, cntParent.getProfileVersion());
        Assert.assertTrue(cntParent.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));
        Assert.assertTrue(cntParent.getProfileIdentities().contains(fooProfile.getIdentity()));
        Assert.assertEquals(2, cntParent.getProfileIdentities().size());

        // Create child container
        builder = DefaultContainerBuilder.create();
        options = builder.addIdentityPrefix("cntB").build();
        Container cntChild = cntManager.createContainer(idParent, options);

        // Verify child identity
        ContainerIdentity idChild = cntChild.getIdentity();
        Assert.assertTrue(idChild.getSymbolicName().startsWith("cntA"));
        Assert.assertTrue(idChild.getSymbolicName().indexOf(":cntB") > 0);

        // Start the child container
        cntChild = cntManager.startContainer(idChild, null);
        Assert.assertSame(State.STARTED, cntChild.getState());
        Assert.assertEquals(DEFAULT_PROFILE_VERSION, cntChild.getProfileVersion());

        // Verify that the child has the default profile assigned
        Assert.assertEquals(Constants.DEFAULT_PROFILE_VERSION, cntChild.getProfileVersion());
        Assert.assertEquals(1, cntChild.getProfileIdentities().size());
        Assert.assertTrue(cntChild.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));

        // Verify that the profile cannot be removed
        // because it is still used by a container
        try {
            prfManager.removeProfile(version20, fooProfile.getIdentity());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Verify that the parent cannot be destroyed
        try {
            cntManager.destroyContainer(idParent);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Setup the provision listener
        final CountDownLatch latchC = new CountDownLatch(1);
        listener = new ProvisionEventListener() {
            @Override
            public void processEvent(ProvisionEvent event) {
                String symbolicName = event.getProfile().getIdentity().getSymbolicName();
                if (event.getType() == EventType.REMOVED && "foo".equals(symbolicName)) {
                    latchC.countDown();
                }
            }
        };

        // Remove profile foo from container
        cntParent = cntManager.removeProfiles(idParent, Collections.singleton(fooProfile.getIdentity()), listener);
        Assert.assertTrue("ProvisionEvent received", latchC.await(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(version20, cntParent.getProfileVersion());
        Assert.assertTrue(cntParent.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));
        Assert.assertEquals(1, cntParent.getProfileIdentities().size());

        // Remove profile foo from 2.0
        prfManager.removeProfile(version20, fooProfile.getIdentity());
        Assert.assertEquals(1, prfManager.getProfileIdentities(version20).size());

        // Verify that the profile version cannot be removed
        // because it is still used by a container
        try {
            prfManager.removeProfileVersion(version20);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }

        // Setup the provision listener
        final CountDownLatch latchD = new CountDownLatch(2);
        listener = new ProvisionEventListener() {
            @Override
            public void processEvent(ProvisionEvent event) {
                Profile profile = event.getProfile();
                String version = profile.getVersion().toString();
                String symbolicName = profile.getIdentity().getSymbolicName();
                if (event.getType() == EventType.REMOVED && "2.0.0".equals(version) && "default".equals(symbolicName)) {
                    latchD.countDown();
                }
                if (event.getType() == EventType.PROVISIONED && "1.0.0".equals(version) && "default".equals(symbolicName)) {
                    latchD.countDown();
                }
            }
        };

        // Set the default profile version
        cntParent = cntManager.setProfileVersion(idParent, Constants.DEFAULT_PROFILE_VERSION, listener);
        Assert.assertTrue("ProvisionEvent received", latchD.await(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(Constants.DEFAULT_PROFILE_VERSION, cntParent.getProfileVersion());
        Assert.assertTrue(cntParent.getProfileIdentities().contains(Constants.DEFAULT_PROFILE_IDENTITY));
        Assert.assertEquals(1, cntParent.getProfileIdentities().size());

        // Remove profile version 2.0
        prfManager.removeProfileVersion(version20);

        cntChild = cntManager.destroyContainer(idChild);
        Assert.assertSame(State.DESTROYED, cntChild.getState());
        cntParent = cntManager.destroyContainer(idParent);
        Assert.assertSame(State.DESTROYED, cntParent.getState());
    }
}
