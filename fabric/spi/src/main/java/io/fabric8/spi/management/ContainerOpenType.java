/*
 * #%L
 * Gravia :: Resource
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
package io.fabric8.spi.management;

import io.fabric8.api.Container;
import io.fabric8.api.HostIdentity;
import io.fabric8.api.ServiceEndpoint;
import io.fabric8.api.ServiceEndpointIdentity;
import io.fabric8.spi.AttributeSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.jboss.gravia.resource.Version;

/**
 * CompositeData support for a {@link Container}.
 *
 * [TODO] Complete ContainerOpenType
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Apr-2014
 */
public final class ContainerOpenType {

    public static final String TYPE_NAME = "ContainerType";
    public static final String ITEM_IDENTITY = "identity";
    public static final String ITEM_ATTRIBUTES = "attributes";

    // Hide ctor
    private ContainerOpenType() {
    }

    private static final CompositeType compositeType;
    static {
        try {
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        } catch (OpenDataException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static CompositeType getCompositeType() {
        return compositeType;
    }

    public static CompositeData getCompositeData(Container container) {
        String identity = container.getIdentity();
        List<Object> items = new ArrayList<Object>();
        items.add(identity);
        try {
            items.add(AttributesOpenType.getCompositeData(container.getAttributes()));
            Object[] itemValues = items.toArray(new Object[items.size()]);
            return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
        } catch (OpenDataException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Container getContainer(CompositeData cdata) {
        return new CompositeDataContainer(cdata, ContainerOpenType.class.getClassLoader());
    }

    public static Container getContainer(CompositeData cdata, ClassLoader classLoader) {
        return new CompositeDataContainer(cdata, classLoader);
    }

    public static String[] getItemNames() {
        return new String[] { ITEM_IDENTITY, ITEM_ATTRIBUTES };
    }

    public static OpenType<?>[] getItemTypes() throws OpenDataException {
        ArrayType<CompositeType> attsType = AttributesOpenType.getArrayType();
        return new OpenType<?>[] { SimpleType.STRING, attsType };
    }

    static class CompositeDataContainer extends AttributeSupport implements Container {

        private final String identity;

        private CompositeDataContainer(CompositeData cdata, ClassLoader classLoader) {
            identity = (String) cdata.get(ContainerOpenType.ITEM_IDENTITY);
            for (CompositeData attData : (CompositeData[]) cdata.get(ContainerOpenType.ITEM_ATTRIBUTES)) {
                AttributesOpenType.addAttribute(this, attData, classLoader);
            }
        }

        @Override
        public String getIdentity() {
            return identity;
        }

        @Override
        public HostIdentity getHostIdentity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public State getState() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getParentIdentity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getChildIdentities() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getManagementDomains() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends ServiceEndpoint> Set<ServiceEndpointIdentity<?>> getEndpointIdentities(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Version getProfileVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getProfileIdentities() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "ContainerIdentity[" + identity + "]";
        }
    }
}
