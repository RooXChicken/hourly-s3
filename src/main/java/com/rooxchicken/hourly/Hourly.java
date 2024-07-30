package com.rooxchicken.hourly;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.rooxchicken.hourly.Commands.GiveItems;
import com.rooxchicken.hourly.Commands.GoAFK;
import com.rooxchicken.hourly.Commands.SetRate;
import com.rooxchicken.hourly.Commands.Withdraw;
import com.rooxchicken.hourly.Data.AFKManager;
import com.rooxchicken.hourly.Data.DataManager;
import com.rooxchicken.hourly.Tasks.Task;
import com.rooxchicken.hourly.Tasks.TickPlayers;

import net.md_5.bungee.api.ChatColor;

public class Hourly extends JavaPlugin implements Listener
{
    public static int TICK_RATE = 1;
    public static ArrayList<Task> tasks;

    public static NamespacedKey timeKey;
    public static NamespacedKey stopwatchesKey;

    public DataManager dataManager;
    public AFKManager afkManager;

    public ItemStack stopwatch;

    @Override
    public void onEnable()
    {
        dataManager = new DataManager(this);
        afkManager = new AFKManager(this);

        tasks = new ArrayList<Task>();
        tasks.add(new TickPlayers(this));

        {
            stopwatch = new ItemStack(Material.PAPER);
            ItemMeta meta = stopwatch.getItemMeta();
            meta.setDisplayName("§6§lStopwatch");
            meta.setCustomModelData(1);
            ArrayList<String> lore = new ArrayList<String>();

            lore.add("§6Adds 0.5 to your stopwatch count,");
            lore.add("§6which raises your heart limit and");
            lore.add("§6increases your hearts per hour!");

            meta.setLore(lore);
            stopwatch.setItemMeta(meta);
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("giveitems").setExecutor(new GiveItems(this));
        this.getCommand("setrate").setExecutor(new SetRate(this));
        this.getCommand("afk").setExecutor(new GoAFK(this));
        this.getCommand("withdraw").setExecutor(new Withdraw(this));

        timeKey = new NamespacedKey(this, "time");
        stopwatchesKey = new NamespacedKey(this, "stopwatches");

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
                ArrayList<Task> _tasks = new ArrayList<Task>();
                for(Task t : tasks)
                    _tasks.add(t);
                
                ArrayList<Task> toRemove = new ArrayList<Task>();

                for(Task t : _tasks)
                {
                    t.tick();

                    if(t.cancel)
                        toRemove.add(t);
                }

                for(Task t : toRemove)
                {
                    t.onCancel();
                    tasks.remove(t);
                }
            }
        }, 0, 1);

        getLogger().info("Hourly SMP! (since 1987) (made by roo)");
    }

    @EventHandler
    public void useStopwatch(PlayerInteractEvent event)
    {
        if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if(item != null && item.hasItemMeta() && item.getItemMeta().equals(stopwatch.getItemMeta()))
        {
            if(!dataManager.addStopwatch(player))
                return;

            item.setAmount(item.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        }
    }

    @EventHandler
    public void dropStopwatchOnDeaht(PlayerDeathEvent event)
    {
        Player player = event.getEntity();

        int stopwatches = dataManager.getStopwatches(player) - 1;

        if(stopwatches < 0)
        {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
            {
                public void run()
                {
                    getServer().broadcast("§c§l" + player.getName() + " has run out of time!", Server.BROADCAST_CHANNEL_USERS);
                    player.ban("§bYou ran out of time!", new Date(System.currentTimeMillis() + (60*60*1000)), null);
                }
            }, 1);
            return;
        }

        dataManager.setStopwatches(player, stopwatches);

        if(player.getKiller() == null)
        {
            player.getWorld().dropItemNaturally(player.getLocation(), stopwatch);
        }
        else
        {
            Player killer = player.getKiller();
            if(dataManager.getStopwatches(killer) < 5)
            {
                dataManager.addStopwatch(killer);
                killer.sendMessage("§2You have stolen a stopwatch!");
            }
            else
                player.getWorld().dropItemNaturally(player.getLocation(), stopwatch);
        }
    }

    @Override
    public void onDisable()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }
}
