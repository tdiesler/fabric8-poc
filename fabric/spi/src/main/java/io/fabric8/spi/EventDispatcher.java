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

package io.fabric8.spi;

import io.fabric8.api.ComponentEvent;
import io.fabric8.api.ProfileEvent;
import io.fabric8.api.ProfileEventListener;
import io.fabric8.api.ProvisionEvent;
import io.fabric8.api.ProvisionEventListener;

public interface EventDispatcher {

    void dispatchProvisionEvent(ProvisionEvent event, ProvisionEventListener listener);

    void dispatchProfileEvent(ProfileEvent event, ProfileEventListener listener);

    void dispatchComponentEvent(ComponentEvent event);

}