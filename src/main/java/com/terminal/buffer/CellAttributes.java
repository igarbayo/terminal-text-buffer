package com.terminal.buffer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class CellAttributes {

    public static final CellAttributes DEFAULT =
            new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, EnumSet.noneOf(TextStyle.class));

    private final TerminalColor foreground;
    private final TerminalColor background;
    private final EnumSet<TextStyle> styles;

    public CellAttributes(TerminalColor foreground, TerminalColor background, Set<TextStyle> styles) {
        this.foreground = foreground;
        this.background = background;
        this.styles = styles.isEmpty() ? EnumSet.noneOf(TextStyle.class) : EnumSet.copyOf(styles);
    }

    public TerminalColor getForeground() {
        return foreground;
    }

    public TerminalColor getBackground() {
        return background;
    }

    public Set<TextStyle> getStyles() {
        return Collections.unmodifiableSet(styles);
    }

    public boolean hasStyle(TextStyle style) {
        return styles.contains(style);
    }

    public CellAttributes withForeground(TerminalColor fg) {
        return new CellAttributes(fg, background, styles);
    }

    public CellAttributes withBackground(TerminalColor bg) {
        return new CellAttributes(foreground, bg, styles);
    }

    public CellAttributes withStyle(TextStyle style) {
        EnumSet<TextStyle> newStyles = EnumSet.copyOf(styles.isEmpty() ? EnumSet.noneOf(TextStyle.class) : styles);
        newStyles.add(style);
        return new CellAttributes(foreground, background, newStyles);
    }

    public CellAttributes withoutStyle(TextStyle style) {
        EnumSet<TextStyle> newStyles = styles.isEmpty() ? EnumSet.noneOf(TextStyle.class) : EnumSet.copyOf(styles);
        newStyles.remove(style);
        return new CellAttributes(foreground, background, newStyles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellAttributes)) return false;
        CellAttributes that = (CellAttributes) o;
        return foreground == that.foreground &&
                background == that.background &&
                styles.equals(that.styles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }

    @Override
    public String toString() {
        return "CellAttributes{fg=" + foreground + ", bg=" + background + ", styles=" + styles + "}";
    }
}
