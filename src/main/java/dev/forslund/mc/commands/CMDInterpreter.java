package dev.forslund.mc.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

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
        String[] r_type = {"add", "sub", "set", "jumpCon"}; // 3 bitar o, 2 bitar $rt, 2 bitar $rs, 1 bit imm
        String[] j_type = {"jump"}; // 3 bitar o, 5 bitar imm
        String[] spec_type = {"input", "print", "exit"}; // (I förekommande ordning) 3 bitar o, 2 bitar $rt, 3 bitar bajs | 3 bitar o, 5 bitar imm eller 3 bitar o, 2 bitar $rt, resterande bajs | 3 bitar o, resterande bajs 

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
                } else {
                    i++; // Fix for temp bug where if you put a lone paper it will infinite loop
                }
            }
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Ouef. (Invalid input)");
        }
    }

    private void add(ItemStack rt, ItemStack rs, ItemStack imm) { // rt = rt + rs + imm (o = 3 bits, rt = 2 bits, rs = 2 bits, imm = 1 bit)
        Score rtScore = getScore(rt);
        if (rtScore.equals(scores.get(0))) { // Check if rt (first arg) == $0, which is NOT allowed.
            p.getServer().broadcastMessage(ChatColor.RED + "Cannot add to $0.");
            return;
        }
        Score rsScore = getScore(rs);
        int immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        if (immVal != 0 && immVal != 1) { // Only 1 bit sadly ):
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value! Maximum 1 bit (0-1)");
            return;
        }
        // dont need to check if > or < int32 max/min because java is default int32
        // it will throw an error anyway
        rtScore.setScore(rtScore.getScore() + rsScore.getScore() + immVal);
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
            return null; // Bad code but whatever.
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


