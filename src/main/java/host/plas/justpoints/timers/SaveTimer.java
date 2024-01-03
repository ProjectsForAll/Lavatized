package host.plas.justpoints.timers;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import io.streamlined.bukkit.instances.BaseRunnable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SaveTimer extends BaseRunnable {
    private LocalDateTime lastSave;

    public SaveTimer() {
        super(0, 1, true);
    }

    @Override
    public void execute() {
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
            PointPlayer.getPlayers().forEach(PointPlayer::save);
        } catch (Exception e) {
            // do nothing as it will spam the console
        }
    }
}
