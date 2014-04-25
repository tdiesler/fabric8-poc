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
package io.fabric8.spi;

import java.io.File;

import org.jboss.gravia.repository.MavenCoordinates;

public abstract class AbstractManagedContainerBuilder<B extends ManagedContainerBuilder<B, C>, C extends AbstractManagedCreateOptions> extends AbstractContainerBuilder<B, C> implements ManagedContainerBuilder<B, C> {

    protected AbstractManagedContainerBuilder(C options) {
        super(options);
    }

    @Override
    @SuppressWarnings("unchecked")
    public B addMavenCoordinates(MavenCoordinates coordinates) {
        assertMutable();
        options.addMavenCoordinates(coordinates);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B setTargetDirectory(String target) {
        assertMutable();
        options.setTargetDirectory(new File(target).getAbsoluteFile());
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B setJavaVmArguments(String javaVmArguments) {
        assertMutable();
        options.setJavaVmArguments(javaVmArguments);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B setOutputToConsole(boolean outputToConsole) {
        assertMutable();
        options.setOutputToConsole(outputToConsole);
        return (B) this;
    }
}
