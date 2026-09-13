package org.unitedlands.services;

import java.util.Locale;
import java.util.UUID;

public interface UnitedLanguageService {

    Locale getLocale(UUID playerId);

    void setLocale(UUID playerId, Locale locale);

}
