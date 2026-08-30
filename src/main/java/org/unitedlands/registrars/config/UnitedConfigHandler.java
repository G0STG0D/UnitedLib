package org.unitedlands.registrars.config;

/**
 * Marker interface for annotation-driven YAML config bindings.
 *
 * <p>Each implementing interface should declare:
 * <pre>static MyConfig get() { return UnitedConfigs.get(MyConfig.class); }</pre>
 */
public interface UnitedConfigHandler {

    void reload();

}
