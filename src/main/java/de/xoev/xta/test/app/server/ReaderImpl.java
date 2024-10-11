package de.xoev.xta.test.app.server;

import java.util.Optional;

import genv5.de.xoev.transport.xta.core.x311.ContentContainerReply;
import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
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
import genv5.de.xoev.transport.xta.core.x311.CoreReader;
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
import lombok.extern.log4j.Log4j2;

@jakarta.jws.WebService(serviceName = "XTACore", portName = "reader", targetNamespace = "https://xoev.de/transport/xta/core/3.1.1", wsdlLocation = "classpath:wsdl/wsdl/xta-core.wsdl", endpointInterface = "genv5.de.xoev.transport.xta.core.x311.CoreReader")
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
@Component
@Log4j2
public class ReaderImpl implements CoreReader {

    private final ParameterValidatorService validator;

    private final CustomizationService customizationService;

    private final ProtocolService protocolService;

    private final UtilComponent utilComponent;

    public ReaderImpl(final ParameterValidatorService validator, final CustomizationService customizationService,
            final ProtocolService protocolService, final UtilComponent utilComponent) {
        this.validator = validator;
        this.customizationService = customizationService;
        this.protocolService = protocolService;
        this.utilComponent = utilComponent;
    }

    @Override
    public ContentContainerReply deliverMessage(final ContentContainerType contentContainer,
            final MessageMetaDataType messageMetaData, final PartyType party,
            final Holder<MessageMetaDataReply> messageMetaDataReply)
            throws TechnicalProblemException, MessageVirusDetectionException, NotImplementedException,
            MessageSchemaViolationException, ParameterNotSupportedException, SyncAsyncException,
            UnsupportedExtensionException, ParameterIsNotValidException, PermissionDeniedException {

        Optional<String> messageId = Optional.empty();
        if (messageMetaData != null) {
            validator.checkMessageMetaData(messageMetaData, messageId);
            messageId = utilComponent.extractAndSaveMessageID(messageMetaData, messageId);
            protocolService.addParameter(XtaParameter.MESSAGE_ID,
                    messageId.orElseThrow(() -> new TechnicalProblemException("MessageID is empty")));
        } else {
            protocolService
                    .addErrorResultMessage(
                            String.format(UtilComponent.HEADER_VALUE_NOT_FOUND, XtaParameter.MESSAGE_META_DATA));
        }

        validator.checkMessage(contentContainer);

        messageMetaDataReply.value = new MessageMetaDataReply();

        final MessageMetaDataType messageMetadataReturn = new MessageMetaDataType();
        final String messageIdReturn;
        if (messageId.isEmpty()) {
            // nothing set in protocol
            messageIdReturn = XtaIdGenerator.generateMessageId().toString();
            protocolService.addParameter(XtaParameter.MESSAGE_ID, messageIdReturn);
        } else {
            messageIdReturn = messageId.get();
        }
        utilComponent.createMessageMetaData(messageMetadataReturn, messageIdReturn);
        messageMetaDataReply.value.setMessageMetaData(messageMetadataReturn);

        final var reply = new ContentContainerReply();
        reply.setContentContainer(customizationService.createGenericContentContainer());
        return reply;
    }

}
