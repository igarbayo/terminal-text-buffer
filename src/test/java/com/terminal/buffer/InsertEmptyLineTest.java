package com.terminal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertEmptyLineTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(5, 3, 100);
    }

    @Test
    void insertEmptyLineShiftsContentUp() {
        buf.setCursor(0, 0);
        buf.writeText("AAAAA");
        buf.setCursor(0, 1);
        buf.writeText("BBBBB");
        buf.setCursor(0, 2);
        buf.writeText("CCCCC");

        buf.insertEmptyLine();

        // Old row 0 → scrollback; old row 1 → row 0; old row 2 → row 1; new blank → row 2
        assertEquals("BBBBB", buf.getLine(0));
        assertEquals("CCCCC", buf.getLine(1));
        assertEquals("", buf.getLine(2));
    }

    @Test
    void insertEmptyLinePushesTopRowToScrollback() {
        buf.setCursor(0, 0);
        buf.writeText("Hello");
        buf.insertEmptyLine();

        assertEquals(1, buf.getScrollbackSize());
        assertEquals("Hello", buf.getLine(-1));
    }

    @Test
    void scrollbackSizeIncrementsWithEachInsert() {
        for (int i = 0; i < 5; i++) {
            buf.insertEmptyLine();
        }
        assertEquals(5, buf.getScrollbackSize());
    }

    @Test
    void scrollbackEvictsOldestWhenFull() {
        buf = new TerminalBuffer(5, 1, 3); // maxScrollback=3
        buf.setCursor(0, 0);
        buf.writeText("A");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("B");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("C");
        buf.insertEmptyLine();
        buf.setCursor(0, 0);
        buf.writeText("D");
        buf.insertEmptyLine(); // 'A' should be evicted now

        assertEquals(3, buf.getScrollbackSize());
        assertEquals("D", buf.getLine(-1));
        assertEquals("C", buf.getLine(-2));
        assertEquals("B", buf.getLine(-3));
    }

    @Test
    void insertEmptyLineWithZeroMaxScrollbackDiscardsRow() {
        buf = new TerminalBuffer(5, 2, 0);
        buf.setCursor(0, 0);
        buf.writeText("Lost");
        buf.insertEmptyLine();
        assertEquals(0, buf.getScrollbackSize());
    }

    @Test
    void newBottomRowIsBlank() {
        buf.insertEmptyLine();
        assertEquals("", buf.getLine(buf.getHeight() - 1));
    }
}
