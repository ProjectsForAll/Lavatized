package host.plas.lavatized.arenas.players;

import host.plas.bou.MessageUtils;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.managers.PlayerManager;
import host.plas.lavatized.utils.LocationUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tv.quaint.objects.Identifiable;

import java.util.Optional;
import java.util.UUID;

@Getter @Setter
public class ArenaPlayer implements Identifiable {
    private String identifier;

    private int wins;
    private int totalPlayed;

    private Optional<PlayState> playState;

    public ArenaPlayer(String identifier, boolean load) {
        this.identifier = identifier;

        wins = 0;
        totalPlayed = 0;

        playState = Optional.empty();

        if (load) PlayerManager.loadPlayer(this);
    }

    public ArenaPlayer(OfflinePlayer player, boolean load) {
        this(player.getUniqueId().toString(), load);
    }

    public ArenaPlayer(String identifier) {
        this(identifier, true);
    }

    public ArenaPlayer(OfflinePlayer player) {
        this(player, true);
    }

    public void addWins(int wins) {
        this.wins += wins;
    }

    public void removeWins(int wins) {
        this.wins -= wins;
    }

    public void addWin() {
        addWins(1);
    }

    public void removeWin() {
        removeWins(1);
    }

    public void addPlayed(int played) {
        this.totalPlayed += played;
    }

    public void removePlayed(int played) {
        this.totalPlayed -= played;
    }

    public void addPlayed() {
        addPlayed(1);
    }

    public void removePlayed() {
        removePlayed(1);
    }

    public double getWinRate() {
        return (double) wins / (double) totalPlayed;
    }

    public void reset() {
        wins = 0;
        totalPlayed = 0;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(identifier));
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(identifier)));
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public static ArenaPlayer wrap(String identifier) {
        return PlayerManager.wrap(identifier);
    }

    public static ArenaPlayer wrap(OfflinePlayer player) {
        return PlayerManager.wrap(player);
    }

    public PlayState joinArena(String identifier) {
        Arena arena = Arena.wrap(identifier);
        arena.onJoin(this);

        addPlayed();

        PlayState playState = new PlayState(this, arena);
        this.playState = Optional.of(playState);
        return playState;
    }

    public boolean isInArena() {
        return false;
    }

    public void voidPlayState() {
        playState = Optional.empty();
    }

    public void onDeath(Arena arena) {
        getPlayer().ifPresent(player -> {
            MessageUtils.sendMessage(player, "&c&lYou have died&8&l!");
        });

        arena.onLeave(this);
    }

    public void onLeave(Arena arena) {
        voidPlayState();

        getPlayer().ifPresent(player -> {
            arena.onClearBad(player);
            LocationUtils.teleport(player, arena.getLobby());
            MessageUtils.sendMessage(player, "&7You have been teleported back to the lobby&8...");
        });
    }
}
