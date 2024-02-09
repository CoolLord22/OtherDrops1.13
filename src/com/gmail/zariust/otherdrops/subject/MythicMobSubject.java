package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.MythicMobData;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

import static com.gmail.zariust.common.Verbosity.HIGH;

public class MythicMobSubject extends CreatureSubject {
    private Data data = null; // MythicMobData
    private Entity entity = null;

    public MythicMobSubject(Data data) {
        this.data = data;
    }

    public MythicMobSubject(Entity entity) {
        if(Dependencies.hasMythicMobs()) {
            this.entity = entity;
            ActiveMob mythicMob = Dependencies.getMythicMobs().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
            this.data = new MythicMobData(mythicMob.getMobType());
        }
    }

    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof MythicMobSubject))
            return false;
        if(data != null)
            return this.data.matches(((MythicMobSubject) other).data);

        return true;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public Location getLocation() {
        if (entity == null) {
            Log.logInfo("MythicMobSubject.getLocation() - agent is null, this shouldn't happen.", HIGH);
            return null;
        }
        return entity.getLocation();
    }

    @Override
    public String getReadableName() {
        if(data != null && data instanceof MythicMobData)
            return "a Mythic " + ((MythicMobData) data).getMythicMobType();
        return null;
    }

    @Override
    public boolean overrideOn100Percent() {
        return false;
    }

    @Override
    public List<Target> canMatch() {
        List<Target> all = new ArrayList<Target>();
        all.add(this);
        if(((MythicMobData) data).getMythicMobType().equalsIgnoreCase("ANY")) {
            for(String s : Dependencies.getMythicMobs().getMobManager().getMobNames()) {
                all.add(new MythicMobSubject(new MythicMobData(s)));
            }
        }
        return all;
    }

    @Override
    public String getKey() {
        if(data != null && data instanceof MythicMobData)
            return ((MythicMobData) data).getMythicMobType();
        return null;
    }

    @Override
    public void setTo(BlockTarget replacement) {
        if (entity == null) {
            Log.logWarning("MythicMobSubject had a null entity; could not remove it and replace with blocks.");
            return;
        }
        Block bl = entity.getLocation().getBlock();
        new BlockTarget(bl).setTo(replacement);
        entity.remove();
    }

    public static MythicMobSubject parse(String data) {
        if (data == null || data.isEmpty())
            return new MythicMobSubject((Data) null);
        Log.logInfo("Parsing MythicMob subject: " + data, Verbosity.HIGHEST);
        return new MythicMobSubject(new MythicMobData(data));
    }

    public LivingEntity getEntity() {
        return (LivingEntity) this.entity;
    }

    public EntityType getCreature() {
        return this.entity.getType();
    }

    @Override
    public String toString() {
        String val = "";
        if (data != null) {
            val += data.toString();
        }
        return val;
    }

    @Override
    public void damage(int amount) {
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).damage(amount);
        }
    }
}
