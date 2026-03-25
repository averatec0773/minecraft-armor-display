package com.awltk.armornum;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ArmorHudRenderer {

    private static final int HEARTS_BASE_FROM_BOTTOM = 39;
    private static final int ROW_HEIGHT = 10;
    private static final int HP_PER_ROW = 20;
    private static final int MAX_HEART_ROWS = 8;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        if (!ModConfig.getInstance().enabled) return;

        PlayerEntity player = client.player;
        int armorValue = (int) Math.floor(calculateTotalArmor(player));
        if (armorValue <= 0) return;

        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        int x = scaledWidth / 2 - 91;
        int y = getTextY(player, scaledHeight);

        context.drawText(client.textRenderer, "Total Armor: " + armorValue, x, y, ModConfig.getInstance().textColor, true);
    }

    /**
     * Calculates total armor value.
     *
     * On RPG servers, weapon/shield armor bonuses may not be synced to the
     * vanilla client attribute system. This method supplements getAttributeValue
     * by reading ATTRIBUTE_MODIFIERS components from mainhand/offhand directly,
     * and only adds modifiers that aren't already tracked in the attribute instance.
     */
    private static double calculateTotalArmor(PlayerEntity player) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.ARMOR);
        double armor = (instance != null) ? instance.getValue() : 0;

        if (instance != null) {
            armor += getUntracked(instance, player.getMainHandStack(), EquipmentSlot.MAINHAND);
            armor += getUntracked(instance, player.getOffHandStack(), EquipmentSlot.OFFHAND);
        }

        return armor;
    }

    /**
     * Returns armor bonus from an item's component that is NOT already reflected
     * in the vanilla attribute instance (avoids double-counting).
     */
    private static double getUntracked(EntityAttributeInstance instance, ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return 0;
        AttributeModifiersComponent comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp == null) return 0;

        double bonus = 0;
        for (AttributeModifiersComponent.Entry entry : comp.modifiers()) {
            if (!entry.attribute().equals(EntityAttributes.ARMOR)) continue;
            // matches() handles ANY, HAND, MAINHAND, OFFHAND correctly
            if (!entry.slot().matches(slot)) continue;
            EntityAttributeModifier modifier = entry.modifier();
            // Only add if NOT already tracked in the vanilla attribute instance
            if (!instance.hasModifier(modifier.id())) {
                bonus += modifier.value();
            }
        }
        return bonus;
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
