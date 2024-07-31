package host.plas.lavatized;

import host.plas.bou.BetterPlugin;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.commands.LavaRisingCMD;
import host.plas.lavatized.config.MainConfig;
import host.plas.lavatized.events.MainListener;
import host.plas.lavatized.managers.ArenaManager;
import host.plas.lavatized.timers.TickerTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

@Getter
@Setter
public final class Lavatized extends BetterPlugin {
    @Getter @Setter
    private static Lavatized instance;

    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static TickerTimer tickerTimer;
    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static LavaRisingCMD lavaRisingCMD;

    public Lavatized() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        setInstance(this);

        setMainConfig(new MainConfig());

        setTickerTimer(new TickerTimer());

        setLavaRisingCMD(new LavaRisingCMD());
        getLavaRisingCMD().register();

        setMainListener(new MainListener());
        Bukkit.getPluginManager().registerEvents(getMainListener(), this);

        ArenaManager.init();
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
        getTickerTimer().cancel();

        ArenaManager.getLoadedArenas().forEach(Arena::resetIfNecessary);
    }
}
