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
package io.fabric8.spi.scr;


import io.fabric8.api.ComponentEvent;
import io.fabric8.api.ComponentEvent.EventType;
import io.fabric8.spi.EventDispatcher;
import io.fabric8.spi.permit.PermitKey;
import io.fabric8.spi.permit.PermitManager;

public abstract class AbstractProtectedComponent<T> extends AbstractComponent {

    protected final ValidatingReference<EventDispatcher> eventDispatcher = new ValidatingReference<EventDispatcher>();
    protected final ValidatingReference<PermitManager> permitManager = new ValidatingReference<PermitManager>();

    protected void activateComponent(PermitKey<T> key, T instance) {
        try {
            super.activateComponent();
            permitManager.get().activate(key, instance);
            ComponentEvent event = new ComponentEvent(key.getType(), EventType.ACTIVATED);
            eventDispatcher.get().dispatchComponentEvent(event);
        } catch (RuntimeException ex) {
            ComponentEvent event = new ComponentEvent(key.getType(), EventType.ERROR, ex);
            eventDispatcher.get().dispatchComponentEvent(event);
            throw ex;
        }
    }

    protected void deactivateComponent(PermitKey<T> key) {
        try {
            permitManager.get().deactivate(key);
            super.deactivateComponent();
            ComponentEvent event = new ComponentEvent(key.getType(), EventType.DEACTIVATED);
            eventDispatcher.get().dispatchComponentEvent(event);
        } catch (RuntimeException ex) {
            ComponentEvent event = new ComponentEvent(key.getType(), EventType.ERROR, ex);
            eventDispatcher.get().dispatchComponentEvent(event);
            throw ex;
        }
    }

    @Override
    protected void activateComponent() {
        throw new UnsupportedOperationException();
    }

    protected final void deactivateComponent() {
        throw new UnsupportedOperationException();
    }

    public void bindEventDispatcher(EventDispatcher service) {
        eventDispatcher.bind(service);
    }
    public void unbindEventDispatcher(EventDispatcher service) {
        eventDispatcher.unbind(service);
    }

    public void bindPermitManager(PermitManager service) {
        permitManager.bind(service);
    }
    public void unbindPermitManager(PermitManager service) {
        permitManager.unbind(service);
    }
}
