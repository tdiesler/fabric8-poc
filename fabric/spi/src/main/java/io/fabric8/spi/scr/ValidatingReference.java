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
package io.fabric8.spi.scr;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reference that validates its content on {@link #get()}
 *
 * @author thomas.diesler@jboss.com
 * @since 13-Sep-2013
 *
 * @ThreadSafe
 */
public class ValidatingReference<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatingReference.class);

    private final AtomicReference<T> reference = new AtomicReference<T>();

    /**
     * Bind the given reference
     */
    public void bind(T ref) {
        LOGGER.trace("bind: {}", ref);
        reference.set(ref);
    }

    /**
     * Unbind the given reference
     */
    public void unbind(T ref) {
        LOGGER.trace("unbind: {}", ref);
        reference.compareAndSet(ref, null);
    }

    /**
     * Get the referenced instance
     * @throws InvalidComponentException If the reference is not valid
     */
    public T get() {
        T ref = reference.get();
        if (ref == null)
            throw new InvalidComponentException("Reference not available");
        return ref;
    }

    /**
     * Get the referenced instance
     * @return The references instance or null
     */
    public T getOptional() {
        return reference.get();
    }

}
