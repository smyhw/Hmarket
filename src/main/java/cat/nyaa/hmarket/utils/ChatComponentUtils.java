package cat.nyaa.hmarket.utils;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

// from https://github.com/NyaaCat/aolib
public class ChatComponentUtils {
    public static String toJson(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    public static Component fromJson(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public static WrappedChatComponent createWrappedChatComponent(Component component) {
        return WrappedChatComponent.fromJson(toJson(component));
    }

    public static TextComponent fromLegacyText(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }
}
