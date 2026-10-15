package org.unitedlands.utils;

final class UnitedFrame {

    record Style(int width, boolean monospace, String color, String leftCap, String rightCap,
                 String titleOpen, String titleClose, String footerCenter) {}

    private UnitedFrame() {}

    static String build(String title, String titlePlain, String body, Style style) {
        return header(title, titlePlain, style) + "\n" + body + "\n" + footer(style);
    }

    static String header(String title, String titlePlain, Style s) {
        var fixed = width(s.leftCap(), false, s) + width(s.titleOpen(), false, s)
                + width(" ", false, s) + width(titlePlain, true, s) + width(" ", false, s)
                + width(s.titleClose(), false, s) + width(s.rightCap(), false, s);

        var fill = split(s.width() - fixed, s);

        return "<color:" + s.color() + "><st>" + s.leftCap() + fill(fill[0], s) + "</st>" + s.titleOpen() + "</color>"
                + " <b>" + title + "</b> "
                + "<color:" + s.color() + ">" + s.titleClose() + "<st>" + fill(fill[1], s) + s.rightCap() + "</st></color>";
    }

    static String footer(Style s) {
        var fixed = width(s.leftCap(), false, s) + width(s.footerCenter(), false, s) + width(s.rightCap(), false, s);
        var fill  = split(s.width() - fixed, s);

        return "<color:" + s.color() + "><st>" + s.leftCap() + fill(fill[0], s) + s.footerCenter()
                + fill(fill[1], s) + s.rightCap() + "</st></color>";
    }

    private static int width(String plain, boolean bold, Style s) {
        if (s.monospace())
            return plain.codePointCount(0, plain.length());

        var total = 0;
        for (var c : plain.toCharArray())
            total += glyph(c) + 1 + (bold ? 1 : 0);

        return total;
    }

    private static int[] split(int total, Style s) {
        if (total <= 0)
            return new int[]{ 0, 0 };

        if (s.monospace())
            return new int[]{ total / 2, total - total / 2 };

        for (var a = total / 2; a >= 0; a--)
            if (fillable(a) && fillable(total - a))
                return new int[]{ a, total - a };

        return new int[]{ total / 2, total - total / 2 };
    }

    private static boolean fillable(int px) {
        return px >= 5 * (px % 4);
    }

    private static String fill(int px, Style s) {
        if (s.monospace())
            return " ".repeat(px);

        while (px > 0 && !fillable(px))
            px--;

        var bold   = px % 4;
        var normal = (px - bold * 5) / 4;

        return " ".repeat(normal) + (bold > 0 ? "<b>" + " ".repeat(bold) + "</b>" : "");
    }

    private static int glyph(char c) {
        return switch (c) {
            case '!', ',', '.', ':', ';', 'i', '|', '\'' -> 1;
            case '`', 'l' -> 2;
            case ' ', 'I', 't', '(', ')', '*', '[', ']', '{', '}', '"' -> 3;
            case 'f', 'k', '<', '>' -> 4;
            case '@', '~' -> 6;
            default -> 5;
        };
    }

}