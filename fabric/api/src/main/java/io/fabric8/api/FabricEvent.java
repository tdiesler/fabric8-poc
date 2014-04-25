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
package io.fabric8.api;

import java.util.EventObject;

import org.jboss.gravia.utils.NotNullException;


/**
 * An abstract fabric event
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
@SuppressWarnings("serial")
public abstract class FabricEvent<S, T> extends EventObject {

    private final T type;
    private final Throwable error;

    public FabricEvent(S source, T type, Throwable error) {
        super(source);
        NotNullException.assertValue(source, "source");
        NotNullException.assertValue(type, "type");
        this.error = error;
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S) super.getSource();
    }

    public T getType() {
        return type;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[source=" + getSource() + ",type=" + getType() + "]";
    }
}
