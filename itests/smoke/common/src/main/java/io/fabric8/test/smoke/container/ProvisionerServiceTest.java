/*
 * #%L
 * Gravia :: Integration Tests :: Common
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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

import io.fabric8.test.smoke.sub.a.CamelTransformHttpActivator;
import io.fabric8.test.smoke.sub.a.SimpleModuleActivator;
import io.fabric8.test.smoke.sub.a1.SimpleModuleState;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.Constants;
import org.jboss.gravia.arquillian.container.ContainerSetup;
import org.jboss.gravia.arquillian.container.ContainerSetupTask;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.ModuleActivatorBridge;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.WebAppContextListener;
import org.jboss.gravia.runtime.Wiring;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.test.gravia.itests.support.AnnotatedContextListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyListener;
import org.jboss.test.gravia.itests.support.AnnotatedProxyServlet;
import org.jboss.test.gravia.itests.support.ArchiveBuilder;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.http.HttpService;

/**
 * Test {@link Provisioner} service.
 *
 * @author thomas.diesler@jboss.com
 * @since 19-Dec-2013
 */
@RunWith(Arquillian.class)
@ContainerSetup(ProvisionerServiceTest.Setup.class)
public class ProvisionerServiceTest {

    static final String RESOURCE_A = "stream-resource";
    static final String RESOURCE_B = "shared-stream-resource";
    static final String RESOURCE_B1 = "shared-stream-resource-client";
    static final String RESOURCE_C = "resourceC";
    static final String RESOURCE_D = "resourceD";
    static final String RESOURCE_E = "resourceE";

    public static class Setup extends ContainerSetupTask {
        protected String[] getInitialFeatureNames() {
            return new String[] { "camel.core" };
        }
    }

    @ArquillianResource
    Deployer deployer;

    @Deployment
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("provisioner-service");
        archive.addClasses(HttpRequest.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(Runtime.class, Resource.class, Provisioner.class, Resolver.class, Repository.class);
                    builder.addImportPackages(MBeanServer.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testProvisionStreamResource() throws Exception {
        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);
        ResourceIdentity identityA = ResourceIdentity.fromString(RESOURCE_A);
        ResourceBuilder builderA = provisioner.getContentResourceBuilder(identityA, deployer.getDeployment(RESOURCE_A));
        ResourceHandle handle = provisioner.installResource(builderA.getResource());
        try {
            // Verify that the module got installed
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            Module module = runtime.getModule(identityA);
            Assert.assertNotNull("Module not null", module);
            Assert.assertEquals("ACTIVE " + module, State.ACTIVE, module.getState());

            // Verify that the module activator was called
            MBeanServer server = ServiceLocator.getRequiredService(MBeanServer.class);
            Assert.assertTrue("MBean registered", server.isRegistered(getObjectName(module)));
            Assert.assertEquals("ACTIVE", server.getAttribute(getObjectName(module), "ModuleState"));
        } finally {
            handle.uninstall();
        }
    }

    @Test
    public void testProvisionSharedStreamResource() throws Exception {
        List<ResourceHandle> handles = new ArrayList<>();
        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);
        ResourceIdentity identityB = ResourceIdentity.fromString(RESOURCE_B);
        ResourceBuilder builderB = provisioner.getContentResourceBuilder(identityB, deployer.getDeployment(RESOURCE_B));
        ResourceIdentity identityB1 = ResourceIdentity.fromString(RESOURCE_B1);
        ResourceBuilder builderB1 = provisioner.getContentResourceBuilder(identityB1, deployer.getDeployment(RESOURCE_B1));
        handles.add(provisioner.installSharedResource(builderB.getResource()));
        handles.add(provisioner.installResource(builderB1.getResource()));
        try {
            // Verify that the modules got installed
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            for (ResourceHandle handle : handles) {
                ResourceIdentity identity = handle.getResource().getIdentity();
                Assert.assertSame(handle.getModule(), runtime.getModule(identity));
                Assert.assertEquals("ACTIVE " + identity, State.ACTIVE, handle.getModule().getState());
            }
            // Verify that the module activator was called
            Module module = runtime.getModule(identityB1);
            MBeanServer server = ServiceLocator.getRequiredService(MBeanServer.class);
            Assert.assertTrue("MBean registered", server.isRegistered(getObjectName(module)));
            Assert.assertEquals("ACTIVE", server.getAttribute(getObjectName(module), "ModuleState"));
        } finally {
            for (ResourceHandle handle : handles) {
                handle.uninstall();
            }
        }
    }

