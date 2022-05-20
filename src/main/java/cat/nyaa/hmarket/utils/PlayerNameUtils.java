package cat.nyaa.hmarket.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlayerNameUtils {
    @NotNull
    public static String getPlayerNameById(UUID playerId){
       return Objects.requireNonNullElse(Bukkit.getOfflinePlayer(playerId).getName(),playerId.toString());
    }
}
