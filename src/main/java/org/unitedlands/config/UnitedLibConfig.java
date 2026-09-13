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

}
