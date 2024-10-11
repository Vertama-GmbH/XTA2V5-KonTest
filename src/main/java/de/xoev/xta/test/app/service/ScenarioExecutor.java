/*
* @formatter:off
* 
* Copyright 2021-2022  Koordinierungsstelle für IT-Standards (KoSIT)
*
* Licensed under the European Public License, Version 1.2 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     https://opensource.org/licenses/EUPL-1.2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* @formatter:on
*/
package de.xoev.xta.test.app.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.xoev.xta.test.app.exception.CancelScenarioExecution;
import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.model.ScenarioDefinition;
import de.xoev.xta.test.app.model.ScenarioEventDefinition;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ScenarioExecutor {

    @Autowired
    private XtaClient xtaClient;

    @Autowired
    private ProtocolService protocolService;

    private ScenarioExecutorRunnable runnable;

    @PostConstruct
    protected void init() {
        runnable = new ScenarioExecutorRunnable();
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @PreDestroy
    public void onExit() {
        runnable.stopScenarioExecution();
        runnable.stop();
    }

    public void execute(final ScenarioDefinition scenarioDefinition) throws XtaTesterException {
        runnable.setScenarioDefinition(scenarioDefinition);
    }

    public void stopScenarioExecution() {
        runnable.stopScenarioExecution();
    }

    private class ScenarioExecutorRunnable implements Runnable {

        private AtomicBoolean stop = new AtomicBoolean(false);
        private AtomicBoolean stopScenarioExecution = new AtomicBoolean(false);
        private AtomicReference<ScenarioDefinition> scenarioDefinitionRef = new AtomicReference<>();

        @Override
        public void run() {
            while (!stop.get()) {
                ScenarioDefinition scenarioDefinition = scenarioDefinitionRef.get();
                if (scenarioDefinition != null) {
                    executeScenarioDefinition(scenarioDefinition);
                    scenarioDefinitionRef.set(null);
                } else {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                stopScenarioExecution.set(false);
            }
        }

        private void executeScenarioDefinition(final ScenarioDefinition scenarioDefinition) {
            try {
                for (ScenarioEventDefinition scenarioEventDefinition : scenarioDefinition
                        .getActiveEventDecriptions()) {
                    while (!protocolService
                            .checkDependendEventsCalled(scenarioEventDefinition.getRequiredPreviousEventTypes())) {
                        if (stopScenarioExecution.get()) {
                            break;
                        }
                        Thread.sleep(2000);
                    }
                    execute(scenarioEventDefinition);

                    if (stopScenarioExecution.get()) {
                        break;
                    }
                }
            } catch (CancelScenarioExecution e) {
                log.error("An error occurred the scenario execution was canceled. {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unexpected error occurred the scenario execution was canceled. {}", e.getMessage(), e);
            }
        }

        public void stop() {
            synchronized (runnable) {
                stop.set(true);
                runnable.notifyAll();
            }
        }

        public void stopScenarioExecution() {
            synchronized (runnable) {
                stopScenarioExecution.set(true);
                runnable.notifyAll();
            }
        }

        public void setScenarioDefinition(final ScenarioDefinition scenarioDefinition) throws XtaTesterException {
            synchronized (runnable) {
                if (this.scenarioDefinitionRef.get() == null) {
                    scenarioDefinitionRef.set(scenarioDefinition);
                    stopScenarioExecution.set(false);
                    runnable.notifyAll();
                } else {
                    throw new XtaTesterException(
                            "Could not set the scenario definition, because a scenario is currently executed");
                }
            }
        }

        private void execute(final ScenarioEventDefinition scenarioEventDefinition) {
            switch (scenarioEventDefinition.getEventType()) {
            case CHECK_ACCOUNT_ACTIVE:
                xtaClient.checkAccountActive();
                break;
            case LOOKUP_SERVICE:
                xtaClient.lookupService();
                break;
            case CREATE_MESSAGE_ID:
                xtaClient.createMessageId();
                break;
            case SEND_MESSAGE:
                xtaClient.sendMessage();
                break;
            case SEND_MESSAGE_SYNC:
                xtaClient.sendMessageSync();
                break;
            case GET_TRANSPORT_REPORT:
                xtaClient.getTransportReport();
                break;
            case GET_STATUS_LIST:
                xtaClient.getStatusList();
                break;
            case GET_MESSAGE:
                xtaClient.getMessage();
                break;
            case CLOSE:
                xtaClient.close();
                break;
            case DELIVER_MESSAGE:
                xtaClient.deliverMessage();
                break;
            default:
                log.error("Event type '{}' not implemented", scenarioEventDefinition.getEventType());
                break;
            }
            protocolService.closeScenarioProtocolEvent(null);
        }
    }
}
