package de.obdachioser.football;

import de.obdachioser.football.events.SlimeMoveEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * package: de.obdachioser.football
 * <p>
 * created by ObdachIoser 19:49 on 01.09.2017.
 * Copyright (C) 2017 all rights reserved
 */
public class FootballSlime {

    @Getter
    private Slime slime;

    @Getter @Setter
    private Player lastKicker = null;

    private Location location;

    @Getter @Setter
    private Long lastscore = System.currentTimeMillis();

    public FootballSlime(Location location) {

        this.location = location;
        slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
        slime.setSize(2);

        d();
        a();
    }

    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    public void destroyAndSpawn() {

        destroy();

        threadPoolExecutor.execute(() -> {

            try {

                TimeUnit.MILLISECONDS.sleep(2250L);

                for(Player player : Bukkit.getOnlinePlayers()) {

                    CustomPlayerCache customPlayerCache = Football.getFootballSession().getCustomPlayerCaches().get(player.getUniqueId());
                    if(customPlayerCache.isIngame()) player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(Football.getInstance(), () -> {

                    slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
                    slime.setSize(2);

                    d();

                });

            } catch (Exception exc) {
                exc.printStackTrace();
            }

        });
    }

    public void setVelocity(Vector velocity) {
        slime.setVelocity(velocity);
    }

    private Location lastLocation = null;

    public void a() {

        Executors.newCachedThreadPool().execute(() -> {

            try {

                for(Integer i = 0; i != -1; i++) {

                    i = 0;

                    TimeUnit.MILLISECONDS.sleep(65L);

                    SlimeMoveEvent slimeMoveEvent = new SlimeMoveEvent(false, slime, lastLocation, slime.getLocation());
                    Bukkit.getPluginManager().callEvent(slimeMoveEvent);

                    if(slimeMoveEvent.isCancelled()) {

                        if(lastLocation == null) lastLocation = getSlime().getLocation();

                        slime.teleport(lastLocation);
                        return;
                    }

                    lastLocation = slime.getLocation();
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }

        });

    }

    public void destroy() {
        if(slime != null) slime.remove();
    }

    private void d() {
        EntityInsentient entitySheep = (EntityInsentient) ((CraftEntity) slime).getHandle();

        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");

            cField.setAccessible(true);
            bField.set(entitySheep.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(entitySheep.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(entitySheep.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(entitySheep.targetSelector, new UnsafeList<PathfinderGoalSelector>());

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}