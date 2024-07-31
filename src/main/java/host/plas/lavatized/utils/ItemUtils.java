package host.plas.lavatized.utils;

import org.bukkit.inventory.ItemStack;
import tv.quaint.thebase.lib.google.gson.Gson;

public class ItemUtils {
    public static final Gson GSON = new Gson();

    public static ItemStack getItem(String nbt) {
        return GSON.fromJson(nbt, ItemStack.class);
    }

    public static String toString(ItemStack item) {
        return GSON.toJson(item);
    }
}
