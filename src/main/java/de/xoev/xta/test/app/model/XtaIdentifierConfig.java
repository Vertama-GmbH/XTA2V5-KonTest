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
public class XtaIdentifierConfig {

    private String identifierScheme;

    private String name;

    private String value;

    private String organizationCategory;

    private String directory;
}
