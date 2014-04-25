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
package io.fabric8.core.internal;

import io.fabric8.spi.PortManager;
import io.fabric8.spi.scr.AbstractComponent;
import io.fabric8.spi.utils.IllegalStateAssertion;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A host wide port manager
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Apr-2014
 */
@Component(service = { PortManager.class }, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class PortManagerImpl extends AbstractComponent implements PortManager {

    @Activate
    void activate() {
        activateComponent();
    }

    @Deactivate
    void deactivate() {
        deactivateComponent();
    }

    @Override
    public int nextAvailablePort(int portValue, InetAddress bindAddr) {
        ServerSocket socket = null;
        int endPort = portValue + 100;
        while (socket == null && portValue < endPort) {
            try {
                socket = new ServerSocket(portValue, 0, bindAddr);
            } catch (IOException ex) {
                portValue++;
            }
        }
        IllegalStateAssertion.assertNotNull(socket, "Cannot obtain next available port");
        int resultPort = socket.getLocalPort();
        try {
            socket.close();
        } catch (IOException e) {
            // ignore
        }
        return resultPort;
    }
}
