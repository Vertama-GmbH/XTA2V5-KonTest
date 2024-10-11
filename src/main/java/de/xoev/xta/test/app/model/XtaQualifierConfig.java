/*
 * Created 2022-12-15
 */
package de.xoev.xta.test.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class XtaQualifierConfig {

    private XtaBusinessScenarioConfig buiBusinessScenarioConfig;
    private XtaMessageTypeConfig messageTypeConfig;
    private String service;
}
