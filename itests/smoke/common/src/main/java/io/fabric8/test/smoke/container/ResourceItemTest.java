/*
 * #%L
 * Fabric8 :: Testsuite :: Smoke :: Common
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
package io.fabric8.test.smoke.container;

import io.fabric8.api.Container;
import io.fabric8.spi.BootstrapComplete;
import io.fabric8.test.smoke.PrePostConditions;
import io.fabric8.test.smoke.ResourceItemTestBase;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import javax.management.MBeanServer;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.arquillian.container.ContainerSetup;
import org.jboss.gravia.arquillian.container.managed.ManagedSetupTask;
import org.jboss.gravia.itests.support.AnnotatedContextListener;
import org.jboss.gravia.itests.support.ArchiveBuilder;
import org.jboss.gravia.itests.support.HttpRequest;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.junit.runner.RunWith;

/**
 * Test profile items functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-May-2014
 */
@RunWith(Arquillian.class)
@ContainerSetup(ResourceItemTest.Setup.class)
public class ResourceItemTest extends ResourceItemTestBase {

    public static class Setup extends ManagedSetupTask {

        Set<ResourceIdentity> identities;

        @Override
        protected void beforeDeploy(ManagedContext context) throws Exception {
            String resname = "META-INF/repository-content/camel.core.feature.xml";
            URL resurl = getClass().getClassLoader().getResource(resname);
            identities = addRepositoryContent(context, resurl);
        }

        @Override
        protected void beforeStop(ManagedContext context) throws Exception {
            removeRepositoryContent(context, identities);
        }
    }

    @ArquillianResource
    Deployer deployer;

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("resource-items-test");
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class);
        archive.addClasses(ResourceItemTestBase.class, PrePostConditions.class);
        archive.addClasses(HttpRequest.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, Resource.class, Container.class, Provisioner.class);
                    builder.addImportPackages(BootstrapComplete.class, MBeanServer.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    builder.addManifestHeader("Dependencies", "io.fabric8.api,io.fabric8.spi,org.jboss.gravia");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Override
    protected InputStream getDeployment(String name) {
        return deployer.getDeployment(name);
    }

    @Deployment(name = RESOURCE_A, managed = false, testable = false)
    public static Archive<?> getResourceA() {
        return ResourceItemTestBase.getResourceA();
    }

    @Deployment(name = RESOURCE_B, managed = false, testable = false)
    public static Archive<?> getResourceB() {
        return ResourceItemTestBase.getResourceB();
    }

    @Deployment(name = RESOURCE_B1, managed = false, testable = false)
    public static Archive<?> getResourceB1() {
        return ResourceItemTestBase.getResourceB1();
    }

    @Deployment(name = RESOURCE_C, managed = false, testable = false)
    public static Archive<?> getResourceC() {
        return ResourceItemTestBase.getResourceC();
    }

    @Deployment(name = CONTENT_F1, managed = false, testable = false)
    public static Archive<?> getContentF1() {
        return ResourceItemTestBase.getContentF1();
    }

    @Deployment(name = CONTENT_F2, managed = false, testable = false)
    public static Archive<?> getContentF2() {
        return ResourceItemTestBase.getContentF2();
    }

    @Deployment(name = CONTENT_F3, managed = false, testable = false)
    public static Archive<?> getContentF3() {
        return ResourceItemTestBase.getContentF3();
    }

    @Deployment(name = CONTENT_G1, managed = false, testable = false)
    public static Archive<?> getContentG1() {
        return ResourceItemTestBase.getContentG1();
    }

    @Deployment(name = CONTENT_G2, managed = false, testable = false)
    public static Archive<?> getContentG2() {
        return ResourceItemTestBase.getContentG2();
    }

    @Deployment(name = CONTENT_G3, managed = false, testable = false)
    public static Archive<?> getContentG3() {
        return ResourceItemTestBase.getContentG3();
    }
}
