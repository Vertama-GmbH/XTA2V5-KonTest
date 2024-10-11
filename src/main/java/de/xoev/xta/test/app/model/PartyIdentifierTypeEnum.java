/*
 * Created 2022-04-28
 */
package de.xoev.xta.test.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PartyIdentifierTypeEnum {

    XOEV("xoev"), JUSTIZ("justiz");

    @Getter
    private String kennnung;
}
