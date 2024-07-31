package com.rooxchicken.hourly.Data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.rooxchicken.hourly.Hourly;
import com.rooxchicken.hourly.Tasks.Task;

public class KitManager extends Task
{
    private Hourly plugin;

    public KitManager(Hourly _plugin)
    {
        super(_plugin);
        plugin = _plugin;
        tickThreshold = 20;
    }

    public void itemLogic(Player player, ItemStack item, HashMap<Material, Integer> itemCountMap)
    {
        if(item == null)
            return;
        if(item.hasItemMeta())
        {
            ItemMeta meta = item.getItemMeta();
            for(Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet())
            {
                if(plugin.dataManager.currentEnchantProgression.containsKey(enchant.getKey()))
                {
                    int limit = plugin.dataManager.currentEnchantProgression.get(enchant.getKey());
                    if(enchant.getValue() > limit)
                    {
                        meta.removeEnchant(enchant.getKey());
                        if(limit != 0)
                            meta.addEnchant(enchant.getKey(), limit, true);
                        player.sendMessage("§4Enchant " + enchant.getKey() + " is too high!");
                    }
                }
            }

            item.setItemMeta(meta);
        }

        if(plugin.dataManager.currentItemProgression.containsKey(item.getType()))
        {
            if(!itemCountMap.containsKey(item.getType()))
                itemCountMap.put(item.getType(), 0);
            
            int count = itemCountMap.get(item.getType()) + item.getAmount();
            int limit = plugin.dataManager.currentItemProgression.get(item.getType());

            if(count > limit)
            {
                ItemStack clone = item.clone();
                clone.setAmount((count-limit));
                Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), clone);
                droppedItem.setPickupDelay(20);
                clone.setAmount(item.getAmount() - (count - limit));
                int first = player.getInventory().first(item);
                if(first == -1)
                    first = 40;
                player.getInventory().setItem(first, clone);
                player.sendMessage("§4You have too many " + item.getType() + "!");

                itemCountMap.replace(item.getType(), limit);
                return;
            }

            itemCountMap.replace(item.getType(), count);
        }
    }

    @Override
    public void run()
    {
        for(Player player : Bukkit.getOnlinePlayers())
        {
            HashMap<Material, Integer> itemCountMap = new HashMap<Material, Integer>();

            //itemLogic(player, player.getInventory().getItemInOffHand(), itemCountMap);
            for(ItemStack item : player.getInventory())
            {
                itemLogic(player, item, itemCountMap);
            }
        }
    }
}
