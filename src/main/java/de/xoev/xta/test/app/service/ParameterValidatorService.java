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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import genv5.de.xoev.transport.xta.core.x311.ContentContainerType;
import genv5.de.xoev.transport.xta.core.x311.MessageFetchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageSelectorType;
import genv5.de.xoev.transport.xta.core.x311.OriginatorsType;
import genv5.de.xoev.transport.xta.core.x311.PartyIdentifierType;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.QualifierType;
import genv5.de.xoev.transport.xta.core.x311.QualifierType.BusinessScenario;
import genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ParameterValidatorService {

    @Autowired
    private ProtocolService protocolService;

    public boolean isNotNull(final Object obj, final CharSequence parameter) {
        if (obj == null) {
            log.error("Parameter '{}' was null", parameter);
            protocolService.addErrorResultMessage(String.format("Parameter '%s' nicht angegeben", parameter));
            return false;
        }
        return true;
    }

    public boolean isNotBlank(final String str, final CharSequence parameter) {
        final boolean result = isNotNull(str, parameter);
        if (result && str.trim().length() == 0) {
            log.error("Parameter '{}' was blank", parameter);
            protocolService.addErrorResultMessage(String.format("Parameter '%s' ohne Inhalt", parameter));
            return false;
        }
        return result;
    }

    public boolean isEquals(final String expectedStr, final String otherStr, final CharSequence parameter) {
        if (isNotNull(otherStr, parameter)) {
            if (!expectedStr.equals(otherStr)) {
                log.error("Parameter '{}' was wrong. expected='{}' received='{}'", parameter, expectedStr, otherStr);
                protocolService.addErrorResultMessage(String.format("Parameter '%s' war falsch.", parameter));
            }
            return false;
        }
        return true;
    }

    public boolean isListNotEmpty(final List<?> list, final CharSequence parameter) {
        final boolean result = isNotNull(list, parameter);
        if (result && list.isEmpty()) {
            log.error("Parameter list '{}' was empty", parameter);
            protocolService.addErrorResultMessage(String.format("Parameterliste '%s' ohne Inhalt", parameter));
            return false;
        }
        return result;
    }

    public boolean isListSizeEquals(final List<?> list, final int expectedSize, final CharSequence parameter) {
        final boolean result = isNotNull(list, parameter);
        if (result && list.size() != expectedSize) {
            log.error("Parameter list '{}' has wrong size.", parameter);
            protocolService.addErrorResultMessage(
                    String.format("Größe der Parameterliste '%s' sollte %d sein", parameter, expectedSize));
            return false;
        }
        return result;
    }

    public void checkMessageMetaData(final MessageMetaDataType messageMetaData, // NOSONAR
            final Optional<String> expectedMessageId) {
        final XtaParameter param = XtaParameter.MESSAGE_META_DATA;
        if (isNotNull(messageMetaData, param)) {
            if (isNotNull(messageMetaData.getXTAMessageID(), param.join(XtaParameter.XTA_MESSAGE_ID))) {
                final XTAMessageIDType msgIdentification = messageMetaData.getXTAMessageID();
                if (expectedMessageId.isPresent()) {
                    isEquals(expectedMessageId.get(), msgIdentification.getValue(),
                            param.join(XtaParameter.XTA_MESSAGE_ID, XtaParameter.MESSAGE_ID));
                }
            }
            if (isNotNull(messageMetaData.getReader(), param.join(XtaParameter.READER))) {
                checkIdentifier(messageMetaData.getReader().getIdentifier(), param.join(XtaParameter.READER));
            }
            if (isNotNull(messageMetaData.getOriginators(), param.join(XtaParameter.ORIGINATORS))) {
                final XtaParameter subparam = param.join(XtaParameter.ORIGINATORS);
                final OriginatorsType originators = messageMetaData.getOriginators();
                checkPartyType(originators.getAuthor(), subparam.join(XtaParameter.AUTHOR));
                // sender is optional
            }
            if (isNotNull(messageMetaData.getQualifier(), param.join(XtaParameter.QUALIFIER))) {
                final XtaParameter subparam = param.join(XtaParameter.QUALIFIER);
                final QualifierType qualifier = messageMetaData.getQualifier();

                if (qualifier.getService() != null) {
                    isNotBlank(qualifier.getService(), subparam.join(XtaParameter.SERVICE));
                }

                if (isNotNull(qualifier.getBusinessScenario(), param.join(XtaParameter.BUSINESS_SCENARIO))) {
                    final BusinessScenario businessScenario = qualifier.getBusinessScenario();
                    final XtaParameter p = param.join(XtaParameter.BUSINESS_SCENARIO);
                    if (businessScenario.getDefined() != null) {
                        final XtaParameter p1 = p.join("Defined");
                        isNotBlank(businessScenario.getDefined().getListURI(), p1.join("listURI"));
                        isNotBlank(businessScenario.getDefined().getListURI(), p1.join("listVersionID"));
                        isNotBlank(businessScenario.getDefined().getCode(), p1.join("code"));
                    } else if (businessScenario.getUndefined() == null) {
                        protocolService
                                .addErrorResultMessage(String.format("Parameter '%s' oder '%s' muss angegeben sein",
                                        p.join("Defined"), p.join("Undefined")));
                    }
                }
                if (isNotNull(qualifier.getMessageType(), subparam.join(XtaParameter.MESSAGE_TYPE))) {
                    isNotBlank(qualifier.getMessageType().getMessageSchema(),
                            subparam.join(XtaParameter.MESSAGE_TYPE).join("messageSchema"));
                }
            }
            isNotNull(messageMetaData.getMessageSize(), param.join(XtaParameter.MESSAGE_SIZE));
            isNotNull(messageMetaData.getDeliveryAttributes(), param.join(XtaParameter.DELIVERY_ATTRIBUTES));

            validateAdditionalIdentifiers(messageMetaData, param);
        }
    }

    private void validateAdditionalIdentifiers(final MessageMetaDataType messageMetaData, final XtaParameter param) {
        if (messageMetaData.getAdditionalIdentifier() != null) {
            final XtaParameter additionalIdentParam = param.join("AdditionalIdentifier");
            final XtaParameter addIdentContextParam = additionalIdentParam.join("Context");

            for (final var additionalIdentifier : messageMetaData.getAdditionalIdentifier()) {
                isNotBlank(additionalIdentifier.getId(), additionalIdentParam.join("id"));
                isNotBlank(additionalIdentifier.getMessageIdentifier(),
                        additionalIdentParam.join("MessageIdentifier"));

                isNotNull(additionalIdentifier.getContext(), addIdentContextParam);
                isNotBlank(additionalIdentifier.getContext().getListURI(), addIdentContextParam.join("listURI"));
                isNotBlank(additionalIdentifier.getContext().getListVersionID(),
                        addIdentContextParam.join("listVersionID"));
            }
        }
    }

    public void checkPartyType(final PartyType partyType, final XtaParameter parameter) {
        if (isNotNull(partyType, parameter)) {
            final XtaParameter subparam = parameter.join(XtaParameter.IDENTIFIER);
            checkIdentifier(partyType.getIdentifier(), subparam);
        }
    }

    public void checkMessage(final ContentContainerType request) {
        final boolean haveMessage = request.getMessage() != null;
        final boolean haveAttachments = request.getAttachment() != null && !request.getAttachment().isEmpty();
        final boolean haveExtensions = request.getExtensions() != null;

        if (!haveMessage && !haveAttachments && !haveExtensions) {
            log.error("None of Message, Attachment and Extensions found.");
            protocolService.addErrorResultMessage("Keine Nachricht enthalten");
            return;
        }

        if (haveMessage) {
            final var msg = request.getMessage();
            isNotNull(msg.getValue(), XtaParameter.MESSAGE.join("value"));
            isNotBlank(msg.getContentType(), XtaParameter.MESSAGE.join("contentType"));
            isNotBlank(msg.getEncoding(), XtaParameter.MESSAGE.join("encoding"));
            isNotBlank(msg.getFilename(), XtaParameter.MESSAGE.join("filename"));
            isNotBlank(msg.getId(), XtaParameter.MESSAGE.join("id"));
            isNotNull(msg.getSize(), XtaParameter.MESSAGE.join("size"));

            try {
                final var dataBuffer = new ByteArrayOutputStream();
                msg.getValue().writeTo(dataBuffer);
                final var data = dataBuffer.toByteArray();

                final var actualMessageSize = BigInteger.valueOf(data.length);
                if (msg.getSize() != null && !actualMessageSize.equals(msg.getSize())) {
                    log.warn(
                            "Angegebene ContentType-Größe stimmt nicht mir tatsächlicher Größe überein. (Angegeben {}, tatsächliche Größe {})",
                            msg.getSize(), actualMessageSize);
                }
            } catch (final IOException ex) {
                throw new RuntimeException("Error during reading of message content.", ex);
            }
        }
    }

    public void checkMsgSelector(final MessageSelectorType msgSelector, final Optional<String> expectedMessageId) {
        if (isNotNull(msgSelector, XtaParameter.MESSAGE_SELECTOR)) {
            isNotNull(msgSelector.getOtherCriteria().isNewEntry(), XtaParameter.MESSAGE_SELECTOR.join("newEntry"));
            isNotNull(msgSelector.getOtherCriteria().getMessageBoxEntryTimeFrom(),
                    XtaParameter.MESSAGE_SELECTOR.join("MsgBoxEntryTimeFrom"));
            isNotNull(msgSelector.getOtherCriteria().getMessageBoxEntryTimeTo(),
                    XtaParameter.MESSAGE_SELECTOR.join("MsgBoxEntryTimeTo"));
            if (expectedMessageId.isPresent() && isListNotEmpty(msgSelector.getXTAMessageIDs().getXTAMessageID(),
                    XtaParameter.MESSAGE_SELECTOR.join(XtaParameter.MESSAGE_ID))) {
                isEquals(expectedMessageId.get(), msgSelector.getXTAMessageIDs().getXTAMessageID().get(0).getValue(),
                        XtaParameter.MESSAGE_SELECTOR.join(XtaParameter.MESSAGE_ID));
            }
        }
    }

    public void checkMsgSelector(final MessageFetchType messageFetch, final Optional<String> expectedMessageId) {
        if (expectedMessageId.isPresent() && isNotNull(messageFetch.getXTAMessageID(),
                XtaParameter.MESSAGE_SELECTOR.join(XtaParameter.MESSAGE_ID))) {
            isEquals(expectedMessageId.get(), messageFetch.getXTAMessageID().getValue(),
                    XtaParameter.MESSAGE_SELECTOR.join(XtaParameter.MESSAGE_ID));
        }
    }

    public void checkPartyDocument(final PartyType partyType) {
        if (isNotNull(partyType, XtaParameter.PARTY)) {
            checkIdentifier(partyType.getIdentifier(), XtaParameter.PARTY);
        }
    }

    public void checkIdentifier(final PartyIdentifierType identifier, final XtaParameter parent) {
        if (isNotNull(identifier, parent)) {
            final XtaParameter param = parent.join(XtaParameter.IDENTIFIER);
            isNotBlank(identifier.getIdentifierScheme(), param.join("value"));
            isNotBlank(identifier.getName(), param.join("type"));
        }
    }
}
