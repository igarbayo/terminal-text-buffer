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

    @Test
    void bufferStartsWithDefaultAttributes() {
        assertEquals(CellAttributes.DEFAULT, buf.getAttributes());
    }

    @Test
    void setForegroundChangesOnlyForeground() {
        buf.setForeground(TerminalColor.RED);
        assertEquals(TerminalColor.RED, buf.getAttributes().getForeground());
        assertEquals(TerminalColor.DEFAULT, buf.getAttributes().getBackground());
        assertTrue(buf.getAttributes().getStyles().isEmpty());
    }

    @Test
    void setBackgroundChangesOnlyBackground() {
        buf.setBackground(TerminalColor.BLUE);
        assertEquals(TerminalColor.BLUE, buf.getAttributes().getBackground());
        assertEquals(TerminalColor.DEFAULT, buf.getAttributes().getForeground());
    }

    @Test
    void addStyleAddsBoldWithoutAffectingOthers() {
        buf.addStyle(TextStyle.BOLD);
        assertTrue(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertFalse(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    @Test
    void addMultipleStyles() {
        buf.addStyle(TextStyle.BOLD);
        buf.addStyle(TextStyle.UNDERLINE);
        assertTrue(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertTrue(buf.getAttributes().hasStyle(TextStyle.UNDERLINE));
        assertFalse(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    @Test
    void removeStyleRemovesOnlyThatStyle() {
        buf.addStyle(TextStyle.BOLD);
        buf.addStyle(TextStyle.ITALIC);
        buf.removeStyle(TextStyle.BOLD);
        assertFalse(buf.getAttributes().hasStyle(TextStyle.BOLD));
        assertTrue(buf.getAttributes().hasStyle(TextStyle.ITALIC));
    }

    @Test
    void resetAttributesRestoresDefault() {
        buf.setForeground(TerminalColor.RED);
        buf.setBackground(TerminalColor.BLUE);
        buf.addStyle(TextStyle.BOLD);
        buf.resetAttributes();
        assertEquals(CellAttributes.DEFAULT, buf.getAttributes());
    }

    @Test
    void setAttributesReplacesEntirely() {
        CellAttributes custom = new CellAttributes(TerminalColor.GREEN, TerminalColor.MAGENTA,
                java.util.EnumSet.of(TextStyle.ITALIC));
        buf.setAttributes(custom);
        assertEquals(custom, buf.getAttributes());
    }
}
