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

import io.fabric8.api.Attributable;
import io.fabric8.api.ContainerIdentity;

import java.util.concurrent.TimeUnit;

import javax.management.remote.JMXConnector;

import org.jboss.gravia.process.api.ManagedProcess;


/**
 * The managed root container
 *
 * @author thomas.diesler@jboss.com
 * @since 26-Feb-2014
 */
public interface ManagedContainer<T extends ManagedCreateOptions> extends ManagedProcess<T>, Attributable {

    ContainerIdentity getIdentity();

    JMXConnector getJMXConnector(String jmxUsername, String jmxPassword, long timeout, TimeUnit unit);
}
