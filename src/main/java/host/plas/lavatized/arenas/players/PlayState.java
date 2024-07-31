package host.plas.lavatized.arenas.players;

import host.plas.lavatized.arenas.Arena;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

@Getter @Setter
public class PlayState implements Identifiable {
    private ArenaPlayer player;

    @Override
    public String getIdentifier() {
        return player.getIdentifier();
    }

    @Override
    public void setIdentifier(String s) {
        // Do nothing
    }

    private Arena currentArena;

    public PlayState(ArenaPlayer player, Arena currentArena) {
        this.player = player;
        this.currentArena = currentArena;
    }

    public boolean isInvulnerable() {
        return currentArena.isGracePeriod();
    }
}
