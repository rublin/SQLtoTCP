package org.rublin.repository;

import org.slf4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.rublin.Main.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Repository provides JDBC connections to SQL database
 *
 * @author Ruslan Sheremet
 * @see Connection
 * @see Statement
 * @see ResultSet
 * @since 1.0
 */
public class JdbcRepository implements Repository {

    private static final Logger LOG = getLogger(JdbcRepository.class);


    private static final JdbcRepository repository = new JdbcRepository();
    private Connection connection;
    private Statement statement;
    private JdbcRepository() {
        try {
            Class.forName(properties.getProperty("db.driverClassName"));
            connection = DriverManager.getConnection(String.format("%s?user=%s&password=%s", properties.getProperty("db.url"), properties.getProperty("db.username"), properties.getProperty("db.password")));
            statement = connection.createStatement();
        } catch (Exception e) {
            LOG.error("Failed to establish database connection. Error: {}", e.getMessage());
        }
    }

    public static JdbcRepository getRepository() {
        return repository;
    }

    @Override
    public List<String> getAllRecords(String table) {
        return select(String.format("SELECT * FROM %s",table));
    }

    @Override
    public List<String> getLastRecords(int id, String table) {
        return select(String.format("SELECT * FROM %s WHERE id > %d", table, id));
    }

    /**
     * Close statement and connection
     */
    @Override
    public void closeRepository() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e ) {
            LOG.error("Failed to close database connection. Error: {}", e.getMessage());       }
    }

    private List<String> select(String query) {
        List<String> result = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                result.add(getString(resultSet));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private String getString (ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            result.append(resultSet.getString(i));
            result.append(",");
        }
        result.replace(result.lastIndexOf(","), result.length(), "\n");
        LOG.debug("String {} added", result);
        return result.toString();
    }
}
