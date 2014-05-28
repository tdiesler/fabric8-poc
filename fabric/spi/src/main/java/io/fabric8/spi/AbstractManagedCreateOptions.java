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
package io.fabric8.spi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.utils.IllegalStateAssertion;


public abstract class AbstractManagedCreateOptions extends AbstractCreateOptions implements ManagedCreateOptions {

    private List<MavenCoordinates> mavenCoordinates = new ArrayList<MavenCoordinates>();
    private boolean outputToConsole;
    private String javaVmArguments;
    private File targetDirectory;
    private boolean zooKeeperServer;

    /**
     * Get the array of maven artefacts that are getting unpacked
     * during {@link ManagedContainer#createFrom(ContainerConfiguration)}
     */
    public List<MavenCoordinates> getMavenCoordinates() {
        return Collections.unmodifiableList(mavenCoordinates);
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    public boolean isOutputToConsole() {
        return outputToConsole;
    }

    public boolean isZooKeeperServer() {
        return zooKeeperServer;
    }

    /*
     * Setters are protected
     */

    protected void addMavenCoordinates(MavenCoordinates coordinates) {
        mavenCoordinates.add(coordinates);
    }

    protected void setTargetDirectory(File target) {
        this.targetDirectory = target;
    }

    protected void setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
    }

    protected void setOutputToConsole(boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
    }

    protected void setZooKeeperServer(boolean zooKeeperServer) {
        this.zooKeeperServer = zooKeeperServer;
    }

    @Override
    protected void validate() {
        IllegalStateAssertion.assertNotNull(targetDirectory, "targetDirectory");
        super.validate();
    }
}
