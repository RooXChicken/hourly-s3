package com.rooxchicken.hourly.Data;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;

import com.rooxchicken.hourly.Hourly;

public class DataManager
{
    private Hourly plugin;

    public int KIT_PROGRESSION = 0;
    public HashMap<Material, Integer> currentItemProgression;
    public HashMap<Enchantment, Integer> currentEnchantProgression;
    private int currentWorldBorder;

    private HashMap<Material, Integer> progression0ItemMap;
    private HashMap<Material, Integer> progression1ItemMap;
    private HashMap<Material, Integer> progression2ItemMap;

    private int progression0WorldBorder;
    private int progression1WorldBorder;
    private int progression2WorldBorder;

    private HashMap<Enchantment, Integer> progression0EnchantMap;
    private HashMap<Enchantment, Integer> progression1EnchantMap;
    private HashMap<Enchantment, Integer> progression2EnchantMap;

    public DataManager(Hourly _plugin)
    {
        plugin = _plugin;
        progression0ItemMap = new HashMap<Material, Integer>();
        progression1ItemMap = new HashMap<Material, Integer>();
        progression2ItemMap = new HashMap<Material, Integer>();

        progression0EnchantMap = new HashMap<Enchantment, Integer>();
        progression1EnchantMap = new HashMap<Enchantment, Integer>();
        progression2EnchantMap = new HashMap<Enchantment, Integer>();
    }

    public void saveSettings()
    {
        plugin.getConfig().set("kit-progression", KIT_PROGRESSION);
    }

    public void loadSettings()
    {
        plugin.reloadConfig();
        progression0ItemMap.clear();
        KIT_PROGRESSION = plugin.getConfig().getInt("kit-progression");

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
            }
        }

        if(KIT_PROGRESSION == 0)
        {
            currentItemProgression = progression0ItemMap;
            currentEnchantProgression = progression0EnchantMap;
            currentWorldBorder = progression0WorldBorder;
        }
        else if(KIT_PROGRESSION == 1)
        {
            currentItemProgression = progression1ItemMap;
            currentEnchantProgression = progression1EnchantMap;
            currentWorldBorder = progression1WorldBorder;
        }
        else if(KIT_PROGRESSION == 2)
        {
            currentItemProgression = progression2ItemMap;
            currentEnchantProgression = progression2EnchantMap;
            currentWorldBorder = progression2WorldBorder;
        }
        
        for(World world : Bukkit.getWorlds())
        {
            world.getWorldBorder().setSize(currentWorldBorder);
        }
    }
}
