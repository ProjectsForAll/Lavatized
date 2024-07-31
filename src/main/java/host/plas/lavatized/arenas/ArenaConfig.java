package host.plas.lavatized.arenas;

import host.plas.lavatized.Lavatized;
import host.plas.lavatized.managers.ArenaManager;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ArenaConfig implements Identifiable {
    private String identifier;

    private String name;
    private String lobbyWorld;

    private double centerX;
    private double centerZ;
    private double maxRadius;
    private double minRadius;
    private long shrinkTime;

    private double lobbyX, lobbyY, lobbyZ;
    private float lobbyYaw, lobbyPitch;

    private long gracePeriodMaxTicks;
    private long ticksPerLayer;
    private List<String> itemStrings;

    private List<String> possibleSeeds;

    private boolean resetOnUnload;

    public ArenaConfig(String identifier, String name, String lobbyWorld, double minRadius, double centerX, double centerZ, double maxRadius, long shrinkTime,
                       double lobbyX, double lobbyY, double lobbyZ, float lobbyYaw, float lobbyPitch, long gracePeriodMaxTicks, long ticksPerLayer,
                       List<String> itemStrings, List<String> possibleSeeds, boolean resetOnUnload) {
        this.identifier = identifier;
        this.name = name;
        this.lobbyWorld = lobbyWorld;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.shrinkTime = shrinkTime;
        this.lobbyX = lobbyX;
        this.lobbyY = lobbyY;
        this.lobbyZ = lobbyZ;
        this.lobbyYaw = lobbyYaw;
        this.lobbyPitch = lobbyPitch;
        this.gracePeriodMaxTicks = gracePeriodMaxTicks;
        this.ticksPerLayer = ticksPerLayer;
        this.itemStrings = itemStrings;
        this.possibleSeeds = possibleSeeds;
        this.resetOnUnload = resetOnUnload;
    }

    public ArenaConfig(String identifier) {
        this(identifier, identifier, "spawn", 0d, 0d, 249.5, 9.5, 36000,
                0d, 0d, 0d, 0f, 0f, 0, 0,
                new ArrayList<>(), new ArrayList<>(), true);
    }

    public void load() {
        ArenaManager.loadConfig(this);
    }

    public void unload() {
        ArenaManager.unloadConfig(getIdentifier());
    }

    public void save() {
        Lavatized.getMainConfig().saveArenaConfigToConfig(this);
    }
}
