package com.scorchedcode.wolfplzz.Eco;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

public class DarkInit {
    static JavaPlugin mine;
    public static long milleseconds;
    public static int ticks;
    public DarkInit(JavaPlugin mine) {
        DarkInit.mine = mine;
        mine.getServer().getScheduler().scheduleSyncRepeatingTask(mine, new Runnable() {
            @Override
            public void run() {
                DarkInit.milleseconds = (Calendar.getInstance()).getTimeInMillis();
                DarkInit.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DarkInit.getPlugin(), new Runnable() {
                    public void run() {
                        Calendar derp = Calendar.getInstance();
                        long actualTime = (derp.getTimeInMillis() - DarkInit.milleseconds) / 1000;
                        ticks = (int)(100 / actualTime);
                    }
                }, 100L);

            }

        }, 0L, 500L);
    }

    public static boolean hasPerm(Player player, String permission) {
        if(player.hasPermission(permission) || player.isOp())
            return true;
        return false;
    }
    public static JavaPlugin getPlugin() {
        return mine;
    }
    public final static Logger getLog() {
        return Logger.getLogger("Minecraft");
    }
    public static PluginManager getPM() {
        return mine.getServer().getPluginManager();
    }
    public static PluginDescriptionFile pdf() {
        PluginDescriptionFile pdf = mine.getDescription();
        return pdf;
    }

    public static List<Block> blocksFromTwoPoints(Location loc1, Location loc2)
    {
        List<Block> blocks = new ArrayList<Block>();

        int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());

        int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

        for(int x = bottomBlockX; x <= topBlockX; x++)
        {
            for(int z = bottomBlockZ; z <= topBlockZ; z++)
            {
                for(int y = bottomBlockY; y <= topBlockY; y++)
                {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public static String getHeightWidthDepth(List<Block> blocks) {
        int highX = blocks.get(0).getX();
        int lowX = blocks.get(0).getX();
        int highY = blocks.get(0).getY();
        int lowY = blocks.get(0).getY();
        int highZ = blocks.get(0).getZ();
        int lowZ = blocks.get(0).getZ();
        for(Block b : blocks) {
            highX = (b.getX() > highX) ? b.getX() : highX;
            lowX = (b.getX() < lowX) ? b.getX() : lowX;
            highY = (b.getY() > highY) ? b.getY() : highY;
            lowY = (b.getY() < lowY) ? b.getY() : lowY;
            highZ = (b.getZ() > highZ) ? b.getZ() : highZ;
            lowZ = (b.getZ() < lowZ) ? b.getZ() : lowZ;
        }
        return "" + (highY-lowY+1) + ", " + (highX-lowX+1) + ", " + (highZ-lowZ+1);
    }
}
/*Register event types uses pm.registerEvent(event, listener, priority, plugin)*/