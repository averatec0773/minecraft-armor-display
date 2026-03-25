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
        if (!ModConfig.getInstance().enabled) return;

        PlayerEntity player = client.player;
        int raw = (int) Math.floor(calculateRawArmor(player));
        if (raw <= 0) return;

        int display = Math.min(raw, ARMOR_CAP);
        String text = raw > ARMOR_CAP
                ? "Total Armor: " + display + " (Actual: " + raw + ")"
                : "Total Armor: " + display;

        int x = context.getScaledWindowWidth() / 2 - 91;
        int y = getTextY(player, context.getScaledWindowHeight());
        context.drawText(client.textRenderer, text, x, y, ModConfig.getInstance().textColor, true);
    }

    /**
     * Calculates total armor without the vanilla 30-point cap.
     *
     * Sums EntityAttributeInstance modifiers directly (bypasses getValue()'s clamp),
     * which captures server-applied bonuses like 赋灵 set effects that are not present
     * in any item's AttributeModifiersComponent.
     *
     * Held-item adjustment corrects for RPG server sync lag: weapons/shields may have
     * armor in their component before the server syncs the modifier to the instance.
     */
    private static double calculateRawArmor(PlayerEntity player) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (instance == null) return 0;

        double total = instance.getBaseValue();
        for (EntityAttributeModifier mod : instance.getModifiers()) {
            total += mod.value();
        }

        Set<Identifier> processedIds = new HashSet<>();
        total += getHeldAdjustment(instance, player.getMainHandStack(), EquipmentSlot.MAINHAND, processedIds);
        total += getHeldAdjustment(instance, player.getOffHandStack(), EquipmentSlot.OFFHAND, processedIds);

        return total;
    }

    /**
     * Returns the armor adjustment for a held item vs what is already in the instance.
     * If the server has not yet synced the modifier: adjustment = componentValue.
     * If already synced: adjustment = 0 (no double-count).
     * If two held items share the same modifier ID: the second always adds componentValue.
     */
    private static double getHeldAdjustment(EntityAttributeInstance instance, ItemStack stack,
                                             EquipmentSlot slot, Set<Identifier> processedIds) {
        if (stack.isEmpty()) return 0;
        AttributeModifiersComponent comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp == null) return 0;

        double adjustment = 0;
        for (AttributeModifiersComponent.Entry entry : comp.modifiers()) {
            if (!entry.attribute().equals(EntityAttributes.ARMOR)) continue;
            if (!entry.slot().matches(slot)) continue;

            Identifier id = entry.modifier().id();
            double componentValue = entry.modifier().value();

            if (processedIds.add(id)) {
                EntityAttributeModifier synced = instance.getModifier(id);
                adjustment += componentValue - (synced != null ? synced.value() : 0.0);
            } else {
                adjustment += componentValue;
            }
        }
        return adjustment;
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
