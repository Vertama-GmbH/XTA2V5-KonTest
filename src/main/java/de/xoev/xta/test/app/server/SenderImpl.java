
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package de.xoev.xta.test.app.server;

import java.util.Optional;
import java.util.UUID;

import de.xoev.xta.test.app.util.XmlIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.ContentContainerReply;
import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
import genv5.de.xoev.transport.xta.core.x311.LookupQuery;
import genv5.de.xoev.transport.xta.core.x311.LookupStatus;
import jakarta.xml.ws.Holder;
import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.service.CustomizationService;
import de.xoev.xta.test.app.service.ParameterValidatorService;
import de.xoev.xta.test.app.service.ProtocolService;
import de.xoev.xta.test.app.service.UtilComponent;
import de.xoev.xta.test.app.service.XtaParameter;
import de.xoev.xta.test.app.util.XtaIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.CoreSender;
import genv5.de.xoev.transport.xta.core.x311.ExternalServiceUnavailableException;
import genv5.de.xoev.transport.xta.core.x311.IsServiceAvailableValueType;
import genv5.de.xoev.transport.xta.core.x311.LookupServiceResultType;
import genv5.de.xoev.transport.xta.core.x311.LookupServiceType;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataReply;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageSchemaViolationException;
import genv5.de.xoev.transport.xta.core.x311.MessageVirusDetectionException;
import genv5.de.xoev.transport.xta.core.x311.NotImplementedException;
import genv5.de.xoev.transport.xta.core.x311.ParameterIsNotValidException;
import genv5.de.xoev.transport.xta.core.x311.ParameterNotSupportedException;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.PermissionDeniedException;
import genv5.de.xoev.transport.xta.core.x311.SyncAsyncException;
import genv5.de.xoev.transport.xta.core.x311.TechnicalProblemException;
import genv5.de.xoev.transport.xta.core.x311.UnsupportedExtensionException;
import genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType;
import jakarta.servlet.annotation.MultipartConfig;
import lombok.extern.log4j.Log4j2;

@jakarta.jws.WebService(serviceName = "XTACore", portName = "sender", targetNamespace = "https://xoev.de/transport/xta/core/3.1.1", wsdlLocation = "classpath:wsdl/wsdl/xta-core.wsdl", endpointInterface = "genv5.de.xoev.transport.xta.core.x311.CoreSender")
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
@Component
@MultipartConfig
@Log4j2
public class SenderImpl implements CoreSender {

    private final ParameterValidatorService validator;

    private final CustomizationService customizationService;

    private final ProtocolService protocolService;

    private final UtilComponent utilComponent;

    public SenderImpl(final ParameterValidatorService validator, final CustomizationService customizationService,
            final ProtocolService protocolService, final UtilComponent utilComponent) {
        this.validator = validator;
        this.customizationService = customizationService;
        this.protocolService = protocolService;
        this.utilComponent = utilComponent;
    }

    @Override
    public LookupStatus lookupService(final LookupQuery lookupQuery, final PartyType party)
            throws TechnicalProblemException,
            NotImplementedException, ParameterNotSupportedException, ExternalServiceUnavailableException,
            UnsupportedExtensionException, ParameterIsNotValidException, PermissionDeniedException {

        final LookupStatus response = new LookupStatus();

        for (final LookupServiceType lookupServiceType : lookupQuery.getLookupService()) {
            final LookupServiceResultType resultType = new LookupServiceResultType();
            response.getLookupServiceResult().add(resultType);

            final IsServiceAvailableValueType serviceAvailable = new IsServiceAvailableValueType();
            serviceAvailable.setServiceIsAvailable(true);
            serviceAvailable.setServiceIsAvailableUnknown(null);
            resultType.setIsServiceAvailableValue(serviceAvailable);

            final PartyType requestReader = lookupServiceType.getReader();
            final PartyType reader = new PartyType();
            reader.setIdentifier(requestReader.getIdentifier());
            resultType.setReader(reader);
            resultType.setService(lookupServiceType.getService());
        }
        return response;
    }

    @Override
    public void sendMessage(final ContentContainerType contentContainer, final MessageMetaDataType messageMetaData,
            final PartyType party)
            throws TechnicalProblemException, MessageVirusDetectionException, NotImplementedException,
            MessageSchemaViolationException, ParameterNotSupportedException, SyncAsyncException,
            UnsupportedExtensionException, ParameterIsNotValidException, PermissionDeniedException {

        log.info("Executing operation sendMessage.");

        if (messageMetaData != null) {
            final Optional<String> messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
            if (validator.isNotNull(messageMetaData, XtaParameter.MESSAGE_META_DATA_RESPONSE)
                    && validator.isNotNull(messageMetaData,
                            XtaParameter.MESSAGE_META_DATA_RESPONSE.join(XtaParameter.MESSAGE_META_DATA))) {
                validator.checkMessageMetaData(messageMetaData, messageId);
            }
        } else {
            protocolService.addErrorResultMessage(
                    String.format(UtilComponent.HEADER_VALUE_NOT_FOUND, XtaParameter.MESSAGE_META_DATA_RESPONSE));
        }
        validator.checkMessage(contentContainer);
    }

    @Override
    public ContentContainerReply sendMessageSync(final ContentContainerType contentContainer,
            final MessageMetaDataType messageMetaData, final PartyType party,
            final Holder<MessageMetaDataReply> messageMetaDataReply)
            throws TechnicalProblemException, MessageVirusDetectionException, NotImplementedException,
            MessageSchemaViolationException, ParameterNotSupportedException, ExternalServiceUnavailableException,
            SyncAsyncException, UnsupportedExtensionException, ParameterIsNotValidException, PermissionDeniedException {

        log.info("Executing operation sendMessageSync");

        Optional<String> messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
        if (messageMetaData != null) {
            validator.checkMessageMetaData(messageMetaData, messageId);
            messageId = utilComponent.extractAndSaveMessageID(messageMetaData, messageId);
        } else {
            protocolService
                    .addErrorResultMessage(
                            String.format(UtilComponent.HEADER_VALUE_NOT_FOUND, XtaParameter.MESSAGE_META_DATA));
        }
        validator.checkMessage(contentContainer);
        final ContentContainerType genericContentContainerReturn = customizationService
                .createGenericContentContainer();

        messageMetaDataReply.value = new MessageMetaDataReply();
        final MessageMetaDataType messageMetaDataReturn = new MessageMetaDataType();
        messageMetaDataReply.value.setMessageMetaData(messageMetaDataReturn);

        String messageIdReturn = null;
        if (messageId.isEmpty()) {
            // nothing set in protocol
            messageIdReturn = XtaIdGenerator.generateMessageId().toString();
            protocolService.addParameter(XtaParameter.MESSAGE_ID, messageIdReturn);
        } else {
            messageIdReturn = messageId.get();
        }
        utilComponent.createMessageMetaData(messageMetaDataReturn, messageIdReturn);

        final var reply = new ContentContainerReply();
        reply.setContentContainer(genericContentContainerReturn);
        return reply;
    }

    @Override
    public genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType createMessageID(final PartyType party)
            throws TechnicalProblemException, NotImplementedException, ParameterNotSupportedException,
            PermissionDeniedException, UnsupportedExtensionException {
        log.info("Executing operation createMessageID");

        final XTAMessageIDType messageIDType = new XTAMessageIDType();
        messageIDType.setValue(XtaIdGenerator.generateMessageId().toString());
        messageIDType.setId(XmlIdGenerator.generateRandomXsId());

        protocolService.addParameter(XtaParameter.MESSAGE_ID, messageIDType.getValue());

        return messageIDType;
    }
}
