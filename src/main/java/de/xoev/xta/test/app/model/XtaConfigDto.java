/*
 * Created 2022-12-14
 */
package de.xoev.xta.test.app.model;

import de.xoev.xta.test.app.config.ClientProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class XtaConfigDto {

    private ProtocolMetadata protocolMetadata;
    private ClientProperties clientProperties;
    private XtaIdentifierConfig partyIdentifierConfig;
    private XtaIdentifierConfig authorIdentifierConfig;
    private XtaIdentifierConfig readerIdentifierConfig;
    private XtaIdentifierConfig senderIdentifierConfig;
    private XtaQualifierConfig qualifierConfig;
}
