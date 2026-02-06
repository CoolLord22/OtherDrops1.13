package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.options.IntRange;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import main.java.com.gmail.zariust.otherdrops.things.ODItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class ItemRequirementCheck extends Condition {
    String name = "ItemRequirementCheck";

    public record ItemRequirement(IntRange quantity, Set<Integer> slots) {}

    private final Map<ODItem, ItemRequirement> requiredStored;
    private final Random rng = new Random();

    public ItemRequirementCheck(Map<ODItem, ItemRequirement> requiredStored) {
        this.requiredStored = requiredStored;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        boolean contained = false;
        for (Entry<ODItem, ItemRequirement> req : requiredStored.entrySet()) {
            Integer reqQuantity = req.getValue().quantity().getRandomIn(rng);
            ItemStack toRemove = getItem(req, occurrence.getPlayerAttacker().getInventory(), reqQuantity);
            if (toRemove != null) {
                contained = true;
                toRemove.setAmount(toRemove.getAmount() - reqQuantity);
            }
        }
        return contained;
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
            ODItem item = ODItem.parseItem(base.trim());
            value.put(item, new ItemRequirement(quantityRange, parseSlots(slotsPart)));
        }

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

        // No slots specified → check entire inventory
        if (slots.isEmpty()) {
            for (ItemStack item : inv.getContents()) {
                if (req.matches(item) && reqQuantity <= item.getAmount()) {
                    return item;
                }
            }
        } else {
            for (Integer slot : slots) {
                ItemStack item = inv.getItem(slot);
                if (req.matches(item) && reqQuantity <= item.getAmount()) {
                    return item;
                }
            }
        }
        return null;
    }
}
