package dev.forslund.mc.commands;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Lectern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;

public class CMDInterpreter implements CommandExecutor {
    private JavaPlugin p;
    private Chest chest;
    private ArrayList<Score> scores;
    private int square;
    private ItemStack[] itemArray;
    private Player sender;

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

        if (args[0].equals("reset") && (sender instanceof Player)) {
            for (Score s : scores) {
                s.setScore(0);
            }
            sender.sendMessage(ChatColor.GREEN + "Registers reset.");
            return true;
        }

        if (args[0].equals("run") && (sender instanceof Player)) {
            sender.sendMessage(ChatColor.GREEN + "Running...");
            for (Score s : scores) {
                s.setScore(0);
            }

            this.sender = (Player) sender;
            square = 0;
            interpret();
            sender.sendMessage(ChatColor.GREEN + "Done.");
            return true;
        }

        if (args[0].equals("onInput") && (sender instanceof CommandSender)) {
            sender.sendMessage(ChatColor.GREEN + "INPUT!");
            readInput();
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Improper usage. Use /interpreter <run|reset>");
        return true;
    }

    private void interpret() {
        // String[] r_type = { "add", "jumpCon" }; // 3 bitar o, 2 bitar $rt, 2 bitar $rs, 1 bit imm (add imm = sign 0=(+)
                                                // 1=(-))
        // String[] j_type = { "jump" }; // 3 bitar o, 5 bitar imm
        // String[] spec_type = { "input", "print", "set", "exit", "addi" }; // (I förekommande ordning) 3 bitar o, 2 bitar
                                                                          // $rt, 3 bitar
        // bajs | 3 bitar o, 5 bitar imm eller 3 bitar o, 2 bitar
        // $rt, resterande bajs | 3 bitar, 2 bitar rt, 3 bitar imm | 3 bitar o,
        // resterande bajs

        String[] instructionBlocks = {"COMPARATOR", "FERN", "NETHER_STAR", "MAGENTA_GLAZED_TERRACOTTA", "ARROW", "DRAGON_HEAD", "OAK_SIGN", "NAME_TAG"};

        try {
            chest = (Chest) p.getServer().getWorld("World").getBlockAt(0, 56, 0).getState();
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Chest does not exist at x: 0, y: 56, z: 0");
            return;
        }
        itemArray = chest.getInventory().getContents();
        // p.getServer().broadcastMessage("" + itemArray.length);

        try {
            while (square < itemArray.length - 1) { // Tobias dont judge the if statement för switch funkade inte for
                                                    // some anledning
                if (itemArray[square] == null) {
                    square++;
                    continue;
                }
                if (!Arrays.toString(instructionBlocks).contains(itemArray[square].getType().name())) {
                    square++;
                    continue;
                }

                if (itemArray[square].getType().equals(Material.COMPARATOR)) {
                    jumCon(itemArray[square + 1], itemArray[square + 2], itemArray[square + 3]); // rt, rs, imm
                    square += 4;
                } else if (itemArray[square].getType().equals(Material.FERN)) {
                    addi(itemArray[square + 1], itemArray[square + 2]); // rt, imm
                    square += 3;
                } else if (itemArray[square].getType().equals(Material.NETHER_STAR)) {
                    add(itemArray[square + 1], itemArray[square + 2], itemArray[square + 3]);
                    square += 4;
                } else if (itemArray[square].getType().equals(Material.MAGENTA_GLAZED_TERRACOTTA)) {
                    jump(itemArray[square + 1], square); // imm
                } else if (itemArray[square].getType().equals(Material.ARROW)) {
                    set(itemArray[square + 1], itemArray[square + 2]); // rt, imm
                    square += 3;
                } else if (itemArray[square].getType().equals(Material.DRAGON_HEAD)) {
                    break; // Is this alllll?!?!??!!
                } else if (itemArray[square].getType().equals(Material.OAK_SIGN)) {
                    print(itemArray[square + 1]);
                    square += 2;
                } else if (itemArray[square].getType().equals(Material.NAME_TAG)) {
                    input(itemArray[square + 1]);
                    break;
                } else {
                    square++; // Fix for temp bug where if you put a lone paper it will infinite loop
                }
            }
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Ouef. (Invalid input)");
            e.printStackTrace();
        }
    }

    private void print(ItemStack rt) {
        Score rtScore = getScore(rt);
        p.getServer().broadcastMessage(ChatColor.BLUE + "" + rtScore.getScore());
    }

