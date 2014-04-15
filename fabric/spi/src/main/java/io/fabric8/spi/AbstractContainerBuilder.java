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
package io.fabric8.spi;

import io.fabric8.api.ContainerBuilder;
import io.fabric8.api.CreateOptions;

public abstract class AbstractContainerBuilder<B extends ContainerBuilder<B, C>, C extends CreateOptions> implements ContainerBuilder<B, C> {

    protected final C options;

    protected AbstractContainerBuilder(C options) {
        this.options = options;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B addIdentity(String symbolicName) {
        getMutableOptions().setSymbolicName(symbolicName);
        return (B) this;
    }

    protected AbstractCreateOptions getMutableOptions() {
        return (AbstractCreateOptions) options;
    }

    @Override
    public C getCreateOptions() {
        getMutableOptions().validateConfiguration();
        getMutableOptions().makeImmutable();
        return options;
    }
}