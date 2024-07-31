package host.plas.lavatized.arenas;

import host.plas.bou.MessageUtils;
import host.plas.bou.scheduling.TaskManager;
import host.plas.lavatized.arenas.lava.LavaHolder;
import host.plas.lavatized.arenas.players.ArenaPlayer;
import host.plas.lavatized.arenas.radius.ArenaRadius;
import host.plas.lavatized.managers.ArenaManager;
import host.plas.lavatized.utils.ItemUtils;
import host.plas.lavatized.utils.LocationUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import tv.quaint.objects.Identifiable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

@Getter @Setter
public class Arena implements Identifiable {
    private String configIdentifier;
    private int index;

    @Override
    public String getIdentifier() {
        return configIdentifier;
    }

    @Override
    public void setIdentifier(String s) {
        // Do nothing
    }

    private String name;
    private Location lobby;

    private ArenaRadius radius;

    private long gracePeriodMaxTicks;
    private long gracePeriodCurrentTicks = 0;

    private List<String> itemStrings;
    private List<String> possibleSeeds;

    private boolean resetOnUnload;

    private List<ItemStack> spawnItems;

    private String seed;

    private ConcurrentSkipListSet<ArenaPlayer> players = new ConcurrentSkipListSet<>();

    private ArenaState state;

    private String worldString;

    private boolean running;

    private LavaHolder lavaHolder;

    public World getWorld() {
        return Bukkit.getWorld(worldString);
    }

    public Arena(ArenaConfig config) {
        this.configIdentifier = config.getIdentifier();
        this.index = ArenaManager.getNextArenaIndex();

        this.name = config.getName();

        this.radius = new ArenaRadius(this, config.getMaxRadius(), config.getMinRadius(), config.getShrinkTime(), config.getCenterX(), config.getCenterZ());

        this.lobby = LocationUtils.getLocationOf(config.getLobbyWorld(), config.getLobbyX(), config.getLobbyY(), config.getLobbyZ(), config.getLobbyYaw(), config.getLobbyPitch());

        this.gracePeriodMaxTicks = config.getGracePeriodMaxTicks();
        this.resetOnUnload = config.isResetOnUnload();

        this.itemStrings = config.getItemStrings();
        this.possibleSeeds = config.getPossibleSeeds();

        this.spawnItems = new ArrayList<>();
        config.getItemStrings().forEach(itemString -> {
            spawnItems.add(ItemUtils.getItem(itemString));
        });

        this.seed = pickSeed(config.getPossibleSeeds());

        this.worldString = UUID.randomUUID().toString();
        buildWorld(worldString);
        TaskManager.getScheduler().runTaskLater(() -> {
            radius.updateBorder(getWorld().getWorldBorder());
            this.lavaHolder = new LavaHolder(this, config.getTicksPerLayer());
        }, 3 * 20);

        this.state = ArenaState.WAITING;

        this.running = false;
    }

    public void buildWorld(String worldString) {
        LocationUtils.createWorld(worldString, seed);
    }

    public static String pickSeed(List<String> strings) {
        int max = strings.size();
        int min = 0;
        Random RNG = new Random();
        return strings.get(RNG.nextInt(max - min) + min);
    }

    public void load() {
        ArenaManager.loadArena(this);
    }

    public void unload() {
        ArenaManager.unloadArena(getIdentifier());
    }

    public void onRandomTeleport(Player player) {
        WorldBorder border = getWorld().getWorldBorder();
        Location min = new Location(getWorld(), border.getCenter().getX() - radius.getCurrentRadius(), 0, border.getCenter().getZ() - radius.getCurrentRadius());
        Location max = new Location(getWorld(), border.getCenter().getX() + radius.getCurrentRadius(), 0, border.getCenter().getZ() + radius.getCurrentRadius());

        final Location rtpSpot = LocationUtils.getRandomTopLocation(min, max);

        LocationUtils.teleport(player, rtpSpot);
    }

    public void onClearBad(Player player) {
        try {
            player.clearActivePotionEffects();
        } catch (Error e) {
            // Do nothing
        }
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        try {
            player.setFreezeTicks(0);
        } catch (Error e) {
            // Do nothing
        }
        player.setRemainingAir(player.getMaximumAir());
    }

