package com.rooxchicken.hourly.Commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.rooxchicken.hourly.Hourly;

import net.md_5.bungee.api.ChatColor;

public class Withdraw implements CommandExecutor
{
    private Hourly plugin;

    public Withdraw(Hourly _plugin)
    {
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player player = Bukkit.getPlayer(sender.getName());
        if(plugin.isGuest(player))
            return true;
        
        int count = 1;
        if(args.length > 0)
            count = Integer.parseInt(args[0]);

        boolean hasStopwatch = false;

        for(ItemStack item : player.getInventory())
        {
            if(item != null && item.hasItemMeta() && item.getItemMeta().equals(plugin.stopwatch.getItemMeta()))
                hasStopwatch = true;
        }

        for(int i = 0; i < count; i++)
        {
            if(plugin.timeManager.withdraw(player))
            {
                if(hasStopwatch || player.getInventory().firstEmpty() != -1)
                    player.getInventory().addItem(plugin.stopwatch);
                else
                    player.getWorld().dropItemNaturally(player.getLocation(), plugin.stopwatch);
            }
            else
                return true;
        }

        return true;
    }

}
