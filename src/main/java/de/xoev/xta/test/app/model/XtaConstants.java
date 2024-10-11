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

import javax.xml.namespace.QName;

import lombok.experimental.UtilityClass;

@UtilityClass
public class XtaConstants {

    public static final String W3_ORG_2005_08_ADDRESSING = "http://www.w3.org/2005/08/addressing";

    public static final String XTA_ACTION_BASE = "https://xoev.de/transport/xta/core/";
    public static final String XTA_ACTION_READER = XTA_ACTION_BASE + "core.reader/";
    public static final String XTA_ACTION_RECIPIENT = XTA_ACTION_BASE + "core.recipient/";
    public static final String XTA_ACTION_SENDER = XTA_ACTION_BASE + "core.sender/";
    public static final String XTA_ACTION_TRANSPORT_BASE = XTA_ACTION_BASE + "core.transport.base/";

    /*
     * Xml QNames in alphabetic order
     */

    public static final QName ACTION = new QName(W3_ORG_2005_08_ADDRESSING, "Action");
    public static final QName TO = new QName(W3_ORG_2005_08_ADDRESSING, "To");

    /*
     * Actions in alphabetic order
     * https://xoev.de/transport/xta/core/core.transport.base/checkAccountActive
     * https://xoev.de/transport/xta/core/core.recipient/close
     * https://xoev.de/transport/xta/core/core.sender/createMessageID
     * https://xoev.de/transport/xta/core/core.reader/deliverMessage
     * https://xoev.de/transport/xta/core/core.recipient/getMessage
     * https://xoev.de/transport/xta/core/core.recipient/getStatusList
     * https://xoev.de/transport/xta/core/core.transport.base/getTransportReport
     * https://xoev.de/transport/xta/core/core.sender/lookupService
     * https://xoev.de/transport/xta/core/core.sender/sendMessage
     * https://xoev.de/transport/xta/core/core.sender/sendMessageSync
     */
    public static final String CHECK_ACCOUNT_ACTIVE_ACTION = XTA_ACTION_TRANSPORT_BASE + "checkAccountActive";
    public static final String CLOSE_ACTION = XTA_ACTION_RECIPIENT + "close";
    public static final String CREATE_MESSAGE_ID_ACTION = XTA_ACTION_SENDER + "createMessageID";
    public static final String DELIVER_MESSAGE_ACTION = XTA_ACTION_READER + "deliverMessage";

    public static final String GET_MESSAGE_ACTION = XTA_ACTION_RECIPIENT + "getMessage";
    public static final String GET_STATUS_LIST_ACTION = XTA_ACTION_RECIPIENT + "getStatusList";
    public static final String GET_TRANSPORT_REPORT_ACTION = XTA_ACTION_TRANSPORT_BASE + "getTransportReport";

    public static final String LOOKUP_SERVICE_ACTION = XTA_ACTION_SENDER + "lookupService";

    public static final String SEND_MESSAGE_ACTION = XTA_ACTION_SENDER + "sendMessage";
    public static final String SEND_MESSAGE_SYNC_ACTION = XTA_ACTION_SENDER + "sendMessageSync";

    public enum ConnectionPortType {
        TRANSPORT_BASE, SENDER_SYNC, SENDER_ASYNC, SENDER_BASE, READER_SYNC, RECIPIENT_ASYNC
    }
}
