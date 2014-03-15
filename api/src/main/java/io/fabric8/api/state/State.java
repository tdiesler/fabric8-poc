/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package io.fabric8.api.state;

import org.jboss.gravia.utils.NotNullException;

/**
 * Represents a system state
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2014
 *
 * @Immutable
 */
public final class State {

    public static final int DEFAULT_MAXIMUM_PERMITS = Integer.MAX_VALUE;

    private final String name;
    private final int maxpermits;

    public State(Class<?> type) {
        this(type.getName(), DEFAULT_MAXIMUM_PERMITS);
    }

    public State(String name) {
        this(name, DEFAULT_MAXIMUM_PERMITS);
    }

    public State(String name, int maxpermits) {
        NotNullException.assertValue(name, "name");
        this.maxpermits = maxpermits;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMaximumPermits() {
        return maxpermits;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof State))
            return false;
        State other = (State) obj;
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return "State[" + name + "]";
    }
}
