package org.unitedlands.registrars.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UnitedDynamicSection<V> {

    V get(String key);

    /** Writes a whole entry (record or config interface) and saves the file.<br>{@code null} removes it. */
    void set(String key, V value);

    void remove(String key);

    boolean has(String key);

    Set<String> keys();

    Collection<V> values();

    Map<String, V> toMap();

}