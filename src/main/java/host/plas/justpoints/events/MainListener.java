package host.plas.justpoints.events;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import host.plas.justpoints.managers.PointsManager;
import host.plas.justpoints.utils.MessageUtils;
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
            PointPlayer playerData = PointsManager.getOrGetPlayer(player.getUniqueId().toString());
            playerData.setUsername(player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PointPlayer playerData = PointsManager.getOrGetPlayer(player.getUniqueId().toString());
        if (JustPoints.getMainConfig().getPointsOnQuitSave()) {
            playerData.save();
        }
        if (JustPoints.getMainConfig().getPointsOnQuitDispose()) {
            PointsManager.unloadPlayer(player.getUniqueId().toString(), false);
        }
    }
}