    public void onSpawnIn(ArenaPlayer player) {
        player.getPlayer().ifPresent(p -> {
            onRandomTeleport(p);
            onClearBad(p);
            clearInventory(p);
            giveStarterKit(p);
        });
    }

    public void teleportLobby(Player player) {
        LocationUtils.teleport(player, lobby);
    }

    public void onFinish() {
        state = ArenaState.ENDING;

        getPlayers().forEach(player -> {
            player.getPlayer().ifPresent(p -> {
                teleportLobby(p);
                broadcastWon(p);
            });

            player.voidPlayState();
            player.addWin();
        });

        running = false;

        reset();
    }

    public void reset() {
        state = ArenaState.RESETTING;

        getPlayers().forEach(this::onLeave);

        getWorld().getPlayers().forEach(this::teleportLobby);

        Bukkit.unloadWorld(getWorld(), false);
        getWorld().getWorldFolder().delete();

        unload();
    }

    public void onStart() {
        state = ArenaState.STARTING;

        getPlayers().forEach(this::onSpawnIn);
        running = true;
        state = ArenaState.RUNNING;

        broadcast("&7The game has started! &8(&f" + getPlayers().size() + " &7players&8)");
    }

    public void broadcastWon(Player player) {
        broadcastServer("&b" + player.getName() + " &7has &a&lwon &c&lLavaRising &7on arena &b" + getName() + "&8!");
    }

