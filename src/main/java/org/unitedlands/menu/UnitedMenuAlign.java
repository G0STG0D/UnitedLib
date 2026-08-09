package org.unitedlands.menu;

public enum UnitedMenuAlign {
    TOP_LEFT,    TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER,     CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

    public int getSlot(int rows) {
        var mid = rows / 2;

        return switch(this) {
            case TOP_LEFT      -> 0;
            case TOP_CENTER    -> 4;
            case TOP_RIGHT     -> 8;

            case CENTER_LEFT   -> mid * 9;
            case CENTER        -> mid * 9 + 4;
            case CENTER_RIGHT  -> mid * 9 + 8;

            case BOTTOM_LEFT   -> (rows - 1) * 9;
            case BOTTOM_CENTER -> (rows - 1) * 9 + 4;
            case BOTTOM_RIGHT  -> (rows - 1) * 9 + 8;
        };
    }
}
