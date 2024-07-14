package host.plas.justpoints.timers;

import host.plas.bou.scheduling.BaseRunnable;
import host.plas.justpoints.JustPoints;
import host.plas.justpoints.managers.PointsManager;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SyncTimer extends BaseRunnable {
    private LocalDateTime lastSave;

    public SyncTimer() {
        super(0, 1);
    }

    @Override
    public void run() {
        if (lastSave == null) {
            doSaveRun();
        }

        if (lastSave.plusMinutes(JustPoints.getMainConfig().getPointsSaveInterval()).isBefore(LocalDateTime.now())) {
            doSaveRun();
        }
    }

    public void doSaveRun() {
        lastSave = LocalDateTime.now(); // Will be called before the saving so that it will not have issues doing recursion.
        try {
            PointsManager.getLoadedPlayers().forEach(player -> {
                try {
                    long dbLastEdited = JustPoints.getMainDatabase().getLastEditedMillis(player.getIdentifier()).join();
                    if (dbLastEdited > player.getLastEditedMillis()) {
                        player.augment(JustPoints.getMainDatabase().loadPlayer(player.getIdentifier()));
                    } else {
                        player.save();
                    }
                } catch (Exception e) {
                    // do nothing as it will spam the console
                }
            });
        } catch (Exception e) {
            // do nothing as it will spam the console
        }
    }
}
