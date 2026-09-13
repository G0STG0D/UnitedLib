package org.unitedlands.factories.mobs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import org.unitedlands.utils.United;

public class VanillaMobFactory extends BaseMobFactory {

    public VanillaMobFactory() {

    }

    public UUID createMobAtLocation(String mobType, Location location) {
        return createMobAtLocation(mobType, location, 1);
    }

    @Override
    public UUID createMobAtLocation(String mobType, Location location, double level) {
        try {
            var entityType = EntityType.valueOf(mobType);
            var entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

            return entity.getUniqueId();
        } catch (Exception ex) {
            United.logger().error("Error creating entity: " + ex.getMessage());
        }
        return null;
    }

    public UUID createMobAtLocation(String mobType, Location location, double level, String faction) {
        United.logger().error("Unable to create mob " + mobType + " with faction, vanilla mobs can't have factions.");
        return null;
    }

    public UUID createMobAtLocation(String mobType, Location location, Player owner, double level) {
        United.logger().error("Unable to create mob " + mobType + " with owner, vanilla mobs can't be owned.");
        return null;
    }

    public UUID createMobAtLocation(String mobType, Location location, Player owner, double level, String faction) {
        United.logger().error("Unable to create mob " + mobType + " with owner, vanilla mobs can't be owned.");
        return null;
    }

    @Override
    public void setName(UUID id, String name) {
        var mob = Bukkit.getServer().getEntity(id);
        if (mob != null) {
            mob.customName(Component.text(name));
        }
        return;
    }

}
