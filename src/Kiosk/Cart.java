package Kiosk;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String orderType;
    private final List<OrderItem> items = new ArrayList<>();

    public Cart(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderType() {
        return orderType;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void updateItem(int index, OrderItem item) {
        items.set(index, item);
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public double getTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}