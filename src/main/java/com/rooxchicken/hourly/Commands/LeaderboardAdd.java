package com.rooxchicken.hourly.Commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;

import com.rooxchicken.hourly.Hourly;

import net.md_5.bungee.api.ChatColor;

public class LeaderboardAdd implements CommandExecutor
{
    private Hourly plugin;

    public LeaderboardAdd(Hourly _plugin)
    {
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(sender.isOp())
        {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            scoreboard.getObjective("timeAlive").getScore(args[0]).setScore(0);
        }

        return true;
    }

}
