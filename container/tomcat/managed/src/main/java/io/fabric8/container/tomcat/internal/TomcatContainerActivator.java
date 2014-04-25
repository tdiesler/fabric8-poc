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
package io.fabric8.container.tomcat.internal;

import io.fabric8.container.tomcat.TomcatContainerCreateHandler;
import io.fabric8.spi.ContainerCreateHandler;

import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceRegistration;

/**
 * The Tomcat container activator
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Apr-2014
 */
public final class TomcatContainerActivator implements ModuleActivator {

    private ServiceRegistration<?> registration;

    @Override
    public void start(ModuleContext context) throws Exception {
        String[] classes = new String[] { TomcatContainerCreateHandler.class.getName(), ContainerCreateHandler.class.getName() };
        registration = context.registerService(classes, new TomcatContainerCreateHandler(), null);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        registration.unregister();
    }
}
