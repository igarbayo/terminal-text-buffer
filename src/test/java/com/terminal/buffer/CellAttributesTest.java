// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CellAttributesTest {

    @Test
    void defaultAttributesHaveDefaultColors() {
        CellAttributes attrs = CellAttributes.DEFAULT;
        assertEquals(TerminalColor.DEFAULT, attrs.getForeground());
        assertEquals(TerminalColor.DEFAULT, attrs.getBackground());
        assertTrue(attrs.getStyles().isEmpty());
    }

    @Test
    void constructorStoresValues() {
        CellAttributes attrs = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE,
                EnumSet.of(TextStyle.BOLD));
        assertEquals(TerminalColor.RED, attrs.getForeground());
        assertEquals(TerminalColor.BLUE, attrs.getBackground());
        assertTrue(attrs.hasStyle(TextStyle.BOLD));
        assertFalse(attrs.hasStyle(TextStyle.ITALIC));
    }

    @Test
    void constructorDefensivelyCopiesStyles() {
        EnumSet<TextStyle> original = EnumSet.of(TextStyle.BOLD);
        CellAttributes attrs = new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, original);
        original.add(TextStyle.ITALIC); // mutate original after construction
        assertFalse(attrs.hasStyle(TextStyle.ITALIC), "mutation of source set must not affect stored styles");
    }

    @Test
    void getStylesReturnsUnmodifiableView() {
        CellAttributes attrs = new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT,
                EnumSet.of(TextStyle.BOLD));
        Set<TextStyle> styles = attrs.getStyles();
        assertThrows(UnsupportedOperationException.class, () -> styles.add(TextStyle.ITALIC));
    }

    @Test
    void withForegroundReturnsNewInstanceWithUpdatedColor() {
        CellAttributes original = CellAttributes.DEFAULT;
        CellAttributes updated = original.withForeground(TerminalColor.RED);
        assertEquals(TerminalColor.RED, updated.getForeground());
        assertEquals(TerminalColor.DEFAULT, updated.getBackground());
        assertEquals(TerminalColor.DEFAULT, original.getForeground(), "original must be unchanged");
    }

    @Test
    void withBackgroundReturnsNewInstanceWithUpdatedColor() {
        CellAttributes original = CellAttributes.DEFAULT;
        CellAttributes updated = original.withBackground(TerminalColor.GREEN);
        assertEquals(TerminalColor.GREEN, updated.getBackground());
        assertEquals(TerminalColor.DEFAULT, original.getBackground(), "original must be unchanged");
    }

    @Test
    void withStyleAddsStyleWithoutAffectingOriginal() {
        CellAttributes original = CellAttributes.DEFAULT;
        CellAttributes updated = original.withStyle(TextStyle.BOLD);
        assertTrue(updated.hasStyle(TextStyle.BOLD));
        assertFalse(original.hasStyle(TextStyle.BOLD), "original must be unchanged");
    }

    @Test
    void withStyleDoesNotDuplicateExistingStyle() {
        CellAttributes attrs = CellAttributes.DEFAULT.withStyle(TextStyle.BOLD).withStyle(TextStyle.BOLD);
        assertEquals(1, attrs.getStyles().size());
    }

    @Test
    void withoutStyleRemovesStyleWithoutAffectingOriginal() {
        CellAttributes original = CellAttributes.DEFAULT.withStyle(TextStyle.BOLD).withStyle(TextStyle.ITALIC);
        CellAttributes updated = original.withoutStyle(TextStyle.BOLD);
        assertFalse(updated.hasStyle(TextStyle.BOLD));
        assertTrue(updated.hasStyle(TextStyle.ITALIC));
        assertTrue(original.hasStyle(TextStyle.BOLD), "original must be unchanged");
    }

    @Test
    void equalAttributesAreEqual() {
        CellAttributes a = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE, EnumSet.of(TextStyle.BOLD));
        CellAttributes b = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE, EnumSet.of(TextStyle.BOLD));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentForegroundMakesNotEqual() {
        CellAttributes a = new CellAttributes(TerminalColor.RED, TerminalColor.DEFAULT,
                EnumSet.noneOf(TextStyle.class));
        CellAttributes b = new CellAttributes(TerminalColor.GREEN, TerminalColor.DEFAULT,
                EnumSet.noneOf(TextStyle.class));
        assertNotEquals(a, b);
    }

    @Test
    void differentStylesMakesNotEqual() {
        CellAttributes a = CellAttributes.DEFAULT.withStyle(TextStyle.BOLD);
        CellAttributes b = CellAttributes.DEFAULT.withStyle(TextStyle.ITALIC);
        assertNotEquals(a, b);
    }

    @Test
    void toStringContainsColorAndStyleInfo() {
        CellAttributes attrs = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE,
                EnumSet.of(TextStyle.BOLD));
        String str = attrs.toString();
        assertTrue(str.contains("RED"));
        assertTrue(str.contains("BLUE"));
        assertTrue(str.contains("BOLD"));
    }
}
