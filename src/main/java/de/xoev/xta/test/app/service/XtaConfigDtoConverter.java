
package de.xoev.xta.test.app.service;

import genv5.de.xoev.transport.xta.core.x311.PartyWithIDType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.xoev.xta.test.app.config.MapStructConfiguration;
import de.xoev.xta.test.app.config.XtaConfig;
import de.xoev.xta.test.app.model.XtaBusinessScenarioConfig;
import de.xoev.xta.test.app.model.XtaConfigDto;
import de.xoev.xta.test.app.model.XtaIdentifierConfig;
import de.xoev.xta.test.app.model.XtaQualifierConfig;
import genv5.de.xoev.transport.xta.core.x311.CodeBusinessScenario;
import genv5.de.xoev.transport.xta.core.x311.PartyType;
import genv5.de.xoev.transport.xta.core.x311.QualifierType;

@Mapper(config = MapStructConfiguration.class)
public interface XtaConfigDtoConverter {

    XtaConfigDto fromModel(final XtaConfig model);

    @Mapping(target = "service", source = "service")
    @Mapping(target = "messageType.messageSchema", source = "messageTypeConfig.messageSchema")
    @Mapping(target = "messageType.value", source = "messageTypeConfig.value")
    @Mapping(target = "businessScenario.undefined", expression = "java(xtaQualifierConfig.getBuiBusinessScenarioConfig().isDefined()?null:xtaQualifierConfig.getBuiBusinessScenarioConfig().getUndefined())")
    @Mapping(target = "businessScenario.defined", expression = "java(!xtaQualifierConfig.getBuiBusinessScenarioConfig().isDefined()?null:fromModelDefined(xtaQualifierConfig.getBuiBusinessScenarioConfig()))")
    @Mapping(target = "id", expression = "java(de.xoev.xta.test.app.util.XmlIdGenerator.generateRandomXsId())")
    QualifierType fromModel(final XtaQualifierConfig xtaQualifierConfig);

    @Mapping(target = "name", ignore = true)
    CodeBusinessScenario fromModelDefined(final XtaBusinessScenarioConfig xtaBusinessScenarioConfig);

    @Mapping(target = "extensions", ignore = true)
    @Mapping(target = "directory", ignore = true)
    @Mapping(target = "identifier.identifierScheme", source = "identifierScheme")
    @Mapping(target = "identifier.organizationCategory", source = "organizationCategory")
    @Mapping(target = "identifier.name", source = "name")
    @Mapping(target = "identifier.value", source = "value")
    PartyType fromModel(final XtaIdentifierConfig xtaIdentifierConfig);

    @Mapping(target = "extensions", ignore = true)
    @Mapping(target = "directory", ignore = true)
    @Mapping(target = "identifier.identifierScheme", source = "identifierScheme")
    @Mapping(target = "identifier.organizationCategory", source = "organizationCategory")
    @Mapping(target = "identifier.name", source = "name")
    @Mapping(target = "identifier.value", source = "value")
    @Mapping(target = "id", expression = "java(de.xoev.xta.test.app.util.XmlIdGenerator.generateRandomXsId())")
    @Mapping(target = "certificate", ignore = true)
    PartyWithIDType fromModelWithId(final XtaIdentifierConfig xtaIdentifierConfig);

}
