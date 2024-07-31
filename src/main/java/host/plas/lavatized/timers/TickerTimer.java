package host.plas.lavatized.timers;

import host.plas.bou.scheduling.BaseRunnable;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.managers.ArenaManager;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TickerTimer extends BaseRunnable {
    public TickerTimer() {
        super(0, 1);
    }

    @Override
    public void run() {
        ArenaManager.getLoadedArenas().forEach(Arena::tick);
    }
}
