package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.things.ODItem;

public class ItemRequirementCheck extends Condition {
	String name = "ItemRequirementCheck";
	private final Map<ODItem, Integer> requiredStored;

	public ItemRequirementCheck(Map<ODItem, Integer> requiredStored) {
		this.requiredStored = requiredStored;
	}

	@Override
	public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
		boolean contained = false;
		for(Entry<ODItem, Integer> req : requiredStored.entrySet()) {
			ItemStack toRemove = getItem(req, occurrence.getPlayerAttacker().getInventory());
			if(toRemove != null) {
				contained = true;
				toRemove.setAmount(toRemove.getAmount() - req.getValue());
			}
		}
		return contained;
	}

	@Override
	public List<Condition> parse(ConfigurationNode node) {
		Map<ODItem, Integer> value = new HashMap<ODItem, Integer>();
		value = parseItemString(node, null);
		if (value == null)
			return null;

		List<Condition> conditionList = new ArrayList<Condition>();
		conditionList.add(new ItemRequirementCheck(value));
		return conditionList;
	}

	public static Map<ODItem, Integer> parseItemString(ConfigurationNode node, Map<ODItem, Integer> def) {
		List<String> input = OtherDropsConfig.getMaybeList(node, "itemrequirement");
		if (input.isEmpty())
			return def;
		HashMap<ODItem, Integer> result = new HashMap<ODItem, Integer>();
		for (String name : input) {
			String[] nameSplit = name.split("/q#");
			Integer quantity = 1;
			if (nameSplit.length > 1)
				quantity = Integer.parseInt(nameSplit[1]);
			ODItem item = ODItem.parseItem(name.replaceAll("(\\/q#\\d{1,9})", ""));
			result.put(item, quantity);
		}
		return result;
	}   

	public ItemStack getItem(Entry<ODItem, Integer> reqEntry, Inventory inv) {
		ODItem req = reqEntry.getKey();
		Integer reqQuantity = reqEntry.getValue();
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
