package com.rooxchicken.hourly.Tasks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import com.rooxchicken.hourly.Hourly;
import com.rooxchicken.hourly.Data.TimeManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class TickPlayers extends Task
{
    private Hourly plugin;

    public TickPlayers(Hourly _plugin)
    {
        super(_plugin);
        plugin = _plugin;

        tickThreshold = 4/Hourly.TICK_RATE;
    }

    public void execute()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for(Player player : Bukkit.getOnlinePlayers())
        {
            scoreboard.getObjective("timeAlive").getScore(player.getName()).setScore(player.getStatistic(Statistic.TIME_SINCE_DEATH) / 1200);

            if(plugin.isGuest(player))
                plugin.timeManager.setStopwatches(player, 2);

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), plugin.timeManager.getMaxHealth(player)));
            double stopwatches = plugin.timeManager.getStopwatches(player)/2.0;

            plugin.afkManager.tick(player);
            if(plugin.afkManager.isAFK(player))
            {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getStopwatchColor((int)(stopwatches*2)) + stopwatches + "§f | §bAFK"));
                continue;
            }

            if(plugin.timeManager.getMaxHealth(player) == player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue())
            {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getStopwatchColor((int)(stopwatches*2)) + stopwatches + "§f | §bMAX"));
                continue;
            }
            int time = plugin.timeManager.getTime(player) / 6;
            int _minutes = + time / 60;
            int _seconds = time % 60;
            String minutes = ((_minutes < 10) ? "0" : "") + _minutes;
            String seconds = ((_seconds < 10) ? "0" : "") + _seconds;

            
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getStopwatchColor((int)(stopwatches*2)) + stopwatches + "§f | §b" + minutes + ":" + seconds));
            
            if(time <= 0)
            {
                plugin.timeManager.handleTimeup(player);
                return;
            }

            plugin.timeManager.setTime(player, plugin.timeManager.getTime(player) - 1);
        }
    }

    @Override
    public void run()
    {
        if(4/Hourly.TICK_RATE <= 0)
        {
            tickThreshold = 1;

            for(int i = 0; i < Hourly.TICK_RATE/4.0; i++)
                execute();
        }
        else
        {
            tickThreshold = 4/Hourly.TICK_RATE;
            execute();
        }
    }

    private String getStopwatchColor(int stopwatch)
    {
        switch(stopwatch)
        {
            case 0: return "§4";
            case 1: return "§c";
            case 2: return "§6";
            case 3: return "§e";
            case 4: return "§a";
            case 5: return "§2";
        }

        return "";
    }
}
