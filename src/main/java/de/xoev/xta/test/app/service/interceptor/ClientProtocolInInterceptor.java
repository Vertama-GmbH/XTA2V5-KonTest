package de.xoev.xta.test.app.service.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.service.ProtocolService;
import lombok.extern.log4j.Log4j2;

/**
 * Interceptor für eingehenden Verkehr beim Client
 * <li>Loggt, ob Mtom genutzt wurde</li>
 * <li>Loggt den Empfang einer Nachricht von Serverseite als Antwort des vorangegangenen Client-Requests</li>
 */
@Log4j2
@Component
public class ClientProtocolInInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private ProtocolService protocolService;

    public ClientProtocolInInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        log.debug(".... IM HERE ClientProtocolInInterceptor");
        checkMtomUsed(message);
        protocolService.addMethodCallReceivedMessage();
    }

    /**
     * checks, if the message uses mtom (NOT Really needed since this is handled by
     * cxf itself (policy))
     *
     * @param message the inbound Soap Message
     */
    private void checkMtomUsed(final SoapMessage message) {
        Message inMessage = message.getExchange().getInMessage();

        String headers = (String) inMessage.get(Message.CONTENT_TYPE);
        if (!headers.contains("type=\"application/xop+xml\"")) {
            protocolService.addErrorResultMessage("Kein MTOM verwendet");
        }
    }
}
