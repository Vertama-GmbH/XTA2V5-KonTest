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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.xoev.xta.test.app.model.XtaConstants.ConnectionPortType;
import lombok.Getter;

public enum ScenarioEventType {

    // alphabetic order
    CHECK_ACCOUNT_ACTIVE(XtaConstants.CHECK_ACCOUNT_ACTIVE_ACTION, ConnectionPortType.TRANSPORT_BASE),
    CLOSE(XtaConstants.CLOSE_ACTION, ConnectionPortType.RECIPIENT_ASYNC),
    CREATE_MESSAGE_ID(XtaConstants.CREATE_MESSAGE_ID_ACTION, ConnectionPortType.SENDER_BASE),

    GET_MESSAGE(XtaConstants.GET_MESSAGE_ACTION, ConnectionPortType.RECIPIENT_ASYNC),
    GET_STATUS_LIST(XtaConstants.GET_STATUS_LIST_ACTION, ConnectionPortType.RECIPIENT_ASYNC),
    GET_TRANSPORT_REPORT(XtaConstants.GET_TRANSPORT_REPORT_ACTION, ConnectionPortType.TRANSPORT_BASE),

    LOOKUP_SERVICE(XtaConstants.LOOKUP_SERVICE_ACTION, ConnectionPortType.TRANSPORT_BASE),

    SEND_MESSAGE(XtaConstants.SEND_MESSAGE_ACTION, ConnectionPortType.SENDER_ASYNC),
    SEND_MESSAGE_SYNC(XtaConstants.SEND_MESSAGE_SYNC_ACTION, ConnectionPortType.SENDER_SYNC),
    DELIVER_MESSAGE(XtaConstants.DELIVER_MESSAGE_ACTION, ConnectionPortType.READER_SYNC);

    @Getter
    private String action;
    @Getter
    private String displayName;
    @Getter
    private ConnectionPortType portType;

    ScenarioEventType(final String action, final ConnectionPortType portType) {
        this.action = action;
        this.displayName = getDisplayName(name());
        this.portType = portType;
    }

    protected static String getDisplayName(final String name) {
        String[] split = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                sb.append(split[i]);
            } else {
                sb.append(StringUtils.capitalize(split[i]));
            }
        }
        return sb.toString();
    }

    public static Optional<ScenarioEventType> findByAction(final String action) {
        for (ScenarioEventType eventType : ScenarioEventType.values()) {
            if (eventType.getAction().equals(action)) {
                return Optional.of(eventType);
            }
        }
        return Optional.empty();
    }
}
