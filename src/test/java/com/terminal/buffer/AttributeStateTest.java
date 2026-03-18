// SPDX-FileCopyrightText: 2026 Ignacio Garbayo Fernández <ignacio.garbayo@rai.usc.es>
// SPDX-License-Identifier: MIT

package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeStateTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(10, 5, 100);
    }

    // A freshly created buffer must report DEFAULT attributes (no color, no styles).
    @Test
    void bufferStartsWithDefaultAttributes() {
        assertEquals(CellAttributes.DEFAULT, buf.getAttributes());
    }

    // Setting the foreground must not affect the background or style set.
    @Test
    void setForegroundChangesOnlyForeground() {
        buf.setForeground(TerminalColor.RED);
        assertEquals(TerminalColor.RED, buf.getAttributes().getForeground());
        assertEquals(TerminalColor.DEFAULT, buf.getAttributes().getBackground());
        assertTrue(buf.getAttributes().getStyles().isEmpty());
    }

    // Setting the background must not affect the foreground.
    @Test
    void setBackgroundChangesOnlyBackground() {
        buf.setBackground(TerminalColor.BLUE);
        assertEquals(TerminalColor.BLUE, buf.getAttributes().getBackground());
        assertEquals(TerminalColor.DEFAULT, buf.getAttributes().getForeground());
    }

    // Adding BOLD must not activate unrelated styles such as ITALIC.
    @Test
    void addStyleAddsBoldWithoutAffectingOthers() {
        buf.addStyle(TextStyle.BOLD);
        assertTrue(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertFalse(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    // Multiple styles can coexist independently in the same attribute state.
    @Test
    void addMultipleStyles() {
        buf.addStyle(TextStyle.BOLD);
        buf.addStyle(TextStyle.UNDERLINE);
        assertTrue(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertTrue(buf.getAttributes().hasStyle(TextStyle.UNDERLINE));
        assertFalse(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    // Removing one style must leave other active styles unchanged.
    @Test
    void removeStyleRemovesOnlyThatStyle() {
        buf.addStyle(TextStyle.BOLD);
        buf.addStyle(TextStyle.ITALIC);
        buf.removeStyle(TextStyle.BOLD);
        assertFalse(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertTrue(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    // resetAttributes must restore the attribute state to exactly DEFAULT.
    @Test
    void resetAttributesRestoresDefault() {
        buf.setForeground(TerminalColor.RED);
        buf.setBackground(TerminalColor.BLUE);
        buf.addStyle(TextStyle.BOLD);
        buf.resetAttributes();
        assertEquals(CellAttributes.DEFAULT, buf.getAttributes());
    }

    // setAttributes must replace the entire current attribute state.
    @Test
    void setAttributesReplacesEntirely() {
        CellAttributes custom = new CellAttributes(TerminalColor.GREEN, TerminalColor.MAGENTA,
                java.util.EnumSet.of(TextStyle.ITALIC));
        buf.setAttributes(custom);
        assertEquals(custom, buf.getAttributes());
    }
}
