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
package io.fabric8.core.internal;

import io.fabric8.api.Container;
import io.fabric8.core.internal.ContainerServiceImpl.ContainerState;
import io.fabric8.spi.scr.AbstractComponent;
import io.fabric8.spi.utils.IllegalStateAssertion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A registry of stateful {@link Container} instances
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Mar-2014
 */
@Component(service = { ContainerRegistry.class }, immediate = true)
public final class ContainerRegistry extends AbstractComponent {

    private final Map<String, ContainerState> containers = new ConcurrentHashMap<String, ContainerState>();

    @Activate
    void activate() {
        activateComponent();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    Set<String> getContainerIdentities() {
        assertValid();
        return Collections.unmodifiableSet(containers.keySet());
    }

    Set<ContainerState> getContainers(Set<String> identities) {
        assertValid();
        Set<ContainerState> result = new HashSet<ContainerState>();
        if (identities == null) {
            result.addAll(containers.values());
        } else {
            for (ContainerState cntState : containers.values()) {
                if (identities.contains(cntState.getIdentity())) {
                    result.add(cntState);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    ContainerState getContainer(String identity) {
        assertValid();
        return getContainerInternal(identity);
    }

    void addContainer(ContainerState cntState) {
        assertValid();
        String identity = cntState.getIdentity();
        IllegalStateAssertion.assertTrue(getContainerInternal(identity) == null, "Container already exists: " + identity);
        containers.put(identity, cntState);
    }

    ContainerState removeContainer(String identity) {
        assertValid();
        ContainerState cntState = getRequiredContainer(identity);
        containers.remove(identity);
        return cntState;
    }

    ContainerState getRequiredContainer(String identity) {
        assertValid();
        ContainerState container = getContainerInternal(identity);
        IllegalStateAssertion.assertNotNull(container, "Container not registered: " + identity);
        return container;
    }

    private ContainerState getContainerInternal(String identity) {
        return containers.get(identity);
    }
}
