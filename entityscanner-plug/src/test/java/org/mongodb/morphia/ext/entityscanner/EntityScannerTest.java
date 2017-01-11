package org.mongodb.morphia.ext.entityscanner;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author us@thomas-daily.de
 */
public class EntityScannerTest {
    @Test
    public void testScanning() throws Exception {
        final Morphia m = new Morphia();
        assertFalse(m.isMapped(E.class));
        new EntityScanner(m, s -> Objects.equals(s, E.class.getName() + ".class"));
        assertTrue(m.isMapped(E.class));
        assertFalse(m.isMapped(F.class));
        new EntityScanner(m, input -> input.startsWith(EntityScannerTest.class.getPackage().getName()));
        assertTrue(m.isMapped(F.class));

    }

    @Entity
    private static class E {
        @Id
        private ObjectId id;
    }

    @Entity
    private static class F {
        @Id
        private ObjectId id;
    }
}
