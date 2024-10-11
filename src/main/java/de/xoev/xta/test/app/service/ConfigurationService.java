/*
 * Created 2022-12-14
 */
package de.xoev.xta.test.app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.xoev.xta.test.app.config.XtaConfig;
import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.model.ProtocolMetadata;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ConfigurationService {

    public static final String CONFIG_REGISTRY_FILENAME = "config.yaml";

    @Value("${configFileName:config.yaml}")
    @Getter
    String configFileName;

    private XtaConfig tempConfig;

    private final ConfigurableEnvironment environment;

    private final ApplicationContext applicationContext;

    public ConfigurationService(final ConfigurableEnvironment environment,
            final ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    void checkConfigFile() throws XtaTesterException {
        log.info("config: {}", getConfigFileName());

        if (getXtaConfig().getClientProperties() == null && !Files.exists(getConfigFileNameCleaned())) {
            try {
                resetXtaConfig();
            } catch (final XtaTesterException e) {
                log.error("Could not write config file '{}'. {}", getConfigFileNameCleaned(), e.getMessage(), e);
            }
        }
        // if still null, exit app
        if (getXtaConfig().getClientProperties() == null && !Files.exists(getConfigFileNameCleaned())) {
            throw new XtaTesterException("Failed to load configuration");
        }

    }

    private void saveConfig() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            mapper.writeValue(getConfigFileNameCleaned().toFile(), tempConfig);
            resetXtaConfigBean();
        } catch (final IOException e) {
            log.error("Could not write config file '{}'. {}", getConfigFileNameCleaned(), e.getMessage(), e);
        }
    }

    public void setXtaConfig(final XtaConfig xtaConfig) {
        this.tempConfig = xtaConfig;
        saveConfig();
    }

    public void resetXtaConfig() throws XtaTesterException {
        try {
            Files.deleteIfExists(getConfigFileNameCleaned().toAbsolutePath());
            Files.copy(new ClassPathResource("configDefault.yaml").getInputStream(), getConfigFileNameCleaned());
            resetXtaConfigBean();
        } catch (final IOException e) {
            throw new XtaTesterException(e);
        }
    }

    void resetXtaConfigBean() {

        try {
            if (Files.exists(getConfigFileNameCleaned())) {
                environment.getPropertySources().remove(CONFIG_REGISTRY_FILENAME);
                final List<PropertySource<?>> result = new YamlPropertySourceLoader().load(CONFIG_REGISTRY_FILENAME,
                        new FileSystemResource(getConfigFileNameCleaned()));
                environment.getPropertySources().addLast(result.get(0));
            }
        } catch (final IOException e) {
            log.error("could not reload the config properly: {}", e.getMessage());
        }
    }

    XtaConfig getXtaConfigBean() {
        final Object autowired = applicationContext.getAutowireCapableBeanFactory().createBean(XtaConfig.class,
                AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        return (XtaConfig) autowired;
    }

    public void setProtocolMetadata(final ProtocolMetadata protocolMetadata) {
        tempConfig.setProtocolMetadata(protocolMetadata);
        saveConfig();
    }

    public XtaConfig getXtaConfig() {
        return getXtaConfigBean();
    }

    private Path getConfigFileNameCleaned() {
        if (getConfigFileName().startsWith("file:")) {
            final String tmpName = getConfigFileName().substring(5);
            return Paths.get(tmpName).toAbsolutePath();
        } else {
            return Paths.get(getConfigFileName()).toAbsolutePath();
        }
    }
}
