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
package io.fabric8.spi.scr;

import io.fabric8.spi.permit.PermitManager;
import io.fabric8.spi.permit.PermitState;

public abstract class AbstractProtectedComponent<T> extends AbstractComponent {

    protected void activateComponent(PermitState<T> state, T instance) {
        super.activateComponent();
        getPermitManager().activate(state, instance);
    }

    protected void deactivateComponent(PermitState<T> state) {
        getPermitManager().deactivate(state);
        super.deactivateComponent();
    }

    protected final void deactivateComponent() {
        throw new UnsupportedOperationException();
    }

    protected abstract PermitManager getPermitManager();
}