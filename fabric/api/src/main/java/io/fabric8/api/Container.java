/*
 * #%L
 * Fabric8 :: API
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
package io.fabric8.api;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import org.jboss.gravia.runtime.RuntimeType;

/**
 * A fabric container
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public interface Container extends Attributable, Identifiable<ContainerIdentity> {

    /**
     * The container states
     */
    enum State {
        CREATED, STARTED, STOPPED, DESTROYED
    }

    /**
     * The configuration PID for this service
     */
    String CONTAINER_SERVICE_PID = "container.service.pid";

    /**
     * Get the container state
     */
    State getState();

    /**
     * The type of the container.
     */
    RuntimeType getRuntimeType();

    /**
     * Get the associated host
     */
    InetAddress getHostIdentity();

    /**
     * Get the parent container
     */
    ContainerIdentity getParentIdentity();

    /**
     * Get the set of child containers
     */
    Set<ContainerIdentity> getChildIdentities();

    /**
     * Get the set of provided management domains
     */
    Set<String> getManagementDomains();

    /**
     * Get the set of available service endpoints
     */
    Set<ServiceEndpoint> getServiceEndpoints();

    /**
     * Get the service endpoint for the given identity
     */
    ServiceEndpoint getServiceEndpoint(ServiceEndpointIdentity identity);

    /**
     * Get the profile version
     */
    VersionIdentity getProfileVersion();

    /**
     * Get the associated list of profiles
     */
    List<ProfileIdentity> getProfileIdentities();
}
