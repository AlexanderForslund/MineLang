package dev.forslund.mc.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;

public class CMDInterpreter implements CommandExecutor {
    private JavaPlugin p;
    private Chest chest;
    private ArrayList<Score> scores;


    public CMDInterpreter(JavaPlugin p, ArrayList<Score> scores) {
        this.p = p;
        this.scores = scores;
        p.getLogger().info("Interpreter is enabled.");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Improper usage. Use /interpreter <run | reset>");
            return true;
        }

        if (args[0].equals("reset")) {
            for (Score s : scores) {
                s.setScore(0);
            }
            sender.sendMessage(ChatColor.GREEN + "Registers reset.");
            return true;
        }

        if (args[0].equals("run")) {
            sender.sendMessage(ChatColor.GREEN + "Running...");
            interpret();
            sender.sendMessage(ChatColor.GREEN + "Done.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Improper usage. Use /interpreter <run|reset>");
        return true;
    }

    private void interpret() {
        String[] r_type = {"add", "sub", "set"};
        String[] j_type = {"jump", "jumpCon"};
        String[] spec_type = {"input", "print", "exit"};

        FileConfiguration config = p.getConfig();

        chest = (Chest) p.getServer().getWorld("World").getBlockAt(0, 56, 0).getState();
        ItemStack[] itemArray = chest.getInventory().getContents();

        int i = 0; 
        try {
            while (i < itemArray.length-1) { // Tobias dont judge the if statement för switch funkade inte for some anledning
                if (itemArray[i] == null) {
                    i++;
                    continue;
                }
                if (itemArray[i].getType().equals(Material.COMPARATOR)) {
                    // jumCon(itemArray[i+1], itemArray[i+2], itemArray[i+3]); // rt, rs, imm
                    i += 4;
                } else if (itemArray[i].getType().equals(Material.FERN)) {
                    add(itemArray[i+1], itemArray[i+2], itemArray[i+3]); // rt, rs, imm
                    i += 4;
                } else if (itemArray[i].getType().equals(Material.IRON_NUGGET)){
                    // sub(itemArray[i+1], itemArray[i+2], itemArray[i+3]); // rt, rs, imm
                    i += 4;
                } else if (itemArray[i].getType().equals(Material.MAGENTA_GLAZED_TERRACOTTA)){
                    // jump(itemArray[i+1]); // imm
                    i += 2;
                } else if (itemArray[i].getType().equals(Material.ARROW)){
                    // set(itemArray[i+1], itemArray[i+2]); // rt, imm
                    i += 3;
                } 
            }    
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Ouef. (Invalid input)");
        }
    }

    private void add(ItemStack rt, ItemStack rs, ItemStack imm) { // rt = rt + rs + imm
        Score rtScore = getScore(rt);
        Score rsScore = getScore(rs);
        int immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        p.getServer().broadcastMessage(""+rtScore);
        p.getServer().broadcastMessage(""+rsScore);
        p.getServer().broadcastMessage(""+immVal);
        rtScore.setScore(rtScore.getScore() + rsScore.getScore() + immVal);
        p.getServer().broadcastMessage(""+rtScore.getScore());
    }

    private Score getScore(ItemStack rt) {
        // Gosh this is some poo code
        if (rt.getType().equals(Material.IRON_BLOCK)) {
            return scores.get(1); // $r0
        } else if (rt.getType().equals(Material.GOLD_BLOCK)) {
            return scores.get(2); // $r1
        } else if (rt.getType().equals(Material.DIAMOND_BLOCK)) {
            return scores.get(3); // $r2
        } else if (rt.getType().equals(Material.NETHERITE_BLOCK)) {
            return scores.get(0); // $0
        } else {
            p.getServer().broadcastMessage("bad bad");
            return null; // Bad bad code.
        }
    }
}

/*
Variabels:
minecraft:paper
    Represents Int (ex. paper named 100 is Int 100)


Instructions:
minecraft:fern
    add rt rs imm (rt = rt + rs + imm)

minecraft:iron_nugget
    sub rt rs imm (rt = rt - rs - imm)

minecraft:magenta_glazed_terracotta
    jump imm (jumps imm)

minecraft:arrow
    set rt imm (sets rt to imm)

minecraft:comparator
    jump imm if rs == rt


Special:
minecraft:oak_sign
    Prints register $r0 to chat

minecraft:dragon_head
    Exits program

minecraft:name_tag
    Wait for input in pedestal
 */


