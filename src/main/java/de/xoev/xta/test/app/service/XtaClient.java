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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import de.xoev.xta.test.app.util.XmlIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.ContentContainerReply;
import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
import genv5.de.xoev.transport.xta.core.x311.LookupQuery;
import genv5.de.xoev.transport.xta.core.x311.LookupStatus;
import genv5.de.xoev.transport.xta.core.x311.MessageFetchType;
import genv5.de.xoev.transport.xta.core.x311.ReportResult;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.xoev.xta.test.app.config.ClientProperties;
import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.util.XtaIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.ExternalServiceUnavailableException;
import genv5.de.xoev.transport.xta.core.x311.InvalidMessageIDException;
import genv5.de.xoev.transport.xta.core.x311.LookupServiceType;
import genv5.de.xoev.transport.xta.core.x311.MessageBoxStatusType;
import genv5.de.xoev.transport.xta.core.x311.MessageBoxStatusType.MessagesRemaining;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataReply;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageSchemaViolationException;
import genv5.de.xoev.transport.xta.core.x311.MessageSelectorType;
import genv5.de.xoev.transport.xta.core.x311.MessageStatusListType;
import genv5.de.xoev.transport.xta.core.x311.MessageVirusDetectionException;
import genv5.de.xoev.transport.xta.core.x311.NotImplementedException;
import genv5.de.xoev.transport.xta.core.x311.ParameterIsNotValidException;
import genv5.de.xoev.transport.xta.core.x311.ParameterNotSupportedException;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.PermissionDeniedException;
import genv5.de.xoev.transport.xta.core.x311.SyncAsyncException;
import genv5.de.xoev.transport.xta.core.x311.TechnicalProblemException;
import genv5.de.xoev.transport.xta.core.x311.TransportReportType;
import genv5.de.xoev.transport.xta.core.x311.UnsupportedExtensionException;
import genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType;
import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.Holder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class XtaClient {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private ParameterValidatorService validator;

    @Autowired
    private CustomizationService customizationService;

    @Autowired
    private ClientConfigurationContainer clientConfigurationContainer;

    @Autowired
    private XtaConfigDtoConverter xtaConfigDtoConverter;

    @Autowired
    private SpringBus bus;

    @Autowired
    private UtilComponent utilComponent;

    @PostConstruct
    void setUp() {
        bus.setExtension((name, address, httpConduit) -> {
            try {
                httpConduit.setTlsClientParameters(createTLSClientParameters());
                httpConduit.setClient(getClientPolicy());
            } catch (final XtaTesterException e) {
                // do nothing
            }
        }, HTTPConduitConfigurer.class);
    }

    /*
     * methods to call the WebServices
     */
    public void checkAccountActive() {
        log.info("Requesting checkAccountActive");
        try {
            clientConfigurationContainer.getTransportPortClient().checkAccountActive(
                    xtaConfigDtoConverter.fromModel(configurationService.getXtaConfig().getAuthorIdentifierConfig()));
            protocolService.addMethodCallSendMessage();
        } catch (final Exception e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    public void lookupService() {
        log.info("Requesting lookupService");
        try {
            final LookupQuery query = new LookupQuery();
            final LookupServiceType lookupServiceRequest = new LookupServiceType();
            query.getLookupService().add(lookupServiceRequest);
            final PartyType readerConfig = xtaConfigDtoConverter.fromModel(
                    configurationService.getXtaConfig().getReaderIdentifierConfig());
            lookupServiceRequest.setReader(readerConfig);
            lookupServiceRequest.setService("DVDV");

            // execute request sync
            final LookupStatus sendResponse = clientConfigurationContainer.getSendPortClient()
                    .lookupService(query, xtaConfigDtoConverter.fromModel(
                            configurationService.getXtaConfig().getAuthorIdentifierConfig()));

            sendResponse.getLookupServiceResult()
                    .forEach(element -> log.info("Service {} is available {}", element.getService(),
                            element.getIsServiceAvailableValue()));
        } catch (final TechnicalProblemException | ExternalServiceUnavailableException | ParameterNotSupportedException
                | UnsupportedExtensionException | ParameterIsNotValidException | PermissionDeniedException
                | XtaTesterException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        } catch (final NotImplementedException e) {
            throw new RuntimeException(e);
        }
    }

    public void createMessageId() {
        log.info("Requesting createMessageId");
        try {
            final XTAMessageIDType response = clientConfigurationContainer.getSendPortClient()
                    .createMessageID(xtaConfigDtoConverter.fromModel(
                            configurationService.getXtaConfig().getAuthorIdentifierConfig()));
            if (validator.isNotBlank(response.getValue(), "messageId")) {
                protocolService.addParameter(XtaParameter.MESSAGE_ID, response.getValue());
            }
        } catch (final XtaTesterException | TechnicalProblemException | NotImplementedException
                | ParameterNotSupportedException | PermissionDeniedException | UnsupportedExtensionException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    public void sendMessage() {
        log.info("Requesting sendMessage");
        try {
            final ContentContainerType genericContentContainer = customizationService
                    .createGenericContentContainer();
            final String messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class,
                    () -> XtaIdGenerator.generateMessageId().toString());

            final MessageMetaDataType messageMetaData = new MessageMetaDataType();
            utilComponent.createMessageMetaData(messageMetaData, messageId);

            clientConfigurationContainer.getSendPortClient().sendMessage(genericContentContainer, messageMetaData,
                    xtaConfigDtoConverter.fromModel(
                            configurationService.getXtaConfig().getAuthorIdentifierConfig()));
        } catch (final SyncAsyncException | MessageVirusDetectionException | TechnicalProblemException
                | MessageSchemaViolationException | NotImplementedException | ParameterNotSupportedException
                | UnsupportedExtensionException | ParameterIsNotValidException | PermissionDeniedException
                | XtaTesterException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    public void sendMessageSync() {
        log.info("Requesting sendMessageSync");
        try {
            final ContentContainerType genericContentContainer = customizationService
                    .createGenericContentContainer();

            final String messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class,
                    () -> XtaIdGenerator.generateMessageId().toString());

            final MessageMetaDataType messageMetaData = new MessageMetaDataType();
            utilComponent.createMessageMetaData(messageMetaData, messageId);

            final Holder<MessageMetaDataReply> messageMetaDataHolder = new Holder<>();
            final ContentContainerReply response = clientConfigurationContainer.getSendPortClient()
                    .sendMessageSync(
                            genericContentContainer,
                            messageMetaData,
                            xtaConfigDtoConverter.fromModel(
                                    configurationService.getXtaConfig().getAuthorIdentifierConfig()),
                            messageMetaDataHolder);

            protocolService.addMethodCallSendMessage();

            validator.checkMessage(response.getContentContainer());

            if (messageMetaDataHolder.value != null) {
                final MessageMetaDataReply messageMetaDataResponse = messageMetaDataHolder.value;
                if (validator.isNotNull(messageMetaDataResponse.getMessageMetaData(), XtaParameter.MESSAGE_META_DATA)) {
                    validator.checkMessageMetaData(messageMetaDataResponse.getMessageMetaData(),
                            Optional.of(messageId));
                }
            } else {
                protocolService.addErrorResultMessage("MessageMetaData nicht im Soap Header angegeben");
            }
        } catch (final SyncAsyncException | MessageVirusDetectionException | TechnicalProblemException
                | MessageSchemaViolationException | NotImplementedException | ExternalServiceUnavailableException
                | ParameterNotSupportedException | UnsupportedExtensionException | ParameterIsNotValidException
                | PermissionDeniedException | XtaTesterException e) {
            protocolService.addErrorResultMessage("Fehler beim Senden der Nachricht: " + e.getMessage());
        }
    }

    public void deliverMessage() {
        log.info("Requesting deliverMessage");
        try {
            final ContentContainerType genericContentContainer = customizationService
                    .createGenericContentContainer();

            final String messageId = XtaIdGenerator.generateMessageId().toString();
            final MessageMetaDataType messageMetaData = new MessageMetaDataType();
            utilComponent.createMessageMetaData(messageMetaData, messageId);

            final Holder<MessageMetaDataReply> messageMetaDataReply = new Holder<>();

            final ContentContainerReply response = clientConfigurationContainer.getReaderPortClient()
                    .deliverMessage(
                            genericContentContainer,
                            messageMetaData, xtaConfigDtoConverter.fromModel(
                                    configurationService.getXtaConfig().getAuthorIdentifierConfig()),
                            messageMetaDataReply);

            protocolService.addMethodCallSendMessage();

            validator.checkMessage(response.getContentContainer());

            if (messageMetaDataReply.value != null) {
                if (validator.isNotNull(messageMetaDataReply.value.getMessageMetaData(),
                        XtaParameter.MESSAGE_META_DATA_RESPONSE.join(XtaParameter.MESSAGE_META_DATA))) {
                    validator.checkMessageMetaData(messageMetaDataReply.value.getMessageMetaData(),
                            Optional.of(messageId));
                }
            } else {
                protocolService.addErrorResultMessage("MessageMetaData nicht im Soap Header angegeben");
            }
        } catch (final SyncAsyncException | XtaTesterException | MessageVirusDetectionException
                | TechnicalProblemException
                | MessageSchemaViolationException | NotImplementedException | ParameterNotSupportedException
                | UnsupportedExtensionException | ParameterIsNotValidException | PermissionDeniedException e) {
            protocolService.addErrorResultMessage("Fehler beim Senden der Nachricht: " + e.getMessage());
        }
    }

    public void getTransportReport() {
        log.info("Requesting getTransportReport");
        try {
            final Optional<String> messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
            final XTAMessageIDType messageIDDocument = new XTAMessageIDType();
            messageIDDocument.setId(XmlIdGenerator.generateRandomXsId());
            messageIDDocument.setValue(messageId.orElseThrow(() -> new XtaTesterException("no MessageID found")));

            final ReportResult reportResult = clientConfigurationContainer.getTransportPortClient()
                    .getTransportReport(
                            messageIDDocument,
                            xtaConfigDtoConverter.fromModel(
                                    configurationService.getXtaConfig().getAuthorIdentifierConfig()));

            validator.isNotNull(reportResult.getTransportReport(), "TransportReport");

            final var transportReport = reportResult.getTransportReport();
            validator.isNotNull(transportReport.getReportTime(), "ReportTime");
            validator.isNotBlank(transportReport.getXTAServerIdentity(), "XTAServerIdentity");
            validator.checkMessageMetaData(transportReport.getMessageMetaData(), messageId);
            if (validator.isNotNull(transportReport.getMessageStatus(), "MessageStatus")) {
                validator.isNotNull(transportReport.getMessageStatus().getStatus(), "MessageStatus.status");
            }
            validator.isNotNull(transportReport.getAdditionalReports(), "AdditionalReports");
            // validator.isNotNull(transportReport.getSignature(), "Signature");
        } catch (final TechnicalProblemException | ParameterNotSupportedException | PermissionDeniedException
                | UnsupportedExtensionException | InvalidMessageIDException | XtaTesterException
                | NotImplementedException e) {
            log.error("Unexpected error.", e);
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    public void getStatusList() {
        log.info("Requesting getStatusList");
        try {
            final MessageSelectorType messageSelector = utilComponent.createMsgSelector(Optional.empty());
            final Holder<MessageBoxStatusType> messageBoxStatusTypeHolder = new Holder<>();

            final MessageStatusListType statusListResponse = clientConfigurationContainer.getRecipientPortClient()
                    .getStatusList(
                            messageSelector, xtaConfigDtoConverter.fromModel(
                                    configurationService.getXtaConfig().getAuthorIdentifierConfig()),
                            messageBoxStatusTypeHolder);

            handleGetStatusListResponse(statusListResponse, messageBoxStatusTypeHolder);
        } catch (final XtaTesterException | TechnicalProblemException | PermissionDeniedException
                | NotImplementedException
                | ParameterNotSupportedException | UnsupportedExtensionException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    private void handleGetStatusListResponse(final MessageStatusListType statusListResponse,
            final Holder<MessageBoxStatusType> holder) {
        if (validator.isListNotEmpty(statusListResponse.getMessageMetaData(), "MessageMetaData")) {
            final MessageMetaDataType messageMetaData = statusListResponse.getMessageMetaData().get(0);

            validator.checkMessageMetaData(messageMetaData, Optional.empty());
            if (messageMetaData.getXTAMessageID() != null && messageMetaData.getXTAMessageID().getValue() != null) {
                protocolService.addParameter(XtaParameter.MESSAGE_ID, messageMetaData.getXTAMessageID().getValue());
            }
        }

        if (holder.value != null) {
            final MessagesRemaining messagesRemaining = holder.value.getMessagesRemaining();
            if (messagesRemaining.getNoMessageAvailable() != null) {
                protocolService.addErrorResultMessage("No message available");
            }
            if (validator.isNotNull(messagesRemaining, XtaParameter.MESSAGE_BOX_RESPONSE.join("Remaining"))) {
                validator.isNotNull(messagesRemaining.getMessagesPending(),
                        XtaParameter.MESSAGE_BOX_RESPONSE.join("Remaining").join("ItemsPending"));
            }
        } else {
            protocolService.addErrorResultMessage("MessageBoxStatusType nicht im Soap Header angegeben");
        }
    }

    public void getMessage() {
        log.info("Requesting getMessage");
        try {
            final Optional<String> messageID = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
            // final MessageSelectorType messageSelector = utilComponent.createMsgSelector(messageID);

            final MessageFetchType messageFetchType = new MessageFetchType();
            messageFetchType.setXTAMessageID(new XTAMessageIDType());
            messageFetchType.getXTAMessageID().setValue(
                    messageID.orElseThrow(() -> new XtaTesterException("Keine MessageID für getMessage vorhanden.")));

            final Holder<MessageMetaDataType> messageMetaDataHolder = new Holder<>();
            final Holder<MessageBoxStatusType> messageBoxStatusTypeHolder = new Holder<>();

            final ContentContainerType containerReturn = clientConfigurationContainer.getRecipientPortClient()
                    .getMessage(messageFetchType, xtaConfigDtoConverter.fromModel(
                            configurationService.getXtaConfig().getAuthorIdentifierConfig()),
                            messageBoxStatusTypeHolder,
                            messageMetaDataHolder);
            validator.checkMessage(containerReturn);
        } catch (final TechnicalProblemException | InvalidMessageIDException | PermissionDeniedException
                | XtaTesterException | NotImplementedException | ParameterNotSupportedException
                | UnsupportedExtensionException | ParameterIsNotValidException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    public void close() {
        log.info("Requesting close");
        final MessageFetchType closeRequest = new MessageFetchType();

        final String messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class,
                () -> XtaIdGenerator.generateMessageId().toString());

        final XTAMessageIDType xtaMessageIDType = new XTAMessageIDType();
        xtaMessageIDType.setId(XmlIdGenerator.generateRandomXsId());
        xtaMessageIDType.setValue(messageId);

        closeRequest.setXTAMessageID(xtaMessageIDType);

        try {
            clientConfigurationContainer.getRecipientPortClient().close(closeRequest,
                    xtaConfigDtoConverter.fromModel(
                            configurationService.getXtaConfig().getReaderIdentifierConfig()));
        } catch (final TechnicalProblemException | InvalidMessageIDException | PermissionDeniedException
                | XtaTesterException | NotImplementedException | ParameterNotSupportedException
                | UnsupportedExtensionException | ParameterIsNotValidException e) {
            protocolService.addErrorResultMessage(e.getMessage());
        }
    }

    private HTTPClientPolicy getClientPolicy() {
        // set timeouts
        final HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
        clientPolicy.setConnectionTimeout(30000);
        clientPolicy.setReceiveTimeout(30000);
        clientPolicy.setAutoRedirect(true);
        clientPolicy.setConnectionRequestTimeout(30000);
        clientPolicy.setAllowChunking(true);
        return clientPolicy;
    }

    private TLSClientParameters createTLSClientParameters() throws XtaTesterException {
        final TLSClientParameters tlsClientParameters = new TLSClientParameters();
        try {
            final SSLContextBuilder sslBuilder = SSLContexts.custom();
            final ClientProperties clientProperties = configurationService.getXtaConfig().getClientProperties();
            log.info(clientProperties.getTrustStoreAsResource().getURL());
            sslBuilder.loadTrustMaterial(clientProperties.getTrustStoreAsResource().getURL(),
                    clientProperties.getTrustStorePassword().toCharArray());
            sslBuilder.loadKeyMaterial(clientProperties.getKeyStoreAsResource().getURL(),
                    clientProperties.getKeyStorePassword().toCharArray(),
                    clientProperties.getKeyPassword().toCharArray(),
                    (aliases, sslParameters) -> {
                        String alias = clientProperties.getKeyAlias();
                        if ((alias == null || alias.isBlank()) && aliases.size() == 1) {
                            // use the first alias from the map
                            alias = aliases.keySet().iterator().next();
                        }
                        return alias;
                    });
            final HostnameVerifier hostnameverifier = clientProperties.isCheckHostnameInCertificate()
                    ? new DefaultHostnameVerifier()
                    : new NoopHostnameVerifier();

            final SSLContext sslContext = sslBuilder.build();
            tlsClientParameters.setHostnameVerifier(hostnameverifier);

            // only by setting socket Factory, this ssl configuration works with cxf 4
            tlsClientParameters.setSSLSocketFactory(sslContext.getSocketFactory());
        } catch (final UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                | IOException | KeyManagementException e) {
            throw new XtaTesterException("Unable to create Soap Client", e);
        }
        return tlsClientParameters;
    }
}