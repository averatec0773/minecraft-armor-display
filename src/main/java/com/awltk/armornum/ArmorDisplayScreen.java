package com.awltk.armornum;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ArmorDisplayScreen extends Screen {

    private static final Text AUTHOR = Text.literal("by averatec0773").styled(s -> s.withColor(0xFFFF55));
    private final Screen parent;

    private boolean rebinding = false;
    private ButtonWidget keybindButton;

    public ArmorDisplayScreen(Screen parent) {
        super(Text.literal("Armor Display Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.getInstance();
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Toggle display on/off
        this.addDrawableChild(ButtonWidget.builder(
                buildToggleText(config.enabled),
                btn -> {
                    config.enabled = !config.enabled;
                    config.save();
                    btn.setMessage(buildToggleText(config.enabled));
                })
                .dimensions(cx - 100, cy - 35, 200, 20)
                .build());

        // Cycle text color preset
        this.addDrawableChild(ButtonWidget.builder(
                buildColorText(config.textColor),
                btn -> {
                    int next = (getColorIndex(config.textColor) + 1) % ModConfig.COLOR_PRESETS.length;
                    config.textColor = ModConfig.COLOR_PRESETS[next];
                    config.save();
                    btn.setMessage(buildColorText(config.textColor));
                })
                .dimensions(cx - 100, cy - 10, 200, 20)
                .build());

        // Keybinding rebind button
        keybindButton = ButtonWidget.builder(
                buildKeybindText(),
                btn -> {
                    rebinding = true;
                    btn.setMessage(Text.literal("> Press a key... <"));
                })
                .dimensions(cx - 100, cy + 15, 200, 20)
                .build();
        this.addDrawableChild(keybindButton);

        // Done
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                btn -> this.client.setScreen(this.parent))
                .dimensions(cx - 50, cy + 43, 100, 20)
                .build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (rebinding) {
            rebinding = false;
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                KeyBinding key = ArmorNumClient.getConfigKey();
                key.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
                KeyBinding.updateKeysByCode();
                this.client.options.write();
            }
            keybindButton.setMessage(buildKeybindText());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, cy - 65, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, AUTHOR, cx, cy - 53, 0xFFFFFF);
    }

    // --- helpers ---

    private static Text buildToggleText(boolean enabled) {
        return Text.literal("Armor Display: ").append(
                Text.literal(enabled ? "ON" : "OFF")
                        .styled(s -> s.withColor(enabled ? 0x55FF55 : 0xFF5555))
        );
    }

    private static Text buildColorText(int color) {
        String name = ModConfig.COLOR_NAMES[getColorIndex(color)];
        return Text.literal("Text Color: ").append(
                Text.literal(name).styled(s -> s.withColor(color))
        );
    }

    private static Text buildKeybindText() {
        return Text.literal("Open Menu: ").append(
                ArmorNumClient.getConfigKey().getBoundKeyLocalizedText()
        );
    }

    private static int getColorIndex(int color) {
        for (int i = 0; i < ModConfig.COLOR_PRESETS.length; i++) {
            if (ModConfig.COLOR_PRESETS[i] == color) return i;
        }
        return 0;
    }
}
