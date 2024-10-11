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
 * Interceptor für ausgehenden Verkehr beim Server
 * <li>Loggt das Senden einer Nachricht</li>
 */
@Log4j2
@Component
public class ServerProtocolOutInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private ProtocolService protocolService;

    public ServerProtocolOutInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        log.debug(".... IM HERE ServerProtocolOutInterceptor");

        protocolService.addMethodCallSendMessage();
        if (protocolService.getCurrentScenarioProtocolEvent().isPresent()) {
            protocolService.closeScenarioProtocolEvent(null);
            log.info("Scenario protocol event closed");
        }
    }

}
