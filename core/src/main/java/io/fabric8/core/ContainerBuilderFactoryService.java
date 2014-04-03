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
package io.fabric8.core;

import io.fabric8.api.ContainerBuilder;
import io.fabric8.api.ContainerBuilderFactory;
import io.fabric8.spi.internal.DefaultContainerBuilder;
import io.fabric8.spi.scr.AbstractComponent;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = { ContainerBuilderFactory.class }, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class ContainerBuilderFactoryService extends AbstractComponent implements ContainerBuilderFactory {

    @Activate
    void activate() throws Exception {
        activateComponent();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ContainerBuilder> T create(Class<T> type) {
        assertValid();
        return (T) new DefaultContainerBuilder();
    }
}