package com.rooxchicken.hourly.Data;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import com.rooxchicken.hourly.Hourly;

public class CombatManager implements Listener
{
    private Hourly plugin;
    private NamespacedKey combatTimeKey;

    public CombatManager(Hourly _plugin)
    {
        plugin = _plugin;
        combatTimeKey = new NamespacedKey(plugin, "combatTime");

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void startCombat(EntityDamageByEntityEvent event)
    {
        if(!(event.getEntity() instanceof Player))
            return;

        Player player = (Player)event.getEntity();
        Player attacker;
        if(event.getDamager() instanceof Projectile)
        {
            if(((Projectile)event.getDamager()).getShooter() instanceof Player)
                    attacker = (Player)((Projectile)event.getDamager()).getShooter();
                else
                    return;
        }
        else if(event.getDamager() instanceof Player)
            attacker = (Player)event.getDamager();
        else
            return;

        player.getPersistentDataContainer().set(combatTimeKey, PersistentDataType.INTEGER, plugin.dataManager.COMBAT_TIMER * 6);
        attacker.getPersistentDataContainer().set(combatTimeKey, PersistentDataType.INTEGER, plugin.dataManager.COMBAT_TIMER * 6);
    }

    public void removeCombat(Player player)
    {
        player.getPersistentDataContainer().set(combatTimeKey, PersistentDataType.INTEGER, 0);
    }

    @EventHandler
    public void preventContainers(PlayerInteractEvent event)
    {
        if(isInCombat(event.getPlayer()))
        {
            if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null)
            {
                switch(event.getClickedBlock().getType())
                {
                    case ENDER_CHEST:
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§4You cannot open Ender Chests in combat!");
                    break;
                    case SHULKER_BOX:
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§4You cannot open Shulker Boxes in combat!");
                    break;

                    default: break;
                }
            }
        }
    }

    @EventHandler
    public void preventEnderChests(PlayerInteractEvent event)
    {
        ItemStack item = event.getItem();
        if(isInCombat(event.getPlayer()) && item != null && item.getType().equals(Material.TRIDENT))
        {
            if(item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.RIPTIDE))
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§4You cannot use Tridents in combat!");
            }
        }
    }

    public boolean isInCombat(Player player)
    {
        return (player.getPersistentDataContainer().get(combatTimeKey, PersistentDataType.INTEGER) > 0);
    }

    // @EventHandler
    // public void preventBoats(VehicleEnterEvent event)
    // {
    //     if(!(event.getVehicle() instanceof Boat && event.getVehicle().getPassengers().get(0) instanceof Player))
    //         return;

    //     if(isInCombat((Player)event.getVehicle().getPassengers().get(0)))
    //         event.setCancelled(true);
    // }

    public String tickPlayer(Player player)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if(!data.has(combatTimeKey, PersistentDataType.INTEGER))
            data.set(combatTimeKey, PersistentDataType.INTEGER, 0);

        int combat = data.get(combatTimeKey, PersistentDataType.INTEGER) - 1;
        data.set(combatTimeKey, PersistentDataType.INTEGER, combat);
        
        if(combat > 0)
        {
            if(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE))
            {
                player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
                player.sendMessage("§4You cannot use Dolphins in combat!");
            }

            if(player.isInsideVehicle() && player.getVehicle().getType().equals(EntityType.BOAT))
            {
                player.leaveVehicle();
                player.sendMessage("§4You cannot use Boats in combat!");
            }

            int time = combat/6;
            int _minutes = + time / 60;
            int _seconds = time % 60;
            String minutes = ((_minutes < 10) ? "0" : "") + _minutes;
            String seconds = ((_seconds < 10) ? "0" : "") + _seconds;
            return "§r | §c" + minutes + ":" + seconds;
        }

        return "";
    }
}
