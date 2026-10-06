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

package com.gmail.zariust.otherdrops.options;

import com.gmail.zariust.otherdrops.ConfigurationNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comparative {
    private static final Pattern RANGE = Pattern.compile("(-?\\d+)\\s*[-~]\\s*(-?\\d+)");

    private final int      compare;
    private final int      val;
    private final IntRange range;

    public Comparative(int v) {
        this(v, 0, null);
    }

    public Comparative(int v, int cmp, IntRange range) {
        val = v;
        compare = cmp;
        this.range = range;
    }

    public boolean matches(int v) {
        if (range != null)
            return range.contains(v);
        return Integer.compare(v, val) == compare;
    }

    public static Comparative parse(String cmp) {
        if (cmp == null) return null;
        String s = cmp.trim();
        try {
            if (s.startsWith(">=")) return new Comparative(Integer.parseInt(s.substring(2).trim()) - 1, 1, null);
            if (s.startsWith("<=")) return new Comparative(Integer.parseInt(s.substring(2).trim()) + 1, -1, null);
            if (s.startsWith(">")) return new Comparative(Integer.parseInt(s.substring(1).trim()), 1, null);
            if (s.startsWith("<")) return new Comparative(Integer.parseInt(s.substring(1).trim()), -1, null);
            if (s.startsWith("=")) return new Comparative(Integer.parseInt(s.substring(1).trim()));
            Matcher range = RANGE.matcher(s);              // "5-10", "-64--10", "-5~5"
            if (range.matches())
                return new Comparative(0, 0, new IntRange(Integer.parseInt(range.group(1)), Integer.parseInt(range.group(2))));
            return new Comparative(Integer.parseInt(s));   // "10", "-5"
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid comparison '" + cmp + "' (use <N, >N, <=N, >=N, =N, N or MIN-MAX)", e);
        }
    }

    public static Comparative parseFrom(ConfigurationNode node, String key,
            Comparative def) {
        Comparative cmp = parse(node.getString(key));
        if (cmp == null)
            return def;
        return cmp;
    }

    @Override
    public String toString() {
        char sep = '?';
        if (compare == -1)
            sep = '<';
        else if (compare == 0)
            sep = '=';
        else if (compare == 1)
            sep = '>';
        return sep + "" + val;
    }
}
