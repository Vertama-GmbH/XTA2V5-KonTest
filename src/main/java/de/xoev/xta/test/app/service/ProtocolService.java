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

import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.xoev.xta.test.app.model.ContainerCustomization;
import de.xoev.xta.test.app.model.ProtocolMetadata;
import de.xoev.xta.test.app.model.ScenarioDefinition;
import de.xoev.xta.test.app.model.ScenarioEventType;
import de.xoev.xta.test.app.model.ScenarioProtocol;
import de.xoev.xta.test.app.model.ScenarioProtocolEvent;
import de.xoev.xta.test.app.model.ScenarioProtocolEventResult;
import de.xoev.xta.test.app.model.XtaProtocol;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProtocolService {

    private static ThreadLocal<ScenarioProtocolEvent> threadContext = new ThreadLocal<>();

    @Autowired
    private ConfigurationService configService;
    private XtaProtocol protocol = new XtaProtocol();

    @Getter
    private String timestamp;

    private void updateTimestamp() {
        timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @PostConstruct
    protected void init() {
        protocol.setMetadata(configService.getXtaConfig().getProtocolMetadata());
        updateTimestamp();
    }

    @PreDestroy
    public void preDestroy() {
        if (getContainerCustomization().getTempFile() != null) {
            try {
                Files.deleteIfExists(getContainerCustomization().getTempFile());
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public void clearProtocol() {
        ContainerCustomization savedConfig = getContainerCustomization();
        protocol = new XtaProtocol();
        protocol.setMetadata(configService.getXtaConfig().getProtocolMetadata());
        protocol.setContainerCustomization(savedConfig);
        updateTimestamp();
    }

    public XtaProtocol getProtocol() {
        synchronized (protocol) {
            return protocol;
        }
    }

    public ContainerCustomization getContainerCustomization() {
        synchronized (protocol) {
            return protocol.getContainerCustomization();
        }
    }

    public void setContainerCustomization(final ContainerCustomization containerCustomization) {
        synchronized (protocol) {
            protocol.setContainerCustomization(containerCustomization);
            updateTimestamp();
        }
    }

    public ProtocolMetadata getProtocolMetadata() {
        return protocol.getMetadata();
    }

    public void setProtocolMetadata(final ProtocolMetadata metadata) {
        synchronized (protocol) {
            protocol.setMetadata(metadata);
            updateTimestamp();
        }
    }

    public void startNewScenario(final ScenarioDefinition scenarioDefinition) {
        ScenarioProtocol scenarioProtocol = new ScenarioProtocol(scenarioDefinition);
        synchronized (protocol) {
            protocol.add(scenarioProtocol);
            updateTimestamp();
        }
    }

    public void restartCurrentScenario() {
        synchronized (protocol) {
            Optional<ScenarioProtocol> currentScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (currentScenarioProtocol.isPresent()) {
                currentScenarioProtocol.get().reset();
                updateTimestamp();
            }
        }
    }

    public void stopCurrentScenario() {
        synchronized (protocol) {
            Optional<ScenarioProtocol> currentScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (currentScenarioProtocol.isPresent()) {
                closeScenarioProtocolEvent(null);
                protocol.stopCurrentScenario();
                updateTimestamp();
            }
        }
    }

    public Optional<ScenarioDefinition> getCurrentScenarioDefinition() {
        return protocol.getCurrentScenarioDefinition();
    }

    public void startScenarioProtocolEvent(final ScenarioEventType eventType) {
        synchronized (protocol) {
            Optional<ScenarioProtocol> optScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (optScenarioProtocol.isPresent()) {
                ScenarioProtocol scenarioProtocol = optScenarioProtocol.get();
                // check if this is already started
                Optional<ScenarioProtocolEvent> currentEvent = getCurrentScenarioProtocolEvent();
                if (currentEvent.isPresent()
                        && currentEvent.get().getEventDefinition().getEventType().equals(eventType)) {
                    log.info("event already started");
                    return;
                }

                ScenarioProtocolEvent scenarioProtocolEvent = scenarioProtocol.get(eventType);
                if (!scenarioProtocolEvent.getEventResult().isStarted()) {
                    scenarioProtocolEvent.getEventResult().setStarted(true);
                    threadContext.set(scenarioProtocolEvent);
                    updateTimestamp();
                }
            } else {
                log.warn("No current scenario found");
            }
        }
    }

    public boolean checkDependendEventsCalled(final List<ScenarioEventType> list) {
        synchronized (protocol) {
            Optional<ScenarioProtocol> optScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (optScenarioProtocol.isPresent()) {
                ScenarioProtocol scenarioProtocol = optScenarioProtocol.get();
                updateTimestamp();
                return scenarioProtocol.checkDependendEventsCalled(list);

            }
            return true;
        }
    }

    public Optional<ScenarioProtocolEvent> getCurrentScenarioProtocolEvent() {
        return Optional.ofNullable(threadContext.get());
    }

    private void logProtocolEventNotFound(final String methodName) {
        log.debug("No protocol event found: {}", methodName);
    }

    public void closeScenarioProtocolEvent(final Exception catchedException) {
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            ScenarioProtocolEventResult eventResult = scenarioProtocolEvent.getEventResult();
            if (eventResult.isEmpty()) {
                eventResult.addMethodCouldNotBeCalled();
                eventResult.addErrorMessage("Nachricht nicht korrekt."
                        + (catchedException != null ? " " + catchedException.getMessage() : ""));
            } else if (catchedException != null) {
                eventResult.addErrorMessage("Nachricht nicht korrekt. " + catchedException.getMessage());
            }
            threadContext.remove();
            updateTimestamp();
        } else {
            logProtocolEventNotFound("closeScenarioProtocolEvent");
        }
    }

    public void addMethodCouldNotBeCalledMessage() {
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            scenarioProtocolEvent.getEventResult().addMethodCouldNotBeCalled();
            updateTimestamp();
        } else {
            logProtocolEventNotFound("addMethodCouldNotBeCalledMessage");
        }
    }

    public void addMethodCallReceivedMessage() {
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            if (scenarioProtocolEvent.isPartOfTheScenario() && scenarioProtocolEvent.getEventDefinition().isPassive()) {
                scenarioProtocolEvent.getEventResult().addMethodCallReceived();
            } else {
                scenarioProtocolEvent.getEventResult().setMethodCalled(true);
            }
            updateTimestamp();
        } else {
            logProtocolEventNotFound("addMethodCallReceivedMessage");
        }
    }

    public void addMethodCallSendMessage() {
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            if (scenarioProtocolEvent.isPartOfTheScenario() && scenarioProtocolEvent.getEventDefinition().isActive()) {
                scenarioProtocolEvent.getEventResult().addMethodCallSend();
            } else {
                scenarioProtocolEvent.getEventResult().setMethodCalled(true);
            }
            updateTimestamp();
        } else {
            logProtocolEventNotFound("addMethodCallSendMessage");
        }
    }

    public void addSuccessResultMessage(final String message) {
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            scenarioProtocolEvent.getEventResult().addSuccessMessage(message);
            updateTimestamp();
        } else {
            logProtocolEventNotFound("addSuccessResultMessage");
        }
    }

    public void addErrorResultMessage(final String message) {
        log.error(message);
        ScenarioProtocolEvent scenarioProtocolEvent = threadContext.get();
        if (scenarioProtocolEvent != null) {
            scenarioProtocolEvent.getEventResult().addErrorMessage(message);
            updateTimestamp();
        } else {
            logProtocolEventNotFound("addErrorResultMessage");
        }
    }

    public void addParameter(final CharSequence key, final Object value) {
        synchronized (protocol) {
            Optional<ScenarioProtocol> currentScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (currentScenarioProtocol.isPresent()) {
                currentScenarioProtocol.get().addParameter(key, value);
                updateTimestamp();
            }
        }
    }

    public <T> Optional<T> getParameter(final CharSequence key, final Class<T> clazz) {
        return Optional.ofNullable(getParameter(key, clazz, null));
    }

    public <T> T getParameter(final CharSequence key, final Class<T> clazz, final Supplier<T> defaultValue) {
        synchronized (protocol) {
            Optional<ScenarioProtocol> currentScenarioProtocol = protocol.getCurrentScenarioProtocol();
            if (currentScenarioProtocol.isPresent()) {
                return currentScenarioProtocol.get().getParameter(key, clazz, defaultValue);
            }
            return defaultValue != null ? defaultValue.get() : null;
        }
    }
}
