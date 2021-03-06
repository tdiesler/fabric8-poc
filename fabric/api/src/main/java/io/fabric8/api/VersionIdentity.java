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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.gravia.resource.Version;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * A profile version identity
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public final class VersionIdentity implements Identity {

    private static final String IDENTITY_FORMAT = "(%s)(,rev=(?<rev>%s))?";
    private static final Pattern IDENTITY_PATTERN = Pattern.compile(String.format(IDENTITY_FORMAT, GROUP, GROUP));

    public static VersionIdentity emptyVersion = new VersionIdentity(Version.emptyVersion, null);

    private final Version version;
    private final String revision;

    public static VersionIdentity create(String version) {
        return new VersionIdentity(Version.parseVersion(version), null);
    }

    public static VersionIdentity create(Version version) {
        return new VersionIdentity(version, null);
    }

    public static VersionIdentity create(String version, String revision) {
        return new VersionIdentity(Version.parseVersion(version), revision);
    }

    public static VersionIdentity create(Version version, String revision) {
        return new VersionIdentity(version, revision);
    }

    public static VersionIdentity createFrom(String canonical) {
        Matcher matcher = IDENTITY_PATTERN.matcher(canonical);
        IllegalArgumentAssertion.assertTrue(matcher.matches(), "Identity '" + canonical + "' does not match pattern: " + IDENTITY_PATTERN);
        String version = matcher.group(1);
        String revision = matcher.group("rev");
        return new VersionIdentity(Version.parseVersion(version), revision);
    }

    private VersionIdentity(Version version, String revision) {
        IllegalArgumentAssertion.assertNotNull(version, "version");
        this.version = version;
        this.revision = revision;
    }

    public Version getVersion() {
        return version;
    }

    public String getRevision() {
        return revision;
    }

    @Override
    public String getCanonicalForm() {
        return version + (revision != null ? ",rev=" + revision : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VersionIdentity)) return false;
        VersionIdentity other = (VersionIdentity) obj;
        return other.getCanonicalForm().equals(getCanonicalForm());
    }

    @Override
    public int hashCode() {
        return getCanonicalForm().hashCode();
    }

    @Override
    public String toString() {
        return getCanonicalForm();
    }
}