    public void broadcastServer(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, message));
    }

    public void clearInventory(Player player) {
        player.getInventory().clear();
    }

    public void giveStarterKit(Player player) {
        spawnItems.forEach(player.getInventory()::addItem);
    }

    public boolean isGracePeriod() {
        return gracePeriodCurrentTicks < gracePeriodMaxTicks;
    }

    public void tick() {
        if (isRunning()) {
            if (isGracePeriod()) {
                gracePeriodTick();
            } else {
                getRadius().tick();
                getLavaHolder().tick();
            }

            checkAndFinish();
        }
    }

    public void removePlayer(String identifier) {
        getPlayers().removeIf(player -> player.getIdentifier().equalsIgnoreCase(identifier));
    }

    public void removePlayer(ArenaPlayer player) {
        removePlayer(player.getIdentifier());
    }

    public void onLeave(ArenaPlayer player) {
        removePlayer(player);
        clearInventory(player.getPlayer().get());
        onClearBad(player.getPlayer().get());

        player.onLeave(this);

        checkAndFinish();
    }

    public boolean ownsPlayer(String identifier) {
        return getPlayers().stream().anyMatch(player -> player.getIdentifier().equalsIgnoreCase(identifier));
    }

    public boolean ownsPlayer(ArenaPlayer player) {
        return ownsPlayer(player.getIdentifier());
    }

    public void onDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ArenaPlayer arenaPlayer = ArenaPlayer.wrap(player);
        if (ownsPlayer(arenaPlayer)) {
            arenaPlayer.onDeath(this);
        }

        checkAndFinish();
    }

    public void checkAndFinish() {
        if (getPlayers().size() <= 1) {
            onFinish();
        }
    }

    public void gracePeriodTick() {
        gracePeriodCurrentTicks ++;
    }

    public void addPlayer(ArenaPlayer player) {
        getPlayers().add(player);
    }

    public void onJoin(ArenaPlayer player) {
        addPlayer(player);
        broadcastWaitingJoin(player);
    }

    public void broadcastWaitingJoin(ArenaPlayer player) {
        broadcast("&b" + player.getOfflinePlayer().getName() + " &7has joined the game! &8(&f" + getPlayers().size() + " &awaiting&7...&8)");
    }

    public void broadcast(String message) {
        getPlayers().forEach(player -> player.getPlayer().ifPresent(p -> MessageUtils.sendMessage(p, message)));
    }

    public static Arena wrap(String identifier) {
        return ArenaManager.isArenaLoaded(identifier) ? ArenaManager.getArena(identifier).get() : new Arena(ArenaManager.getConfig(identifier).get());
    }

    public static Arena wrap(ArenaConfig config) {
        return ArenaManager.isArenaLoaded(config.getIdentifier()) ? ArenaManager.getArena(config.getIdentifier()).get() : new Arena(config);
    }

    public void saveToConfig() {
        ArenaConfig config = ArenaManager.getConfig(getIdentifier()).get();
        config.setIdentifier(getIdentifier());
        config.setName(getName());
        config.setLobbyWorld(getLobby().getWorld().getName());
        config.setLobbyX(getLobby().getX());
        config.setLobbyY(getLobby().getY());
        config.setLobbyZ(getLobby().getZ());
        config.setLobbyYaw(getLobby().getYaw());
        config.setLobbyPitch(getLobby().getPitch());
        config.setCenterX(getRadius().getCenterX());
        config.setCenterZ(getRadius().getCenterZ());
        config.setMaxRadius(getRadius().getMaxRadius());
        config.setMinRadius(getRadius().getMinRadius());
        config.setShrinkTime(getRadius().getShrinkTime());
        config.setGracePeriodMaxTicks(getGracePeriodMaxTicks());
        config.setTicksPerLayer(getLavaHolder().getTicksPerLayer());
        config.setPossibleSeeds(getPossibleSeeds());
        config.setItemStrings(getItemStrings());
        config.setResetOnUnload(isResetOnUnload());

        config.save();
    }

    public Material getLavaMaterial() {
        return Material.LAVA;
    }

    public static List<String> getFillMaterialStrings() {
        return List.of(
                // Contains
                "DOOR",
                "TRAPDOOR",
                "FENCE",
                "SLAB",
                "BUTTON",
                "PRESSURE_PLATE",
                "PLATE",
                "TORCH",
                "WALL",
                "AIR",
                "CAVE",
                "VOID",
                "WATER",
                "FLOWING",
                "BARS",

                // Equals
                "AIR",
                "CAVE_AIR",
                "VOID_AIR",
                "WATER",
                "OBSIDIAN",
                "STRUCTURE_VOID",
                "OAK_DOOR",
                "SPRUCE_DOOR",
                "ACACIA_DOOR",
                "DARK_OAK_DOOR",
                "BIRCH_DOOR",
                "JUNGLE_DOOR",
                "CRIMSON_DOOR",
                "WARPED_DOOR",
                "MANGROVE_DOOR",
                "IRON_DOOR",
                "UPPER_OAK_DOOR",
                "UPPER_SPRUCE_DOOR",
                "UPPER_ACACIA_DOOR",
                "UPPER_DARK_OAK_DOOR",
                "UPPER_BIRCH_DOOR",
                "UPPER_JUNGLE_DOOR",
                "UPPER_CRIMSON_DOOR",
                "UPPER_WARPED_DOOR",
                "UPPER_MANGROVE_DOOR",
                "UPPER_IRON_DOOR",
                "OAK_TRAPDOOR",
                "SPRUCE_TRAPDOOR",
                "ACACIA_TRAPDOOR",
                "DARK_OAK_TRAPDOOR",
                "BIRCH_TRAPDOOR",
                "JUNGLE_TRAPDOOR",
                "CRIMSON_TRAPDOOR",
                "WARPED_TRAPDOOR",
                "MANGROVE_TRAPDOOR",
                "TRAPDOOR",
                "IRON_TRAPDOOR",
                "FENCE",
                "FENCE_GATE",
                "IRON_BARS",
                "STAINED_GLASS_PANE",
                "NETHER_FENCE",
                "OAK_SLAB",
                "SPRUCE_SLAB",
                "BIRCH_SLAB",
                "JUNGLE_SLAB",
                "ACACIA_SLAB",
                "DARK_OAK_SLAB",
                "CRIMSON_SLAB",
                "WARPED_SLAB",
                "MANGROVE_SLAB",
                "STONE_SLAB",
                "SMOOTH_STONE_SLAB",
                "SANDSTONE_SLAB",
                "PETRIFIED_OAK_SLAB",
                "COBBLESTONE_SLAB",
                "BRICK_SLAB",
                "STONE_BRICK_SLAB",
                "NETHER_BRICK_SLAB",
                "QUARTZ_SLAB",
                "RED_SANDSTONE_SLAB",
                "COBBLESTONE_SLAB",
                "CUT_SANDSTONE_SLAB",
                "CUT_RED_SANDSTONE_SLAB",
                "PURPUR_SLAB",
                "PRISMARINE_SLAB",
                "PRISMARINE_BRICK_SLAB",
                "DARK_PRISMARINE_SLAB",
                "OAK_BUTTON",
                "SPRUCE_BUTTON",
                "BIRCH_BUTTON",
                "JUNGLE_BUTTON",
                "ACACIA_BUTTON",
                "DARK_OAK_BUTTON",
                "CRIMSON_BUTTON",
                "WARPED_BUTTON",
                "MANGROVE_BUTTON",
                "STONE_BUTTON",
                "WEIGHTED_PRESSURE_PLATE_LIGHT",
                "WEIGHTED_PRESSURE_PLATE_HEAVY",
                "LIGHT_WEIGHTED_PRESSURE_PLATE",
                "HEAVY_WEIGHTED_PRESSURE_PLATE",
                "OAK_PRESSURE_PLATE",
                "SPRUCE_PRESSURE_PLATE",
                "BIRCH_PRESSURE_PLATE",
                "JUNGLE_PRESSURE_PLATE",
                "ACACIA_PRESSURE_PLATE",
                "DARK_OAK_PRESSURE_PLATE",
                "CRIMSON_PRESSURE_PLATE",
                "WARPED_PRESSURE_PLATE",
                "MANGROVE_PRESSURE_PLATE",
                "STONE_PRESSURE_PLATE",
                "POLISHED_BLACKSTONE_PRESSURE_PLATE",
                "REDSTONE_TORCH",
                "REDSTONE_WALL_TORCH",
                "LANTERN",
                "SOUL_LANTERN",
                "TORCH",
                "SOUL_TORCH",
                "END_ROD",
                "SEA_LANTERN",
                "COBBLESTONE_WALL",
                "MOSSY_COBBLESTONE_WALL",
                "BRICK_WALL",
                "PRISMARINE_WALL",
                "RED_SANDSTONE_WALL",
                "NETHER_BRICK_WALL",
                "END_STONE_BRICK_WALL",
                "DIORITE_WALL",
                "ANDESITE_WALL",
                "GRANITE_WALL",
                "SANDSTONE_WALL",
                "STONE_BRICK_WALL",
                "MOSSY_STONE_BRICK_WALL",
                "NETHER_BRICK_FENCE",
                "END_STONE_BRICK_FENCE",
                "COBBLESTONE_WALL",
                "PISTON",
                "STICKY_PISTON",
                "PISTON_HEAD",
                "MOVING_PISTON",
                "MOVING_PISTON_HEAD",
                "PISTON_EXTENSION",
                "VINE",
                "LILY_PAD",
                "VINES",
                "LADDER",
                "RAIL",
                "POWERED_RAIL",
                "DETECTOR_RAIL",
                "ACTIVATOR_RAIL"
        );
    }

    public static List<Material> getAllThatContain(String contains) {
        List<Material> materials = new ArrayList<>();

        for (Material material : Material.values()) {
            if (material.name().contains(contains)) {
                materials.add(material);
            }
        }

        return materials;
    }

    public static List<Material> getFillMaterials() {
        List<Material> materials = new ArrayList<>();

        for (String materialString : getFillMaterialStrings()) {
            try {
                materials.addAll(getAllThatContain(materialString));
            } catch (Throwable e) {
                // Do nothing
            }
        }

        return materials;
    }

    public Predicate<Block> getFillPredicate() {
        return block -> {
            if (block.getType() != getLavaMaterial()) {
//                return getFillMaterials().contains(block.getType());
                return true;
            }

            return false;
        };
    }

    public void resetIfNecessary() {
        if (resetOnUnload) {
            reset();
        }
    }
}
