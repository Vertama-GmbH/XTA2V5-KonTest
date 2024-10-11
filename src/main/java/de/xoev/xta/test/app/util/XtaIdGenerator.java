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
package de.xoev.xta.test.app.util;

import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class XtaIdGenerator {

    public static final String MESSAGE_ID_NAMESPACE = "de:xta:messageid";
    public static final String REQUEST_ID_NAMESPACE = "de:xta:requestid";

    private static final String APPLICATION_NAME = "xta-tester";

    public static XtaId generateMessageId() {
        return generateId(MESSAGE_ID_NAMESPACE);
    }

    public static XtaId generateRequestId() {
        return generateId(REQUEST_ID_NAMESPACE);
    }

    public static XtaId generateId(final String namespace) {
        return new XtaId("urn", namespace, APPLICATION_NAME, UUID.randomUUID().toString());
    }

}
