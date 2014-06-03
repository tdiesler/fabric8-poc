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

import io.fabric8.api.CreateOptions;
import io.fabric8.api.ServiceEndpoint;

import java.util.Collections;
import java.util.Set;

import org.jboss.gravia.runtime.LifecycleException;

/**
 * An abstract handle to a container instance
 *
 * @author thomas.diesler@jboss.com
 * @since 21-Apr-2014
 */
public abstract class AbstractContainerHandle implements ContainerHandle {

    private final CreateOptions options;

    protected AbstractContainerHandle(CreateOptions options) {
        this.options = options;
    }

    @Override
    public CreateOptions getCreateOptions() {
        return options;
    }

    @Override
    public void start() throws LifecycleException {
    }

    @Override
    public void stop() throws LifecycleException {
    }

    @Override
    public void destroy() throws LifecycleException {
    }

    @Override
    public Set<ServiceEndpoint> getServiceEndpoints() {
        return Collections.emptySet();
    }
}