    private void jumCon(ItemStack rt, ItemStack rs, ItemStack imm) {
        Score rtScore = getScore(rt);
        Score rsScore = getScore(rs);

        int immVal = -1; // inval
        try {
            immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value!");
        }

        if (rtScore.getScore() == rsScore.getScore() && immVal == 0) {
            square++;
        } else if (rtScore.getScore() != rsScore.getScore() && immVal == 1) {
            square++;
        }
    }

    private void add(ItemStack rt, ItemStack rs, ItemStack imm) { // rt = rt + rs + imm (o = 3 bits, rt = 2 bits, rs = 2
                                                                  // bits, imm = 1 bit) (r-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }

        Score rsScore = getScore(rs);
        int immVal = -1; // inval
        try {
            immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value!");
        }
        
        if (immVal == 0) {
            rtScore.setScore(rtScore.getScore() + rsScore.getScore());
        } else if (immVal == 1) {
            rtScore.setScore(rtScore.getScore() - rsScore.getScore());
        } else {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value! Maximum 1 bit (0-1), where 0 = + and 1 = -");
        }
    }

    private void addi(ItemStack rt, ItemStack imm) { // rt = rt + imm (o = 3 bits, rt = 2 bits, imm = 3 bit) (r-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }

        int immVal = -69; // Inval
        try {
            immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value!");
        }
        if (immVal >= -4 && immVal <= 3) {
            rtScore.setScore(rtScore.getScore() + immVal);
        } else {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value! Maximum 3 bit, first bit is sign either - or none and values range from -4 to 3");
        }
    }

    private void jump(ItemStack imm, int square) {
        int immVal = 0;
        try {
            immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value!");
        }
        jumpInstructions(immVal);
    }

    private void jumpInstructions(int jumps) {
        String[] instructionBlocks = {"COMPARATOR", "FERN", "NETHER_STAR", "MAGENTA_GLAZED_TERRACOTTA", "ARROW", "DRAGON_HEAD", "OAK_SIGN", "NAME_TAG"};
        while (jumps < 0) {
            square--;
            while (!Arrays.toString(instructionBlocks).contains(itemArray[square].getType().name())){
                square--;
            }
            jumps++;
        }

        while (jumps > 0) {
            square++;
            while (!Arrays.toString(instructionBlocks).contains(itemArray[square].getType().name())){
                square++;
            }
            jumps--;
        }
    }

    private void set(ItemStack rt, ItemStack imm) { // rt = imm (o = 3 bits, rt = 2 bits, imm = 3 bits) (special-type)
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }

        int immVal = -69; // Inval
        try {
            immVal = Integer.parseInt(imm.getItemMeta().getDisplayName());
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value!");
        }

        if (immVal < -4 || 3 < immVal) { // Only 3 bit happy :)
            p.getServer().broadcastMessage(ChatColor.RED + "Bad immediate value! Maximum 3 bit, first bit is sign either - or none and values range from -4 to 3");
            return;
        }
        rtScore.setScore(immVal);
    }

    private void input(ItemStack rt) { // 3 bits O, 2 bits $rt, resten bajs
        Score rtScore = getScore(rt);
        if (isZeroRegister(rtScore)) {
            return;
        }
        chest.getLocation().add(1, 0, 0).getBlock().setType(Material.LECTERN);
        Lectern lectern = (Lectern) chest.getLocation().add(1, 0, 0).getBlock().getState();
        // chest.getLocation().add(0, -2, -2).getBlock().setType(Material.AIR);

        // Spawn effect
        Location l = lectern.getLocation();
        p.getServer().getWorld("world").playEffect(l, Effect.END_GATEWAY_SPAWN, 1000);

        // Give sender a book (how nice of me)
        sender.getInventory().setItemInMainHand(new ItemStack(Material.WRITABLE_BOOK, 1));
    }

    private void readInput() {
        try {
            Lectern lectern = (Lectern) chest.getLocation().add(1, 0, 0).getBlock().getState();
            ItemStack[] bookArray = lectern.getInventory().getContents();
            p.getServer().getWorld("world").playEffect(lectern.getLocation(), Effect.GHAST_SHOOT, 1000);
            lectern.getLocation().getBlock().setType(Material.AIR);
    
            String[] bookSplit = bookArray[0].getItemMeta().getAsString().split("\"");
            int bookVal = Integer.parseInt(bookSplit[1]); // Value written inside book
    
            Score rtScore = getScore(itemArray[square + 1]);
            rtScore.setScore(bookVal);
            square += 2;
            interpret();    
        } catch (Exception e) {
            p.getServer().broadcastMessage(ChatColor.RED + "Fatal error while reading input. Maybe the book was empty?");
            return;
        }
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
