/*
 * #%L
 * Fabric8 :: SPI
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
package io.fabric8.spi;

import java.io.File;

import org.jboss.gravia.resource.MavenCoordinates;

public abstract class AbstractManagedContainerBuilder<B extends ManagedContainerBuilder<B, T>, T extends AbstractManagedCreateOptions> extends AbstractContainerBuilder<B, T> implements ManagedContainerBuilder<B, T> {

    protected AbstractManagedContainerBuilder(T options) {
        super(options);
    }

    @Override
    @SuppressWarnings("unchecked")
    public B addMavenCoordinates(MavenCoordinates coordinates) {
        options.addMavenCoordinates(coordinates);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B targetDirectory(String target) {
        options.setTargetDirectory(new File(target).getAbsoluteFile());
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B jvmArguments(String javaVmArguments) {
        options.setJavaVmArguments(javaVmArguments);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B outputToConsole(boolean outputToConsole) {
        options.setOutputToConsole(outputToConsole);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B zooKeeperServer(boolean zooKeeperServer) {
        options.setZooKeeperServer(zooKeeperServer);
        return (B) this;
    }
}
