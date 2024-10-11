package de.xoev.xta.test.app.server;

import java.math.BigInteger;
import java.util.Optional;

import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
import genv5.de.xoev.transport.xta.core.x311.MessageFetchType;
import genv5.de.xoev.transport.xta.core.x311.ParameterIsNotValidException;
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
import genv5.de.xoev.transport.xta.core.x311.CoreRecipient;
import genv5.de.xoev.transport.xta.core.x311.InvalidMessageIDException;
import genv5.de.xoev.transport.xta.core.x311.MessageBoxStatusType;
import genv5.de.xoev.transport.xta.core.x311.MessageBoxStatusType.MessagesRemaining;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageSelectorType;
import genv5.de.xoev.transport.xta.core.x311.MessageStatusListType;
import genv5.de.xoev.transport.xta.core.x311.NotImplementedException;
import genv5.de.xoev.transport.xta.core.x311.ParameterNotSupportedException;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.PermissionDeniedException;
import genv5.de.xoev.transport.xta.core.x311.TechnicalProblemException;
import genv5.de.xoev.transport.xta.core.x311.UnsupportedExtensionException;
import lombok.extern.log4j.Log4j2;

@jakarta.jws.WebService(serviceName = "XTACore", portName = "recipient", targetNamespace = "https://xoev.de/transport/xta/core/3.1.1", wsdlLocation = "classpath:wsdl/wsdl/xta-core.wsdl", endpointInterface = "genv5.de.xoev.transport.xta.core.x311.CoreRecipient")
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
@Component
@Log4j2
public class RecipientImpl implements CoreRecipient {

    private final ParameterValidatorService validator;

    private final CustomizationService customizationService;

    private final ProtocolService protocolService;

    private final UtilComponent utilComponent;

    public RecipientImpl(final ParameterValidatorService validator, final CustomizationService customizationService,
            final ProtocolService protocolService, final UtilComponent utilComponent) {
        this.validator = validator;
        this.customizationService = customizationService;
        this.protocolService = protocolService;
        this.utilComponent = utilComponent;
    }

    @Override
    public void close(final MessageFetchType messageFetch, final PartyType party) throws TechnicalProblemException,
            NotImplementedException, ParameterNotSupportedException, UnsupportedExtensionException,
            ParameterIsNotValidException, InvalidMessageIDException, PermissionDeniedException {

        log.info("Executing close operation");

        if (validator.isNotNull(messageFetch.getXTAMessageID(), XtaParameter.CONFIRM_XTA_MESSAGE_ID)
                && validator.isNotBlank(messageFetch.getXTAMessageID().getValue(),
                        XtaParameter.CONFIRM_XTA_MESSAGE_ID)) {
            final Optional<String> messageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
            if (messageId.isPresent()) {
                validator.isEquals(messageId.get(), messageFetch.getXTAMessageID().getValue(),
                        XtaParameter.MESSAGE_ID);
            }
        }
    }

    @Override
    public ContentContainerType getMessage(final MessageFetchType messageFetch, final PartyType party,
            final Holder<MessageBoxStatusType> messageBoxStatus, final Holder<MessageMetaDataType> messageMetaData)
            throws TechnicalProblemException, NotImplementedException, ParameterNotSupportedException,
            UnsupportedExtensionException, ParameterIsNotValidException, InvalidMessageIDException,
            PermissionDeniedException {

        log.info("Executing getMessage operation");

        validator.checkMsgSelector(messageFetch,
                protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class));

        final MessageMetaDataType messageMetaDataReturn = new MessageMetaDataType();
        messageMetaData.value = messageMetaDataReturn;

        utilComponent.createMessageMetaData(messageMetaDataReturn, protocolService.getParameter(XtaParameter.MESSAGE_ID,
                String.class, () -> XtaIdGenerator.generateMessageId().toString()));

        final MessageBoxStatusType messageBoxStatusType = new MessageBoxStatusType();
        messageBoxStatus.value = messageBoxStatusType;

        final MessagesRemaining remaining = new MessagesRemaining();
        messageBoxStatusType.setMessagesRemaining(remaining);
        remaining.setNoMessageAvailable("No more Messages available");

        return customizationService.createGenericContentContainer();
    }

    @Override
    public MessageStatusListType getStatusList(final MessageSelectorType messageSelector, final PartyType party,
            final Holder<MessageBoxStatusType> messageBoxStatus)
            throws TechnicalProblemException, NotImplementedException,
            ParameterNotSupportedException, UnsupportedExtensionException, PermissionDeniedException {

        log.info("Executing getStatusList operation");

        final String messageId = XtaIdGenerator.generateMessageId().toString();
        // save messageID
        protocolService.addParameter(XtaParameter.MESSAGE_ID, messageId);
        validator.checkMsgSelector(messageSelector, Optional.empty());

        final MessageStatusListType response = new MessageStatusListType();
        final MessageMetaDataType messageMetaData = new MessageMetaDataType();
        response.getMessageMetaData().add(messageMetaData);

        utilComponent.createMessageMetaData(messageMetaData, messageId);

        final MessageBoxStatusType messageBoxStatusType = new MessageBoxStatusType();
        messageBoxStatus.value = messageBoxStatusType;

        final MessagesRemaining remaining = new MessagesRemaining();
        messageBoxStatusType.setMessagesRemaining(remaining);
        remaining.setMessagesPending(BigInteger.ONE);

        return response;
    }

}
