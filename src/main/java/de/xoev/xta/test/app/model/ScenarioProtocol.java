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
package de.xoev.xta.test.app.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import lombok.Getter;

public class ScenarioProtocol {

    @Getter
    private final ScenarioDefinition scenarioDefinition;
    private List<ScenarioProtocolEvent> scenarioProtocolEvents = new LinkedList<>();
    private Set<ScenarioEventType> previousEvents = new HashSet<>();
    private Map<String, Object> parameterMap = new HashMap<>();
    private boolean scenarioFinished;

    public ScenarioProtocol(final ScenarioDefinition scenarioDefinition) {
        Objects.requireNonNull(scenarioDefinition);
        this.scenarioDefinition = scenarioDefinition;
        init();
    }

    private void init() {
        for (ScenarioEventDefinition eventDefinition : scenarioDefinition.getEventDescriptions(false)) {
            // add only required events
            ScenarioProtocolEvent scenarioProtocolEvent = new ScenarioProtocolEvent(eventDefinition);
            scenarioProtocolEvent.setEventResult(new ScenarioProtocolEventResult());
            scenarioProtocolEvents.add(scenarioProtocolEvent);
        }
        scenarioFinished = false;
    }

    public void reset() {
        scenarioProtocolEvents.clear();
        previousEvents.clear();
        init();
    }

    public List<ScenarioProtocolEvent> getScenarioProtocolEvents() {
        return Collections.unmodifiableList(scenarioProtocolEvents);
    }

    public ScenarioProtocolEvent get(final ScenarioEventType eventType) {
        if (scenarioFinished) {
            return null;
        }
        ScenarioProtocolEvent nextScenarioProtocolEvent = findFirstNotCalledEntry(eventType);
        if (nextScenarioProtocolEvent == null) {
            nextScenarioProtocolEvent = findOptionalScenarioProtocolEvent(eventType);
        }

        // find the position to insert the protocol event
        ListIterator<ScenarioProtocolEvent> insertIterator = scenarioProtocolEvents.listIterator();
        while (insertIterator.hasNext()) {
            ScenarioProtocolEvent scenarioProtocolEvent = insertIterator.next();
            if (!scenarioProtocolEvent.getEventResult().isStarted()) {
                insertIterator.previous();
                break;
            }
        }

        if (nextScenarioProtocolEvent == null) {
            // insert unexpected event
            ScenarioProtocolEvent unexpectedProtocolEvent = createUnexpectedProtocolEvent(eventType);
            nextScenarioProtocolEvent = unexpectedProtocolEvent;
        } else {
            // check if the call order is correct
            checkCallOrder(nextScenarioProtocolEvent);
            previousEvents.add(eventType); // add the eventType to the previousEvents
        }
        insertIterator.add(nextScenarioProtocolEvent);
        return nextScenarioProtocolEvent;
    }

    protected ScenarioProtocolEvent findFirstNotCalledEntry(final ScenarioEventType eventType) {
        // find the first entry for the given eventType that has not been called yet
        ListIterator<ScenarioProtocolEvent> findEventIterator = scenarioProtocolEvents.listIterator();
        while (findEventIterator.hasNext()) {
            ScenarioProtocolEvent scenarioProtocolEvent = findEventIterator.next();
            if (scenarioProtocolEvent.getEventDefinition().getEventType() == eventType
                    && scenarioProtocolEvent.getEventResult().getWorstType() == ResultType.NOT_CALLED) {
                findEventIterator.remove(); // remove the event, we insert it later on the right position
                return scenarioProtocolEvent;
            }
        }
        // try to find a multiple callable
        for (ScenarioProtocolEvent scenarioProtocolEvent : scenarioProtocolEvents) {
            if (scenarioProtocolEvent.getEventDefinition().getEventType() == eventType
                    && scenarioProtocolEvent.getEventDefinition().isMultipleCallable()) {
                ScenarioProtocolEvent firstNotCalledScenarioProtocolEvent = new ScenarioProtocolEvent(
                        scenarioProtocolEvent.getEventDefinition());
                firstNotCalledScenarioProtocolEvent.setEventResult(new ScenarioProtocolEventResult());
                return firstNotCalledScenarioProtocolEvent;
            }
        }
        return null;
    }

    protected ScenarioProtocolEvent findOptionalScenarioProtocolEvent(final ScenarioEventType eventType) {
        // check if the eventType is an optional event
        for (ScenarioEventDefinition eventDefinition : scenarioDefinition.getEventDescriptions(true)) {
            if (eventDefinition.getEventType() == eventType
                    && (eventDefinition.isMultipleCallable() || !previousEvents.contains(eventType))) {
                ScenarioProtocolEvent scenarioProtocolEvent = new ScenarioProtocolEvent(eventDefinition);
                scenarioProtocolEvent.setEventResult(new ScenarioProtocolEventResult());
                return scenarioProtocolEvent;
            }
        }
        return null;
    }

