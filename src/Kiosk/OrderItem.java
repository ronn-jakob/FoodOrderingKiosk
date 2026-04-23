package Kiosk;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    private MenuItemData menuItem;
    private MenuItemData drink;
    private List<MenuItemData> addons;
    private int quantity;

    public OrderItem(MenuItemData menuItem, MenuItemData drink, List<MenuItemData> addons, int quantity) {
        this.menuItem = menuItem;
        this.drink = drink;
        this.addons = addons != null ? new ArrayList<>(addons) : new ArrayList<>();
        this.quantity = quantity;
    }

    public MenuItemData getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItemData menuItem) {
        this.menuItem = menuItem;
    }

    public MenuItemData getDrink() {
        return drink;
    }

    public void setDrink(MenuItemData drink) {
        this.drink = drink;
    }

    public List<MenuItemData> getAddons() {
        return addons;
    }

    public void setAddons(List<MenuItemData> addons) {
        this.addons = addons != null ? new ArrayList<>(addons) : new ArrayList<>();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        double total = 0;

        if (menuItem != null) total += menuItem.getPrice();
        if (drink != null) total += drink.getPrice();

        for (MenuItemData addon : addons) {
            total += addon.getPrice();
        }

        return total;
    }

    public double getSubtotal() {
        return getUnitPrice() * quantity;
    }

    public String getDetailsText() {
        StringBuilder sb = new StringBuilder();

        if (drink != null) {
            sb.append("Drink: ").append(drink.getName());
        }

        if (addons != null && !addons.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Add-ons: ");

            for (int i = 0; i < addons.size(); i++) {
                sb.append(addons.get(i).getName());
                if (i < addons.size() - 1) {
                    sb.append(", ");
                }
            }
        }

        if (sb.length() == 0) {
            sb.append("No drink / no add-on");
        }

        return sb.toString();
    }
}