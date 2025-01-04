package com.gmail.zariust.otherdrops.event;

import com.gmail.zariust.otherdrops.OtherDrops;

import java.util.NavigableMap;
import java.util.TreeMap;

public class RandomDropCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private double total = 0;

    public RandomDropCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = OtherDrops.rng.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
