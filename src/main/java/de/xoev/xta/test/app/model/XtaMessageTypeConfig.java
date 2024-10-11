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
public class XtaMessageTypeConfig {

    private String messageSchema;
    private String value;
}
