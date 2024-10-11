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

import lombok.Getter;

public enum ResultType {

    SUCCESS("ScenarioProtocolEventResult.ResultType.success"),
    NOT_CALLED("ScenarioProtocolEventResult.ResultType.not_called"),
    // WARN(""), //maybe we need this later
    ERROR("ScenarioProtocolEventResult.ResultType.error");

    @Getter
    private final String messageKey;

    ResultType(final String messageKey) {
        this.messageKey = messageKey;
    }
}
