package host.plas.lavatized.arenas.lava;

import host.plas.lavatized.arenas.Arena;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class LavaHolder implements Identifiable {
    private Arena arena;

    @Override
    public String getIdentifier() {
        return arena.getIdentifier();
    }

    @Override
    public void setIdentifier(String s) {
        // Do nothing
    }

    private ConcurrentSkipListSet<LavaLayer> layers;
    private int currentLayer;
    private long ticksPerLayer;
    private long ticksUntilNextLayer;

    public LavaHolder(Arena arena, long ticksPerLayer) {
        this.arena = arena;
        this.layers = new ConcurrentSkipListSet<>();
        this.currentLayer = 0;
        this.ticksPerLayer = ticksPerLayer;
        this.ticksUntilNextLayer = ticksPerLayer;
        generateNewLayers();
    }

    public void generateNewLayers() {
        layers.clear();
        int minY = 0;
        int maxY = 219;
        try {
            minY = arena.getWorld().getMinHeight() + 1; // leave a layer of bedrock
            maxY = arena.getWorld().getMaxHeight();
        } catch (Error e) {
            // Do nothing
        }
        for (int y = minY; y <= maxY; y++) {
            layers.add(new LavaLayer(this, y));
        }
    }

    public Optional<LavaLayer> getLayer(int y) {
        return layers.stream().filter(layer -> layer.getY() == y).findFirst();
    }

    public void fill(int y) {
        getLayer(y).ifPresent(LavaLayer::fill);
    }

    public void fillAll(int y1, int y2) {
        for (int y = y1; y <= y2; y++) {
            fill(y);
        }
    }

    public void tick() {
        try {
            if (ticksUntilNextLayer-- <= 0) {
                if (currentLayer < layers.size()) {
                    fillAll(0, currentLayer++);
                }
                ticksUntilNextLayer = ticksPerLayer;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
