package de.xoev.xta.test.app.service.interceptor;

import java.security.cert.X509Certificate;
import java.util.Optional;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.model.ScenarioEventType;
import de.xoev.xta.test.app.service.ProtocolService;
import lombok.extern.log4j.Log4j2;

/**
 * Interceptor für eingehenden Verkehr beim Server
 * <li>Started das aktuelle Szenario, sofern es noch nicht gestartet wurde</li>
 * <li>Loggt die Nutzung eines Client-Zertifikates</li>
 * <li>Loggt die Nutzung von MTOM</li>
 * <li>Loggt den Empfang einer Nachricht</li>
 */
@Log4j2
@Component
public class ServerProtocolInInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private ProtocolService protocolService;

    public ServerProtocolInInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        log.debug(".... IM HERE ServerProtocolInInterceptor");

        startScenarioIfPossible(message);
        checkClientCertUsed();
        checkMtomUsed(message);
        protocolService.addMethodCallReceivedMessage();
    }

    /**
     * Starts the scenario if it needs to be started
     *
     * @param message the inbound Soap Message
     */
    private void startScenarioIfPossible(final SoapMessage message) {
        Message inMessage = message.getExchange().getInMessage();
        String soapAction = (String) inMessage.get("SOAPAction");

        if (soapAction != null) {
            Optional<ScenarioEventType> optEventType = ScenarioEventType.findByAction(soapAction);
            if (optEventType.isPresent()) {
                log.info("Start scenario protocol event for SOAP action '{}'", optEventType.get());
                protocolService.startScenarioProtocolEvent(optEventType.get());
            }
        }
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

    /**
    *
    */
    private void checkClientCertUsed() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (object instanceof X509Certificate) {
            log.debug("TRANSPORT LOGGER: used certificate");
            // Certificate matches due to user auth used, this could be more specified here or in Userauth
        } else {
            log.debug("TRANSPORT LOGGER: no certificate");
            protocolService.addErrorResultMessage("kein Client Zertifikat verwendet");
        }
    }
}
