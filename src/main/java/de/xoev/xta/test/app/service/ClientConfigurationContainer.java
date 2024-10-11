/*
 * Created 2023-03-15
 */
package de.xoev.xta.test.app.service;

import java.util.Collection;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.exception.XtaTesterException;
import de.xoev.xta.test.app.service.interceptor.ClientProtocolFaultInterceptor;
import de.xoev.xta.test.app.service.interceptor.ClientProtocolInInterceptor;
import de.xoev.xta.test.app.service.interceptor.ClientProtocolOutInterceptor;
import genv5.de.xoev.transport.xta.core.x311.CoreReader;
import genv5.de.xoev.transport.xta.core.x311.CoreRecipient;
import genv5.de.xoev.transport.xta.core.x311.CoreSender;
import genv5.de.xoev.transport.xta.core.x311.CoreTransportBase;
import genv5.de.xoev.transport.xta.core.x311.XTACore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ClientConfigurationContainer {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ClientProtocolInInterceptor clientProtocolInInterceptor;

    @Autowired
    private ClientProtocolFaultInterceptor clientProtocolFaultOutInterceptor;

    @Autowired
    private ClientProtocolOutInterceptor clientProtocolOutInterceptor;

    @Value("${app.logRequestResponse}")
    private boolean logRequestResponse;

    Collection<Interceptor<? extends Message>> getDefaultInInterceptors() {
        return logRequestResponse ? List.of(clientProtocolInInterceptor, new LoggingInInterceptor())
                : List.of(clientProtocolInInterceptor);
    }

    Collection<Interceptor<? extends Message>> getDefaultFaultOutInterceptors() {
        return logRequestResponse ? List.of(clientProtocolFaultOutInterceptor, new LoggingOutInterceptor())
                : List.of(clientProtocolFaultOutInterceptor);
    }

    Collection<Interceptor<? extends Message>> getDefaultOutInterceptors() {
        return logRequestResponse ? List.of(clientProtocolOutInterceptor, new LoggingOutInterceptor())
                : List.of(clientProtocolOutInterceptor);
    }

    private XTACore getStub() {
        return new XTACore(XTACore.WSDL_LOCATION);
    }

    public CoreReader getReaderPortClient() throws XtaTesterException {
        final CoreReader readerPortType = getStub().getReader();
        final Client client = ClientProxy.getClient(readerPortType);
        setClientProperties(client,
                configurationService.getXtaConfig().getClientProperties().getServerUrl().getReaderPort());
        return readerPortType;
    }

    public CoreRecipient getRecipientPortClient() throws XtaTesterException {
        final CoreRecipient recipientPortType = getStub().getRecipient();
        final Client client = ClientProxy.getClient(recipientPortType);
        setClientProperties(client,
                configurationService.getXtaConfig().getClientProperties().getServerUrl().getRecipientPort());
        return recipientPortType;
    }

    public CoreSender getSendPortClient() throws XtaTesterException {
        final CoreSender sendPortType = getStub().getSender();
        final Client client = ClientProxy.getClient(sendPortType);
        setClientProperties(client,
                configurationService.getXtaConfig().getClientProperties().getServerUrl().getSenderPort());
        return sendPortType;
    }

    public CoreTransportBase getTransportPortClient() throws XtaTesterException {
        final CoreTransportBase transportPortType = getStub().getTransportBase();
        final Client client = ClientProxy.getClient(transportPortType);
        setClientProperties(client,
                configurationService.getXtaConfig().getClientProperties().getServerUrl().getTransportBasePort());
        return transportPortType;
    }

    private void setClientProperties(final Client client, final String url) throws XtaTesterException {
        // set remote endpoint
        client.getRequestContext().put(Message.ENDPOINT_ADDRESS, url);
        client.getInInterceptors().addAll(getDefaultInInterceptors());
        client.getOutFaultInterceptors().addAll(getDefaultFaultOutInterceptors());
        client.getInFaultInterceptors().addAll(getDefaultFaultOutInterceptors());
        client.getOutInterceptors().addAll(getDefaultOutInterceptors());
    }
}
