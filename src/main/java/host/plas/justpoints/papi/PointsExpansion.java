package host.plas.justpoints.papi;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.quaint.utils.StringUtils;

public class PointsExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "points";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Drakify";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] split = params.split("_", 3);

        if (params.startsWith("other_")) {
            if (split.length >= 3) {
                OfflinePlayer other = Bukkit.getOfflinePlayer(split[1]);

                return getPoints(other, StringUtils.argsToStringMinus(split, 0, 1));
            } else {
                return null;
            }
        } else {
            return getPoints(player, params);
        }
    }

    public static String getPoints(OfflinePlayer offlinePlayer, String key) {
        PointPlayer pointPlayer = PointPlayer.getOrGetPlayer(offlinePlayer.getUniqueId().toString()).join();

        if (pointPlayer != null) {
            return String.valueOf(pointPlayer.getPoints(key));
        } else {
            return String.valueOf(JustPoints.getMainConfig().getPointsDefault());
        }
    }
}
