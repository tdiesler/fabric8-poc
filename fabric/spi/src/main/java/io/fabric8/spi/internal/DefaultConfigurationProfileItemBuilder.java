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
package io.fabric8.spi.internal;

import io.fabric8.api.ConfigurationProfileItem;
import io.fabric8.api.ConfigurationProfileItemBuilder;
import io.fabric8.spi.AbstractAttributableBuilder;
import io.fabric8.spi.AbstractProfileItem;
import io.fabric8.spi.utils.IllegalStateAssertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class DefaultConfigurationProfileItemBuilder extends AbstractAttributableBuilder<ConfigurationProfileItemBuilder, ConfigurationProfileItem> implements ConfigurationProfileItemBuilder {

    private final MutableConfigurationProfileItem mutableItem;
    private boolean immutable;

    DefaultConfigurationProfileItemBuilder(String identity) {
        mutableItem = new MutableConfigurationProfileItem(identity);
    }

    @Override
    public ConfigurationProfileItemBuilder setConfiguration(Map<String, Object> config) {
        assertMutable();
        mutableItem.setConfiguration(config);
        return this;
    }

    @Override
    public ConfigurationProfileItem build() {
        validate();
        makeImmutable();
        return mutableItem.immutableProfileItem();
    }

    private void validate() {
        IllegalStateAssertion.assertNotNull(mutableItem.getIdentity(), "Configuration item must have an identity");
    }

    protected void assertMutable() {
        IllegalStateAssertion.assertFalse(immutable, "Builder is immutable");
    }

    protected void makeImmutable() {
        assertMutable();
        immutable = true;
    }

    private final class MutableConfigurationProfileItem extends AbstractProfileItem implements ConfigurationProfileItem {

        private final Map<String, Object> configuration = new HashMap<String, Object>();

        private MutableConfigurationProfileItem(String identity) {
            super(identity);
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return Collections.unmodifiableMap(configuration);
        }

        private void setConfiguration(Map<String, Object> config) {
            configuration.clear();
            if (config != null) {
                configuration.putAll(config);
            }
        }

        private ConfigurationProfileItem immutableProfileItem() {
            return new ImmutableConfigurationProfileItem(getIdentity(), getConfiguration());
        }
    }

    private final class ImmutableConfigurationProfileItem extends AbstractProfileItem implements ConfigurationProfileItem {

        private final Map<String, Object> configuration = new HashMap<String, Object>();

        private ImmutableConfigurationProfileItem(String identity, Map<String, Object> configuration) {
            super(identity);
            this.configuration.putAll(configuration);
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return Collections.unmodifiableMap(configuration);
        }

        @Override
        public int hashCode() {
            return getIdentity().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ImmutableConfigurationProfileItem)) return false;
            ImmutableConfigurationProfileItem other = (ImmutableConfigurationProfileItem) obj;
            return getIdentity().equals(other.getIdentity());
        }

        @Override
        public String toString() {
            return "ConfigurationItem[id=" + getIdentity() + ",config=" + configuration + "]";
        }
    }
}
