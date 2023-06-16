package db;

import products.Dessert;
import products.Ingredient;
import products.ProductType;
import products.Size;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DessertDAO extends DAO<Dessert> {

    protected DessertDAO() {
        super("product", "id");
    }

    @Override
    protected Map<Integer, Dessert> readAll() throws SQLException {
        String query = """
                SELECT p.id,
                p.name,
                p.type_id,
                p.isVegan,
                pt.type_name,
                it.ingredient_type_name,
                ps.price,
                ps.size_id,
                s.size_name
                FROM product p JOIN product_type pt on p.type_id = pt.id
                JOIN product_ingredient pi on p.id = pi.product_id
                JOIN ingredient i on pi.ingredient_id = i.id
                JOIN ingredient_type it on i.ingredient_type_id = it.id
                JOIN product_size ps on pi.product_id = ps.product_id
                JOIN size s on ps.size_id = s.id
                WHERE type_name = 'dessert'
                """;
        Map<Integer, Dessert> entries = new HashMap<>();
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Dessert object = mapResultSetToModel(resultSet);
                entries.put(getKey(object), object);
            }
        }
        return entries;
    }

    @Override
    protected String buildInsertQuery(Object object) {
        return "INSERT INTO " + this.tableName + "(id,name,type_id,isVegan) VALUES(?,?,?,?)";
    }

    @Override
    protected void setInsertValues(PreparedStatement statement, Object object) throws SQLException {
        if (object instanceof Dessert dessert) {
            statement.setInt(1, dessert.getId());
            statement.setString(2, dessert.getName());
            statement.setInt(3, dessert.getProductType().getId());
            statement.setBoolean(4, dessert.isVegan());
        }
    }

    @Override
    protected Integer getKey(Dessert object) {
        return object.getId();
    }

    @Override
    protected Dessert mapResultSetToModel(ResultSet resultSet) throws SQLException {
        int pizzaId = resultSet.getInt("id");
        List<Size> availableSizes = readAllAvailableSizes(pizzaId);

        return new Dessert(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getDouble("price"),
                new ProductType(resultSet.getInt("type_id"), resultSet.getString("type_name")),
                availableSizes,
                resultSet.getBoolean("isVegan")
        );
    }

    @Override
    String buildUpdateQuery(int variableIndex) {
        Map<Integer, String> columnMap = Map.of(
                1, "product_id",
                2, "name",
                3, "type_id",
                4, "isVegan"
        );
        String columnName = columnMap.get(variableIndex);
        return "UPDATE " + this.tableName + " SET " + columnName + "=? WHERE id=?";
    }

    @Override
    void setUpdatedValues(PreparedStatement statement, int variableIndex, Object updatedValue) throws SQLException {
        switch (variableIndex) {
            case 1, 3 -> statement.setInt(1, (Integer) updatedValue);
            case 2 -> statement.setString(1, (String) updatedValue);
            case 4 -> statement.setBoolean(1, (boolean) updatedValue);
        }
    }
}