    protected void checkCallOrder(final ScenarioProtocolEvent nextScenarioProtocolEvent) {
        List<ScenarioEventType> requiredPreviousEventTypes = nextScenarioProtocolEvent.getEventDefinition()
                .getRequiredPreviousEventTypes();
        for (ScenarioEventType requiredEventType : requiredPreviousEventTypes) {
            if (!previousEvents.contains(requiredEventType)) {
                nextScenarioProtocolEvent.getEventResult().addMissingRequiredPreviousEventType(requiredEventType);
            }
        }
    }

    public void addEventResult(final ScenarioEventType eventType, final ScenarioProtocolEventResult eventResult) {
        if (scenarioFinished) {
            return;
        }
        // find the first entry that has not yet been called
        int index = 0;
        ScenarioProtocolEvent nextScenarioProtocolEvent = null;
        for (index = 0; index < scenarioProtocolEvents.size(); index++) {
            ScenarioProtocolEvent scenarioProtocolEvent = scenarioProtocolEvents.get(index);
            if (scenarioProtocolEvent.getEventResult().isEmpty()) {
                nextScenarioProtocolEvent = scenarioProtocolEvent;
                break;
            }
        }
        if (nextScenarioProtocolEvent != null) {
            if (nextScenarioProtocolEvent.getEventDefinition().getEventType() == eventType) {
                nextScenarioProtocolEvent.setEventResult(eventResult);
            } else {
                // insert unexpected event
                ScenarioProtocolEvent unexpectedProtocolEvent = createUnexpectedProtocolEvent(eventType, eventResult);
                scenarioProtocolEvents.add(index, unexpectedProtocolEvent);
            }
        } else {
            // no event found
            ScenarioProtocolEvent unexpectedProtocolEvent = createUnexpectedProtocolEvent(eventType, eventResult);
            scenarioProtocolEvents.add(unexpectedProtocolEvent);
        }
    }

    protected ScenarioProtocolEvent createUnexpectedProtocolEvent(final ScenarioEventType eventType,
            final ScenarioProtocolEventResult eventResult) {
        ScenarioProtocolEvent unexpectedProtocolEvent = new ScenarioProtocolEvent(
                ScenarioEventDefinition.builder()
                        .eventType(eventType)
                        .build(),
                false);
        eventResult.addMessage(ResultType.ERROR, "Unexpected event");
        unexpectedProtocolEvent.setEventResult(eventResult);
        return unexpectedProtocolEvent;
    }

    protected ScenarioProtocolEvent createUnexpectedProtocolEvent(final ScenarioEventType eventType) {
        ScenarioProtocolEvent unexpectedProtocolEvent = new ScenarioProtocolEvent(
                ScenarioEventDefinition.builder()
                        .eventType(eventType)
                        .build(),
                false);
        ScenarioProtocolEventResult eventResult = new ScenarioProtocolEventResult(ResultType.ERROR, "Unexpected event");
        unexpectedProtocolEvent.setEventResult(eventResult);
        return unexpectedProtocolEvent;
    }

    public boolean isConformityTestPassed() {
        for (ScenarioProtocolEvent protocolEvent : scenarioProtocolEvents) {
            ScenarioProtocolEventResult eventResult = protocolEvent.getEventResult();
            if (eventResult == null || eventResult.getWorstType() != ResultType.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    public void addParameter(final CharSequence key, final Object value) {
        parameterMap.put(key.toString(), value);
    }

    public <T> Optional<T> getParameter(final CharSequence key, final Class<T> clazz) {
        return Optional.ofNullable(getParameter(key, clazz, null));
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(final CharSequence key, final Class<T> clazz, final Supplier<T> defaultValue) {
        Object obj = parameterMap.get(key.toString());
        if (obj != null && clazz.isInstance(obj)) {
            return (T) obj;
        }
        return defaultValue != null ? defaultValue.get() : null;
    }

    /**
     * @param list
     * @return
     */
    public boolean checkDependendEventsCalled(final List<ScenarioEventType> list) {
        Set<ScenarioEventType> eventSet = new HashSet<>(list);
        for (ScenarioProtocolEvent protocolEvent : scenarioProtocolEvents) {
            ScenarioProtocolEventResult eventResult = protocolEvent.getEventResult();
            if (eventSet.contains(protocolEvent.getEventDefinition().getEventType()) && eventResult != null
                    && ResultType.NOT_CALLED != eventResult.getWorstType()) {
                eventSet.remove(protocolEvent.getEventDefinition().getEventType());
            }
        }
        return eventSet.isEmpty();
    }

    /**
     * 
     */
    public void setFinished() {
        scenarioFinished = true;
    }
}
