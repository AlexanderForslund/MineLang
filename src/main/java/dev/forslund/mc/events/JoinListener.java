package dev.forslund.mc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;

public class JoinListener implements Listener {
    private Scoreboard sb;

    public JoinListener(Scoreboard sb) {
        this.sb = sb;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setScoreboard(sb);

        player.sendTitle(ChatColor.GREEN + "MineLANG", ChatColor.RED + "ඞ When the language is SUS ඞ", 50, 100, 25);
    }
}
