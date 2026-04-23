package Kiosk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuSQL {

    private final Connection connection;

    public MenuSQL(Connection connection) {
        this.connection = connection;
    }

    public List<MenuItemData> findByCategoryId(int categoryId) throws SQLException {
        String sql = "select itemID, itemName, price, imagePath "
                + "from menu_Items "
                + "where categoryId = ? "
                + "order by itemID";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return readItems(rs, "itemID", "itemName", "price", "imagePath");
            }
        }
    }

    public List<MenuItemData> findDrinks() throws SQLException {
        String sql = "select drinkID, drinkName, price, image_path "
                + "from drinks "
                + "order by drinkID";

        return findItems(sql, "drinkID", "drinkName", "price", "image_path");
    }

    public List<MenuItemData> findAddons() throws SQLException {
        String sql = "select addonId, addonName, price, imagePath "
                + "from add_ons "
                + "order by addonId";

        return findItems(sql, "addonId", "addonName", "price", "imagePath");
    }

    private List<MenuItemData> findItems(
            String sql,
            String idColumn,
            String nameColumn,
            String priceColumn,
            String imageColumn
    ) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return readItems(rs, idColumn, nameColumn, priceColumn, imageColumn);
        }
    }

    private List<MenuItemData> readItems(
            ResultSet rs,
            String idColumn,
            String nameColumn,
            String priceColumn,
            String imageColumn
    ) throws SQLException {
        List<MenuItemData> items = new ArrayList<>();

        while (rs.next()) {
            items.add(new MenuItemData(
                    rs.getInt(idColumn),
                    rs.getString(nameColumn),
                    rs.getDouble(priceColumn),
                    rs.getString(imageColumn)
            ));
        }

        return items;
    }
    
    
}
