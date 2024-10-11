/*
 * Created 2022-12-14
 */
package de.xoev.xta.test.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import de.xoev.xta.test.app.model.ProtocolMetadata;
import de.xoev.xta.test.app.model.XtaIdentifierConfig;
import de.xoev.xta.test.app.model.XtaQualifierConfig;
import de.xoev.xta.test.app.service.ConfigurationService;
import de.xoev.xta.test.app.util.YamlPropertySourceFactory;
import lombok.Data;

@Data
@ConfigurationProperties
@Configuration
@PropertySource(name = ConfigurationService.CONFIG_REGISTRY_FILENAME, value = "${configFileName:file:config.yaml}", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class)
public class XtaConfig {

    private ProtocolMetadata protocolMetadata;
    private ClientProperties clientProperties;
    private XtaIdentifierConfig partyIdentifierConfig;
    private XtaIdentifierConfig authorIdentifierConfig;
    private XtaIdentifierConfig readerIdentifierConfig;
    private XtaIdentifierConfig senderIdentifierConfig;
    private XtaQualifierConfig qualifierConfig;
}
