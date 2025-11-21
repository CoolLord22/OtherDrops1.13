package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ProjectileAgent;
import com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LoreNameCheck extends Condition {

    String name = "LoreNameCheck";
    private final String loreName;

    public LoreNameCheck(String loreName) {
        this.loreName = ODVariables.preParse(loreName);
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        String parsedLorename = MessageAction.parseVariables(loreName, drop, occurrence, -1);
        Log.logInfo("Starting lorename check (" + parsedLorename + ")", Verbosity.HIGHEST);
        if (occurrence.getTool() instanceof PlayerSubject ps) {
            return checkLoreName(ps, parsedLorename);
        } else if (occurrence.getTool() instanceof ProjectileAgent pa) {
            if (pa.getShooter() instanceof PlayerSubject ps) {
                return checkLoreName(ps, parsedLorename);
            }
        }
        return false;
    }

    private boolean checkLoreName(PlayerSubject player, String parsedLorename) {
        ItemStack item = player.getTool().getActualTool();
        if (item == null) return false; // not sure when item would be null but it can be

        Log.logInfo("tool name = " + item.getType().name(), Verbosity.HIGHEST);
        if (item.hasItemMeta()) {
            String displayName = item.getItemMeta().getDisplayName();
            Log.logInfo("Checking for lorename condition... '" + displayName + "' == '" + parsedLorename + "'", Verbosity.HIGHEST);
            return displayName.equalsIgnoreCase(parsedLorename);
        }
        return false;
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        String loreName = node.getString("lorename");
        if (loreName == null) {
            loreName = node.getString("displayname");
            if (loreName == null) return null;
        }

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new LoreNameCheck(loreName));
        return conditionList;
    }

}
