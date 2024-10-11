package de.xoev.xta.test.app.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class XtaIdTest {

    @Test
    void parseTest_messageIdIsCorrect_returnedOptionalIsPresent() {
        // given
        String messageId = "urn:de:xta:messageid:application:000ca2fe-f4e1-45c2-8233-3a0eb760bd16";

        // when
        Optional<XtaId> optId = XtaId.parse(messageId);

        // then
        assertTrue(optId.isPresent());
        XtaId xtaMessageId = optId.get();
        assertAll(
                () -> assertEquals("urn", xtaMessageId.getSchema(), "wrong schema"),
                () -> assertEquals("de:xta:messageid", xtaMessageId.getNamespace(), "wrong namespace"),
                () -> assertEquals("application", xtaMessageId.getApplication(), "wrong application"),
                () -> assertEquals("000ca2fe-f4e1-45c2-8233-3a0eb760bd16", xtaMessageId.getUuid(), "wrong uuid"));
    }

    @Test
    void parseTest_messageIdIsNotCorrect_returnedOptionalIsEmpty() {
        // given
        String messageId = "urn:application:000ca2fe-f4e1-45c2-8233-3a0eb760bd16";

        // when
        Optional<XtaId> optId = XtaId.parse(messageId);

        // then
        assertTrue(optId.isEmpty());
    }
}
