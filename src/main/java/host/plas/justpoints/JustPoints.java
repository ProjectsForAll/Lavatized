package host.plas.justpoints;

import host.plas.justpoints.commands.PointsCMD;
import host.plas.justpoints.config.MainConfig;
import host.plas.justpoints.data.PointPlayer;
import host.plas.justpoints.data.sql.PointsOperator;
import host.plas.justpoints.events.MainListener;
import host.plas.justpoints.managers.PointsManager;
import host.plas.justpoints.papi.PointsExpansion;
import host.plas.justpoints.timers.SyncTimer;
import io.streamlined.bukkit.PluginBase;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

@Getter @Setter
public final class JustPoints extends PluginBase {
    @Getter @Setter
    private static JustPoints instance;

    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static PointsOperator mainDatabase;
    @Getter @Setter
    private static SyncTimer syncTimer;
    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static PointsExpansion expansion;

    @Getter @Setter
    private static PointsCMD pointsCMD;

    public JustPoints() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        setInstance(this);

        setMainConfig(new MainConfig());
        setMainDatabase(new PointsOperator(getMainConfig().buildConnectorSet()));
        getMainDatabase().ensureUsable(); // Test connection

        setExpansion(new PointsExpansion());
        getExpansion().register();

        setSyncTimer(new SyncTimer());

        setPointsCMD(new PointsCMD());
        getPointsCMD().register();

        setMainListener(new MainListener());
        Bukkit.getPluginManager().registerEvents(getMainListener(), this);
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
        getSyncTimer().cancel();

        PointsManager.getLoadedPlayers().forEach(PointPlayer::saveAndUnload);
    }
}
