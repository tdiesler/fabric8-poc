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
import io.fabric8.api.ContainerIdentity;
import io.fabric8.api.HostIdentity;
import io.fabric8.api.LockHandle;
import io.fabric8.api.ServiceEndpoint;
import io.fabric8.api.ServiceEndpointIdentity;
import io.fabric8.core.internal.ContainerServiceImpl.ContainerState;
import io.fabric8.core.internal.ProfileServiceImpl.ProfileVersionState;
import io.fabric8.spi.AttributeSupport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.gravia.resource.Version;

/**
 * An immutable container
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Mar-2014
 *
 * @Immutable
 */
final class ImmutableContainer extends AttributeSupport implements Container {

    private final ContainerIdentity identity;
    private final Version profileVersion;
    private final Set<ContainerIdentity> children = new HashSet<>();
    private final Set<String> profiles = new HashSet<>();
    private final Set<ServiceEndpointIdentity<?>> endpoints = new HashSet<>();
    private final ContainerIdentity parent;
    private final String tostring;
    private final State state;

    ImmutableContainer(ContainerState cntState) {
        super(cntState.getAttributes());
        LockHandle readLock = cntState.aquireReadLock();
        try {
            identity = cntState.getIdentity();
            ContainerState parentState = cntState.getParentState();
            parent = parentState != null ? parentState.getIdentity() : null;
            ProfileVersionState versionState = cntState.getProfileVersion();
            profileVersion = versionState != null ? versionState.getIdentity() : null;
            state = cntState.getState();
            children.addAll(cntState.getChildContainers());
            profiles.addAll(cntState.getProfiles());
            endpoints.addAll(cntState.getServiceEndpointIdentities());
            tostring = cntState.toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ContainerIdentity getParentIdentity() {
        return parent;
    }

    @Override
    public ContainerIdentity getIdentity() {
        return identity;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Set<ContainerIdentity> getChildIdentities() {
        return Collections.unmodifiableSet(children);
    }

    @Override
    public Version getProfileVersion() {
        return profileVersion;
    }

    @Override
    public Set<String> getProfileIdentities() {
        return Collections.unmodifiableSet(profiles);
    }

    @Override
    public HostIdentity getHostIdentity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getManagementDomains() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends ServiceEndpoint> Set<ServiceEndpointIdentity<?>> getEndpointIdentities(Class<T> type) {
        Set<ServiceEndpointIdentity<?>> result = new HashSet<>();
        for (ServiceEndpointIdentity<?> epid : endpoints) {
            if (type == null || type.isAssignableFrom(epid.getType())) {
                result.add(epid);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImmutableContainer)) return false;
        ImmutableContainer other = (ImmutableContainer) obj;
        return other.identity.equals(identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    @Override
    public String toString() {
        return tostring;
    }
}
