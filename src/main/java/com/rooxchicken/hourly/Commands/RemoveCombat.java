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

public class RemoveCombat implements CommandExecutor
{
    private Hourly plugin;

    public RemoveCombat(Hourly _plugin)
    {
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player player = Bukkit.getPlayer(sender.getName());
        if(!plugin.combatManager.isInCombat(player))
            return true;

        Player attacker = plugin.combatManager.combatList.get(player);

        if(!plugin.combatManager.combatRemove.contains(player))
        {
            plugin.combatManager.combatRemove.add(player);
            if(plugin.combatManager.combatRemove.contains(attacker))
            {
                plugin.combatManager.removeCombat(player);
                plugin.combatManager.removeCombat(attacker);

                plugin.combatManager.combatRemove.remove(player);
                plugin.combatManager.combatRemove.remove(attacker);
                return true;
            }

            player.sendMessage("§cWaiting on " + attacker.getName() + " to accept!");
            attacker.sendMessage("§cPlayer " + player.getName() + " has requested to end combat. Run /removecombat to accept!");

        }

        return true;
    }

}
