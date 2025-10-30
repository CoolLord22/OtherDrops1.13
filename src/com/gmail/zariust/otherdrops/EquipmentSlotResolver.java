package com.gmail.zariust.otherdrops;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.bukkit.inventory.EquipmentSlot.OFF_HAND;

/**
 * Utility for resolving the {@link EquipmentSlot} containing a player's weapon for an
 * {@link EntityDamageByEntityEvent}. This is used since that event type doesn't provide a
 * way for finding out the equipment slot.
 */
public class EquipmentSlotResolver {

    /**
     * Resolve the used equipment slot from the damage cause of an {@link EntityDamageByEntityEvent}.
     * @param damageCause The damage cause of the event.
     * @param damager The {@link HumanEntity} who caused the event.
     * @return The used equipment slot. {@code null} if it could not be resolved.
     */
    public static EquipmentSlot resolve(DamageCause damageCause, HumanEntity damager) {
        final PlayerInventory damagerInventory = damager.getInventory();

        return switch (damageCause) {
            case ENTITY_ATTACK ->
                // Melee attacks can only be performed from the main hand.
                // Therefore, no additional checks are required.
                    HAND;
            case PROJECTILE ->
                // Ranged damage
                    getHandWithMaterial(damagerInventory, Material.BOW, true);
            case MAGIC ->
                // Potion
                    getHandWithMaterial(damagerInventory, Material.SPLASH_POTION, true);
            default -> null;
        };

    }

    /**
     * Get the hand in which a player is holding an item with a specific material.
     * @param playerInventory The inventory of the player.
     * @param material The material of the item that the player should be holding.
     * @param preferMainHand Whether to prefer the player's main hand ({@code true}) or the
     *                       off-hand ({@code false}), in case the player is holding an item with
     *                       the requested material in both hands.
     * @return The {@link EquipmentSlot} representing the hand in which the player is holding an
     *         item with the requested material. {@code null}, in case an item meeting the requirements
     *         was found in none of the player's hands.
     */
    public static EquipmentSlot getHandWithMaterial(PlayerInventory playerInventory, Material material,
                                                     boolean preferMainHand) {
        boolean inMainHand = playerInventory.getItemInMainHand().getType() == material;
        boolean inOffHand = playerInventory.getItemInOffHand().getType() == material;

        if (inMainHand && inOffHand) {
            // If the player is holding an item with the requested material
            // in both hands, we will return the preferred hand here.
            return preferMainHand ? HAND : OFF_HAND;
        } else if (inMainHand) {
            return HAND;
        } else if (inOffHand) {
            return OFF_HAND;
        } else {
            return null;
        }
    }
}