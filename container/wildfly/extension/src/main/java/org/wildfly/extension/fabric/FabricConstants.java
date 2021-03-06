/*
 * #%L
 * Fabric8 :: Container :: WildFly :: Extension
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


package org.wildfly.extension.fabric;

import org.jboss.msc.service.ServiceName;

/**
 * Fabric subsystem constants.
 *
 * @since 13-Nov-2013
 */
public interface FabricConstants {

    /** The base name for all gravia services */
    ServiceName FABRIC_BASE_NAME = ServiceName.JBOSS.append("wildfly", "fabric");
    /** The name for the gravia subsystem service */
    ServiceName FABRIC_SUBSYSTEM_SERVICE_NAME = FABRIC_BASE_NAME.append("Subsystem");
}
