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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

public class ScenarioProtocolEventResult {

    private static final String METHOD_NOT_CALLED_MESSAGE = "Methode wurde nicht aufgerufen";
    private static final String METHOD_COULD_NOT_BE_CALLED_MESSAGE = "Methode konnte nicht aufgerufen werden";

    private static final String MISSING_REQUIRED_PREVIOUS_EVENT_TYPE = "Erwartetes vorheriges Ereignis '%s' wurde nicht aufgerufen";

    private static final String METHOD_CALL_RECEIVED_MESSAGE = "Methodenaufruf empfangen";
    private static final String METHOD_CALL_SEND_MESSAGE = "Methodenaufruf gesendet";

    private static final List<ResultMessage> NOT_CALLED_MESSAGE_LIST = List
            .of(new ResultMessage(ResultType.ERROR, METHOD_NOT_CALLED_MESSAGE));

    @Getter
    private final LocalDateTime dateTime;
    private List<ResultMessage> messages = new ArrayList<>();
    @Getter
    @Setter
    private boolean methodCalled = false;
    @Getter
    @Setter
    private boolean started = false;

    public ScenarioProtocolEventResult() {
        dateTime = LocalDateTime.now();
    }

    public ScenarioProtocolEventResult(final ResultType resultType, final String message) {
        dateTime = LocalDateTime.now();
        addMessage(resultType, message);
    }

    /*
     * Getter
     */

    public ResultType getWorstType() {
        ResultType type = null;
        for (ResultMessage resultMessage : messages) {
            if (type == null || type.ordinal() < resultMessage.getType().ordinal()) {
                type = resultMessage.getType();
            }
        }
        return type != null ? type : ResultType.NOT_CALLED;
    }

    public List<ResultMessage> getMessages() {
        if (messages.isEmpty()) {
            return NOT_CALLED_MESSAGE_LIST;
        }
        return Collections.unmodifiableList(messages);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public int size() {
        return messages.size();
    }
    /*
     * Methods to add messages
     */

    public ScenarioProtocolEventResult addMessage(final ResultType resultType, final String message) {
        Objects.requireNonNull(resultType);
        messages.add(new ResultMessage(resultType, message != null ? message : "null"));
        return this;
    }

    public ScenarioProtocolEventResult addMethodCouldNotBeCalled() {
        addMessage(ResultType.ERROR, METHOD_COULD_NOT_BE_CALLED_MESSAGE);
        return this;
    }

    public ScenarioProtocolEventResult addMethodCallReceived() {
        if (!methodCalled) {
            addMessage(ResultType.SUCCESS, METHOD_CALL_RECEIVED_MESSAGE);
            methodCalled = true;
        }
        return this;
    }

    public ScenarioProtocolEventResult addMethodCallSend() {
        if (!methodCalled) {
            addMessage(ResultType.SUCCESS, METHOD_CALL_SEND_MESSAGE);
            methodCalled = true;
        }
        return this;
    }

    public ScenarioProtocolEventResult addMissingRequiredPreviousEventType(final ScenarioEventType eventType) {
        addMessage(ResultType.ERROR, String.format(MISSING_REQUIRED_PREVIOUS_EVENT_TYPE, eventType.name()));
        return this;
    }

    public ScenarioProtocolEventResult addSuccessMessage(final String message) {
        addMessage(ResultType.SUCCESS, message);
        return this;
    }

    public ScenarioProtocolEventResult addErrorMessage(final String message) {
        addMessage(ResultType.ERROR, message);
        return this;
    }
}
