package de.xoev.xta.test.app.config;

import java.util.Collection;
import java.util.List;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.xoev.xta.test.app.service.interceptor.ServerProtocolFaultInterceptor;
import de.xoev.xta.test.app.service.interceptor.ServerProtocolInInterceptor;
import de.xoev.xta.test.app.service.interceptor.ServerProtocolOutInterceptor;
import genv5.de.xoev.transport.xta.core.x311.CoreReader;
import genv5.de.xoev.transport.xta.core.x311.CoreRecipient;
import genv5.de.xoev.transport.xta.core.x311.CoreSender;
import genv5.de.xoev.transport.xta.core.x311.CoreTransportBase;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.soap.SOAPBinding;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class XTAServer {

    private final SpringBus bus;

    private final ServerProtocolInInterceptor serverProtocolInInterceptor;

    private final ServerProtocolFaultInterceptor serverProtocolFaultInterceptor;

    private final ServerProtocolOutInterceptor serverProtocolOutInterceptor;

    @Value("${app.logRequestResponse}")
    private boolean logRequestResponse;

    public XTAServer(final SpringBus bus, final ServerProtocolInInterceptor serverProtocolInInterceptor,
            final ServerProtocolFaultInterceptor serverProtocolFaultInterceptor,
            final ServerProtocolOutInterceptor serverProtocolOutInterceptor) {
        this.bus = bus;
        this.serverProtocolInInterceptor = serverProtocolInInterceptor;
        this.serverProtocolFaultInterceptor = serverProtocolFaultInterceptor;
        this.serverProtocolOutInterceptor = serverProtocolOutInterceptor;
    }

    Collection<Interceptor<? extends Message>> getDefaultInInterceptors() {
        return logRequestResponse ? List.of(serverProtocolInInterceptor, new LoggingInInterceptor())
                : List.of(serverProtocolInInterceptor);
    }

    Collection<Interceptor<? extends Message>> getDefaultFaultOutInterceptors() {
        return logRequestResponse ? List.of(serverProtocolFaultInterceptor, new LoggingOutInterceptor())
                : List.of(serverProtocolFaultInterceptor);
    }

    Collection<Interceptor<? extends Message>> getDefaultOutInterceptors() {
        return logRequestResponse ? List.of(serverProtocolOutInterceptor, new LoggingOutInterceptor())
                : List.of(serverProtocolOutInterceptor);
    }

    @Bean
    Endpoint readerEndpoint(final CoreReader readerPortType) {
        final EndpointImpl endpoint = new EndpointImpl(bus, readerPortType);
        endpoint.publish("/XTACore/Reader");
        configureEndpoint(endpoint);
        return endpoint;
    }

    @Bean
    Endpoint recipientEndpoint(final CoreRecipient recipientPortType) {
        final EndpointImpl endpoint = new EndpointImpl(bus, recipientPortType);
        endpoint.publish("/XTACore/Recipient");
        configureEndpoint(endpoint);
        return endpoint;
    }

    @Bean
    Endpoint senderEndpoint(final CoreSender sendPortType) {
        final EndpointImpl endpoint = new EndpointImpl(bus, sendPortType);
        endpoint.publish("/XTACore/Sender");
        configureEndpoint(endpoint);
        return endpoint;
    }

    @Bean
    Endpoint transportBaseEndpoint(final CoreTransportBase transportBasePortType) {
        final EndpointImpl endpoint = new EndpointImpl(bus, transportBasePortType);
        endpoint.publish("/XTACore/TransportBase");
        configureEndpoint(endpoint);
        return endpoint;
    }

    private void configureEndpoint(final EndpointImpl endpoint) {
        log.debug("configuring endpoint {}", endpoint.getBeanName());
        final Collection<Interceptor<? extends Message>> defaultFaultOutInterceptor = getDefaultFaultOutInterceptors();
        final Collection<Interceptor<? extends Message>> defaultOutInterceptor = getDefaultOutInterceptors();
        final Collection<Interceptor<? extends Message>> defaultInInterceptor = getDefaultInInterceptors();
        endpoint.getInInterceptors().addAll(defaultInInterceptor);
        endpoint.getOutFaultInterceptors().addAll(defaultFaultOutInterceptor);
        endpoint.getInFaultInterceptors().addAll(defaultFaultOutInterceptor);
        endpoint.getOutInterceptors().addAll(defaultOutInterceptor);
        final SOAPBinding binding = (SOAPBinding) endpoint.getBinding();
        binding.setMTOMEnabled(true);
    }
}
