/*
 * Created 2023-03-24
 */
package de.xoev.xta.test.app.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(final String name, final EncodedResource encodedResource)
            throws IOException {
        if (encodedResource == null) {
            throw new IOException("Resource is null");
        }
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        if (!encodedResource.getResource().exists()) {
            return new PropertiesPropertySource(name, null);
        }
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(name, properties);
    }
}
