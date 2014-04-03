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
package io.fabric8.spi.internal;

import io.fabric8.api.ConfigurationItem;
import io.fabric8.spi.AbstractProfileItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class DefaultConfigurationItem extends AbstractProfileItem implements ConfigurationItem {

    private final Map<String, String> configuration = new HashMap<String, String>();

    DefaultConfigurationItem(String identity, Map<String, String> config) {
        super(identity);
        if (config != null) {
            configuration.putAll(config);
        }
    }

    @Override
    public Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }
}