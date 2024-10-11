package de.xoev.xta.test.app.service;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.xoev.xta.test.app.util.XmlIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.DateTimeWithIDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.model.Role;
import de.xoev.xta.test.app.model.ScenarioDefinition;
import genv5.de.xoev.transport.xta.core.x311.DeliveryAttributesType;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageSelectorType;
import genv5.de.xoev.transport.xta.core.x311.OriginatorsType;
import genv5.de.xoev.transport.xta.core.x311.ServiceQualityType;
import genv5.de.xoev.transport.xta.core.x311.TechnicalProblemException;
import genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType;

@Component
public class UtilComponent {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private XtaConfigDtoConverter xtaConfigDtoConverter;

    @Autowired
    private ProtocolService protocolService;

    public static final String HEADER_VALUE_NOT_FOUND = "Die erforderliche Header Variable '%s' wurde nicht gefunden";

    public Optional<String> extractAndSaveMessageID(final MessageMetaDataType messageMetaData,
            final Optional<String> savedMessageId) {
        if (savedMessageId != null && savedMessageId.isPresent()) {
            return savedMessageId;
        }
        if (messageMetaData.getXTAMessageID() != null) {
            return Optional.ofNullable(messageMetaData.getXTAMessageID().getValue());
        }
        return Optional.empty();
    }

    private DateTimeWithIDType createNowDateTimeWithId(final XMLGregorianCalendar dateTime) {
        final var dateTimeWithId = new DateTimeWithIDType();
        dateTimeWithId.setValue(dateTime);
        dateTimeWithId.setId(XmlIdGenerator.generateRandomXsId());

        return dateTimeWithId;
    }

    public void createMessageMetaData(final MessageMetaDataType messageMetaData, final String messageId)
            throws TechnicalProblemException {
        final XMLGregorianCalendar calendarNow = createGC(0);

        final DeliveryAttributesType deliveryAttributes = new DeliveryAttributesType();
        messageMetaData.setDeliveryAttributes(deliveryAttributes);

        final Optional<ScenarioDefinition> scenarioDefinition = protocolService.getCurrentScenarioDefinition();
        final Role role = scenarioDefinition.isPresent()
                ? scenarioDefinition.get().getScenarioRole().getRoleTestumgebung()
                : Role.LESER;

        switch (role) {
            case LESER:
                // fallthrough
                deliveryAttributes.setDelivery(createNowDateTimeWithId(calendarNow));
            case EMPFAENGER:
                // fallthrough
                deliveryAttributes.setInitialFetch(createNowDateTimeWithId(calendarNow));
            case SENDER:
                // fallthrough
                deliveryAttributes.setInitialSend(createNowDateTimeWithId(calendarNow));
            case AUTOR:
                deliveryAttributes.setOrigin(createNowDateTimeWithId(calendarNow));
                break;
            default:
                throw new TechnicalProblemException("Role not supported");
        }

        final OriginatorsType originators = new OriginatorsType();
        messageMetaData.setOriginators(originators);
        originators.setAuthor(
                xtaConfigDtoConverter.fromModelWithId(configurationService.getXtaConfig().getAuthorIdentifierConfig()));
        originators.setSender(
                xtaConfigDtoConverter.fromModelWithId(configurationService.getXtaConfig().getSenderIdentifierConfig()));

        messageMetaData.setReader(
                xtaConfigDtoConverter.fromModelWithId(configurationService.getXtaConfig().getReaderIdentifierConfig()));

        final XTAMessageIDType msgIdentification = new XTAMessageIDType();
        messageMetaData.setXTAMessageID(msgIdentification);
        msgIdentification.setValue(messageId);
        msgIdentification.setId(XmlIdGenerator.generateRandomXsId());

        messageMetaData.setQualifier(
                xtaConfigDtoConverter.fromModel(configurationService.getXtaConfig().getQualifierConfig()));

        final ServiceQualityType serviceQualityType = new ServiceQualityType();
        serviceQualityType.setUndefined("Its undefined");
        serviceQualityType.setDefined(null);
        serviceQualityType.setId(XmlIdGenerator.generateRandomXsId());
        messageMetaData.setServiceQuality(serviceQualityType);
        messageMetaData.setMessageSize(BigInteger.valueOf(10L));
    }

    public XMLGregorianCalendar createGC(final int offsetMonth) {

        final GregorianCalendar cal = new GregorianCalendar();
        if (offsetMonth != 0) {
            cal.add(Calendar.MONTH, offsetMonth);
        }
        return DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(cal);
    }

    public MessageSelectorType createMsgSelector(final Optional<String> messageId) {
        final MessageSelectorType msgSelector = new MessageSelectorType();
        msgSelector.getOtherCriteria().setNewEntry(Boolean.TRUE);
        if (messageId.isPresent()) {
            final XTAMessageIDType attUriType = new XTAMessageIDType();
            attUriType.setValue(messageId.get());
            // FIXME: Access via getXTAMessageIDs().getXTAMessageID is redundant.
            msgSelector.getXTAMessageIDs().getXTAMessageID().add(attUriType);
        }

        msgSelector.getOtherCriteria().setMessageBoxEntryTimeFrom(createGC(-1));
        msgSelector.getOtherCriteria().setMessageBoxEntryTimeTo(createGC(0));
        return msgSelector;
    }

}
