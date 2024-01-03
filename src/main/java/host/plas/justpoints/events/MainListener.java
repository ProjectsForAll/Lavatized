package host.plas.justpoints.events;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MainListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (JustPoints.getMainConfig().getPointsOnJoinLoad()) {
            PointPlayer.getOrGetPlayer(player.getUniqueId().toString());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PointPlayer.getOrGetPlayer(player.getUniqueId().toString()).whenComplete((pointPlayer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                if (JustPoints.getMainConfig().getPointsOnQuitSave()) {
                    pointPlayer.save();
                }

                if (JustPoints.getMainConfig().getPointsOnQuitDispose()) {
                    pointPlayer.unregister();
                }
            }
        });
    }
}
