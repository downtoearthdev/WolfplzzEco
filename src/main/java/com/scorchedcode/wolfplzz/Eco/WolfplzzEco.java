package com.scorchedcode.wolfplzz.Eco;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class WolfplzzEco extends JavaPlugin implements Listener {

    public Economy vaultEco = null;
    public Permission vaultPerm = null;

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        new DarkInit(this);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if(getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            RegisteredServiceProvider<Economy> rse = getServer().getServicesManager().getRegistration(Economy.class);
            vaultPerm = rsp.getProvider();
            vaultEco = rse.getProvider();
        }
        else
            setEnabled(false);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("buycommmand")) {
            if(args.length == 1) {
                for(CommandHandler.PayCommand cmd : CommandHandler.getInstance().loadedCommands) {
                    if(cmd.getName().equalsIgnoreCase(args[0])) {
                        boolean succeed = false;
                        if(cmd.getType() == Type.BUY)
                            succeed = cmd.buy((Player)sender);
                        else if(cmd.getType() == Type.BUY_ONCE)
                            succeed = cmd.buyOnce((Player)sender);
                        sender.sendMessage((succeed) ? ChatColor.LIGHT_PURPLE + "You have purchased this command!" : ChatColor.RED + "You do not have enough money to purchase this command!");
                        return true;
                    }
                }
            }
        }
        else if(command.getName().equals("rentcommand")) {
            if(args.length == 1) {
                for(CommandHandler.PayCommand cmd : CommandHandler.getInstance().loadedCommands) {
                    if(cmd.getName().equalsIgnoreCase(args[0]) && cmd.getType() == Type.RENT) {
                            boolean succeed = cmd.startRent((Player) sender);
                            sender.sendMessage((succeed) ? ChatColor.LIGHT_PURPLE + "You have rented this command!" : ChatColor.RED + "You do not have enough money to rent this command!");
                        return true;
                    }
                }
            }

        }

        else if(command.getName().equals("reloadwolfplzzeco")) {
            CommandHandler.getInstance().resetHandler();
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "WolfplzzEco has been reloaded.");
        }

        else if(command.getName().equals("listpaidcommands")) {
            String commandList = "";
            for(CommandHandler.PayCommand cmd : CommandHandler.getInstance().loadedCommands) {
                commandList += ChatColor.GREEN + cmd.getName() + ChatColor.YELLOW + " [" + cmd.getType().toString() + "]\n";
            }
            sender.sendMessage(commandList);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        for(String name : DarkInit.getPlugin().getConfig().getConfigurationSection("join-commands").getKeys(false)) {
            if(e.getPlayer().getName().equalsIgnoreCase(name)) {
                ConfigurationSection joinCommand = DarkInit.getPlugin().getConfig().getConfigurationSection("join-commands." + name);
                if(!joinCommand.getString("run-as-server", "none").equals("none")) {
                    DarkInit.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(DarkInit.getPlugin(),
                            () -> {
                                DarkInit.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DarkInit.getPlugin(), () -> {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), joinCommand.getString("run-as-server").replaceAll("\\{player}", e.getPlayer().getName()));
                                });
                            }, joinCommand.getInt("wait-time")*20L);
                }
                else if(!joinCommand.getString("run-as-player", "none").equals("none")) {
                    DarkInit.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(DarkInit.getPlugin(),
                            () -> {
                                DarkInit.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DarkInit.getPlugin(), () -> {
                                    Bukkit.dispatchCommand(e.getPlayer(), joinCommand.getString("run-as-player").replaceAll("\\{player}", e.getPlayer().getName()));
                                });
                            }, joinCommand.getInt("wait-time")*20L);
                }
            }
        }

        //Check if they need a rented command to expire
        File persist = new File(getDataFolder().getAbsolutePath() + File.separatorChar + "revoke.yml");
        if(persist.exists()) {
            YamlConfiguration persistYML = YamlConfiguration.loadConfiguration(persist);
            if(persistYML.getKeys(false).contains(e.getPlayer().getUniqueId().toString())) {
                DarkInit.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DarkInit.getPlugin(), () -> {
                    Bukkit.dispatchCommand((persistYML.getString(e.getPlayer().getUniqueId().toString()).contains("{player}") ? Bukkit.getConsoleSender() : e.getPlayer()), persistYML.getString(e.getPlayer().getUniqueId().toString()).replaceAll("\\{player}", e.getPlayer().getName() + " "));
                    e.getPlayer().sendMessage(ChatColor.RED + "You've had a rented command expire.");
                    persistYML.set(e.getPlayer().getUniqueId().toString(), null);
                    try {
                        persistYML.save(persist);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
            }
        }

    }
}
