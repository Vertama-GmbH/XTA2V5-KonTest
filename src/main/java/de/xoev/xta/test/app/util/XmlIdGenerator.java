package de.xoev.xta.test.app.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class XmlIdGenerator {

    public static String generateRandomXsId() {
        return "id-" + UUID.randomUUID();
    }

}
