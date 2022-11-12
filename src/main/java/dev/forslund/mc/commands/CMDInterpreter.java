package dev.forslund.mc.commands;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.ThrownExpBottle;
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
        String[] r_type = { "add", "jumpCon" };  // 3 bitar o, 2 bitar $rt, 2 bitar $rs, 1 bit imm (add imm = sign 0=(+) 1=(-))
        String[] j_type = { "jump" };   // 3 bitar o, 5 bitar imm
        String[] spec_type = {"input", "print", "set", "exit", "addi" }; // (I förekommande ordning) 3 bitar o, 2 bitar $rt, 3 bitar
                                                           // bajs | 3 bitar o, 5 bitar imm eller 3 bitar o, 2 bitar
                                                           // $rt, resterande bajs | 3 bitar, 2 bitar rt, 3 bitar imm | 3 bitar o, resterande bajs

        FileConfiguration config = p.getConfig();

        chest = (Chest) p.getServer().getWorld("World").getBlockAt(0, 56, 0).getState();
        ItemStack[] itemArray = chest.getInventory().getContents();

        int square = 0;
        try {
            while (square < itemArray.length - 1) { // Tobias dont judge the if statement för switch funkade inte for some
                                               // anledning
                if (itemArray[square] == null) {
                    square++;
                    continue;
                }
                if (itemArray[square].getType().equals(Material.COMPARATOR)) {
                    // jumCon(itemArray[i+1], itemArray[i+2], itemArray[i+3]); // rt, rs, imm
                    square += 4;
                } else if (itemArray[square].getType().equals(Material.FERN)) {
                    add(itemArray[square + 1], itemArray[square + 2], itemArray[square + 3]); // rt, rs, imm
                    square += 4;
                } else if (itemArray[square].getType().equals(Material.IRON_NUGGET)) {
                    sub(itemArray[square + 1], itemArray[square + 2], itemArray[square + 3]); // rt, rs, imm
                    square += 4;
                } else if (itemArray[square].getType().equals(Material.MAGENTA_GLAZED_TERRACOTTA)) {
                    square = jump(itemArray[square+1], square); // imm
                } else if (itemArray[square].getType().equals(Material.ARROW)) {
                    set(itemArray[square+1], itemArray[square+2]); // rt, imm
                    square += 3;
                } else if (itemArray[square].getType().equals(Material.NAME_TAG)){
                    input(itemArray[square+1]);
                    square += 2;
                }
                
                else {
                    square++; // Fix for temp bug where if you put a lone paper it will infinite loop
                }
            }
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Ouef. (Invalid input)");
            e.printStackTrace();
        }
    }

    private void add(ItemStack rt, ItemStack rs, ItemStack imm) { // rt = rt + rs + imm (o = 3 bits, rt = 2 bits, rs = 2 bits, imm = 1 bit) (r-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
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

    private void sub(ItemStack rt, ItemStack rs, ItemStack imm) { // rt = rt - rs - imm (o = 3 bits, rt = 2 bits, rs = 2 bits, imm = 1 bit) (r-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
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
        rtScore.setScore(rtScore.getScore() - rsScore.getScore() - immVal);
    }

    private int jump(ItemStack imm, int square) {
        int immValue = Integer.parseInt(imm.getItemMeta().getDisplayName());
        return square - immValue;
    }

    private void set(ItemStack rt, ItemStack imm) { // rt = imm (o = 3 bits, rt = 2 bits, imm = 3 bits) (special-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }

        int immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        if (immVal < 0 || 7 < immVal) { // Only 3 bit happy :)
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value! Maximum 3 bit (0-7)");
            return;
        }
        // dont need to check if > or < int32 max/min because java is default int32
        // it will throw an error anyway
        rtScore.setScore(immVal);
    }

    private void input(ItemStack rt) { // 3 bits O, 2 bits $rt, resten bajs
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }
        chest.getLocation().add(0, -2, -2).getBlock().setType(Material.REDSTONE_BLOCK);
        Lectern lectern = (Lectern) chest.getLocation().add(-2, 0, 0).getBlock().getState();

        // Spawn effect
        Location l = lectern.getLocation();
        p.getServer().getWorld("world").playEffect(l, Effect.END_GATEWAY_SPAWN, 1000);
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
            return null; // Bad code but whatever.
        }
    }

    private boolean isZeroRegister(Score rtScore) {
        if (rtScore.equals(scores.get(0))) { // Check if rt (first arg) == $0, which is NOT allowed.
            p.getServer().broadcastMessage(ChatColor.RED + "Cannot set $0.");
            return true;
        }
        return false;

    }
}

/*
 * Variabels:
 * minecraft:paper
 * Represents Int (ex. paper named 100 is Int 100)
 * 
 * 
 * Instructions:
 * minecraft:fern
 * add rt rs imm (rt = rt + rs + imm)
 * 
 * minecraft:iron_nugget
 * sub rt rs imm (rt = rt - rs - imm)
 * 
 * minecraft:magenta_glazed_terracotta
 * jump imm (jumps imm)
 * 
 * minecraft:arrow
 * set rt imm (sets rt to imm)
 * 
 * minecraft:comparator
 * jump imm if rs == rt
 * 
 * 
 * Special:
 * minecraft:oak_sign
 * Prints register $r0 to chat
 * 
 * minecraft:dragon_head
 * Exits program
 * 
 * minecraft:name_tag
 * Wait for input in pedestal
 */
