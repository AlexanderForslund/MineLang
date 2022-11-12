package dev.forslund.mc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

public class CMDDebug implements CommandExecutor  {

    private ArrayList<Score> scores;
    private JavaPlugin p;

    public CMDDebug(JavaPlugin p, ArrayList<Score> scores) {
        this.scores = scores;
        this.p = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Improper usage. Use /interpreter <run | reset>");
            return true;
        }
        
        if (args[0].equals("set") && args.length == 3) {
            setRegistry(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            return true;
        }

        return true;
    }

    private void setRegistry(int regIndex, int val) {
        try {
            scores.get(regIndex).setScore(val);
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Err while setting score, maybe index is wrong?");
        }
    }
}
