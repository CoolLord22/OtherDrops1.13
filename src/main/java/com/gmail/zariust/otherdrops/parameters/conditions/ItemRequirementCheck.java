package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.config.ConfigSubject;
import com.gmail.zariust.otherdrops.data.item.ODItem;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.parameters.Committable;
import com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class ItemRequirementCheck extends Condition implements Committable {
    String name = "ItemRequirementCheck";

    public record ItemRequirement(IntRange quantity, Set<Integer> slots) {}
    private record Claim(ODItem item, ItemStack stack, int amount) {}

    private final Map<ODItem, ItemRequirement> requiredStored;
    private final Random rng = new Random();

    public ItemRequirementCheck(Map<ODItem, ItemRequirement> requiredStored) {
        this.requiredStored = requiredStored;
    }

    /** CHECK ONLY: find matching stacks and remember them. Nothing is removed here. */
    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        Player player = occurrence.getPlayerAttacker();
        if (player == null) return false; // no player, no inventory to check

        List<Claim> claims = new ArrayList<>();
        for (Entry<ODItem, ItemRequirement> req : requiredStored.entrySet()) {
            int reqQuantity = req.getValue().quantity().getRandomIn(rng); // rolled once, reused at commit
            ItemStack found = getItem(req, player.getInventory(), reqQuantity);
            if (found != null) claims.add(new Claim(req.getKey(), found, reqQuantity));
        }

        // Current behavior kept: passes if ANY listed item was found (OR), and every found item is taken.
        // For AND semantics, replace the next line with:
        //     if (claims.size() != requiredStored.size()) return false;
        if (claims.isEmpty()) return false;

        occurrence.setCommitData(this, claims);
        return true;
    }

    /**
     * Re-verify the exact stacks captured during the check. Fails if another section in this event already
     * took them (e.g. two sections both need the player's last cookie), or if the stack changed.
     */
    @Override
    public boolean canCommit(CustomDrop drop, OccurredEvent occurrence) {
        List<Claim> claims = getClaims(occurrence);
        if (claims == null) return true;
        for (Claim claim : claims) {
            if (claim.stack() == null || !claim.item().matches(claim.stack())) return false;
            if (claim.stack().getAmount() < claim.amount()) return false;
        }
        return true;
    }

    /** Take the items from the stacks captured at check time (not whatever the player is holding now). */
    @Override
    public void commit(CustomDrop drop, OccurredEvent occurrence) {
        List<Claim> claims = getClaims(occurrence);
        if (claims == null) return;
        for (Claim claim : claims) {
            if (claim.amount() <= 0) continue; // /q#0 (or no /q#): required but not taken
            claim.stack().setAmount(claim.stack().getAmount() - claim.amount());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Claim> getClaims(OccurredEvent occurrence) {
        Object data = occurrence.getCommitData(this);
        return data instanceof List ? (List<Claim>) data : null;
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        List<String> input = OtherDropsConfig.getMaybeList(node, "itemrequirement");
        if (input.isEmpty()) return null;

        Map<ODItem, ItemRequirement> value = new HashMap<>();
        for (String name : input) {
            // format is now ITEM@!~CustomName;lore line1/q#MIN-MAX/s#0-8,36,40
            String base;
            IntRange quantityRange = new IntRange(0); // default quantity 0 to indicate just required
            String slotsPart = "";

            String[] qSplit = name.split("/q#", 2); // limit=2 to preserve rest
            base = qSplit[0];

            if (qSplit.length > 1) {
                String afterQ = qSplit[1];
                String[] sSplit = afterQ.split("/s#", 2);
                quantityRange = IntRange.parse(sSplit[0]);
                if (sSplit.length > 1) slotsPart = sSplit[1];
            } else {
                String[] sSplit = name.split("/s#", 2);
                base = sSplit[0];
                if (sSplit.length > 1) slotsPart = sSplit[1];
            }
            ODItem item = ConfigSubject.parseSubject(base.trim()).getODItem();
            if (item == null) {
                Log.logWarning("itemrequirement: '" + base.trim() + "' isn't a valid item; skipping...");
                continue;
            }
            value.put(item, new ItemRequirement(quantityRange, parseSlots(slotsPart)));
        }
        if (value.isEmpty()) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new ItemRequirementCheck(value));
        return conditionList;
    }

    private static Set<Integer> parseSlots(String input) {
        Set<Integer> slots = new HashSet<>();
        if (input == null || input.isEmpty()) return slots;

        for (String part : input.split(",")) {
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                int start = Integer.parseInt(bounds[0]);
                int end = Integer.parseInt(bounds[1]);
                for (int i = start; i <= end; i++) slots.add(i);
            } else {
                slots.add(Integer.parseInt(part));
            }
        }
        return slots;
    }

    public ItemStack getItem(Entry<ODItem, ItemRequirement> reqEntry, Inventory inv, int reqQuantity) {
        ODItem req = reqEntry.getKey();
        Set<Integer> slots = reqEntry.getValue().slots();

        // No slots specified -> check entire inventory
        if (slots.isEmpty()) {
            for (ItemStack item : inv.getContents()) {
                if (item != null && req.matches(item) && reqQuantity <= item.getAmount()) return item;
            }
        } else {
            for (Integer slot : slots) {
                ItemStack item = inv.getItem(slot);
                if (item != null && req.matches(item) && reqQuantity <= item.getAmount()) return item;
            }
        }
        return null;
    }
}
