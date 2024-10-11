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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import de.xoev.xta.test.app.service.XtaConfigDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.xoev.xta.test.app.config.XtaConfig;
import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.model.CustomResponseEntity;
import de.xoev.xta.test.app.model.XtaConfigDto;
import de.xoev.xta.test.app.service.ConfigurationService;
import de.xoev.xta.test.app.service.CustomizationService;
import de.xoev.xta.test.app.service.ProtocolService;
import de.xoev.xta.test.app.service.ScenarioService;
import de.xoev.xta.test.app.service.XtaConfigDtoConverterImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("rest/api/mgmt")
@Tag(name = "Management Controller", description = "manage Environment")
public class ManagementController {

    private final ScenarioService scenarioService;

    private final ProtocolService protocolService;

    private final ConfigurationService configService;

    private final CustomizationService customizationService;

    public ManagementController(final ScenarioService scenarioService, final ProtocolService protocolService,
                                final ConfigurationService configService, final CustomizationService customizationService) {
        this.scenarioService = scenarioService;
        this.protocolService = protocolService;
        this.configService = configService;
        this.customizationService = customizationService;
    }

    @Operation(summary = "startScenario")
    @GetMapping(value = "/startScenario")
    @ResponseStatus(HttpStatus.OK)
    public void startScenario(@RequestParam(value = "scenario") final String scenarioName) {
        log.info("startScenario: {}", scenarioName);
        scenarioService.startNewScenario(scenarioName);
    }

    @Operation(summary = "restartCurrentScenario")
    @GetMapping(value = "/restartCurrentScenario")
    @ResponseStatus(HttpStatus.OK)
    public void restartCurrentScenario() {
        log.info("restartCurrentScenario");
        scenarioService.restartCurrentScenario();
    }

    @Operation(summary = "clearProtocol")
    @GetMapping(value = "/clearProtocol")
    @ResponseStatus(HttpStatus.OK)
    public void clearProtocol() {
        log.info("clearProtocol");
        protocolService.clearProtocol();
    }

    @Operation(summary = "saveXtaConfig")
    @PutMapping(value = "/xtaConfig/update", consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<XtaConfigDto> saveXtaConfig(@RequestBody final XtaConfig xtaConfig) {
        configService.setXtaConfig(xtaConfig);
        return ResponseEntity.ok(new XtaConfigDtoConverterImpl().fromModel(configService.getXtaConfig()));
    }

    @Operation(summary = "resetXtaConfig")
    @PostMapping(value = "/xtaConfig/reset")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<XtaConfigDto> resetXtaConfig() {
        try {
            configService.resetXtaConfig();
            return ResponseEntity.ok(new XtaConfigDtoConverterImpl().fromModel(configService.getXtaConfig()));
        } catch (final XtaTesterException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "contentConfiguration")
    @PostMapping(value = "/contentConfiguration", consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CustomResponseEntity> contentConfiguration(
            @RequestPart(name = "file", required = false) final MultipartFile file,
            @RequestPart(name = "data") final String data) {

        try {
            CustomResponseEntity result = null;
            if (file != null) {
                final Path tempfile = Files.createTempFile("xta_", "");
                try (final InputStream is = file.getInputStream()) {
                    Files.copy(is, tempfile, StandardCopyOption.REPLACE_EXISTING);
                }
                result = customizationService.contentConfiguration(tempfile,
                        file.getOriginalFilename(), data);
            } else {
                result = customizationService.contentConfiguration(null,
                        null, data);
            }
            return result.isError() ? ResponseEntity.unprocessableEntity().body(result)
                    : ResponseEntity.ok().body(result);
        } catch (final IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new CustomResponseEntity().setError(true).setMessage(e.getMessage()));
        }
    }

    @Operation(summary = "contentConfigurationPreview")
    @PostMapping(value = "/contentConfigurationPreview", consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CustomResponseEntity> contentConfigurationPreview(
            @RequestPart(name = "file", required = false) final MultipartFile file,
            @RequestPart(name = "data") final String data) {
        Path tempfile = null;
        CustomResponseEntity body = null;
        try {
            if (file != null) {
                tempfile = Files.createTempFile("xta_", "");

                try (final InputStream is = file.getInputStream()) {
                    Files.copy(is, tempfile, StandardCopyOption.REPLACE_EXISTING);
                }
                body = customizationService.getXmlPreview(tempfile, file.getOriginalFilename(), data);
                Files.deleteIfExists(tempfile);
            } else {
                body = customizationService.getXmlPreview(null, null, data);
            }
            return ResponseEntity.ok(body);
        } catch (final IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new CustomResponseEntity().setError(true).setMessage(e.getMessage()));
        }

    }

    @Operation(summary = "getProtocolTimestamp")
    @GetMapping(value = "/protocol/timestamp", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getProtocolTimestamp() {
        final String ts = protocolService.getTimestamp();
        return ResponseEntity.ok(ts);
    }

}
