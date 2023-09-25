package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.things.ODItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class ItemRequirementCheck extends Condition {
	String name = "ItemRequirementCheck";
	private final Map<ODItem, IntRange> requiredStored;
	private final Random rng = new Random();

	public ItemRequirementCheck(Map<ODItem, IntRange> requiredStored) {
		this.requiredStored = requiredStored;
	}

	@Override
	public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
		boolean contained = false;
		for(Entry<ODItem, IntRange> req : requiredStored.entrySet()) {
			Integer reqQuantity = req.getValue().getRandomIn(rng);
			ItemStack toRemove = getItem(req, occurrence.getPlayerAttacker().getInventory(), reqQuantity);
			if(toRemove != null) {
				contained = true;
				toRemove.setAmount(toRemove.getAmount() - reqQuantity);
			}
		}
		return contained;
	}

	@Override
	public List<Condition> parse(ConfigurationNode node) {
		Map<ODItem, IntRange> value;
		value = parseItemString(node, null);
		if (value == null)
			return null;

		List<Condition> conditionList = new ArrayList<Condition>();
		conditionList.add(new ItemRequirementCheck(value));
		return conditionList;
	}

	public static Map<ODItem, IntRange> parseItemString(ConfigurationNode node, Map<ODItem, IntRange> def) {
		List<String> input = OtherDropsConfig.getMaybeList(node, "itemrequirement");
		if (input.isEmpty())
			return def;
		HashMap<ODItem, IntRange> result = new HashMap<>();
		for (String name : input) {
			String[] nameSplit = name.split("/q#");
			IntRange quantityRange = new IntRange(0);
			if (nameSplit.length > 1)
				quantityRange = IntRange.parse(nameSplit[1]);
			ODItem item = ODItem.parseItem(name.replaceAll("(\\/q#\\d{1,9}-\\d{1,9}|\\/q#\\d{1,9})", ""));
			result.put(item, quantityRange);
		}
		return result;
	}   

	public ItemStack getItem(Entry<ODItem, IntRange> reqEntry, Inventory inv, Integer reqQuantity) {
		ODItem req = reqEntry.getKey();
		for(ItemStack item : inv.getContents()) {
			if(item != null && item.getType().equals(req.getMaterial())) {
				boolean isContained = true;
				if (req.displayname != null) 
					if (!req.displayname.equals(item.getItemMeta().getDisplayName())) 
						isContained = false;

				if (isContained && req.lore != null && !req.lore.isEmpty()) 
					if (!req.lore.equals(item.getItemMeta().getLore())) 
						isContained = false;

				if (isContained && !req.getEnchantments().isEmpty()) 
					if(item.getEnchantments().isEmpty())
						isContained = false;
					else isContained = CommonEnchantments.matches(req.getEnchantments(), item.getEnchantments());

				if (isContained && reqQuantity > item.getAmount())
					isContained = false;
				
				if(isContained)  
					return item;
				
			}
		}
		return null;
	}
}
