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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
import org.apache.cxf.xkms.model.xmlenc.EncryptedDataType;
import org.apache.cxf.xkms.model.xmlenc.EncryptedKeyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.xoev.xta.test.app.exception.ConfigurationException;
import de.xoev.xta.test.app.model.ContainerCustomization;
import de.xoev.xta.test.app.model.CustomResponseEntity;
import genv5.de.xoev.transport.xta.core.x311.ContentType;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.URLDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomizationService {

    @Autowired
    private ProtocolService protocolService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JAXBContext jaxbContext;
    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;
    private SAXParserFactory spf;

    @PostConstruct
    public void postConstruct() {
        try {
            OBJECT_MAPPER.readValue("{\"useCustomContainer\":false}", ContainerCustomization.class);

            jaxbContext = JAXBContext.newInstance(ContentContainerType.class, EncryptedDataType.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            spf = SAXParserFactory.newInstance();
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setNamespaceAware(true);
            spf.setValidating(true);
        } catch (final JsonProcessingException | JAXBException | SAXNotRecognizedException | SAXNotSupportedException
                | ParserConfigurationException e) {
            // should stop working if an exception is thrown
            log.debug(e);
            throw new ConfigurationException("unable to create container preview: " + e.getMessage(), e);
        }

    }

    public CustomResponseEntity contentConfiguration(final Path file,
            final String originalFileName, final String data) {
        final CustomResponseEntity result = processCustomConfiguration(file, originalFileName, data);
        if (!result.isError()) {
            protocolService.setContainerCustomization(result.getContainerCustomization());
            return result.setMessage("Speichern erfolgreich");
        }
        return result.setContainerCustomization(null);
    }

    public CustomResponseEntity processCustomConfiguration(final Path file, final String originalFileName,
            final String data) {
        if (data == null) {
            return new CustomResponseEntity().setError(true).setMessage("Fehlerhafte Daten gesendet!");
        }
        try {
            final ContainerCustomization c = OBJECT_MAPPER.readValue(data, ContainerCustomization.class);
            // default container
            if (c != null && c.getUseCustomContainer() != null && !c.getUseCustomContainer().booleanValue()) {
                return new CustomResponseEntity().setError(false).setContainerCustomization(c);
            }
            if (c == null || c.getUseCustomContainer() == null || c.getUseEncyptedContainer() == null) {
                return new CustomResponseEntity().setError(true).setMessage("Fehlerhafte Daten gesendet!");
            }

            return processData(file, originalFileName, c);

        } catch (final Exception e) {
            return new CustomResponseEntity().setError(true).setMessage(e.getMessage());
        }
    }

    private CustomResponseEntity processData(final Path file,
            final String originalFileName, final ContainerCustomization c)
            throws IOException {
        try {
            if (c.getUseCustomContainer().booleanValue() && c.getUseEncyptedContainer().booleanValue()) {
                encryptedXmlToObject(c);
            } else if (c.getUseCustomContainer().booleanValue() && !c.getUseEncyptedContainer().booleanValue()) {
                // process unencrypted custom container
                final List<String> errors = new ArrayList<>();
                if (file == null || originalFileName == null || originalFileName.isBlank()) {
                    errors.add("Es muss eine Datei angegeben werden!");
                }
                if (c.getContentType() == null || c.getContentType().isBlank()) {
                    errors.add("Der Content-Type muss angegeben werden!");
                }
                if (!errors.isEmpty()) {
                    final String errorString = String.join("<br />", errors);
                    return new CustomResponseEntity().setError(true).setMessage(errorString);
                }

                final String filename = Paths.get(originalFileName).getFileName().toString();
                c.setFilename(filename);

                c.setTempFile(file);
                c.setSize(Files.size(file));
            }
        } catch (final ConfigurationException e) {
            // substring: remove "error:"
            final String errormessage = e.getMessage().replace("\"", "'").substring(7);
            return new CustomResponseEntity().setError(true).setMessage("Fehler beim Parsen der XML: " + errormessage);
        }
        return new CustomResponseEntity().setError(false).setContainerCustomization(c);
    }

    public ContentContainerType createGenericContentContainer() {
        return createGenericContentContainer(null);
    }

    public ContentContainerType createGenericContentContainer(
            final ContainerCustomization providedCustomization) {
        final ContentContainerType containerDocument = new ContentContainerType();

        final ContainerCustomization customization = providedCustomization != null ? providedCustomization
                : protocolService.getContainerCustomization();
        final boolean customContainer = customization.getUseCustomContainer() != null
                && customization.getUseCustomContainer().booleanValue();
        final boolean encryptedContainer = customization.getUseEncyptedContainer() != null
                && customization.getUseEncyptedContainer().booleanValue();
        log.info("using container: {}, type {}", customContainer, encryptedContainer);
        if (customContainer) {
            if (encryptedContainer) {
                createCustomEncryptedContainer(containerDocument, customization);
            } else {
                createCustomPlainContainer(containerDocument, customization);
            }
        } else {
            createDefaultContentContainer(containerDocument);
        }
        return containerDocument;
    }

    /**
     * @param contentContainer
     * @param customization
     */
    private void createCustomEncryptedContainer(final ContentContainerType contentContainer,
            final ContainerCustomization customization) {
        try {
            final EncryptedDataType encryptedDataXmlObject = encryptedXmlToObject(customization);
            // FIXME: How are encrypted messages passed to methods???
            // contentContainer.setEncryptedData(encryptedDataXmlObject);
            contentContainer.setMessage(null);
        } catch (final ConfigurationException e) {
            log.error("could not set encrypted container", e);
            // EncryptedContainer not usable, use default unencrypted
            createDefaultContentContainer(contentContainer);
        }
    }

    /**
     * @param contentContainer
     * @param customization
     */
    private void createCustomPlainContainer(final ContentContainerType contentContainer,
            final ContainerCustomization customization) {
        final ContentType message = new ContentType();
        contentContainer.setMessage(message);
        message.setContentType(customization.getContentType());
        message.setEncoding(customization.getEncoding());
        message.setFilename(customization.getFilename());
        message.setId(customization.getId());
        if (customization.getTempFile() != null) {
            final DataSource source = new FileDataSource(customization.getTempFile().toFile());
            final DataHandler dh = new DataHandler(source);
            message.setValue(dh);
            message.setSize(BigInteger.valueOf(customization.getSize()));
        }
    }

    /**
     * @param contentContainer
     */
    private void createDefaultContentContainer(final ContentContainerType contentContainer) {
        final ContentType message = new ContentType();
        contentContainer.setMessage(message);
        message.setContentType("image/gif");
        message.setEncoding(StandardCharsets.UTF_8.name());
        message.setFilename("kosit.gif");
        message.setId("kositgif");
        final URL logo;
        try {
            logo = new ClassPathResource("static/kosit_logo.gif").getURL();
            final DataSource source = new URLDataSource(logo);
            final DataHandler dh = new DataHandler(source);
            message.setValue(dh);
            message.setSize(BigInteger.valueOf(logo.openConnection().getContentLengthLong()));
        } catch (final IOException e) {
            log.error("cannot find kosit logo for attachment", e);
        }
    }

    public CustomResponseEntity getXmlPreview(final Path file, final String originalFileName, final String data) {
        final CustomResponseEntity result = processCustomConfiguration(file, originalFileName, data);
        final ContentContainerType container = createGenericContentContainer(result.getContainerCustomization());
        return result.setPreview(objectToXml(container));
    }

    private EncryptedDataType encryptedXmlToObject(final ContainerCustomization customization)
            throws ConfigurationException {
        try {
            final SAXSource xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(),
                    new InputSource(new StringReader(customization.getEncodedXMLContent())));
            final JAXBElement<org.apache.cxf.xkms.model.xmlenc.EncryptedDataType> data = (JAXBElement<EncryptedDataType>) jaxbUnmarshaller
                    .unmarshal(xmlSource);
            final EncryptedDataType encryptedData = data.getValue();
            if (encryptedData.getCipherData().getCipherReference() != null
                    && encryptedData.getCipherData().getCipherReference().getTransforms() == null
                    && encryptedData.getCipherData().getCipherReference().getURI() == null) {
                encryptedData.getCipherData().setCipherReference(null);
            }
            if (encryptedData.getKeyInfo().getContent().get(0) != null) {
                final JAXBElement jaxbElement = encryptedData.getKeyInfo().getContent().stream()
                        .filter(JAXBElement.class::isInstance).map(JAXBElement.class::cast).findFirst().orElseThrow();
                if (jaxbElement.getValue() instanceof final EncryptedKeyType encryptedKeyType
                        && encryptedKeyType.getCipherData().getCipherReference() != null
                        && encryptedKeyType.getCipherData().getCipherReference().getTransforms() == null
                        && encryptedKeyType.getCipherData().getCipherReference().getURI() == null) {
                    encryptedKeyType.getCipherData().setCipherReference(null);
                }
            }
            return encryptedData;
        } catch (final JAXBException | SAXException | ParserConfigurationException | NoSuchElementException e) {
            // should stop working if an exception is thrown
            throw new ConfigurationException("unable to read encrypted file: " + e.getMessage(), e);
        }
    }

    private String objectToXml(final ContentContainerType container)
            throws ConfigurationException {
        try {
            final StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(container, sw);
            return sw.toString();
        } catch (final JAXBException e) {
            // should stop working if an exception is thrown
            throw new ConfigurationException("unable to create container preview: " + e.getMessage(), e);
        }
    }
}