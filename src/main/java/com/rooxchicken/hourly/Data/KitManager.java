package com.rooxchicken.hourly.Data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

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

    public void itemLogic(Player player, ItemStack item, int index, HashMap<Material, Integer> itemCountMap, HashMap<PotionEffectType, Integer> potionCountMap)
    {
        if(item == null)
            return;

        if(item.hasItemMeta())
        {
            if(item.getItemMeta() instanceof PotionMeta)
            {
                PotionMeta meta = (PotionMeta)item.getItemMeta();
                PotionEffect potion = meta.getBasePotionType().getPotionEffects().get(0);
                
                if(plugin.dataManager.currentPotionCountProgression.containsKey(potion.getType()))
                {
                    if(!potionCountMap.containsKey(potion.getType()))
                        potionCountMap.put(potion.getType(), 0);
                    
                    int count = potionCountMap.get(potion.getType()) + item.getAmount();
                    int limit = plugin.dataManager.currentPotionCountProgression.get(potion.getType());

                    if(count > limit)
                    {
                        Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);
                        droppedItem.setPickupDelay(20);
                        int first = player.getInventory().first(item);
                        if(first == -1)
                            first = 40;
                        player.getInventory().clear(first);
                        player.sendMessage("§4You have too many " + potion.getType().getKey().getKey() + "!");

                        potionCountMap.replace(potion.getType(), limit);
                        return;
                    }

                    potionCountMap.replace(potion.getType(), count);
                }

                if(plugin.dataManager.currentPotionEffectProgression.containsKey(potion.getType()))
                {
                    if(potion.getAmplifier() >= plugin.dataManager.currentPotionEffectProgression.get(potion.getType()))
                    {
                        meta.setBasePotionType(PotionType.valueOf(potion.getType().getKey().getKey()));
                        item.setItemMeta(meta);
                        player.sendMessage("§4Effect " + potion.getType().getKey().getKey() + " was too high!");
                    }
                }
            }

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
                player.getInventory().setItem(index, clone);
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
            if(plugin.isJuggernaught(player))
            {
                plugin.dataManager.selectKit(1);
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            }
            else
                plugin.dataManager.selectKit(0);
            HashMap<Material, Integer> itemCountMap = new HashMap<Material, Integer>();
            HashMap<PotionEffectType, Integer> potionCountMap = new HashMap<PotionEffectType, Integer>();

            int index = 0;
            for(ItemStack item : player.getInventory())
            {
                itemLogic(player, item, index, itemCountMap, potionCountMap);
                index++;
            }
        }
    }
}
