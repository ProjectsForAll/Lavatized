package host.plas.lavatized.events;

import host.plas.lavatized.arenas.players.ArenaPlayer;
import host.plas.lavatized.managers.ArenaManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MainListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ArenaPlayer arenaPlayer = ArenaPlayer.wrap(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ArenaPlayer arenaPlayer = ArenaPlayer.wrap(player);
        // save player data
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        ArenaManager.getLoadedArenas().forEach(arena -> arena.onDeathEvent(event));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (! (entity instanceof Player)) return;
        Player player = (Player) entity;

        ArenaPlayer arenaPlayer = ArenaPlayer.wrap(player);
        arenaPlayer.getPlayState().ifPresent(playState -> {
            if (playState.isInvulnerable()) {
                event.setCancelled(true);
            }
        });
    }
}
