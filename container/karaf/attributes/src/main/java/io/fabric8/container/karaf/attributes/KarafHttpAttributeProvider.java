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

package io.fabric8.container.karaf.attributes;

import static io.fabric8.api.ContainerAttributes.HTTPS_BINDING_PORT_KEY;
import static io.fabric8.api.ContainerAttributes.HTTP_BINDING_PORT_KEY;
import io.fabric8.api.ContainerAttributes;
import io.fabric8.spi.AttributeProvider;
import io.fabric8.spi.Configurer;
import io.fabric8.spi.HttpAttributeProvider;
import io.fabric8.spi.RuntimeService;
import io.fabric8.spi.scr.AbstractAttributeProvider;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(configurationPid = KarafHttpAttributeProvider.PAX_WEB_PID, policy = ConfigurationPolicy.REQUIRE, immediate = true)
@Service({AttributeProvider.class, HttpAttributeProvider.class})
@Properties({
                @Property(name = "type", value = ContainerAttributes.TYPE),
                @Property(name = "classifier", value = "http")
})
public class KarafHttpAttributeProvider extends AbstractAttributeProvider implements HttpAttributeProvider {

    static final String PAX_WEB_PID = "org.ops4j.pax.web";
    private static final String HTTP_CONNECTION_PORT_KEY = "io.fabric8.http.connection.port";
    private static final String HTTPS_CONNECTION_PORT_KEY = "io.fabric8.http.connection.port.secure";

    private static final String HTTP_ENABLED = "org.osgi.service.http.enabled";
    private static final String HTTPS_ENABLED = "org.osgi.service.http.secure.enabled";
    private static final String HTTP_URL_FORMAT = "%s://${container:%s/fabric8.ip}:%d";

    @Property(name = HTTP_BINDING_PORT_KEY, value = "${" + HTTP_BINDING_PORT_KEY + "}")
    private int httpPort = 8080;
    @Property(name = HTTPS_BINDING_PORT_KEY, value = "${" + HTTPS_BINDING_PORT_KEY + "}")
    private int httpPortSecure = 8443;
    @Property(name = HTTP_CONNECTION_PORT_KEY, value = "${" + HTTP_CONNECTION_PORT_KEY + "}")
    private int httpConnectionPort = 0;
    @Property(name = HTTPS_CONNECTION_PORT_KEY, value = "${" + HTTPS_CONNECTION_PORT_KEY + "}")
    private int httpConnectionPortSecure = 0;
    @Property(name = HTTP_ENABLED, value = "${" + HTTP_ENABLED + "}")
    private boolean httpEnabled = true;
    @Property(name = HTTPS_ENABLED, value = "${" + HTTPS_ENABLED + "}")
    private boolean httpSecureEnabled = false;
    @Property(name = "runtimeId", value = "${" + RuntimeService.RUNTIME_IDENTITY + "}")
    private String runtimeId;

    @Reference
    private Configurer configurer;

    private String httpUrl;
    private String httpsUrl;

    @Activate
    void activate(Map<String, Object> configuration) throws Exception {
        configureInternal(configuration);
        updateAttributes();
        activateComponent();
    }

    @Modified
    void modified(Map<String, Object> configuration) throws Exception {
        configureInternal(configuration);
        updateAttributes();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    @Override
    public String getHttpsUrl() {
        return httpsUrl;
    }

    @Override
    public String getHttpUrl() {
        return httpUrl;
    }

    private void configureInternal(Map<String, Object> configuration) throws Exception {
        configurer.configure(configuration, this, "org.osgi.service", "io.fabric8");
        httpConnectionPort = httpConnectionPort != 0 ? httpConnectionPort : httpPort;
        httpConnectionPortSecure = httpConnectionPortSecure != 0 ? httpConnectionPortSecure : httpPortSecure;

    }

    private void updateAttributes() throws Exception {
        putAttribute(ContainerAttributes.ATTRIBUTE_KEY_HTTP_PORT, httpConnectionPort != 0 ? httpConnectionPort : httpPort);
        putAttribute(ContainerAttributes.ATTRIBUTE_KEY_HTTPS_PORT, httpConnectionPortSecure != 0 ? httpConnectionPortSecure : httpPortSecure);
        int httpPort = httpSecureEnabled && !httpEnabled ? httpConnectionPortSecure : httpConnectionPort;
        int httpsPort = httpSecureEnabled ? httpConnectionPortSecure : 0;
        putAttribute(ContainerAttributes.ATTRIBUTE_KEY_HTTP_URL, getHttpUrl(runtimeId, httpPort));
        putAttribute(ContainerAttributes.ATTRIBUTE_KEY_HTTPS_URL, getHttpsUrl(runtimeId, httpsPort));
    }

    private String getHttpUrl(String id, int port) {
        return httpUrl = String.format(HTTP_URL_FORMAT, "http", id, port);
    }

    private String getHttpsUrl(String id, int port) {
        return httpsUrl = String.format(HTTP_URL_FORMAT, "https", id, port);
    }

    void bindConfigurer(Configurer configurer) {
        this.configurer = configurer;
    }
    void unbindConfigurer(Configurer configurer) {
        this.configurer = null;
    }
}
