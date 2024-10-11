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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

public class XtaProtocol {

    @Getter
    @Setter
    private ProtocolMetadata metadata;

    @Getter
    @Setter
    private ContainerCustomization containerCustomization = new ContainerCustomization();

    private final LinkedList<ScenarioProtocol> scenarioProtocols = new LinkedList<>();

    public Optional<ScenarioProtocol> getCurrentScenarioProtocol() {
        return scenarioProtocols.isEmpty() ? Optional.empty() : Optional.of(scenarioProtocols.getLast());
    }

    public Optional<ScenarioDefinition> getCurrentScenarioDefinition() {
        return scenarioProtocols.isEmpty() ? Optional.empty()
                : Optional.of(scenarioProtocols.getLast().getScenarioDefinition());
    }

    public void add(final ScenarioProtocol scenarioProtocol) {
        Objects.requireNonNull(scenarioProtocol);
        scenarioProtocols.add(scenarioProtocol);
    }

    public List<ScenarioProtocol> getScenarioProtocols() {
        return Collections.unmodifiableList(scenarioProtocols);
    }

    public String getScenarioDisplayNamesAsText() {
        final StringBuilder sb = new StringBuilder();
        for (final ScenarioProtocol scenarioProtocol : scenarioProtocols) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(scenarioProtocol.getScenarioDefinition().getDisplayName());
        }
        return sb.toString();
    }

    public int scenarioCount() {
        return scenarioProtocols.size();
    }

    public int conformityTestPassedScenarioCount() {
        int count = 0;
        for (final ScenarioProtocol sp : scenarioProtocols) {
            if (sp.isConformityTestPassed()) {
                count++;
            }
        }
        return count;
    }

    public boolean areAllConformityTestPassed() {
        boolean passed = !scenarioProtocols.isEmpty();
        for (final ScenarioProtocol sp : scenarioProtocols) {
            passed &= sp.isConformityTestPassed();
        }
        return passed;
    }

    public void stopCurrentScenario() {
        getCurrentScenarioProtocol().ifPresent(ScenarioProtocol::setFinished);
    }
}
