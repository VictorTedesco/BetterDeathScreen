package me.tedesk.events.bukkit;

import com.comphenix.protocol.events.PacketEvent;
import me.tedesk.animations.Animation;
import me.tedesk.api.ActionBarAPI;
import me.tedesk.api.SoundAPI;
import me.tedesk.api.TitleAPI;
import me.tedesk.configs.Config;
import me.tedesk.events.Listeners;
import me.tedesk.systems.Randomizer;
import me.tedesk.systems.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

public class EntityDamageListener extends Listeners {

    private static void sendEvents(Player p) {
        int time = Config.TIME;
        if (time <= 0) {
            time = 1;
        }
        Config.DEAD_PLAYERS.add(p.getName());
        if (!Config.MOVE_SPECTATOR) {
            p.setWalkSpeed(0F);
            p.setFlySpeed(0F);
        }
        for (PotionEffect pe : p.getActivePotionEffects()) {
            p.removePotionEffect(pe.getType());
        }
        if (p.getWorld().getGameRuleValue("keepInventory").equals("false")) {
            if (!p.hasPermission(Config.KEEP_XP)) {
                p.setLevel(0);
                p.setExp(0);
            }
        }
        p.setGameMode(GameMode.SPECTATOR);
        SoundAPI.sendSound(p, p.getLocation(), Randomizer.randomSound(Config.SOUND_DEATH), Config.SOUND_DEATH_VOLUME, Config.SOUND_DEATH_PITCH);
        TitleAPI.sendTitle(p, 2, 20 * time, 2, Randomizer.customTitles(), Randomizer.customSubtitles());
        if (!Bukkit.getServer().isHardcore()) {
            Tasks.normalTimer(p);
        }
        if (Bukkit.getServer().isHardcore()) {
            Tasks.hardcoreTimer(p);
        }
        Animation.sendAnimation(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        Entity victim = e.getEntity();

        if (victim instanceof Player) {
            Player pv = (Player) victim;
            if (Config.DEAD_PLAYERS.contains(pv.getName())) {
                e.setCancelled(true);
                return;
            }
            sendEvents(pv);
            if (e instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) e).getDamager();
                if (pv.getHealth() <= e.getFinalDamage()) {
                    if (damager instanceof Player) {
                        Player pd = (Player) damager;
                        ActionBarAPI.sendActionBar(pd, Randomizer.customKillActionBar(pv));
                        SoundAPI.sendSound(pd, pd.getLocation(), Randomizer.randomSound(Config.SOUND_KILL), Config.SOUND_KILL_VOLUME, Config.SOUND_KILL_PITCH);
                        return;
                    }
                    if (damager instanceof Projectile) {
                        Projectile pj = (Projectile) damager;
                        if (pj.getShooter() instanceof Player) {
                            Player pd = (Player) pj.getShooter();
                            ActionBarAPI.sendActionBar(pd, Randomizer.customKillActionBar(pv));
                            SoundAPI.sendSound(pd, pd.getLocation(), Randomizer.randomSound(Config.SOUND_KILL), Config.SOUND_KILL_VOLUME, Config.SOUND_KILL_PITCH);
                        }
                    }
                }
            }
        }
    }
}