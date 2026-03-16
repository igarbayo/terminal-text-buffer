package com.terminal.buffer;

import java.util.Scanner;
import java.util.Set;

public class Main {

    private static TerminalBuffer buf = new TerminalBuffer(80, 24, 200);

    public static void main(String[] args) {
        printHelp();
        printBuffer();

        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                boolean changed = handleCommand(line);
                if (changed) printBuffer();
            }
        }
    }

    private static boolean handleCommand(String line) {
        String[] parts = line.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1] : "";

        try {
            switch (cmd) {
                case "write":
                    if (arg.isEmpty()) { err("write <texto>"); return false; }
                    buf.writeText(arg);
                    return true;

                case "insert":
                    if (arg.isEmpty()) { err("insert <texto>"); return false; }
                    buf.insertText(arg);
                    return true;

                case "fill": {
                    char ch = arg.isEmpty() ? ' ' : arg.charAt(0);
                    buf.fillLine(ch);
                    return true;
                }

                case "newline":
                    buf.insertEmptyLine();
                    return true;

                case "clear":
                    buf.clearScreen();
                    buf.setCursor(0, 0);
                    return true;

                case "clearall":
                    buf.clearAll();
                    buf.setCursor(0, 0);
                    return true;

                case "cursor": {
                    String[] xy = arg.split(" ");
                    if (xy.length < 2) { err("cursor <col> <row>"); return false; }
                    buf.setCursor(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
                    return true;
                }

                case "move": {
                    String[] mv = arg.split(" ");
                    String dir = mv[0].toLowerCase();
                    int n = mv.length > 1 ? Integer.parseInt(mv[1]) : 1;
                    switch (dir) {
                        case "up":    buf.moveCursorUp(n);    break;
                        case "down":  buf.moveCursorDown(n);  break;
                        case "left":  buf.moveCursorLeft(n);  break;
                        case "right": buf.moveCursorRight(n); break;
                        default: err("move up|down|left|right [n]"); return false;
                    }
                    return true;
                }

                case "fg":
                    if (arg.isEmpty()) { err("fg <COLOR>"); return false; }
                    buf.setForeground(TerminalColor.valueOf(arg.toUpperCase()));
                    return true;

                case "bg":
                    if (arg.isEmpty()) { err("bg <COLOR>"); return false; }
                    buf.setBackground(TerminalColor.valueOf(arg.toUpperCase()));
                    return true;

                case "bold":
                    buf.addStyle(TextStyle.BOLD);
                    return true;

                case "italic":
                    buf.addStyle(TextStyle.ITALIC);
                    return true;

                case "underline":
                    buf.addStyle(TextStyle.UNDERLINE);
                    return true;

                case "nostyle":
                    if (arg.isEmpty()) { err("nostyle BOLD|ITALIC|UNDERLINE"); return false; }
                    buf.removeStyle(TextStyle.valueOf(arg.toUpperCase()));
                    return true;

                case "reset":
                    buf.resetAttributes();
                    return true;

                case "resize": {
                    String[] wh = arg.split(" ");
                    if (wh.length < 2) { err("resize <w> <h>"); return false; }
                    buf.resize(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
                    return true;
                }

                case "print":
                    return true;

                case "scrollback":
                    printScrollback();
                    return false;

                case "info":
                    printInfo();
                    return false;

                case "codepoints":
                    if (arg.isEmpty()) { err("codepoints <texto>"); return false; }
                    for (int i = 0; i < arg.length(); ) {
                        int cp = arg.codePointAt(i);
                        System.out.printf("  U+%04X  isWide=%-5b  char=%s%n",
                                cp, cp >= 0x4E00 && cp <= 0x9FFF, new String(Character.toChars(cp)));
                        i += Character.charCount(cp);
                    }
                    System.out.print("> ");
                    System.out.flush();
                    return false;

                case "help":
                    printHelp();
                    return false;

                case "exit":
                case "quit":
                    System.out.println("Hasta luego.");
                    System.exit(0);
                    return false;

                default:
                    err("Comando desconocido: '" + cmd + "'. Escribe 'help' para ver los comandos.");
                    return false;
            }
        } catch (IllegalArgumentException e) {
            err("Error: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Print buffer
    // -------------------------------------------------------------------------

    private static void printBuffer() {
        int w = buf.getWidth();
        String top    = "\u250c" + repeat("\u2500", w) + "\u2510";
        String bottom = "\u2514" + repeat("\u2500", w) + "\u2518";

        System.out.println(top);
        CursorPosition cur = buf.getCursor();
        for (int row = 0; row < buf.getHeight(); row++) {
            StringBuilder sb = new StringBuilder("\u2502");
            CellAttributes prev = null;
            for (int col = 0; col < w; col++) {
                Cell cell = buf.getCell(col, row);
                CellAttributes attrs = cell.getAttributes();
                if (!attrs.equals(prev)) {
                    sb.append("\033[0m");
                    sb.append(ansiAttrs(attrs));
                    prev = attrs;
                }
                boolean isCursor = (col == cur.getCol() && row == cur.getRow());
                char ch = cell.getCharacter();
                if (isCursor) {
                    sb.append(ch == ' ' || ch == '\0' ? '\u258c' : ch);
                } else {
                    sb.append(ch == '\0' ? ' ' : ch);
                }
            }
            sb.append("\033[0m\u2502");
            System.out.println(sb);
        }
        System.out.println(bottom);

        CellAttributes attrs = buf.getAttributes();
        Set<TextStyle> styles = attrs.getStyles();
        System.out.printf("Cursor: (%d, %d)  fg=%-14s bg=%-14s estilos=[%s%s]  Scrollback: %d lineas%n",
                cur.getCol(), cur.getRow(),
                attrs.getForeground(),
                attrs.getBackground(),
                styles.isEmpty() ? "ninguno" : "",
                styles.isEmpty() ? "" : styleList(styles),
                buf.getScrollbackSize());
        System.out.print("> ");
        System.out.flush();
    }

    private static String styleList(Set<TextStyle> styles) {
        StringBuilder sb = new StringBuilder();
        for (TextStyle s : styles) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s.name());
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Print scrollback
    // -------------------------------------------------------------------------

    private static void printScrollback() {
        int size = buf.getScrollbackSize();
        if (size == 0) {
            System.out.println("--- Scrollback vacio ---");
            System.out.print("> ");
            System.out.flush();
            return;
        }
        System.out.println("--- Scrollback (" + size + " lineas, mas antigua primero) ---");
        for (int i = -size; i <= -1; i++) {
            String content = buf.getLine(i);
            System.out.printf("[%3d] %s%n", i, content);
        }
        System.out.print("> ");
        System.out.flush();
    }

    // -------------------------------------------------------------------------
    // Print info
    // -------------------------------------------------------------------------

    private static void printInfo() {
        CursorPosition cur = buf.getCursor();
        CellAttributes attrs = buf.getAttributes();
        System.out.println("Dimensiones : " + buf.getWidth() + " x " + buf.getHeight());
        System.out.println("Scrollback  : " + buf.getScrollbackSize() + " / " + buf.getMaxScrollback() + " lineas");
        System.out.println("Cursor      : col=" + cur.getCol() + " row=" + cur.getRow());
        System.out.println("Foreground  : " + attrs.getForeground());
        System.out.println("Background  : " + attrs.getBackground());
        Set<TextStyle> styles = attrs.getStyles();
        System.out.println("Estilos     : " + (styles.isEmpty() ? "ninguno" : styleList(styles)));
        System.out.print("> ");
        System.out.flush();
    }

    // -------------------------------------------------------------------------
    // Help
    // -------------------------------------------------------------------------

    private static void printHelp() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          Terminal Text Buffer — REPL                 ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Escritura                                            ║");
        System.out.println("║   write <texto>              Escribe en posicion     ║");
        System.out.println("║   insert <texto>             Inserta (hace shift)    ║");
        System.out.println("║   fill [char]                Rellena la linea actual ║");
        System.out.println("║   newline                    Inserta linea vacia     ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Cursor                                               ║");
        System.out.println("║   cursor <col> <row>         Mueve el cursor         ║");
        System.out.println("║   move up|down|left|right [n]  Mueve N posiciones    ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Atributos                                            ║");
        System.out.println("║   fg <COLOR>                 Color de texto          ║");
        System.out.println("║   bg <COLOR>                 Color de fondo          ║");
        System.out.println("║   bold | italic | underline  Activa estilo           ║");
        System.out.println("║   nostyle BOLD|ITALIC|UNDERLINE  Desactiva estilo    ║");
        System.out.println("║   reset                      Resetea atributos       ║");
        System.out.println("║ Colores: DEFAULT RED GREEN YELLOW BLUE MAGENTA       ║");
        System.out.println("║          CYAN WHITE BLACK  (y BRIGHT_* variantes)    ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Pantalla                                             ║");
        System.out.println("║   clear                      Limpia la pantalla      ║");
        System.out.println("║   clearall                   Limpia pantalla+scrollb ║");
        System.out.println("║   resize <w> <h>             Redimensiona el buffer  ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Info                                                 ║");
        System.out.println("║   print                      Repinta el buffer       ║");
        System.out.println("║   scrollback                 Muestra el scrollback   ║");
        System.out.println("║   info                       Estado actual           ║");
        System.out.println("║   help                       Muestra esta ayuda      ║");
        System.out.println("║   exit / quit                Salir                   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void err(String msg) {
        System.out.println("  [!] " + msg);
        System.out.print("> ");
        System.out.flush();
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    private static String ansiAttrs(CellAttributes attrs) {
        StringBuilder sb = new StringBuilder();
        if (attrs.hasStyle(TextStyle.BOLD))      sb.append("\033[1m");
        if (attrs.hasStyle(TextStyle.ITALIC))    sb.append("\033[3m");
        if (attrs.hasStyle(TextStyle.UNDERLINE)) sb.append("\033[4m");
        String fg = ansiColor(attrs.getForeground(), false);
        if (!fg.isEmpty()) sb.append(fg);
        String bg = ansiColor(attrs.getBackground(), true);
        if (!bg.isEmpty()) sb.append(bg);
        return sb.toString();
    }

    private static String ansiColor(TerminalColor color, boolean background) {
        int base = background ? 40 : 30;
        switch (color) {
            case BLACK:          return "\033[" + base + "m";
            case RED:            return "\033[" + (base + 1) + "m";
            case GREEN:          return "\033[" + (base + 2) + "m";
            case YELLOW:         return "\033[" + (base + 3) + "m";
            case BLUE:           return "\033[" + (base + 4) + "m";
            case MAGENTA:        return "\033[" + (base + 5) + "m";
            case CYAN:           return "\033[" + (base + 6) + "m";
            case WHITE:          return "\033[" + (base + 7) + "m";
            case BRIGHT_BLACK:   return "\033[" + (base + 60) + "m";
            case BRIGHT_RED:     return "\033[" + (base + 61) + "m";
            case BRIGHT_GREEN:   return "\033[" + (base + 62) + "m";
            case BRIGHT_YELLOW:  return "\033[" + (base + 63) + "m";
            case BRIGHT_BLUE:    return "\033[" + (base + 64) + "m";
            case BRIGHT_MAGENTA: return "\033[" + (base + 65) + "m";
            case BRIGHT_CYAN:    return "\033[" + (base + 66) + "m";
            case BRIGHT_WHITE:   return "\033[" + (base + 67) + "m";
            default:             return "";
        }
    }
}
