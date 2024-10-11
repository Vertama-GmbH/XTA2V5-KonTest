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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
@Builder
public class ScenarioDefinition {

    private final String name;
    private final String displayName;
    private final String description;
    private final String image;
    private final ScenarioRole scenarioRole;
    @Singular
    private List<ScenarioEventDefinition> eventDescriptions;

    public Optional<ScenarioEventDefinition> getEventDefinition(final ScenarioEventType eventType) {
        if (eventDescriptions == null) {
            return Optional.empty();
        }
        ScenarioEventDefinition scenarioEventDefinition = null;
        for (ScenarioEventDefinition sed : eventDescriptions) {
            if (sed.getEventType() == eventType) {
                scenarioEventDefinition = sed;
                break;
            }
        }
        return Optional.ofNullable(scenarioEventDefinition);
    }

    public List<ScenarioEventDefinition> getEventDescriptions(final boolean optional) {
        if (eventDescriptions == null) {
            return Collections.emptyList();
        }
        return eventDescriptions.stream().filter(sed -> sed.isOptional() == optional).collect(Collectors.toList());
    }

    public boolean containsActiveEvents() {
        if (eventDescriptions != null) {
            return eventDescriptions.stream()
                    .filter((final ScenarioEventDefinition e) -> e.getExecutionType() == ExecutionType.ACTIVE)
                    .count() > 0;
        }
        return false;
    }

    public List<ScenarioEventDefinition> getActiveEventDecriptions() {
        if (eventDescriptions != null) {
            return eventDescriptions.stream()
                    .filter((final ScenarioEventDefinition e) -> e.getExecutionType() == ExecutionType.ACTIVE)
                    .toList();
        }
        return Collections.emptyList();
    }

    @SneakyThrows
    public String getJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("name", name);
        node.put("displayName", displayName);
        node.put("description", description);
        node.put("image", image);
        return mapper.writeValueAsString(node);
    }
}
