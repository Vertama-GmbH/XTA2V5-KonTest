
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package de.xoev.xta.test.app.server;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import de.xoev.xta.test.app.util.XmlIdGenerator;
import genv5.de.xoev.transport.xta.core.x311.NotImplementedException;
import genv5.de.xoev.transport.xta.core.x311.ReportResult;
import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import de.xoev.xta.test.app.service.CustomizationService;
import de.xoev.xta.test.app.service.ParameterValidatorService;
import de.xoev.xta.test.app.service.ProtocolService;
import de.xoev.xta.test.app.service.UtilComponent;
import de.xoev.xta.test.app.service.XtaParameter;
import genv5.de.xoev.transport.xta.core.x311.AdditionalReportListType;
import genv5.de.xoev.transport.xta.core.x311.AdditionalReportListType.Report;
import genv5.de.xoev.transport.xta.core.x311.CodeRecordType;
import genv5.de.xoev.transport.xta.core.x311.CodeReportType;
import genv5.de.xoev.transport.xta.core.x311.CoreTransportBase;
import genv5.de.xoev.transport.xta.core.x311.InvalidMessageIDException;
import genv5.de.xoev.transport.xta.core.x311.MessageMetaDataType;
import genv5.de.xoev.transport.xta.core.x311.MessageStatusType;
import genv5.de.xoev.transport.xta.core.x311.MessageStatusType.InfoList;
import genv5.de.xoev.transport.xta.core.x311.ParameterNotSupportedException;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.PermissionDeniedException;
import genv5.de.xoev.transport.xta.core.x311.RecordType;
import genv5.de.xoev.transport.xta.core.x311.TechnicalProblemException;
import genv5.de.xoev.transport.xta.core.x311.TransportReportType;
import genv5.de.xoev.transport.xta.core.x311.UnsupportedExtensionException;
import genv5.de.xoev.transport.xta.core.x311.XTAMessageIDType;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;
import jakarta.annotation.Generated;
import lombok.extern.log4j.Log4j2;

@jakarta.jws.WebService(serviceName = "XTACore", portName = "transport.base", targetNamespace = "https://xoev.de/transport/xta/core/3.1.1", wsdlLocation = "classpath:wsdl/wsdl/xta-core.wsdl", endpointInterface = "genv5.de.xoev.transport.xta.core.x311.CoreTransportBase")
@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
@Component
@Log4j2
public class TransportBaseImpl implements CoreTransportBase {

    private final ParameterValidatorService validator;

    private final CustomizationService customizationService;

    private final ProtocolService protocolService;

    private final UtilComponent utilComponent;

    public TransportBaseImpl(final ParameterValidatorService validator, final CustomizationService customizationService,
            final ProtocolService protocolService, final UtilComponent utilComponent) {
        this.validator = validator;
        this.customizationService = customizationService;
        this.protocolService = protocolService;
        this.utilComponent = utilComponent;
    }

    /*
     * (non-Javadoc)
     * @see
     * genv5.de.xoev.transport.xta.core.x311.CoreTransportBase#checkAccountActive(genv5.de.xoev.transport.xta.core.x311.
     * PartyType party)*
     */
    @Override
    @Generated(value = "org.apache.cxf.tools.wsdlto.WSDLToJava", date = "2023-05-22T12:07:21.644+02:00")
    public void checkAccountActive(final PartyType party) throws TechnicalProblemException,
            ParameterNotSupportedException, PermissionDeniedException, UnsupportedExtensionException {
        log.info("Executing operation checkAccountActive");
        log.info("checkAccountActive called, author: {}", party);
    }

    @Override
    public ReportResult getTransportReport(final XTAMessageIDType xtaMessageID, final PartyType party)
            throws TechnicalProblemException, NotImplementedException, ParameterNotSupportedException,
            UnsupportedExtensionException, InvalidMessageIDException, PermissionDeniedException {

        log.info("Executing getTransportReport operation");

        final String messageId = xtaMessageID.getValue();
        final Optional<String> expectedMessageId = protocolService.getParameter(XtaParameter.MESSAGE_ID, String.class);
        if (validator.isNotBlank(messageId, XtaParameter.MESSAGE_ID) && expectedMessageId.isPresent()) {
            validator.isEquals(expectedMessageId.get(), messageId, XtaParameter.MESSAGE_ID);
        }

        final TransportReportType transportReport = new TransportReportType();
        transportReport.setReportTime(utilComponent.createGC(0));
        transportReport.setXTAServerIdentity("xta-tester");
        transportReport.setId(XmlIdGenerator.generateRandomXsId());

        final MessageMetaDataType messageMetaData = new MessageMetaDataType();
        transportReport.setMessageMetaData(messageMetaData);
        utilComponent.createMessageMetaData(messageMetaData, messageId);

        final MessageStatusType messageStatusType = new MessageStatusType();
        final InfoList infoList = new InfoList();
        final RecordType recordType = new RecordType();
        recordType.setReason("All fine");
        recordType.setTimestamp(utilComponent.createGC(0));
        final CodeRecordType codeRecordType = new CodeRecordType();
        codeRecordType.setCode("INFO");
        codeRecordType.setListURI("http://INFO");
        codeRecordType.setListVersionID("1");
        codeRecordType.setName(null);
        recordType.setCode(codeRecordType);
        infoList.getInfo().add(recordType);
        messageStatusType.setInfoList(infoList);
        messageStatusType.setStatus(BigInteger.ZERO);
        transportReport.setMessageStatus(messageStatusType);

        final Report additionalReport = new Report();
        final CodeReportType codeReportType = new CodeReportType();
        codeReportType.setCode("INFO");
        codeReportType.setListURI("http://INFO");
        codeReportType.setListVersionID("1");
        codeReportType.setName(null);
        additionalReport.setKey(codeReportType);

        try {
            final ClassPathResource classPathResource = new ClassPathResource("static/kosit_favicon.ico");

            final DataSource source = new URLDataSource(classPathResource.getURL());
            final DataHandler dh = new DataHandler(source);
            additionalReport.setData(dh);
        } catch (final IOException e) {
            throw new TechnicalProblemException(e.getMessage());
        }

        final AdditionalReportListType additionalReportListType = new AdditionalReportListType();
        additionalReportListType.getReport().add(additionalReport);
        transportReport.setAdditionalReports(additionalReportListType);

        final var result = new ReportResult();
        result.setTransportReport(transportReport);

        return result;
    }

}
