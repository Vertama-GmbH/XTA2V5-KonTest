package de.xoev.xta.test.app.service.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.service.ProtocolService;
import lombok.extern.log4j.Log4j2;

/**
 * Interceptor für den Fehlerfall auf Server- und Clientseite
 * <li>Loggt Fehlernachrichten für die Oberfläche, sofern ein Fault vorliegt</li>
 */
@Log4j2
@Component
public class ServerProtocolFaultInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private ProtocolService protocolService;

    public ServerProtocolFaultInterceptor() {
        super(Phase.PROTOCOL);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        log.debug(".... IM HERE ServerProtocolFaultOutInterceptor");
        Fault f = (Fault) message.getContent(Exception.class);
        if (f != null) {
            protocolService.addErrorResultMessage(f.getMessage());
        }

        if (protocolService.getCurrentScenarioProtocolEvent().isPresent()) {
            protocolService.closeScenarioProtocolEvent(f);
            log.info("Scenario protocol event closed");
        }
    }
}
