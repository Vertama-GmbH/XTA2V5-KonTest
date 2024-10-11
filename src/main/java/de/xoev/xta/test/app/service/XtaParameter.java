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

import java.util.StringJoiner;

import groovy.transform.EqualsAndHashCode;

@EqualsAndHashCode
public class XtaParameter implements CharSequence {

    private static final String SEPARATOR = ".";

    public static final XtaParameter AUTHOR = new XtaParameter("Author");
    public static final XtaParameter BUSINESS_SCENARIO = new XtaParameter("BusinessScenario");
    public static final XtaParameter CATEGORY = new XtaParameter("category");
    public static final XtaParameter CONFIRM_XTA_MESSAGE_ID = new XtaParameter("ConfirmXTAMessageID");
    public static final XtaParameter DELIVERY = new XtaParameter("Delivery");
    public static final XtaParameter DELIVERY_ATTRIBUTES = new XtaParameter("DeliveryAttributes");
    // public static final XtaParameter DESTINATIONS = new XtaParameter("Destinations");
    public static final XtaParameter IDENTIFIER = new XtaParameter("Identifier");
    public static final XtaParameter INITIAL_FETCH = new XtaParameter("InitialFetch");
    public static final XtaParameter INITIAL_SEND = new XtaParameter("InitialSend");
    public static final XtaParameter LOOKUP_SERVICE = new XtaParameter("LookupService");
    public static final XtaParameter LOOKUP_SERVICE_REQUEST = new XtaParameter("LookupServiceRequest");
    public static final XtaParameter LOOKUP_SERVICE_REQUEST_LIST = new XtaParameter("LookupServiceRequestList");
    public static final XtaParameter LOOKUP_SERVICE_RESPONSE = new XtaParameter("LookupServiceResponse");
    public static final XtaParameter LOOKUP_SERVICE_RESULT = new XtaParameter("LookupServiceResult");
    public static final XtaParameter LOOKUP_SERVICE_RESULT_LIST = new XtaParameter("LookupServiceResultList");
    public static final XtaParameter MESSAGE = new XtaParameter("Message");
    public static final XtaParameter MESSAGE_ID = new XtaParameter("MessageID");
    public static final XtaParameter MESSAGE_META_DATA = new XtaParameter("MessageMetaData");
    public static final XtaParameter MESSAGE_META_DATA_RESPONSE = new XtaParameter("MessageMetaDataResponse");
    public static final XtaParameter MESSAGE_STATUS = new XtaParameter("MessageStatus");
    public static final XtaParameter MESSAGE_TYPE = new XtaParameter("MessageType");
    public static final XtaParameter MESSAGE_BOX_RESPONSE = new XtaParameter("MessageBoxResponse");
    public static final XtaParameter MESSAGE_SELECTOR = new XtaParameter("MessageSelector");
    public static final XtaParameter MESSAGE_SIZE = new XtaParameter("MessageSize");
    public static final XtaParameter PARTY = new XtaParameter("Party");
    public static final XtaParameter QUALIFIER = new XtaParameter("Qualifier");
    public static final XtaParameter READER = new XtaParameter("Reader");
    public static final XtaParameter REMAINING = new XtaParameter("Remaining");
    public static final XtaParameter SENDER = new XtaParameter("Sender");
    public static final XtaParameter SERVICE = new XtaParameter("Service");
    public static final XtaParameter TRANSPORT_REPORT = new XtaParameter("TransportReport");
    public static final XtaParameter ORIGIN = new XtaParameter("Origin");
    public static final XtaParameter ORIGINATORS = new XtaParameter("Originators");
    public static final XtaParameter TYPE = new XtaParameter("type");
    public static final XtaParameter XTA_MESSAGE_ID = new XtaParameter("XTAMessageID");

    private String parameter;

    public XtaParameter(final String parameter) {
        this.parameter = parameter;
    }

    public XtaParameter join(final CharSequence parameter) {
        return new XtaParameter(this.parameter + SEPARATOR + parameter);
    }

    public XtaParameter join(final CharSequence parameter, final CharSequence parameter2) {
        return new XtaParameter(this.parameter + SEPARATOR + parameter + SEPARATOR + parameter2);
    }

    public XtaParameter join(final CharSequence parameter, final CharSequence... charSequences) {
        StringJoiner stringJoiner = new StringJoiner(SEPARATOR);
        stringJoiner.add(this.parameter);
        stringJoiner.add(parameter);
        for (int i = 0; i < charSequences.length; i++) {
            stringJoiner.add(charSequences[i]);
        }
        return new XtaParameter(stringJoiner.toString());
    }

    @Override
    public int length() {
        return parameter.length();
    }

    @Override
    public char charAt(final int index) {
        return parameter.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return parameter.subSequence(start, end);
    }

    @Override
    public String toString() {
        return parameter;
    }
}
