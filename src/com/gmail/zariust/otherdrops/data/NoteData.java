// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NoteData implements Data, RangeableData {
    private Note note;
    private Instrument instrument;

    public NoteData(BlockData data) {
        if (data instanceof NoteBlock) {
            note = ((NoteBlock) data).getNote();
            instrument = ((NoteBlock) data).getInstrument();
        }
    }

    public NoteData(Note tone, Instrument instrument) {
        this.note = tone;
        this.instrument = instrument;
    }

    @SuppressWarnings("deprecation")
	@Override
    public int getData() {
        return note.getId();
    }

    @Override
    public void setData(int d) {
        note = new Note((byte) d);
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof NoteData noteData)) // matches target isnt note data
            return false;
        if (note != null && !note.equals(noteData.note))
            return false;
        return instrument == null || instrument.equals(noteData.instrument);
    }

    @Override
    public String get(Enum<?> mat) {
        String result = "";
        if (mat == Material.NOTE_BLOCK) {
            if(note != null) {
                result += note.getTone();
                if (note.isSharped())
                    result += "#";
                result += note.getOctave();
            }
            result += "/" + instrument.name();
        }
        return result;
    }

    @Override
    public void setOn(BlockState state) {
        if (!(state.getBlockData() instanceof NoteBlock noteBlock)) {
            Log.logWarning("Tried to change a note block, but no note block was found!");
            return;
        }
        noteBlock.setNote(note);
        noteBlock.setInstrument(instrument);
        state.setBlockData(noteBlock);
    }

    @Override
    // Note blocks are not entities, so nothing to do here
    public void setOn(Entity entity, Player witness) {
    }

    public static Data parse(String state) throws IllegalArgumentException {
        if (state == null || state.isEmpty())
            return null;
        String[] args = state.split("/");
        Note note = null;
        Instrument instrument = null;
        for(String arg : args) {
            try {
                instrument = Instrument.valueOf(arg.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            try {
                Log.logWarning("State: " + arg);
                if (arg.startsWith("RANGE"))
                    return RangeData.parse(arg);
                if (arg.matches("([A-G])(#?)([0-2]?)")) {
                    Note.Tone tone = Note.Tone.valueOf(arg.substring(0, 1));
                    byte octave;
                    if (arg.matches("..?[0-2]"))
                        octave = Byte.parseByte(arg.substring(arg.length() - 1));
                    else
                        octave = 1;
                    note = new Note(octave, tone, arg.contains("#"));
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return new NoteData(note, instrument);
    }

    @SuppressWarnings("deprecation")
	@Override
    public int hashCode() {
        // Note doesn't define a hashCode() and is not an enum, so use the note
        // ID instead
        return note == null ? 0 : note.getId();
    }

    @Override
    public Boolean getSheared() {
        // TODO Auto-generated method stub
        return null;
    }
}
