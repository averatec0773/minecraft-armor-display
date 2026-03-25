package com.awltk.armornum;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArmorNumClient implements ClientModInitializer {

    private static KeyBinding configKey;

    public static KeyBinding getConfigKey() {
        return configKey;
    }

    @Override
    public void onInitializeClient() {
        ModConfig.getInstance();

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armor_display_averatec.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.armor_display_averatec"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new ArmorDisplayScreen(client.currentScreen));
            }
        });

        HudRenderCallback.EVENT.register(ArmorHudRenderer::render);
    }
}
