package org.unitedlands.factories.mobs;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.unitedlands.utils.United;

public class MythicMobFactory extends BaseMobFactory {

    public MythicMobFactory() {

    }

    @Override
    public UUID createMobAtLocation(String mobType, Location location) {
        return createMobAtLocation(mobType, location, 1);
    }

    @Override
    public UUID createMobAtLocation(String mobType, Location location, double level) {
        var mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mobType).orElse(null);
        if (mythicMob != null) {
            ActiveMob activeMythicMob = MythicBukkit.inst().getMobManager().spawnMob(mobType, location, level);
            return activeMythicMob.getUniqueId();
        } else {
            try {
                var entityType = EntityType.valueOf(mobType);
                var entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType, SpawnReason.CUSTOM);

                return entity.getUniqueId();
            } catch (Exception ex) {
                United.logger().error("Error creating entity: " + ex.getMessage());
            }
        }
        return null;
    }

    public UUID createMobAtLocation(String mobType, Location location, Player owner, double level) {
        return createMobAtLocation(mobType, location, owner, level, null);
    }

    public UUID createMobAtLocation(String mobType, Location location, double level, String faction) {
        return createMobAtLocation(mobType, location, null, level, faction);
    }

    public UUID createMobAtLocation(String mobType, Location location, Player owner, double level, String faction) {
        var mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mobType).orElse(null);
        if (mythicMob != null) {
            ActiveMob activeMythicMob = MythicBukkit.inst().getMobManager().spawnMob(mobType, location, level);
            if (owner != null) {
                activeMythicMob.setOwnerUUID(owner.getUniqueId());
            }
            if (faction != null) {
                United.logger().info("Setting faction " + faction);
                activeMythicMob.setFaction(faction);
            }
            return activeMythicMob.getUniqueId();
        } else {
            United.logger().error("Unable to create custom mob " + mobType + ", vanilla mobs are not supported as minions.");
        }
        return null;
    }

    @Override
    public void setName(UUID id, String name) {
        var mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(id);
        if (mythicMob.isPresent()) {
            mythicMob.get().setDisplayName(name);
        }
    }

}
