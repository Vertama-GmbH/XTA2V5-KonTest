package de.xoev.xta.test.app.service.interceptor;

import java.util.Optional;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.model.ScenarioEventType;
import de.xoev.xta.test.app.service.ProtocolService;
import lombok.extern.log4j.Log4j2;

/**
 * Interceptor für ausgehenden Verkehr beim Client
 * <li>Started das aktuelle Szenario, sofern es noch nicht gestartet wurde</li>
 * <li>Loggt den Versand einer Nachricht</li>
 */

@Log4j2
@Component
public class ClientProtocolOutInterceptor extends AbstractSoapInterceptor {

    @Autowired
    private ProtocolService protocolService;

    public ClientProtocolOutInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        log.debug(".... IM HERE ClientProtocolOutInterceptor");
        startScenarioIfPossible(message);
        protocolService.addMethodCallSendMessage();
    }

    /**
     * Starts the scenario if it needs to be started
     *
     * @param message the inbound Soap Message
     */
    private void startScenarioIfPossible(final SoapMessage message) {
        Message outMessage = message.getExchange().getOutMessage();
        AddressingProperties props = (AddressingProperties) outMessage
                .get("jakarta.xml.ws.addressing.context.outbound");

        if (props != null && props.getAction().getValue() != null) {
            Optional<ScenarioEventType> optEventType = ScenarioEventType.findByAction(props.getAction().getValue());
            if (optEventType.isPresent()) {
                log.info("Start scenario protocol event for SOAP action '{}'", optEventType.get());
                protocolService.startScenarioProtocolEvent(optEventType.get());
            }
        }
    }

}
