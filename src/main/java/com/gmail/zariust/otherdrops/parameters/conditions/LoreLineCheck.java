package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import main.java.com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import main.java.com.gmail.zariust.otherdrops.subject.PlayerSubject;
import main.java.com.gmail.zariust.otherdrops.subject.ProjectileAgent;
import main.java.com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LoreLineCheck extends Condition {
    private final String loreLine;

    public LoreLineCheck(String loreLine) {
        this.loreLine = ODVariables.preParse(loreLine);
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (loreLine == null) return true;
        String parsedLoreline = MessageAction.parseVariables(loreLine, drop, occurrence, -1);
        Log.logInfo("Starting loreline check (" + parsedLoreline + ")", Verbosity.HIGHEST);
        if (occurrence.getTool() instanceof PlayerSubject ps) {
            return checkLoreLines(ps, parsedLoreline);
        } else if (occurrence.getTool() instanceof ProjectileAgent pa) {
            if (pa.getShooter() instanceof PlayerSubject ps) {
                return checkLoreLines(ps, parsedLoreline);
            }
        }
        return false;
    }

    private boolean checkLoreLines(PlayerSubject player, String parsedLoreline) {
        ItemStack item = player.getTool().getActualTool();
        if (item == null) return false; // not sure when item would be null but it can be

        Log.logInfo("Tool material = " + item.getType().name(), Verbosity.HIGHEST);
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = ODVariables.preParse(item.getItemMeta().getLore());
            for (String loreLine : lore) {
                Log.logInfo("Checking for loreline condition... '" + lore + "' == '" + parsedLoreline + "'", Verbosity.HIGHEST);
                if (loreLine.equals(parsedLoreline)) return true;
            }
            Log.logInfo("Tool has no matching lore.", Verbosity.HIGHEST);
        }
        Log.logInfo("Tool has no lore.", Verbosity.HIGHEST);
        return false;
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        String loreName = node.getString("loreline");
        if (loreName == null) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new LoreLineCheck(loreName));
        return conditionList;
    }

}
