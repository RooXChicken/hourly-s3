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
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
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
import com.rooxchicken.hourly.Commands.LeaderboardAdd;
import com.rooxchicken.hourly.Commands.LeaderboardRemove;
import com.rooxchicken.hourly.Commands.ReloadConfig;
import com.rooxchicken.hourly.Commands.RemoveCombat;
import com.rooxchicken.hourly.Commands.SetRate;
import com.rooxchicken.hourly.Commands.Withdraw;
import com.rooxchicken.hourly.Data.AFKManager;
import com.rooxchicken.hourly.Data.CombatManager;
import com.rooxchicken.hourly.Data.DataManager;
import com.rooxchicken.hourly.Data.TimeManager;
import com.rooxchicken.hourly.Data.KitManager;
import com.rooxchicken.hourly.Tasks.Task;
import com.rooxchicken.hourly.Tasks.TickPlayers;

import net.md_5.bungee.api.ChatColor;

public class Hourly extends JavaPlugin implements Listener
{
    public static int TICK_RATE = 1;
    public static ArrayList<Task> tasks;

    public static NamespacedKey timeKey;
    public static NamespacedKey stopwatchesKey;
    public static NamespacedKey netheriteKeyRecipeKey; //not at all confusing

    public DataManager dataManager;
    public TimeManager timeManager;
    public AFKManager afkManager;
    public KitManager kitManager;
    public CombatManager combatManager;

    public ItemStack stopwatch;
    public ItemStack netheriteKey;
    public ItemStack netheriteShard;
    public ShapedRecipe netheriteKeyRecipe;

