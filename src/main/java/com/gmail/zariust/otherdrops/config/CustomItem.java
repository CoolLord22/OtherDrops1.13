package main.java.com.gmail.zariust.otherdrops.config;

import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public class CustomItem {
    String customName;

    // for All
    String material;
    String loreName;
    String loreText;

    // for Books
    String bookTitle;
    String bookAuthor;
    List<String> bookPages;

    // for LeatherArmour
    String armourColour;

    // for Skulls
    String owner;

    Map<Enchantment, Integer> enchantments;

}
