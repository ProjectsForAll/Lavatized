package host.plas.lavatized.arenas.radius;

import host.plas.lavatized.arenas.Arena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import tv.quaint.objects.Identifiable;

@Getter @Setter
public class ArenaRadius implements Identifiable {
    private Arena arena;

    @Override
    public String getIdentifier() {
        return arena.getIdentifier();
    }

    @Override
    public void setIdentifier(String s) {
        // Do nothing
    }

    private double maxRadius;
    private double minRadius;
    private long shrinkTime;
    private double currentRadius;
    private double centerX;
    private double centerZ;

    public ArenaRadius(Arena arena, double maxRadius, double minRadius, long shrinkTime, double centerX, double centerZ) {
        this.arena = arena;
        this.maxRadius = maxRadius;
        this.minRadius = minRadius;
        this.shrinkTime = shrinkTime;
        this.currentRadius = maxRadius;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public double getShrinkRate() {
        return Math.abs(maxRadius - minRadius) / shrinkTime;
    }

    public void tick() {
        if (isBorderAtMinRadius()) return;

        World world = getArena().getWorld();
        WorldBorder border = world.getWorldBorder();

        if (isPositivePolarity()) {
            tickPositivePolarity();
        } else {
            tickNegativePolarity();
        }

        updateBorder(border);
    }

    public boolean isBorderAtMinRadius() {
        return currentRadius <= minRadius;
    }

    public boolean isPositivePolarity() {
        return maxRadius >= minRadius;
    }

    public boolean isNegativePolarity() {
        return maxRadius < minRadius;
    }

    // positive is shrinking or staying the same, negative is expanding
    public void tickPositivePolarity() {
        currentRadius -= getShrinkRate();
    }

    public void tickNegativePolarity() {
        currentRadius += getShrinkRate();
    }

    public void updateBorder(WorldBorder border) {
        border.setCenter(centerX, centerZ);
        border.setSize(currentRadius * 2);
    }

    public Location getMaxLocation() {
        return getMaxLocationAtY(0);
    }

    public Location getMaxLocationAtY(double y) {
        return new Location(getArena().getWorld(), getCenterX() + getMaxRadius(), y, getCenterZ() + getMaxRadius());
    }

    public Location getMinLocation() {
        return getMinLocationAtY(0);
    }

    public Location getMinLocationAtY(double y) {
        return new Location(getArena().getWorld(), getCenterX() - getMaxRadius(), y, getCenterZ() - getMaxRadius());
    }
}
