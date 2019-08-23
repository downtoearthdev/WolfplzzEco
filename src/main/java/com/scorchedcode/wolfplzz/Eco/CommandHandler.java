package com.scorchedcode.wolfplzz.Eco;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CommandHandler {

    public String customRentMsg;
    public ArrayList<PayCommand> loadedCommands = new ArrayList<>();
    private static CommandHandler instance = null;


    private CommandHandler() {
        customRentMsg = DarkInit.getPlugin().getConfig().getString("rent-expire-message");
        for(String sec : DarkInit.getPlugin().getConfig().getConfigurationSection("pay-commands").getKeys(false)) {
            ConfigurationSection payCommand = DarkInit.getPlugin().getConfig().getConfigurationSection("pay-commands." + sec);
            if(payCommand.getString("type").equals("rent")) {
                String init = (payCommand.getString("run-as-server", "null").equals("null") ? payCommand.getString("run-as-player") : payCommand.getString("run-as-server"));
                String end = (payCommand.getString("run-as-server", "null").equals("null") ? payCommand.getString("expire-as-player") : payCommand.getString("expire-as-server"));
                String[] commands = {init, end};
                PayCommand cmd = new PayCommand(Type.RENT, sec, payCommand.getInt("price"),
                        commands, payCommand.getInt("time"),
                        payCommand.getInt("warning"));
                loadedCommands.add(cmd);
            }
            else if(payCommand.getString("type").equals("buy")){
                PayCommand cmd = new PayCommand(Type.BUY, sec, payCommand.getInt("price"), payCommand.getString("permission"));
                loadedCommands.add(cmd);
            }
            else {
                String init = (payCommand.getString("run-as-server", "null").equals("null") ? payCommand.getString("run-as-player") : payCommand.getString("run-as-server"));
                PayCommand cmd = new PayCommand(Type.BUY_ONCE, sec, payCommand.getInt("price"), init);
                loadedCommands.add(cmd);
            }
        }
    }

    public static CommandHandler getInstance() {
        if(instance == null)
            instance = new CommandHandler();
        return instance;
    }

    public void resetHandler() {
        loadedCommands.clear();
        instance = null;
    }


    class PayCommand {
        private Type type;
        private int price;
        private String name;
        private String permission;
        private String initCommand;
        private String endCommand;
        private int time, warning;

        public PayCommand(Type type, String name, int price,  String[] commands, int time, int warning) {
            this.type = type;
            this.name = name;
            this.price = price;
            this.initCommand = commands[0];
            this.endCommand = commands[1];
            this.time = time;
            this.warning = warning;
        }

        public PayCommand(Type type, String name, int price, String permission) {
            this.type = type;
            this.name = name;
            this.price = price;
            this.permission = permission;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }


        public boolean buy(Player p) {
            if(((WolfplzzEco)DarkInit.getPlugin()).vaultEco.has(p, price)) {
                ((WolfplzzEco)DarkInit.getPlugin()).vaultEco.withdrawPlayer(p, price);
                ((WolfplzzEco)DarkInit.getPlugin()).vaultPerm.playerAdd(p, permission);
                return true;
            }
            else
                return false;
        }

        public boolean buyOnce(Player p) {
            if(((WolfplzzEco)DarkInit.getPlugin()).vaultEco.has(p, price)) {
                ((WolfplzzEco)DarkInit.getPlugin()).vaultEco.withdrawPlayer(p, price);
                if(permission.contains("{player}"))
                Bukkit.dispatchCommand((permission.contains("{player}") ? Bukkit.getConsoleSender() : p), permission.replaceAll("\\{player}", p.getName()));
                return true;
            }
            else
                return false;
        }

        public boolean startRent(Player p) {
            if(((WolfplzzEco)DarkInit.getPlugin()).vaultEco.has(p, price)) {
                ((WolfplzzEco)DarkInit.getPlugin()).vaultEco.withdrawPlayer(p, price);
                RentRunner runner = new RentRunner(p);
                runner.setId(DarkInit.getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(DarkInit.getPlugin(),
                        runner, ((time*1200) - (warning * 1200)), warning * 1200));
                Bukkit.dispatchCommand((initCommand.contains("{player}") ? Bukkit.getConsoleSender() : p), initCommand.replaceAll("\\{player}", p.getName()));
                return true;
            }
            else
                return false;
        }

        class RentRunner implements Runnable {

            private UUID player;
            private int id;
            private boolean firstpass;
            public RentRunner(Player p) {
                this.player = p.getUniqueId();
            }

            public void setId(int id) {
                this.id = id;
            }

            @Override
            public void run() {
                if(Bukkit.getOfflinePlayer(player).isOnline()) {
                    if (!firstpass) {
                        firstpass = true;
                        Bukkit.getPlayer(player).sendMessage(ChatColor.LIGHT_PURPLE + customRentMsg.replaceAll("\\{command}", name + " ").replaceAll("\\{timeleft}", String.valueOf(warning) + " minutes "));
                    } else {
                        Bukkit.getPlayer(player).sendMessage(ChatColor.LIGHT_PURPLE + "Your command rental has expired!");
                        DarkInit.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DarkInit.getPlugin(), () -> {
                            Bukkit.dispatchCommand((endCommand.contains("{player}") ? Bukkit.getConsoleSender() : Bukkit.getPlayer(player)), endCommand.replaceAll("\\{player}", Bukkit.getPlayer(player).getName() + " "));
                        });
                        DarkInit.getPlugin().getServer().getScheduler().cancelTask(id);
                    }
                }
                else {
                    if(!firstpass)
                        firstpass = true;
                    else {
                        //Persist to disable command on player login
                        File persist = new File(DarkInit.getPlugin().getDataFolder().getAbsolutePath() + File.separatorChar + "revoke.yml");
                        if(!persist.exists()) {
                            try {
                                persist.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        YamlConfiguration persistYML = YamlConfiguration.loadConfiguration(persist);
                        persistYML.set(player.toString(), endCommand);
                        try {
                            persistYML.save(persist);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DarkInit.getPlugin().getServer().getScheduler().cancelTask(id);
                    }
                }
            }
        }
    }


}
