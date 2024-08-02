package com.rooxchicken.hourly.Data;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionEffectType;

import com.rooxchicken.hourly.Hourly;

public class DataManager
{
    private Hourly plugin;

    public ArrayList<String> guestPlayers;

    public int KIT_PROGRESSION = 0;
    public HashMap<Material, Integer> currentItemProgression;
    public HashMap<Enchantment, Integer> currentEnchantProgression;
    public HashMap<PotionEffectType, Integer> currentPotionCountProgression;
    public HashMap<PotionEffectType, Integer> currentPotionEffectProgression;
    private int currentWorldBorder;

    private HashMap<Material, Integer> progression0ItemMap;
    private HashMap<Material, Integer> progression1ItemMap;

    private HashMap<PotionEffectType, Integer> progression0PotionCountMap;
    private HashMap<PotionEffectType, Integer> progression1PotionCountMap;

    private HashMap<PotionEffectType, Integer> progression0PotionEffectMap;
    private HashMap<PotionEffectType, Integer> progression1PotionEffectMap;

    private int progression0WorldBorder;
    private int progression1WorldBorder;

    private HashMap<Enchantment, Integer> progression0EnchantMap;
    private HashMap<Enchantment, Integer> progression1EnchantMap;

    public DataManager(Hourly _plugin)
    {
        plugin = _plugin;
        progression0ItemMap = new HashMap<Material, Integer>();
        progression1ItemMap = new HashMap<Material, Integer>();

        progression0EnchantMap = new HashMap<Enchantment, Integer>();
        progression1EnchantMap = new HashMap<Enchantment, Integer>();

        progression0PotionCountMap = new HashMap<PotionEffectType, Integer>();
        progression1PotionCountMap = new HashMap<PotionEffectType, Integer>();

        progression0PotionEffectMap = new HashMap<PotionEffectType, Integer>();
        progression1PotionEffectMap = new HashMap<PotionEffectType, Integer>();

        guestPlayers = new ArrayList<String>();
    }

    public void saveSettings()
    {
        plugin.getConfig().set("kit-progression", KIT_PROGRESSION);
        plugin.saveConfig();
    }

    public void loadSettings()
    {
        plugin.reloadConfig();
        progression0ItemMap.clear();
        KIT_PROGRESSION = plugin.getConfig().getInt("kit-progression");

        for(String player : plugin.getConfig().getStringList("guests"))
        {
            guestPlayers.add(player.toLowerCase());
        }

        for(String _data : plugin.getConfig().getStringList("progression0-map"))
        {
            String[] data = _data.toString().split("\\.");
            switch(data[0])
            {
                case "WORLDBORDER":
                    progression0WorldBorder = Integer.parseInt(data[1]);
                break;
                case "ITEM":
                    switch(data[2])
                    {
                        case "LIMIT": progression0ItemMap.put(Material.getMaterial(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
                case "ENCHANT":
                    switch(data[2])
                    {
                        case "LIMIT": progression0EnchantMap.put(Enchantment.getByName(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
                case "POTION":
                    switch(data[2])
                    {
                        case "COUNT": progression0PotionCountMap.put(PotionEffectType.getByName(data[1]), Integer.parseInt(data[3])); break;
                        case "LIMIT": progression0PotionEffectMap.put(PotionEffectType.getByName(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
            }
        }

        for(String _data : plugin.getConfig().getStringList("progression1-map"))
        {
            String[] data = _data.toString().split("\\.");
            switch(data[0])
            {
                case "WORLDBORDER":
                    progression1WorldBorder = Integer.parseInt(data[1]);
                break;
                case "ITEM":
                    switch(data[2])
                    {
                        case "LIMIT": progression1ItemMap.put(Material.getMaterial(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
                case "ENCHANT":
                    switch(data[2])
                    {
                        case "LIMIT": progression1EnchantMap.put(Enchantment.getByName(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
                case "POTION":
                    switch(data[2])
                    {
                        case "COUNT": progression1PotionCountMap.put(PotionEffectType.getByName(data[1]), Integer.parseInt(data[3])); break;
                        case "LIMIT": progression1PotionEffectMap.put(PotionEffectType.getByName(data[1]), Integer.parseInt(data[3])); break;
                    }
                break;
            }
        }

        if(KIT_PROGRESSION == 0)
        {
            currentItemProgression = progression0ItemMap;
            currentEnchantProgression = progression0EnchantMap;
            currentWorldBorder = progression0WorldBorder;

            currentPotionCountProgression = progression0PotionCountMap;
            currentPotionEffectProgression = progression0PotionEffectMap;
        }
        else if(KIT_PROGRESSION == 1)
        {
            currentItemProgression = progression1ItemMap;
            currentEnchantProgression = progression1EnchantMap;
            currentWorldBorder = progression1WorldBorder;

            currentPotionCountProgression = progression1PotionCountMap;
            currentPotionEffectProgression = progression1PotionEffectMap;
        }

        for(World world : Bukkit.getWorlds())
        {
            world.getWorldBorder().setSize(currentWorldBorder, 30);
        }
    }
}