    @Test
    public void testProvisionMavenResource() throws Exception {

        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);
        Runtime runtime = RuntimeLocator.getRequiredRuntime();

        Assume.assumeFalse(RuntimeType.TOMCAT == RuntimeType.getRuntimeType());

        ResourceIdentity identityA = ResourceIdentity.fromString("camel.core.unshared");
        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.camel:camel-core:jar:2.11.0");
        ResourceBuilder builderA = provisioner.getMavenResourceBuilder(identityA, mavenid);
        ResourceHandle handleA = provisioner.installResource(builderA.getResource());
        try {
            Assert.assertSame(handleA.getModule(), runtime.getModule(identityA));
            Assert.assertEquals("ACTIVE " + identityA, State.ACTIVE, handleA.getModule().getState());
        } finally {
            handleA.uninstall();
        }
    }

    @Test
    public void testProvisionSharedMavenResource() throws Exception {

        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);
        Runtime runtime = RuntimeLocator.getRequiredRuntime();

        ResourceIdentity identityA = ResourceIdentity.fromString("camel.core.shared");
        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.camel:camel-core:jar:2.11.0");
        ResourceBuilder builderA = provisioner.getMavenResourceBuilder(identityA, mavenid);
        builderA.addIdentityRequirement("javax.api");
        builderA.addIdentityRequirement("org.slf4j");
        ResourceHandle handleA = provisioner.installSharedResource(builderA.getResource());
        try {
            Assert.assertSame(handleA.getModule(), runtime.getModule(identityA));
            Assert.assertEquals("ACTIVE " + identityA, State.ACTIVE, handleA.getModule().getState());

            ResourceIdentity identityC = ResourceIdentity.fromString(RESOURCE_C);
            ResourceBuilder builderC = provisioner.getContentResourceBuilder(identityC, deployer.getDeployment(RESOURCE_C));
            ResourceHandle handleC = provisioner.installResource(RESOURCE_C + ".war", builderC.getResource());
            try {
                // Make a call to the HttpService endpoint that goes through a Camel route
                String reqspec = "/service?test=Kermit";
                String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + RESOURCE_C;
                Assert.assertEquals("Hello Kermit", performCall(context, reqspec));
            } finally {
                handleC.uninstall();
            }
        } finally {
            handleA.uninstall();
        }
    }

    @Test
    public void testProvisionAbstractFeature() throws Exception {

        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);

        // Provision the camel.core feature
        ResourceIdentity identity = ResourceIdentity.fromString("camel.core.feature:0.0.0");
        Requirement req = new IdentityRequirementBuilder(identity).getRequirement();
        Set<ResourceHandle> result = provisioner.provisionResources(Collections.singleton(req));

        List<ResourceHandle> handles = new ArrayList<>(result);
        try {
            // Verify the wiring
            Environment environment = provisioner.getEnvironment();
            Map<Resource, Wiring> wirings = environment.getWirings();
            ResourceIdentity residA = ResourceIdentity.create("org.apache.camel.core", "2.11.0");
            Resource resA = environment.getResource(residA);
            Assert.assertNotNull("Resource in environment", resA);
            Wiring wiringA = wirings.get(resA);
            Assert.assertNotNull("Wiring in environment", wiringA);
            Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());

            // Build a resource that has a class loading dependency
            DefaultResourceBuilder builder = new DefaultResourceBuilder();
            ResourceIdentity residB = ResourceIdentity.create(RESOURCE_D, Version.emptyVersion);
            Capability icap = builder.addIdentityCapability(residB);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, RESOURCE_D + ".war");
            builder.addContentCapability(deployer.getDeployment(RESOURCE_D));
            Resource resB = builder.getResource();

            // Deploy a resource through the {@link ResourceInstaller}
            handles.add(provisioner.installResource(resB));
            Assert.assertTrue("At least one resource", handles.size() > 0);

            // Make a call to the HttpService endpoint that goes through a Camel route
            String reqspec = "/service?test=Kermit";
            String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + RESOURCE_D;
            Assert.assertEquals("Hello Kermit", performCall(context, reqspec));

            // Verify module available
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            Assert.assertNotNull("Module available", runtime.getModule(residA));
            Assert.assertNotNull("Module available", runtime.getModule(residB));

            // Verify the wiring
            wirings = environment.getWirings();
            resA = environment.getResource(residA);
            Assert.assertNotNull("Resource in environment", resA);
            wiringA = wirings.get(resA);
            Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());

            // Deployment did not go through the {@link Provisioner} service
            // There is no wiring
            resB = environment.getResource(residB);
            Assert.assertNotNull("Resource in environment", resB);
            Wiring wiringB = wirings.get(resB);
            Assert.assertNull("Wiring not in environment", wiringB);
        } finally {
            for (ResourceHandle handle : handles) {
                handle.uninstall();
            }
        }
    }

    @Test
    public void testProvisionRepositoryResourceWithDependency() throws Exception {

        Provisioner provisioner = ServiceLocator.getRequiredService(Provisioner.class);

        // Build a resource that has a dependency on camel.core
        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability(RESOURCE_E, Version.emptyVersion);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE, RESOURCE_E + ".war");
        builder.addContentCapability(deployer.getDeployment(RESOURCE_E));
        builder.addIdentityRequirement("org.apache.camel.core", new VersionRange("[2.11,3.0)"));
        Resource res = builder.getResource();

        // Add that resource to the repository
        Resource resB = provisioner.getRepository().addResource(res);
        Assert.assertEquals(RESOURCE_E + ":0.0.0", resB.getIdentity().toString());

        try {
            // Provision that resource
            Requirement req = new IdentityRequirementBuilder(resB.getIdentity()).getRequirement();
            Set<ResourceHandle> result = provisioner.provisionResources(Collections.singleton(req));

            List<ResourceHandle> handles = new ArrayList<ResourceHandle>(result);
            try {
                // Make a call to the HttpService endpoint that goes through a Camel route
                String reqspec = "/service?test=Kermit";
                String context = RuntimeType.getRuntimeType() == RuntimeType.KARAF ? "" : "/" + RESOURCE_E;
                Assert.assertEquals("Hello Kermit", performCall(context, reqspec));

                // Verify module available
                Runtime runtime = RuntimeLocator.getRequiredRuntime();
                ResourceIdentity residA = ResourceIdentity.create("org.apache.camel.core", "2.11.0");
                Assert.assertNotNull("Module available", runtime.getModule(residA));
                ResourceIdentity residB = ResourceIdentity.create(RESOURCE_E, Version.emptyVersion);
                Assert.assertNotNull("Module available", runtime.getModule(residB));

                // Verify the wiring
                Environment environment = provisioner.getEnvironment();
                Map<Resource, Wiring> wirings = environment.getWirings();
                Resource resA = environment.getResource(residA);
                Assert.assertNotNull("Resource in environment", resA);
                Wiring wiringA = wirings.get(resA);
                Assert.assertNotNull("Wiring in environment", wiringA);
                Assert.assertEquals("Two required wires", 2, wiringA.getRequiredResourceWires(null).size());
                Assert.assertEquals("One provided wires", 1, wiringA.getProvidedResourceWires(null).size());
                resB = environment.getResource(residB);
                Assert.assertNotNull("Resource in environment", resB);
                Wiring wiringB = wirings.get(resB);
                Assert.assertNotNull("Wiring in environment", wiringB);
                Assert.assertEquals("One required wires", 1, wiringB.getRequiredResourceWires(null).size());
                Assert.assertEquals("Zero provided wires", 0, wiringB.getProvidedResourceWires(null).size());
            } finally {
                for (ResourceHandle handle : handles) {
                    handle.uninstall();
                }
            }
        } finally {
            provisioner.getRepository().removeResource(resB.getIdentity());
        }
    }

    private String performCall(String context, String path) throws Exception {
        return performCall(context, path, null, 2, TimeUnit.SECONDS);
    }

    private String performCall(String context, String path, Map<String, String> headers, long timeout, TimeUnit unit) throws Exception {
        return HttpRequest.get("http://localhost:8080" + context + path, headers, timeout, unit);
    }

    private ObjectName getObjectName(Module module) throws MalformedObjectNameException {
        ResourceIdentity identity = module.getIdentity();
        return new ObjectName("test:name=" + identity.getSymbolicName() + ",version=" + identity.getVersion());
    }

    @Deployment(name = RESOURCE_A, managed = false, testable = false)
    public static Archive<?> getResourceA() {
        final ArchiveBuilder archive = new ArchiveBuilder(RESOURCE_A);
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(RuntimeType.KARAF, ModuleActivatorBridge.class);
        archive.addClasses(SimpleModuleActivator.class, SimpleModuleState.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_A);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, SimpleModuleActivator.class.getName());
                    builder.addImportPackages(Runtime.class, Resource.class, ServiceLocator.class, MBeanServer.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_A, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, SimpleModuleActivator.class.getName());
                    builder.addManifestHeader("Dependencies", "org.jboss.gravia");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Deployment(name = RESOURCE_B, managed = false, testable = false)
    public static Archive<?> getResourceB() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, RESOURCE_B + ".jar");
        archive.addClasses(SimpleModuleState.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(RESOURCE_B);
                builder.addExportPackages(SimpleModuleState.class);
                builder.addImportPackages(Runtime.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = RESOURCE_B1, managed = false, testable = false)
    public static Archive<?> getResourceB1() {
        final ArchiveBuilder archive = new ArchiveBuilder(RESOURCE_B1);
        archive.addClasses(RuntimeType.TOMCAT, AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(RuntimeType.KARAF, ModuleActivatorBridge.class);
        archive.addClasses(SimpleModuleActivator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_B1);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, SimpleModuleActivator.class.getName());
                    builder.addImportPackages(Runtime.class, Resource.class, ServiceLocator.class);
                    builder.addImportPackages(MBeanServer.class, SimpleModuleState.class);
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_B1, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, SimpleModuleActivator.class.getName());
                    builder.addManifestHeader("Dependencies", "org.jboss.gravia," + RESOURCE_B);
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Deployment(name = RESOURCE_C, managed = false, testable = false)
    public static Archive<?> getResourceC() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, RESOURCE_C + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformHttpActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_C);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_C, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    builder.addManifestHeader("Dependencies", "camel.core.shared");
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }

    @Deployment(name = RESOURCE_D, managed = false, testable = false)
    public static Archive<?> getResourceD() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, RESOURCE_D + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformHttpActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_D);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_D, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    builder.addManifestHeader("Dependencies", "org.apache.camel.core");
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }

    @Deployment(name = RESOURCE_E, managed = false, testable = false)
    public static Archive<?> getResourceE() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, RESOURCE_E + ".war");
        archive.addClasses(AnnotatedProxyServlet.class, AnnotatedProxyListener.class);
        archive.addClasses(AnnotatedContextListener.class, WebAppContextListener.class);
        archive.addClasses(CamelTransformHttpActivator.class, ModuleActivatorBridge.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(RESOURCE_E);
                    builder.addBundleActivator(ModuleActivatorBridge.class);
                    builder.addManifestHeader(Constants.GRAVIA_ENABLED, Boolean.TRUE.toString());
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    builder.addImportPackages(ModuleActivatorBridge.class, Runtime.class, Servlet.class, HttpServlet.class, HttpService.class);
                    builder.addImportPackages(CamelContext.class, DefaultCamelContext.class, RouteBuilder.class, RouteDefinition.class);
                    builder.addBundleClasspath("WEB-INF/classes");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(RESOURCE_E, Version.emptyVersion);
                    builder.addManifestHeader(Constants.MODULE_ACTIVATOR, CamelTransformHttpActivator.class.getName());
                    return builder.openStream();
                }
            }
        });
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.apache.felix:org.apache.felix.http.proxy").withoutTransitivity().asFile();
        archive.addAsLibraries(libs);
        return archive;
    }
}