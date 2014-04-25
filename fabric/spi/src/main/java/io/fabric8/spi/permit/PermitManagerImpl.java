/*
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
 *
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
 */
package io.fabric8.spi.permit;

import io.fabric8.spi.scr.AbstractComponent;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = { PermitManager.class }, immediate = true)
public final class PermitManagerImpl extends AbstractComponent implements PermitManager {

    private final PermitManager delegate = new DefaultPermitManager();

    @Activate
    void activate() {
        activateComponent();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    @Override
    public <T> void activate(PermitKey<T> state, T instance) {
        assertValid();
        delegate.activate(state, instance);
    }

    @Override
    public void deactivate(PermitKey<?> state) {
        assertValid();
        delegate.deactivate(state);
    }

    @Override
    public void deactivate(PermitKey<?> state, long timeout, TimeUnit unit) throws PermitStateTimeoutException {
        assertValid();
        delegate.deactivate(state, timeout, unit);
    }

    @Override
    public <T> Permit<T> aquirePermit(PermitKey<T> state, boolean exclusive) {
        assertValid();
        return delegate.aquirePermit(state, exclusive);
    }

    @Override
    public <T> Permit<T> aquirePermit(PermitKey<T> state, boolean exclusive, long timeout, TimeUnit unit) throws PermitStateTimeoutException {
        assertValid();
        return delegate.aquirePermit(state, exclusive, timeout, unit);
    }

}
