/*
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
 */

package io.fabric8.test.basic.container;



import io.fabric8.api.Container;
import io.fabric8.api.management.ContainerManagement;
import io.fabric8.container.karaf.KarafContainerBuilder;
import io.fabric8.container.tomcat.TomcatContainerBuilder;
import io.fabric8.container.wildfly.WildFlyContainerBuilder;
import io.fabric8.spi.BootstrapComplete;
import io.fabric8.test.basic.ManagedContainerLifecycleTests;
import io.fabric8.test.smoke.PrePostConditions;

import java.io.InputStream;

import javax.management.remote.JMXConnector;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.junit.runner.RunWith;

/**
 * Test basic container functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
@RunWith(Arquillian.class)
public class ManagedContainerLifecycleTest extends ManagedContainerLifecycleTests {

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("managed-container-test");
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class);
        archive.addClasses(ManagedContainerLifecycleTests.class, PrePostConditions.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, Resource.class, Container.class);
                    builder.addImportPackages(KarafContainerBuilder.class, TomcatContainerBuilder.class, WildFlyContainerBuilder.class);
                    builder.addImportPackages(ContainerManagement.class, JMXConnector.class);
                    builder.addImportPackages(BootstrapComplete.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    builder.addManifestHeader("Dependencies", "org.jboss.gravia,io.fabric8.api,io.fabric8.spi");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }
}