    @Override
    public void onEnable()
    {
        Bukkit.resetRecipes();

        dataManager = new DataManager(this);
        timeManager = new TimeManager(this);
        afkManager = new AFKManager(this);
        kitManager = new KitManager(this);
        combatManager = new CombatManager(this);

        timeKey = new NamespacedKey(this, "time");
        stopwatchesKey = new NamespacedKey(this, "stopwatches");
        netheriteKeyRecipeKey = new NamespacedKey(this, "netheriteKeyRecipe");

        saveDefaultConfig();
        dataManager.loadSettings();

        tasks = new ArrayList<Task>();
        tasks.add(new TickPlayers(this));
        tasks.add(kitManager);

        {
            stopwatch = new ItemStack(Material.PAPER);
            ItemMeta meta = stopwatch.getItemMeta();
            meta.setDisplayName("§6§lStopwatch");
            meta.setCustomModelData(1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            ArrayList<String> lore = new ArrayList<String>();

            lore.add("§6Adds 0.5 to your stopwatch count,");
            lore.add("§6which raises your heart limit and");
            lore.add("§6increases your hearts per hour!");

            meta.setLore(lore);
            stopwatch.setItemMeta(meta);
        }

        {
            netheriteKey = new ItemStack(Material.PAPER);
            ItemMeta meta = netheriteKey.getItemMeta();
            meta.setDisplayName("§8§lNetherite Key");
            meta.setCustomModelData(2);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            ArrayList<String> lore = new ArrayList<String>();

            lore.add("§8Unlocks the next progression!");

            meta.setLore(lore);
            netheriteKey.setItemMeta(meta);
        }

        {
            netheriteShard = new ItemStack(Material.PAPER);
            ItemMeta meta = netheriteShard.getItemMeta();
            meta.setDisplayName("§7§lNetherite Shard");
            meta.setCustomModelData(3);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            ArrayList<String> lore = new ArrayList<String>();

            lore.add("§7One of the four shards needed to");
            lore.add("§7craft the netherite key.");

            meta.setLore(lore);
            netheriteShard.setItemMeta(meta);
        }

        {
            netheriteKeyRecipe = new ShapedRecipe(netheriteKeyRecipeKey, netheriteKey);
            netheriteKeyRecipe.shape("aSa", "SaS", "aSa");

            netheriteKeyRecipe.setIngredient('S', Material.PAPER);

            Bukkit.addRecipe(netheriteKeyRecipe);
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        boolean create = true;
        for(Objective objective : scoreboard.getObjectives())
        {
            if(objective.getName().equals("timeAlive"))
                create = false;
        }
        if(create)
            scoreboard.registerNewObjective("timeAlive", Criteria.DUMMY, "timeAlive");

        scoreboard.getObjective("timeAlive").setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboard.getObjective("timeAlive").setDisplayName("§6Time Since Death");
    
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("giveitems").setExecutor(new GiveItems(this));
        this.getCommand("setrate").setExecutor(new SetRate(this));
        this.getCommand("afk").setExecutor(new GoAFK(this));
        this.getCommand("withdraw").setExecutor(new Withdraw(this));
        this.getCommand("removecombat").setExecutor(new RemoveCombat(this));
        this.getCommand("reloadconfig").setExecutor(new ReloadConfig(this));
        this.getCommand("leaderboardremove").setExecutor(new LeaderboardRemove(this));
        this.getCommand("leaderboardadd").setExecutor(new LeaderboardAdd(this));

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

    public boolean isGuest(Player player)
    {
        return (dataManager.guestPlayers.contains(player.getName().toLowerCase()));
    }

    public boolean isJuggernaught(Player player)
    {
        return (dataManager.juggernaughts.contains(player.getName().toLowerCase()));
    }

    public boolean isLBBlacklisted(Player player)
    {
        return (dataManager.juggernaughts.contains(player.getName().toLowerCase()));
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
            if(!timeManager.addStopwatch(player))
                return;

            item.setAmount(item.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        }
    }

    @EventHandler
    public void useNetheriteKey(PlayerInteractEvent event)
    {
        if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if(item != null && item.hasItemMeta() && item.getItemMeta().equals(netheriteKey.getItemMeta()))
        {
            dataManager.KIT_PROGRESSION = 1;
            dataManager.saveSettings();
            dataManager.loadSettings();

            item.setAmount(item.getAmount() - 1);
            for(Player p : Bukkit.getOnlinePlayers())
            {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 1);
                p.sendMessage("§8§lThe Netherite Key has been used!");
            }

            for(World world : Bukkit.getWorlds())
            {
                world.getWorldBorder().setSize(dataManager.currentWorldBorder, 60);
            }
        }
    }

    @EventHandler
    private void shardsInCraft(PrepareItemCraftEvent event)
    {
        if(event.getRecipe() == null || !event.getRecipe().getResult().hasItemMeta())
            return;

        for(ItemStack item : event.getInventory())
        {
            if(item != null && item.getType().equals(Material.PAPER))
            {
                if(!item.hasItemMeta())
                    event.getInventory().setResult(new ItemStack(Material.AIR));

                if(item.getItemMeta().equals(netheriteKey.getItemMeta()))
                    continue;
                    
                if(!item.getItemMeta().equals(netheriteShard.getItemMeta()))
                    event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void dropStopwatchOnDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        timeManager.resetTime(player);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        if(isGuest(player) || (player.getKiller() != null && isGuest(player.getKiller())))
            return;

        combatManager.removeCombat(player);
        int stopwatches = timeManager.getStopwatches(player) - 1;

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

        timeManager.setStopwatches(player, stopwatches);

        if(player.getKiller() == null)
        {
            player.getWorld().dropItemNaturally(player.getLocation(), stopwatch);
        }
        else if(player.getKiller() != null && !isGuest(player.getKiller()))
        {
            Player killer = player.getKiller();
            if(timeManager.getStopwatches(killer) < 5)
            {
                timeManager.addStopwatch(killer);
                killer.sendMessage("§2You have stolen a stopwatch!");
            }
            else
                player.getWorld().dropItemNaturally(player.getLocation(), stopwatch);
        }
    }

    @EventHandler
    public void resetHeartsOnPop(EntityResurrectEvent event)
    {
        if(!(event.getEntity() instanceof Player))
            return;

        ((Player)event.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
    }

    @EventHandler
    public void tattle(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if(combatManager.isInCombat(player))
        {
            getServer().broadcast("§c§l" + player.getName() + " logged out in combat!", Server.BROADCAST_CHANNEL_USERS);
        }
    }

    @Override
    public void onDisable()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }
}
