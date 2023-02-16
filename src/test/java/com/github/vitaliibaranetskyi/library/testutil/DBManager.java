package com.github.vitaliibaranetskyi.library.testutil;

import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {
    //private static final Logger logger = LogManager.getLogger("TEST");
    private static final String INIT_FILE = "/data/webServers/library/sql/library-test-db-init.sql";
    private static final String PROPERTIES_FILE = "test_db.properties";
    private static final String URL_PROPERTY = "test.db.url";
    private static final String USER_PROPERTY = "test.db.user";
    private static final String PASSWORD_PROPERTY = "test.db.password";
    private static final String DB_NAME_PROPERTY = "test.db.name";
    private static class DBManagerHolder {
        private static final DBManager INSTANCE = new DBManager();
        private static final String URL;
        private static final String USER;
        private static final String PASSWORD;
        private static final String DB_NAME;

        static {
            Properties prop = new Properties();
            try (Reader r = new FileReader(PROPERTIES_FILE)) {
                prop.load(r);
            } catch (IOException e) {
                System.err.println("Unable to read connection data for test DB");
                e.printStackTrace();
            }
            USER = prop.getProperty(USER_PROPERTY);
            PASSWORD = prop.getProperty(PASSWORD_PROPERTY);
            DB_NAME = prop.getProperty(DB_NAME_PROPERTY);
            URL = prop.getProperty(URL_PROPERTY) + DB_NAME + "?user=" + USER + "&password=" + PASSWORD;
        }
    }

    private final List<String> scriptLines;

    private DBManager() {
        scriptLines = loadScript();
    }

    public static DBManager getInstance() {
        return DBManagerHolder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBManagerHolder.URL);
    }

    private List<String> loadScript() {
        List<String> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(INIT_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("") || line.startsWith("-- ")) {
                    continue;
                }

                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void executeScript() throws IOException, InterruptedException, ServiceException {
        final String command = "/usr/local/bin/mysql";

        ProcessBuilder pb = new ProcessBuilder(command, "-u", DBManagerHolder.USER,
                "--password=" + DBManagerHolder.PASSWORD, DBManagerHolder.DB_NAME);
        pb.redirectInput(new File(INIT_FILE));
        Process p = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // Read the output from the command
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            sb.append(s).append(System.lineSeparator());
        }
        if (!sb.toString().isEmpty()) {
            sb.insert(0, "Output: ");
            System.out.println(sb);
            sb = new StringBuilder();
        }

        // Read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            sb.append(s).append(System.lineSeparator());
        }

        if (!sb.toString().isEmpty()) {
            sb.insert(0, "Error: ");
            System.out.println(sb);
        }

        int exitVal = p.waitFor();
        System.out.println("Script executed, exit value = " + exitVal);
    }

    public boolean read(String table, String column, String value) throws SQLException {
        final String sql = "SELECT * FROM %s WHERE %s = ?";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(prepareQuery(sql, table, column))) {
                ps.setString(1, value);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    public boolean read(String table, String column, long id) throws SQLException {
        final String sql = "SELECT * FROM %s WHERE %s = ?";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(prepareQuery(sql, table, column))) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    public String readField(String retrieveField, String table, String column, String value) throws SQLException {
        final String sql = "SELECT %s FROM %s WHERE %s = ?";
        String result = null;

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(prepareQuery(sql, retrieveField, table, column))) {
                ps.setString(1, value);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = rs.getString(1);
                    }
                }
            }
        }
        return result;
    }

    public boolean delete(String table, String emailColumn, String email) throws SQLException {
        final String sql = "DELETE from %s WHERE %s = ?";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(prepareQuery(sql, table, emailColumn))) {
                ps.setString(1, email);
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean execute(String sql) throws SQLException {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement()) {
                return st.execute(sql);
            }
        }
    }

    private static String prepareQuery(String sql, String... args) {
        return String.format(sql, (Object[]) args);
    }

    public void testWrapper(TestBody t) throws SQLException, ServiceException, DaoException {
        try (Connection c = getConnection()) {
            t.accept(c);
        }
    }
}
