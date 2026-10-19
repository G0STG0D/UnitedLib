package org.unitedlands.utils;

public final class United {

    private static final UnitedMessenger MESSENGER = new UnitedMessenger();
    private static final UnitedLogger    LOGGER    = new UnitedLogger();
    private static final UnitedFormatter FORMATTER = new UnitedFormatter();
    private static final UnitedDiscord   DISCORD   = new UnitedDiscord();
    private static final UnitedEconomy   ECONOMY   = new UnitedEconomy();

    private United() {}

    public static UnitedMessenger messenger() { return MESSENGER; }
    public static UnitedLogger    logger()    { return LOGGER;    }
    public static UnitedFormatter formatter() { return FORMATTER; }
    public static UnitedDiscord   discord()   { return DISCORD;   }
    public static UnitedEconomy   economy()   { return ECONOMY;   }

}
