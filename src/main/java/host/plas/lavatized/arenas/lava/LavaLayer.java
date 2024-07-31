package host.plas.lavatized.arenas.lava;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.arenas.radius.ArenaRadius;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter @Setter
public class LavaLayer implements Comparable<LavaLayer> {
    private LavaHolder holder;

    private int y;
    private boolean active;

    public LavaLayer(LavaHolder holder, int y, boolean active) {
        this.holder = holder;
        this.y = y;
        this.active = active;
    }

    public LavaLayer(LavaHolder holder, int y) {
        this(holder, y, false);
    }

    public Arena getArena() {
        return holder.getArena();
    }

    public ArenaRadius getRadius() {
        return getArena().getRadius();
    }

    public void fill() {
        fillWithLava(getMin(), getMax());

        this.active = true;
    }

    public void fillWithLava(Location minLocation, Location maxLocation) {
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(minLocation.getWorld()), -1);
            CuboidRegion region = new CuboidRegion(BukkitUtil.toVector(minLocation), BukkitUtil.toVector(maxLocation));

            BaseBlock lava = new BaseBlock(getArena().getLavaMaterial().getId());

            List<BaseBlock> blocks = new ArrayList<>();
            getArena().getFillMaterials().forEach(material -> {
                try {
                    blocks.add(new BaseBlock(material.getId()));
                } catch (Throwable e) {
                    // Do nothing
                }
            });
            BlockMask mask = new BlockMask(editSession, blocks);
            editSession.setMask(mask);

            CompletableFuture.runAsync(() -> {
                try {
                    editSession.setBlocks(region, lava);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    editSession.flushQueue();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Location getMin() {
        return getRadius().getMinLocationAtY(y);
    }

    public Location getMax() {
        return getRadius().getMaxLocationAtY(y);
    }

    @Override
    public int compareTo(@NotNull LavaLayer o) {
        return Integer.compare(y, o.getY());
    }
}
