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
package de.xoev.xta.test.app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.xoev.xta.test.app.model.ContainerCustomization;
import de.xoev.xta.test.app.service.ConfigurationService;
import de.xoev.xta.test.app.service.ProtocolService;
import de.xoev.xta.test.app.service.ScenarioService;

@Controller
@RequestMapping("view")
public class ViewController {

    private final ConfigurationService configurationService;

    private final ProtocolService protocolService;

    private final ScenarioService scenarioService;

    private final BuildProperties buildProperties;

    public ViewController(final ConfigurationService configurationService, final ProtocolService protocolService,
            final ScenarioService scenarioService, final BuildProperties buildProperties) {
        this.configurationService = configurationService;
        this.protocolService = protocolService;
        this.scenarioService = scenarioService;
        this.buildProperties = buildProperties;
    }

    @GetMapping(value = "/home")
    public String index(final Model model) {
        setDefaultProperties(model);
        model.addAttribute("scenarioList", scenarioService.getScenarioNames());
        return "index";
    }

    @GetMapping(value = "/control")
    public String control(final Model model) {
        setDefaultProperties(model);
        model.addAttribute("scenarioList", scenarioService.getScenarioNames());
        model.addAttribute("currentScenarioDefinition",
                protocolService.getCurrentScenarioDefinition().orElse(null));
        return "control";
    }

    @GetMapping(value = "/settings")
    public String settings(final Model model) {
        setDefaultProperties(model);
        model.addAttribute("metadata", configurationService.getXtaConfig().getProtocolMetadata());
        model.addAttribute("connectionSettings", configurationService.getXtaConfig().getClientProperties());
        model.addAttribute("qualifierConfig", configurationService.getXtaConfig().getQualifierConfig());
        model.addAttribute("partyIdentifier", configurationService.getXtaConfig().getPartyIdentifierConfig());
        model.addAttribute("authorIdentifier", configurationService.getXtaConfig().getAuthorIdentifierConfig());
        model.addAttribute("readerIdentifier", configurationService.getXtaConfig().getReaderIdentifierConfig());
        model.addAttribute("senderIdentifier", configurationService.getXtaConfig().getSenderIdentifierConfig());
        return "settings";
    }

    @GetMapping(value = "/customization")
    public String customization(final Model model) {
        setDefaultProperties(model);
        // load default encrypted content from ressources
        final ContainerCustomization cust = protocolService.getContainerCustomization();
        final StringBuilder currentFile = new StringBuilder();
        if (cust.getUseCustomContainer() == null) {
            cust.setUseCustomContainer(false);
        }
        if (cust.getUseEncyptedContainer() == null) {
            cust.setUseEncyptedContainer(false);
        }

        if (cust.getUseCustomContainer() && !cust.getUseEncyptedContainer() && !cust.getFilename().isBlank()) {
            currentFile.append("Aktuelle gespeicherte Datei: ");
            currentFile.append(cust.getFilename());
            currentFile.append(" (");
            currentFile.append(cust.getSize());
            currentFile.append(" Bytes)");
        } else {
            currentFile.append("Aktuell keine Datei ausgewählt");
        }

        model.addAttribute("customization", protocolService.getContainerCustomization());
        model.addAttribute("defaultEncryptedContainer", getDefaultEncryptedContainer());

        model.addAttribute("currentFile", currentFile.toString());
        return "contentConfiguration";
    }

    @GetMapping(value = "/conformity")
    public String conformity(final Model model) {
        setDefaultProperties(model);
        model.addAttribute("scenarioList", scenarioService.getScenarioNames());
        return "conformity";
    }

    @GetMapping(value = "/report")
    public String report(final Model model) {
        setDefaultProperties(model);
        model.addAttribute("protocol", protocolService.getProtocol());
        model.addAttribute("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        model.addAttribute("protocolTimestamp", protocolService.getTimestamp());
        model.addAttribute("wsdlversion", buildProperties.get("wsdl.version"));
        return "report";
    }

    @GetMapping(value = "/client")
    public String client(final Model model) {
        return "client";
    }

    private String getDefaultEncryptedContainer() {
        final ClassPathResource c = new ClassPathResource("defaultEncodedFile.txt");
        try (final InputStream is = c.getInputStream()) {
            final StringWriter stringWriter = new StringWriter();
            IOUtils.copy(is, stringWriter, StandardCharsets.UTF_8);
            return stringWriter.toString();
        } catch (final IOException e) {
            return "could not get the default contents, sorry";
        }
    }

    private Model setDefaultProperties(final Model model) {
        model.addAttribute("version", buildProperties.getVersion());
        final LocalDateTime dateTime = LocalDateTime.ofInstant(buildProperties.getTime(), ZoneId.systemDefault());
        model.addAttribute("buildTimestamp", dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        return model;
    }
}
