package com.rooxchicken.hourly.Data;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.rooxchicken.hourly.Hourly;

public class AFKManager
{
    private Hourly plugin;
    private NamespacedKey afkTimeKey;

    private HashMap<Player, Location> playerLocationMap;
    private HashMap<Player, Boolean> playerAFKMap;

    public AFKManager(Hourly _plugin)
    {
        plugin = _plugin;
        afkTimeKey = new NamespacedKey(plugin, "afkTime");

        playerLocationMap = new HashMap<Player, Location>();
        playerAFKMap = new HashMap<Player, Boolean>();
    }

    public void checkHasAFKTime(Player player)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if(!data.has(afkTimeKey, PersistentDataType.INTEGER))
            data.set(afkTimeKey, PersistentDataType.INTEGER, 0);

        if(!playerAFKMap.containsKey(player))
            playerAFKMap.put(player, false);

        if(!playerLocationMap.containsKey(player))
            playerLocationMap.put(player, player.getLocation().clone());
    }

    public boolean isAFK(Player player)
    {
        checkHasAFKTime(player);
        return playerAFKMap.get(player);
    }

    public void setAFKTime(Player player, int time)
    {
        player.getPersistentDataContainer().set(afkTimeKey, PersistentDataType.INTEGER, time);
    }

    public void setAFK(Player player, boolean afk)
    {
        if(playerAFKMap.containsKey(player))
            playerAFKMap.remove(player);

        playerAFKMap.put(player, afk);
    }

    public void tick(Player player)
    {
        checkHasAFKTime(player);

        PersistentDataContainer data = player.getPersistentDataContainer();
        Location playerLoc = player.getLocation();

        if(isAFK(player))
            return;

        if(playerLocationMap.get(player).getBlock().getLocation().equals(playerLoc.getBlock().getLocation()) || playerLocationMap.get(player).getDirection().equals(playerLoc.getDirection()))
        {
            int time = data.get(afkTimeKey, PersistentDataType.INTEGER) + 1;
            data.set(afkTimeKey, PersistentDataType.INTEGER, time);

            if(time > 10*60*6)
            {
                data.set(afkTimeKey, PersistentDataType.INTEGER, 0);
                player.kickPlayer("Please run the /afk command to afk!");
            }
        }
        else
        {
            data.set(afkTimeKey, PersistentDataType.INTEGER, 0);
            playerLocationMap.replace(player, playerLoc.clone());
        }
    }
}
