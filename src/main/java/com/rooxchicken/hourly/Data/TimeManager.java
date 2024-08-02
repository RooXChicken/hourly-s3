package com.rooxchicken.hourly.Data;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.rooxchicken.hourly.Hourly;

public class TimeManager
{
    private Hourly plugin;

    public TimeManager(Hourly _plugin)
    {
        plugin = _plugin;
    }

    public void handleTimeup(Player player)
    {
        checkHasStopwatches(player);
        
        int stopwatches = getStopwatches(player);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + (stopwatches);
        
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.min(maxHealth, getMaxHealth(player)));
        resetTime(player);
    }

    public void resetTime(Player player)
    {
        setTime(player, 60*60*6 - 1);
    }

    public double getMaxHealth(Player player)
    {
        switch(getStopwatches(player))
        {
            case 0: return 20.0;
            case 1: return 24.0;
            case 2: return 28.0;
            case 3: return 32.0;
            case 4: return 36.0;
            case 5: return 40.0;
        }

        return 20.0;
    }

    public void checkHasTime(Player player)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if(!data.has(Hourly.timeKey, PersistentDataType.INTEGER))
            data.set(Hourly.timeKey, PersistentDataType.INTEGER, 60*60 - 1);
    }

    public int getTime(Player player)
    {
        checkHasTime(player);
        return player.getPersistentDataContainer().get(Hourly.timeKey, PersistentDataType.INTEGER);
    }

    public void setTime(Player player, int time)
    {
        player.getPersistentDataContainer().set(Hourly.timeKey, PersistentDataType.INTEGER, time);
    }

    public void checkHasStopwatches(Player player)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if(!data.has(Hourly.stopwatchesKey, PersistentDataType.INTEGER))
            data.set(Hourly.stopwatchesKey, PersistentDataType.INTEGER, 2);
    }

    public int getStopwatches(Player player)
    {
        checkHasStopwatches(player);
        return player.getPersistentDataContainer().get(Hourly.stopwatchesKey, PersistentDataType.INTEGER);
    }

    public void setStopwatches(Player player, int amount)
    {
        player.getPersistentDataContainer().set(Hourly.stopwatchesKey, PersistentDataType.INTEGER, amount);
    }

    public boolean addStopwatch(Player player)
    {
        if(plugin.isGuest(player))
        {
            player.sendMessage("§4Guests cannot use stopwatches!");
            return false;
        }
        if(getStopwatches(player) >= 5)
        {
            player.sendMessage("§4You are at the max amount of stopwatches!");
            return false;
        }

        setStopwatches(player, getStopwatches(player) + 1);
        return true;
    }

    public boolean withdraw(Player player)
    {
        if(getStopwatches(player) <= 0)
        {
            player.sendMessage("§4You have no stopwatches to withdraw!");
            return false;
        }

        setStopwatches(player, getStopwatches(player) - 1);
        return true;
    }
}
