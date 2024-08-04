package data;

import utils.DateUtils;
import utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseController {

    final String TABLE_PREFS = "prefs";

    private Connection db;
    private String dbFileName;
    private boolean connected;
    private boolean newDatabase;
    private boolean transactionMode;
    private boolean oldAutoCommit;

    public DatabaseController(String dbFileName) throws Exception {
        this();
        this.dbFileName = dbFileName;
        connect(dbFileName);
    }

    public DatabaseController() {
        db = null;
        connected = false;
        dbFileName = null;
        newDatabase = false;
    }

    public String getDbFileName() {
        return dbFileName;
    }

    public void setDbFileName(String dbFileName) throws Exception {
        if (connected)
            throw new Exception("Can't change database file name because the connection is already established!");

        this.dbFileName = dbFileName;
    }

    public void connect(String dbFileName) throws Exception {
        if (dbFileName == null)
            throw new Exception("The database file was not specified!");
        if (connected)
            throw new Exception("The connection is already established!");

        setDbFileName(dbFileName);
        try {
            Class.forName("org.sqlite.JDBC");
            //Establishing a connection
            db = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
            connected = true;
            initDB();
        } catch (Exception e) {
            connected = false;
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    throw new Exception(e2.getMessage());
                }
            }
            throw new Exception("Can't open a connection with <" + dbFileName + ">\n" + e.getMessage());
        }
    }

    public void disconnect() throws Exception {
        if (!connected)
            return;

        try {
            db.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            connected = false;
        }
    }

    public void reconnect() throws Exception {
        if (!connected)
            return;

        disconnect();

        try {
            Class.forName("org.sqlite.JDBC");
            //Establishing a connection
            db = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
            connected = true;
        } catch (Exception e) {
            connected = false;
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    throw new Exception(e2.getMessage());
                }
            }
            throw new Exception("Can't open a connection with <" + dbFileName + ">\n" + e.getMessage());
        }
    }

    public boolean isNewDatabase() {
        return newDatabase;
    }

    public void transactionBegin() throws SQLException {
        if (transactionMode)
            return;

        oldAutoCommit = db.getAutoCommit();
        db.setAutoCommit(false);
        transactionMode = true;
    }

    public void transactionRollback() throws SQLException {
        if (!transactionMode)
            return;

        db.rollback();
        transactionMode = false;
    }

    public void transactionCommit() throws SQLException {
        if (!transactionMode)
            return;

        db.commit();
        db.setAutoCommit(oldAutoCommit);
        transactionMode = false;
    }

    public List<Object[]> execQuery(String sql) throws Exception {
        checkConnection();

        List<Object[]> buffer = new ArrayList<>();

        try {
            int colCount = -1;
            Statement stm = db.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                if (colCount == -1)
                    colCount = rs.getMetaData().getColumnCount();
                Object[] curRow = new Object[colCount];
                for (int col = 1; col <= colCount; col++)
                    curRow[col - 1] = rs.getObject(col);
                buffer.add(curRow);
            }
        } catch(SQLException e) {
            throw new Exception("Can't execute a query\n" + e.getMessage());
        }

        return buffer;
    }

    public void createTable(String tableName, String[] params) throws Exception {
        checkConnection();

        try {
            String sql = "CREATE TABLE " + tableName + " (" + StringUtils.arrayToString(params) + ")";
            Statement stm = db.createStatement();
            stm.execute(sql);
        } catch(SQLException e){
            throw new Exception("Can't create a table <" + tableName + ">\n" + e.getMessage());
        }
    }

    public void createIndex(String tableName, int indexNumber, String[] columns) throws Exception {
        checkConnection();

        try {
            String sql = "CREATE INDEX " + tableName + "_index_" + indexNumber + " ON " + tableName + " (" + StringUtils.arrayToString(columns) + ")";
            Statement stm = db.createStatement();
            stm.execute(sql);
        } catch(SQLException e){
            throw new Exception("Can't create an index for a table <" + tableName + ">\n" + e.getMessage());
        }
    }

    public void dropTable(String tableName) throws Exception {
        checkConnection();

        try {
            String sql = "DROP TABLE " + tableName;
            Statement stm = db.createStatement();
            stm.execute(sql);
        } catch(SQLException e){
            throw new Exception("Can't drop a table <" + tableName + ">\n" + e.getMessage());
        }
    }

    public int insertData(String tableName, String[] params, Object[] values) throws Exception {
        checkConnection();
        int lastId = -1;

        try {
            checkParamsValues(params, values);
            StringBuilder sqlValues = new StringBuilder();
            for (Object ignored : values) {
                if (sqlValues.length() > 0)
                    sqlValues.append(", ");
                sqlValues.append("?");
            }
            String sql = "INSERT INTO " + tableName + " (" + StringUtils.arrayToString(params) + ") VALUES (" + sqlValues + ")";
            PreparedStatement pstmt = db.prepareStatement(sql);
            valuesToParams(values, pstmt);
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            generatedKeys.next();
            if (generatedKeys.getRow() > 0) {
                lastId = generatedKeys.getInt(1);
            }
        } catch(SQLException e){
            throw new Exception("Can't insert data into a table <" + tableName + ">\n" + e.getMessage());
        }

        return lastId;
    }

    public void insertDataBatch(String tableName, final String[] params, final List<Object[]> valuesList) throws Exception {
        checkConnection();
        if (params == null || params.length == 0)
            throw new Exception("No header's columns");
        if (valuesList == null || valuesList.size() == 0)
            throw new Exception("No values");

        //Prepare header
        StringBuilder sqlParams = new StringBuilder();
        StringBuilder sqlValues = new StringBuilder();
        for (var colName : params) {
            sqlParams.append((sqlParams.length() > 0) ? ", " : "").append(colName);
            sqlValues.append((sqlValues.length() > 0) ? ", " : "").append("?");
        }

        //Insert records loop
        final int INSERT_BATCH_SIZE = 5;
        int recordsLeft = valuesList.size();
        int rowIndex = 0;
        while (recordsLeft > 0) {
            int recordsToInsert = Math.min(recordsLeft, INSERT_BATCH_SIZE);
            recordsLeft -= recordsToInsert;
            //Insert pack of records
            try {
                StringBuilder valuesTemplate = new StringBuilder();
                List<Object> values = new ArrayList<>();
                for (var i = 0; i < recordsToInsert; i++) {
                    try {
                        Object[] subValues = valuesList.get(rowIndex++);
                        if (subValues == null || subValues.length != params.length)
                            throw new Exception("Amount of values in a row #" + rowIndex + " not equal to an amount columns of the header");
                        values.addAll(Arrays.asList(subValues));
                        valuesTemplate.append((valuesTemplate.length() > 0) ? ", " : "").append("(").append(sqlValues).append(")");
                    } catch (Exception exception) {
                        throw new Exception("[rowIndex=" + rowIndex + "]: " + exception.getMessage());
                    }
                }
                var sql = "INSERT INTO " + tableName + " (" + sqlParams + ") VALUES " + valuesTemplate;
                PreparedStatement pstmt = db.prepareStatement(sql);
                valuesToParams(values.toArray(), pstmt);
                pstmt.executeUpdate();
            } catch (Exception e) {
                throw new Exception("Unable to insert rows #" + (rowIndex - recordsToInsert) + "-" + (rowIndex - 1) + ": " + e.getMessage());
            }
        }
    }

    public int updateData(String tableName, String condition, String[] params, Object[] values) throws Exception {
        checkConnection();
        int rowsAffected;

        try {
            checkParamsValues(params, values);
            StringBuilder sqlParams = new StringBuilder();
            for (String prm : params) {
                if (sqlParams.length() > 0)
                    sqlParams.append(", ");
                sqlParams.append(prm);
                sqlParams.append("=?");
            }
            String sql = "UPDATE " + tableName + " SET " + sqlParams + (condition != null && condition.length() > 0 ? " WHERE " + condition : "");
            PreparedStatement pstmt = db.prepareStatement(sql);
            valuesToParams(values, pstmt);
            rowsAffected = pstmt.executeUpdate();
        } catch(SQLException e){
            throw new Exception("Can't update data in a table <" + tableName + ">\n" + e.getMessage());
        }

        return rowsAffected;
    }

    public int deleteData(String tableName, String condition) throws Exception {
        checkConnection();
        int rowsAffected;

        try {
            String sql = "DELETE FROM " + tableName + (condition != null && condition.length() > 0 ? " WHERE " + condition : "");
            PreparedStatement pstmt = db.prepareStatement(sql);
            rowsAffected = pstmt.executeUpdate();
        } catch(SQLException e){
            throw new Exception("Can't update data in a table <" + tableName + ">\n" + e.getMessage());
        }

        return rowsAffected;
    }

    public boolean tableExists(String tableName) throws Exception {
        checkConnection();

        try {
            DatabaseMetaData md = db.getMetaData();
            ResultSet rs = md.getTables(null, null, tableName, null);
            rs.next();
            return rs.getRow() > 0;
        } catch(SQLException e){
            throw new Exception("Can't check a table <" + tableName + ">\n" + e.getMessage());
        }
    }

    private void initDB() throws Exception {
        try {
            if (!tableExists(TABLE_PREFS)) {
                createTable(TABLE_PREFS, new String[] { "paramName VARCHAR(64)", "strValue VARCHAR(250)", "intValue INT" });
                newDatabase = true;
            }
        } catch(Exception e){
            throw new Exception("Can't initialize the database\n" + e.getMessage());
        }
    }

    private void valuesToParams(Object[] values, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof java.util.Date)
                value = DateUtils.dateToInt((java.util.Date) value);
            preparedStatement.setObject(i + 1, value);
        }
    }

    private void checkParamsValues(String[] params, Object[] values) {
        if (params == null || params.length == 0)
            throw new IllegalArgumentException("An argument 'params' shouldn't be empty!");
        if (values == null || values.length == 0)
            throw new IllegalArgumentException("An argument 'values' shouldn't be empty!");
        if (params.length != values.length)
            throw new IllegalArgumentException("An amount 'params' and 'values' should be equal!");
    }

    private void checkConnection() throws Exception {
        if (!connected)
            throw new Exception("A connection is not established!");
    }
}
