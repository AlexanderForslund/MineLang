package dev.forslund.mc;
import dev.forslund.mc.commands.CMDDebug;
import dev.forslund.mc.commands.CMDInterpreter;
import dev.forslund.mc.events.JoinListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;

public class MineLang extends JavaPlugin {
    private Scoreboard sb;
    private ArrayList<Score> scores;

    @Override
    public void onEnable() {
        getLogger().info("MineLang helper is enabled.");

        createScoreBoard();

        getServer().getPluginManager().registerEvents(new JoinListener(sb), this);

        getCommand("interpreter").setExecutor(new CMDInterpreter(this, scores));
        getCommand("debug").setExecutor(new CMDDebug(this, scores));
    }

    @Override
    public void onDisable() {
        getLogger().info("MineLang helper is disabled.");
    }

    private void createScoreBoard() {
        sb = getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = sb.registerNewObjective("register", Criteria.DUMMY, ChatColor.YELLOW + "Registers");
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        Score r0 = objective.getScore("$r0:"); //Get a fake offline player
        r0.setScore(0);

        Score r1 = objective.getScore("$r1:");
        r1.setScore(0);

        Score r2 = objective.getScore("$r2:");
        r2.setScore(0);

        // Final?
        Score zero = objective.getScore("$0:");
        zero.setScore(0);

        scores = new ArrayList<>();
        scores.add(zero);
        scores.add(r0);
        scores.add(r1);
        scores.add(r2);

    }
}