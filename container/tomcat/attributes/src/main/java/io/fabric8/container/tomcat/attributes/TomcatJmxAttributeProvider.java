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

package io.fabric8.container.tomcat.attributes;


import io.fabric8.api.ContainerAttributes;
import io.fabric8.spi.AttributeProvider;
import io.fabric8.spi.JmxAttributeProvider;
import io.fabric8.spi.NetworkAttributeProvider;
import io.fabric8.spi.RuntimeService;
import io.fabric8.spi.scr.AbstractAttributeProvider;
import io.fabric8.spi.scr.ValidatingReference;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(policy = ConfigurationPolicy.IGNORE, immediate = true)
@Service({ AttributeProvider.class, JmxAttributeProvider.class})
@Properties({
        @Property(name = "type", value = ContainerAttributes.TYPE),
        @Property(name = "classifier", value = "jmx")
})
public class TomcatJmxAttributeProvider extends AbstractAttributeProvider implements JmxAttributeProvider  {

    private static final String JMX_REMOTE_PORT = "com.sun.management.jmxremote.port";
    private static final int DEFAULT_JMX_REMOTE_PORT = 1099;

    //private static final String JMX_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://${container:%s/fabric8.ip}/:%d/jmxrmi";
    private static final String JMX_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";

    @Reference(referenceInterface = RuntimeService.class)
    private final ValidatingReference<RuntimeService> runtimeService = new ValidatingReference<>();
    @Reference(referenceInterface = NetworkAttributeProvider.class)
    private final ValidatingReference<NetworkAttributeProvider> networkProvider = new ValidatingReference<>();

    private String ip;
    private int jmxRemotePort;
    private String jmxServerUrl;
    private String jmxUsername;
    private String jmxPassword;

    @Activate
    void activate() throws Exception {
        jmxRemotePort = Integer.parseInt(runtimeService.get().getProperty(JMX_REMOTE_PORT, "" + DEFAULT_JMX_REMOTE_PORT));
        ip = networkProvider.get().getIp();
        putAttribute(ContainerAttributes.ATTRIBUTE_KEY_JMX_SERVER_URL, getJmxUrl(ip, jmxRemotePort));
        activateComponent();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    @Override
    public String getJmxServerUrl() {
        return jmxServerUrl;
    }

    @Override
    public String getJmxUsername() {
        return jmxUsername;
    }

    @Override
    public String getJmxPassword() {
        return jmxPassword;
    }

    private String getJmxUrl(String ip, int port)  {
        return jmxServerUrl = String.format(JMX_URL_FORMAT, ip, port);
    }

    void bindNetworkProvider(NetworkAttributeProvider service) {
        networkProvider.bind(service);
    }
    void unbindNetworkProvider(NetworkAttributeProvider service) {
        networkProvider.unbind(service);
    }

    void bindRuntimeService(RuntimeService service) {
        runtimeService.bind(service);
    }
    void unbindRuntimeService(RuntimeService service) {
        runtimeService.unbind(service);
    }
}
