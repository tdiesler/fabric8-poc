/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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

import io.fabric8.api.state.State;
import io.fabric8.api.state.StateService;
import io.fabric8.api.state.StateService.Permit;
import io.fabric8.api.state.StateTimeoutException;
import io.fabric8.spi.DefaultStateService;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link StateService}.
 *
 * @author thomas.diesler@jboss.com
 * @since 05-Mar-2014
 */
public class StateServiceTestCase {

    StateService stateService;

    @Before
    public void setUp() {
        stateService = new DefaultStateService();
    }

    @Test
    public void testBasicLifecycle() throws Exception {
        State stateA = new State("A", 1);

        // No permit on inactive state
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);

        // Activate the state
        stateService.activate(stateA);

        // Aquire max permits
        Permit permit = stateService.aquirePermit(stateA, false);
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);

        // Cannot deactivate while permits in use
        assertDeactivateTimeout(stateA, 100, TimeUnit.MILLISECONDS);
        permit.release();

        // Deactivate state
        stateService.deactivate(stateA, 100, TimeUnit.MILLISECONDS);

        // No permit on inactive state
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReleaseFromOtherThread() throws Exception {

        final State stateA = new State("A", 1);

        stateService.activate(stateA);

        final Permit permit = stateService.aquirePermit(stateA, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // ignore
                }
                permit.release();
            }
        }).start();

        stateService.deactivate(stateA, 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testAquireExclusive() throws Exception {

        State stateA = new State("A", 2);

        stateService.activate(stateA);

        // Aquire exclusive permit
        Permit permit = stateService.aquirePermit(stateA, true);

        // Assert that no other permit can be obtained
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);
        assertPermitTimeout(stateA, true, 100, TimeUnit.MILLISECONDS);

        permit.release();
        stateService.aquirePermit(stateA, false);
    }

    @Test
    public void testDeactivateWithExclusivePermit() throws Exception {

        State stateA = new State("A", 2);

        stateService.activate(stateA);

        // Aquire all permits
        Permit permit = stateService.aquirePermit(stateA, true);

        // Deactivate while holding an exclusive permit
        stateService.deactivate(stateA, 100, TimeUnit.MILLISECONDS);

        stateService.activate(stateA);

        // Assert that no other permit can be obtained
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);

        permit.release();
        stateService.aquirePermit(stateA, false);
    }

    @Test
    public void testMaxPermits() throws Exception {

        State stateA = new State("A", 2);

        stateService.activate(stateA);

        // Aquire all permits
        stateService.aquirePermit(stateA, false);
        Permit permit = stateService.aquirePermit(stateA, false);

        // Assert that no other permit can be obtained
        assertPermitTimeout(stateA, false, 100, TimeUnit.MILLISECONDS);

        permit.release();
        stateService.aquirePermit(stateA, false);
    }

    private void assertPermitTimeout(State state, boolean exclusive, long timeout, TimeUnit unit) {
        try {
            stateService.aquirePermit(state, exclusive, timeout, unit);
            Assert.fail("TimeoutException expected");
        } catch (StateTimeoutException ex) {
            // expected
        }
    }

    private void assertDeactivateTimeout(State state, long timeout, TimeUnit unit) {
        try {
            stateService.deactivate(state, timeout, unit);
            Assert.fail("TimeoutException expected");
        } catch (StateTimeoutException ex) {
            // expected
        }
    }
}