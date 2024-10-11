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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.model.ExecutionType;
import de.xoev.xta.test.app.model.ScenarioDefinition;
import de.xoev.xta.test.app.model.ScenarioEventDefinition;
import de.xoev.xta.test.app.model.ScenarioEventType;
import de.xoev.xta.test.app.model.ScenarioRole;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ScenarioService {

    private static final Map<String, ScenarioDefinition> SCENARIO_DEFINITIONS = new HashMap<>();

    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private ScenarioExecutor scenarioExecutor;

    @PostConstruct
    protected void initScenarioDefinitions() {
        ScenarioDefinition autorAsynchronScenario = autorAsynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(autorAsynchronScenario.getName(), autorAsynchronScenario);

        ScenarioDefinition autorSynchronScenario = autorSynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(autorSynchronScenario.getName(), autorSynchronScenario);

        ScenarioDefinition senderAsynchronScenario = senderAsynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(senderAsynchronScenario.getName(), senderAsynchronScenario);

        ScenarioDefinition senderSynchronScenario = senderSynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(senderSynchronScenario.getName(), senderSynchronScenario);

        ScenarioDefinition leserAsynchronScenario = leserAsynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(leserAsynchronScenario.getName(), leserAsynchronScenario);

        ScenarioDefinition leserSynchronScenario = leserSynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(leserSynchronScenario.getName(), leserSynchronScenario);

        ScenarioDefinition empfaengerAsynchronScenario = empfaengerAsynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(empfaengerAsynchronScenario.getName(), empfaengerAsynchronScenario);

        ScenarioDefinition empfaengerSynchronScenario = empfaengerSynchronScenarioBuilder();
        SCENARIO_DEFINITIONS.put(empfaengerSynchronScenario.getName(), empfaengerSynchronScenario);
    }

    private ScenarioDefinition autorSynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("autorSynchron")
                .displayName("Autor (synchron)")
                .scenarioRole(ScenarioRole.AUTOR_SYNCHRON)
                .description("Es wird die Rolle Autor (synchron) getestet. "
                        + "Die Testumgebung nimmt dabei die passive Rolle Sender (synchron) ein.")
                .image("autor_synchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.LOOKUP_SERVICE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.SEND_MESSAGE_SYNC)
                        .requiredPreviousEventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.SEND_MESSAGE_SYNC)
                        .build())
                .build();
    }

    private ScenarioDefinition autorAsynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("autorAsynchron")
                .displayName("Autor (asynchron)")
                .scenarioRole(ScenarioRole.AUTOR_ASYNCHRON)
                .description("Es wird die Rolle Autor (asynchron) getestet. "
                        + "Die Testumgebung nimmt dabei die passive Rolle Sender (asynchron) ein.")
                .image("autor_asynchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.LOOKUP_SERVICE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.SEND_MESSAGE)
                        .requiredPreviousEventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.SEND_MESSAGE)
                        .build())
                .build();
    }

    private ScenarioDefinition senderSynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("senderSynchron")
                .displayName("Sender (synchron)")
                .scenarioRole(ScenarioRole.SENDER_SYNCHRON)
                .description("Es wird die Rolle Sender (synchron) getestet. "
                        + "Die Testumgebung nimmt dabei die aktive Rolle Autor (synchron) ein.")
                .image("sender_synchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.LOOKUP_SERVICE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.SEND_MESSAGE_SYNC)
                        .requiredPreviousEventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.SEND_MESSAGE_SYNC)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .build();
    }

    private ScenarioDefinition senderAsynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("senderAsynchron")
                .displayName("Sender (asynchron)")
                .scenarioRole(ScenarioRole.SENDER_ASYNCHRON)
                .description("Es wird die Rolle Sender (asynchron) getestet. "
                        + "Die Testumgebung nimmt dabei die aktive Rolle Autor (asynchron) ein.")
                .image("sender_asynchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.LOOKUP_SERVICE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.SEND_MESSAGE)
                        .requiredPreviousEventType(ScenarioEventType.CREATE_MESSAGE_ID)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.SEND_MESSAGE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .build();
    }

    private ScenarioDefinition empfaengerSynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("empfängerSynchron")
                .displayName("Empfänger (synchron)")
                .scenarioRole(ScenarioRole.EMPFAENGER_SYNCHRON)
                .description("Es wird die Rolle Empfänger (synchron) getestet. "
                        + "Die Testumgebung nimmt dabei die passive/aktive Rolle Leser (synchron) ein.")
                .image("empfaenger_synchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.DELIVER_MESSAGE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .requiredPreviousEventType(ScenarioEventType.DELIVER_MESSAGE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.DELIVER_MESSAGE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .build();
    }

    private ScenarioDefinition empfaengerAsynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("empfängerAsynchron")
                .displayName("Empfänger (asynchron)")
                .scenarioRole(ScenarioRole.EMPFAENGER_ASYNCHRON)
                .description("Es wird die Rolle Empfänger (asynchron) getestet. "
                        + "Die Testumgebung nimmt dabei die aktive Rolle Leser (asynchron) ein.")
                .image("empfaenger_asynchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_STATUS_LIST)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_MESSAGE)
                        .requiredPreviousEventType(ScenarioEventType.GET_STATUS_LIST)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CLOSE)
                        .requiredPreviousEventType(ScenarioEventType.GET_MESSAGE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.GET_STATUS_LIST)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .build();
    }

    private ScenarioDefinition leserSynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("leserSynchron")
                .displayName("Leser (synchron)")
                .scenarioRole(ScenarioRole.LESER_SYNCHRON)
                .description("Es wird die Rolle Leser (synchron) getestet. "
                        + "Die Testumgebung nimmt dabei die aktive/passive Rolle Empfänger (synchron) ein.")
                .image("leser_synchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.DELIVER_MESSAGE)
                        .executionType(ExecutionType.ACTIVE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .requiredPreviousEventType(ScenarioEventType.DELIVER_MESSAGE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.DELIVER_MESSAGE)
                        .build())
                .build();
    }

    private ScenarioDefinition leserAsynchronScenarioBuilder() {
        return ScenarioDefinition.builder()
                .name("leserAsynchron")
                .displayName("Leser (asynchron)")
                .scenarioRole(ScenarioRole.LESER_ASYNCHRON)
                .description("Es wird die Rolle Leser (asynchron) getestet. "
                        + "Die Testumgebung nimmt dabei die passive Rolle Empfänger (asynchron) ein.")
                .image("leser_asynchron.svg")
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                        .multipleCallable(true)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_STATUS_LIST)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_MESSAGE)
                        .requiredPreviousEventType(ScenarioEventType.GET_STATUS_LIST)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.CLOSE)
                        .requiredPreviousEventType(ScenarioEventType.GET_MESSAGE)
                        .build())
                .eventDescription(ScenarioEventDefinition.builder()
                        .eventType(ScenarioEventType.GET_TRANSPORT_REPORT)
                        .requiredPreviousEventType(ScenarioEventType.GET_STATUS_LIST)
                        .build())
                .build();
    }

    public Collection<ScenarioDefinition> getScenarioNames() {
        List<ScenarioDefinition> list = new ArrayList<>(SCENARIO_DEFINITIONS.values());
        Collections.sort(list, ScenarioDefinitionComparator.getInstance());
        return list;
    }

    public Optional<ScenarioDefinition> getScenarioDefinitionByName(final String scenarioName) {
        return Optional.ofNullable(SCENARIO_DEFINITIONS.get(scenarioName));
    }

    public void startNewScenario(final String scenarioName) {
        scenarioExecutor.stopScenarioExecution();
        Optional<ScenarioDefinition> optScenarioDefinition = getScenarioDefinitionByName(scenarioName);
        if (optScenarioDefinition.isPresent()) {
            ScenarioDefinition scenarioDefinition = optScenarioDefinition.get();
            log.info("start new scenario '{}'", scenarioDefinition.getDisplayName());
            protocolService.startNewScenario(scenarioDefinition);
            if (scenarioDefinition.containsActiveEvents()) {
                try {
                    scenarioExecutor.execute(scenarioDefinition);
                } catch (XtaTesterException e) {
                    log.warn("Scenario could not be started '{}'", e.getMessage());
                }
            }
        } else {
            log.warn("No scenario found with name '{}'", scenarioName);
        }
    }

    public void restartCurrentScenario() {
        Optional<ScenarioDefinition> optScenarioDefinition = protocolService.getCurrentScenarioDefinition();
        if (optScenarioDefinition.isPresent()) {
            ScenarioDefinition scenarioDefinition = optScenarioDefinition.get();
            if (scenarioDefinition.containsActiveEvents()) {
                // try to stop if running
                scenarioExecutor.stopScenarioExecution();
            }
            protocolService.restartCurrentScenario();
            if (scenarioDefinition.containsActiveEvents()) {
                try {
                    scenarioExecutor.execute(scenarioDefinition);
                } catch (XtaTesterException e) {
                    log.warn("Scenario could not be started '{}'", e.getMessage());
                }
            }
        }
    }

}
