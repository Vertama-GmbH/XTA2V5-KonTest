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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioEventDefinition {

    private ScenarioEventType eventType;
    @Builder.Default
    private boolean optional = false;
    @Builder.Default
    private boolean multipleCallable = false;
    @Builder.Default
    private ExecutionType executionType = ExecutionType.PASSIV;
    @Singular
    private List<ScenarioEventType> requiredPreviousEventTypes;

    public boolean isActive() {
        return executionType == ExecutionType.ACTIVE;
    }

    public boolean isPassive() {
        return executionType == ExecutionType.PASSIV;
    }
}
