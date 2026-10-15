package org.unitedlands.config;

import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.registrars.config.UnitedConfigHandler;
import org.unitedlands.registrars.config.UnitedConfigs;

@UnitedConfig
public interface UnitedLibConfig extends UnitedConfigHandler {
    static UnitedLibConfig get() { return UnitedConfigs.get(UnitedLibConfig.class); }

    @UnitedSection(key = "messages")
    Messages messages();

    record Messages(
            @UnitedSetting(key = "prefix-ul") String prefixUL,
            @UnitedSetting(key = "prefix")    String prefix
    ) {}

    @UnitedSection(key = "frame")
    Frame frame();

    record Frame(
            @UnitedSetting(key = "width",         def = "300") int    width,
            @UnitedSetting(key = "console-width", def = "50")  int    consoleWidth,
            @UnitedSetting(key = "color",         def = "red") String color,
            @UnitedSetting(key = "left-cap",      def = "+|✦") String leftCap,
            @UnitedSetting(key = "right-cap",     def = "|✦+") String rightCap,
            @UnitedSetting(key = "title-open",    def = "[")   String titleOpen,
            @UnitedSetting(key = "title-close",   def = "]")   String titleClose,
            @UnitedSetting(key = "footer-center", def = "+✦+") String footerCenter
    ) {}

}
