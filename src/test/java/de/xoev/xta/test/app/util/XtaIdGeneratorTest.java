package de.xoev.xta.test.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class XtaIdGeneratorTest {

    @Test
    void generateTest() {
        // when
        XtaId xtaMessageId1 = XtaIdGenerator.generateMessageId();
        XtaId xtaMessageId2 = XtaIdGenerator.generateMessageId();

        // then
        assertNotNull(xtaMessageId1);
        assertNotNull(xtaMessageId2);
        assertEquals(xtaMessageId1.getSchema(), xtaMessageId2.getSchema());
        assertEquals(xtaMessageId1.getNamespace(), xtaMessageId2.getNamespace());
        assertEquals(xtaMessageId1.getApplication(), xtaMessageId2.getApplication());
        assertNotEquals(xtaMessageId1.getUuid(), xtaMessageId2.getUuid());
    }
}
