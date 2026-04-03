package com.averatec.armordisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ArmorHudRenderer {

    private static final int ARMOR_CAP = 30;
    private static final int HEARTS_BASE_FROM_BOTTOM = 39;
    private static final int ROW_HEIGHT = 10;
    private static final int HP_PER_ROW = 20;
    private static final int MAX_HEART_ROWS = 8;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        ModConfig config = ModConfig.getInstance();
        if (!config.enabled) return;

        PlayerEntity player = client.player;
        int raw = (int) Math.floor(calculateRawArmor(player));
        if (raw <= 0) return;

        int display = Math.min(raw, ARMOR_CAP);
        Text text = raw > ARMOR_CAP
                ? Text.translatable("hud.armor_display_averatec.value_over_cap", display, raw)
                : Text.translatable("hud.armor_display_averatec.value", display);

        int x = context.getScaledWindowWidth() / 2 - 91;
        int y = getTextY(player, context.getScaledWindowHeight());
        context.drawText(client.textRenderer, text, x, y, config.textColor, true);
    }

    /**
     * Calculates total armor without the vanilla 30-point cap.
     *
     * Applies modifiers using the vanilla formula:
     *   (base + ADD_VALUE) * (1 + sum(ADD_MULTIPLIED_BASE)) * product(1 + ADD_MULTIPLIED_TOTAL)
     *
     * Iterates EntityAttributeInstance modifiers directly (bypasses getValue()'s clamp),
     * which captures server-applied bonuses not present in any item's AttributeModifiersComponent.
     *
     * Held-item adjustment corrects for RPG server sync lag: weapons/tools may carry
     * armor modifiers in their component before the server syncs them to the instance.
     */
    private static double calculateRawArmor(PlayerEntity player) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (instance == null) return 0;

        double addValue = 0;
        double addMultBase = 0;
        double addMultTotal = 1.0;

        for (EntityAttributeModifier mod : instance.getModifiers()) {
            switch (mod.operation()) {
                case ADD_VALUE           -> addValue    += mod.value();
                case ADD_MULTIPLIED_BASE -> addMultBase += mod.value();
                case ADD_MULTIPLIED_TOTAL -> addMultTotal *= (1.0 + mod.value());
            }
        }

        // Held-item sync lag correction: adj = {addValue, addMultBase, addMultTotal factor}
        double[] adj = {0.0, 0.0, 1.0};
        Set<Identifier> processedIds = new HashSet<>();
        applyHeldAdjustment(instance, player.getMainHandStack(), EquipmentSlot.MAINHAND, processedIds, adj);
        applyHeldAdjustment(instance, player.getOffHandStack(), EquipmentSlot.OFFHAND, processedIds, adj);

        addValue    += adj[0];
        addMultBase += adj[1];
        addMultTotal *= adj[2];

        return (instance.getBaseValue() + addValue) * (1.0 + addMultBase) * addMultTotal;
    }

    /**
     * Accumulates the armor adjustment for a held item into adj[].
     * adj[0] = ADD_VALUE delta, adj[1] = ADD_MULTIPLIED_BASE delta, adj[2] = ADD_MULTIPLIED_TOTAL factor.
     *
     * For each modifier in the item component not yet synced to the instance,
     * contributes the missing amount per operation type so there is no double-count.
     */
    private static void applyHeldAdjustment(EntityAttributeInstance instance, ItemStack stack,
                                             EquipmentSlot slot, Set<Identifier> processedIds,
                                             double[] adj) {
        if (stack.isEmpty()) return;
        AttributeModifiersComponent comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp == null) return;

        for (AttributeModifiersComponent.Entry entry : comp.modifiers()) {
            if (!entry.attribute().equals(EntityAttributes.ARMOR)) continue;
            if (!entry.slot().matches(slot)) continue;

            Identifier id = entry.modifier().id();
            double componentValue = entry.modifier().value();
            EntityAttributeModifier.Operation op = entry.modifier().operation();

            boolean firstSeen = processedIds.add(id);
            EntityAttributeModifier synced = firstSeen ? instance.getModifier(id) : null;
            double syncedValue = synced != null ? synced.value() : 0.0;
            double delta = componentValue - syncedValue;

            switch (op) {
                case ADD_VALUE            -> adj[0] += delta;
                case ADD_MULTIPLIED_BASE  -> adj[1] += delta;
                case ADD_MULTIPLIED_TOTAL ->
                        adj[2] *= (1.0 + componentValue) / (1.0 + syncedValue);
            }
        }
    }

    private static int getTextY(PlayerEntity player, int scaledHeight) {
        int heartRows;
        if (player.getAbilities().creativeMode) {
            heartRows = 1;
        } else {
            float totalHealth = player.getMaxHealth() + player.getAbsorptionAmount();
            heartRows = Math.min(MAX_HEART_ROWS,
                    Math.max(1, (int) Math.ceil(totalHealth / HP_PER_ROW)));
        }
        return scaledHeight - HEARTS_BASE_FROM_BOTTOM - heartRows * ROW_HEIGHT - ROW_HEIGHT;
    }
}
