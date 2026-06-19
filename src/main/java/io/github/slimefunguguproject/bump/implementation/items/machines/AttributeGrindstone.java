package io.github.slimefunguguproject.bump.implementation.items.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Multimap;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.slimefunguguproject.bump.api.appraise.AppraiseType;
import io.github.slimefunguguproject.bump.core.services.sounds.BumpSound;
import io.github.slimefunguguproject.bump.implementation.Bump;
import io.github.slimefunguguproject.bump.implementation.BumpItems;
import io.github.slimefunguguproject.bump.implementation.groups.BumpItemGroups;
import io.github.slimefunguguproject.bump.utils.AppraiseUtils;
import io.github.slimefunguguproject.bump.utils.GuiItems;
import io.github.slimefunguguproject.bump.utils.ValidateUtils;
import io.github.slimefunguguproject.bump.utils.constant.Keys;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil;

/**
 * The {@link AttributeGrindstone} can purge the appraisal result from
 * appraised equipment.
 *
 * @author ybw0014
 */
public final class AttributeGrindstone extends SimpleMenuBlock {

    // energy
    public static final int ENERGY_CONSUMPTION = 1314;

    public AttributeGrindstone() {
        super(BumpItemGroups.MACHINE, BumpItems.ATTRIBUTE_GRINDSTONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[]{
            SlimefunItems.ELECTRO_MAGNET, BumpItems.APPRAISAL, SlimefunItems.ELECTRO_MAGNET,
            BumpItems.MECHA_GEAR, BumpItems.CPU, BumpItems.MECHA_GEAR,
            BumpItems.UPDATE_POWER, BumpItems.ZONGZI, BumpItems.UPDATE_POWER
        });
    }

    @Override
    @Nonnull
    public ItemStack getOperationSlotItem() {
        return GuiItems.GRIND_BUTTON;
    }

    @Override
    public int getCapacity() {
        return ENERGY_CONSUMPTION;
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void onOperate(BlockMenu menu, Block b, Player p, ClickAction action) {
        grind(menu, p);
    }

    private void grind(@Nonnull BlockMenu blockMenu, @Nonnull Player p) {
        ItemStack item = blockMenu.getItemInSlot(getInputSlot());

        // null check
        if (!ValidateUtils.noAirItem(item)) {
            Bump.getLocalization().sendMessage(p, "no-input");
            BumpSound.ATTRIBUTE_GRINDSTONE_FAIL.playFor(p);
            return;
        }

        // check if input item is appraised
        if (!AppraiseUtils.isAppraised(item)) {
            Bump.getLocalization().sendMessage(p, "machine.attribute-grindstone.invalid");
            BumpSound.ATTRIBUTE_GRINDSTONE_FAIL.playFor(p);
            return;
        }

        // check output slot
        if (blockMenu.getItemInSlot(getOutputSlot()) != null) {
            Bump.getLocalization().sendMessage(p, "output-no-space");
            BumpSound.ATTRIBUTE_GRINDSTONE_FAIL.playFor(p);
            return;
        }

        // check energy
        int charge = getCharge(blockMenu.getLocation());
        if (charge < ENERGY_CONSUMPTION) {
            Bump.getLocalization().sendMessage(p, "not-enough-power");
            BumpSound.ATTRIBUTE_GRINDSTONE_FAIL.playFor(p);
            return;
        }

        ItemStack output = item.clone();
        clearAttributes(output);
        blockMenu.replaceExistingItem(getInputSlot(), null);
        blockMenu.pushItem(output, getOutputSlot());

        setCharge(blockMenu.getLocation(), 0);
        Bump.getLocalization().sendMessage(p, "machine.attribute-grindstone.success");
        BumpSound.ATTRIBUTE_GRINDSTONE_SUCCEED.playFor(p);
    }

    private void clearAttributes(@Nonnull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        // check the appraising version
        byte version = PersistentDataAPI.getByte(meta, Keys.APPRAISE_VERSION, (byte) 1);

        removeModifiers(meta, version);
        restoreDefaultAttributesIfEmpty(meta);

        // set pdc
        PersistentDataAPI.setBoolean(meta, Keys.APPRAISABLE, true);
        PersistentDataAPI.remove(meta, Keys.APPRAISE_LEVEL);
        PersistentDataAPI.remove(meta, Keys.APPRAISE_VERSION);
        PersistentDataAPI.remove(meta, Keys.APPRAISE_MODIFIERS);

        // set lore
        String appraisedLore = ChatUtil.color(Bump.getLocalization().getString("lores.appraised"));
        String appraisedLorePrefix = appraisedLore.substring(0, appraisedLore.indexOf("{0}"));
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).startsWith(appraisedLorePrefix)) {
                lore.set(i, ChatUtil.color(Bump.getLocalization().getString("lores.not-appraised")));
                break;
            }
        }
        meta.setLore(lore);

        // done
        itemStack.setItemMeta(meta);
    }

    @ParametersAreNonnullByDefault
    private void removeModifiers(ItemMeta meta, byte version) {
        String modifierIds = PersistentDataAPI.getString(meta, Keys.APPRAISE_MODIFIERS, "");
        if (!modifierIds.isBlank()) {
            removeMarkedModifiers(meta, modifierIds);
            return;
        }

        removeAppraisalModifiers(meta);

        if (version == 1) {
            // legacy version, remove all attribute modifiers
            removeAllModifiers(meta);
        } else {
            removeUuidNamedModifiers(meta);
        }
    }

    private void removeMarkedModifiers(@Nonnull ItemMeta meta, @Nonnull String modifierIds) {
        Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
        if (modifierMap == null) {
            return;
        }

        List<String> ids = List.of(modifierIds.split(","));
        for (Map.Entry<Attribute, AttributeModifier> entry : new ArrayList<>(modifierMap.entries())) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            if (ids.contains(modifier.getUniqueId().toString())) {
                meta.removeAttributeModifier(attribute, modifier);
            }
        }
    }

    private void removeAllModifiers(@Nonnull ItemMeta meta) {
        Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
        if (modifierMap == null) {
            return;
        }

        for (Map.Entry<Attribute, AttributeModifier> entry : new ArrayList<>(modifierMap.entries())) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            meta.removeAttributeModifier(attribute, modifier);
        }
    }

    private void removeAppraisalModifiers(@Nonnull ItemMeta meta) {
        Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
        if (modifierMap == null) {
            return;
        }

        for (Map.Entry<Attribute, AttributeModifier> entry : new ArrayList<>(modifierMap.entries())) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            NamespacedKey key = NamespacedKey.fromString(modifier.getName(), Bump.getInstance());
            AppraiseType appraiseType = key == null ? null : AppraiseType.getByKey(key);

            if (appraiseType != null) {
                meta.removeAttributeModifier(attribute, modifier);
            }
        }
    }

    private void removeUuidNamedModifiers(@Nonnull ItemMeta meta) {
        Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
        if (modifierMap == null) {
            return;
        }

        for (Map.Entry<Attribute, AttributeModifier> entry : new ArrayList<>(modifierMap.entries())) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            if (isUuid(modifier.getName())) {
                meta.removeAttributeModifier(attribute, modifier);
            }
        }
    }

    private void restoreDefaultAttributesIfEmpty(@Nonnull ItemMeta meta) {
        Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
        if (modifierMap == null || modifierMap.isEmpty()) {
            meta.setAttributeModifiers(null);
        }
    }

    private boolean isUuid(@Nonnull String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
