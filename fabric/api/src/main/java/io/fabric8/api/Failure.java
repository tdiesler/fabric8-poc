/*
 * #%L
 * Fabric8 :: API
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

package io.fabric8.api;

import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * A container failure abstraction.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public final class Failure {

    private final String message;
    private final long timestamp;
    private final Throwable cause;

    public Failure(String message) {
        this(message, null);
    }

    public Failure(String message, Throwable cause) {
        IllegalArgumentAssertion.assertNotNull(message, "message");
        this.timestamp = System.currentTimeMillis();
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Throwable getCause() {
        return cause;
    }
}
