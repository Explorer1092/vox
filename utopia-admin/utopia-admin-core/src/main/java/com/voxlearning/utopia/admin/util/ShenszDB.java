package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chongfeng.qi
 * @since 2018/06/12
 */
/**
 * 处理神算子问题 by chongfeng.qi
 */
@Slf4j
public class ShenszDB {

    private static final String dbUserName = "ssz_17zuoye";
    private static final String dbUserPassword = "cfuw6Db1mwW!";
    private static final String dbUrl = "jdbc:mysql://rm-2zeq59j09r3n2rtx9.mysql.rds.aliyuncs.com:3306/shensz?useUnicode=true&useDynamicCharsetInfo=false&autoReconnect=true";
    private static AtomicInteger nextIdx = new AtomicInteger(-1);
    private static Map<String, List<String>> columnNameCache = new HashMap<>();
    private static List<DataSource> readOnlyDataSources = new ArrayList<>();
    private static Map<String,Map<String, String>> tableMeta = new HashMap<>();
    private static HikariDataSource masterDataSource = null;

    static {
        init();
        initReadOnly();
    }

    private static void init() {
        if (masterDataSource != null) {
            masterDataSource.close();
        }
        masterDataSource = initDataSource(dbUrl);
    }

    private static void initReadOnly() {
        for (DataSource dataSource : readOnlyDataSources) {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource)dataSource).close();
            }
        }
        String readOnlyUrls = "";
        String[] urls = StringUtils.split(readOnlyUrls, ",");
        for (String url : urls) {
            HikariDataSource ds = initDataSource(url);
            if (ds != null) readOnlyDataSources.add(ds);
        }
    }

    private static HikariDataSource initDataSource(String dbUrl) {
        HikariDataSource ds = null;
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(dbUserName);
            config.setPassword(dbUserPassword);
            config.setAutoCommit(false);
            config.setConnectionTimeout(30 * 1000);
            config.setIdleTimeout(60 * 1000);
            config.setMaximumPoolSize(10);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setConnectionInitSql("SET NAMES utf8mb4");
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            log.error("ShenszDB error %s" , e);
            e.printStackTrace();
        }
        return ds;
    }

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        Connection connection = getMasterConnection();
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tableRet = databaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});
            while(tableRet.next()) {
                String tableName = tableRet.getString("TABLE_NAME");
                tableMeta.put(tableName, new HashMap<>());
                ResultSet colRet = databaseMetaData.getColumns(null, "%", tableName, "%");
                while(colRet.next()) {
                    String columnName = colRet.getString("COLUMN_NAME");
                    String columnType = colRet.getString("TYPE_NAME");
                    tableMeta.get(tableName).put(columnName, columnType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static String getColumnType(String tableName, String columnName) {
        return tableMeta.get(tableName).get(columnName);
    }

    public static boolean isDatetimeColumn(String tableName, String columnName) {
        return Objects.equals(getColumnType(tableName, columnName), "DATETIME");
    }

    public static boolean containsColumn(String tableName, String columnName) {
        return tableMeta.get(tableName).containsKey(columnName);
    }

    /**
     * <p>
     * 定义连接mysql的驱动器.
     * <p>
     * @return connection:数据库连接
     */
    public static Connection getMasterConnection() {
        if (masterDataSource == null) init();
        try {
            return masterDataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public static Connection getReadOnlyConnection() {
        if (readOnlyDataSources == null || readOnlyDataSources.isEmpty()) initReadOnly();
        DataSource ds = null;
        for (;;) {
            int current = nextIdx.get();
            int next = current >= readOnlyDataSources.size() - 1 ? 0 : current + 1;
            if (nextIdx.compareAndSet(current, next)) {
                if (next < readOnlyDataSources.size()) {
                    ds = readOnlyDataSources.get(next);
                }
                break;
            }
        }
        // 如果从库都不存在，只好用主库来读了
        if (ds == null) {
            return getMasterConnection();
        }
        try {
            return ds.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * Execute an SQL query
     * @param sql sql
     * @return The query resultSet
     */
    public static <T> T executeQuery(MySQLCallback<T> callback, String sql, Object ... params) throws Exception {
        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            connection = getReadOnlyConnection();
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 0;

            if (params != null) {
                for (Object param : params) {
                    pst.setObject(++index, param);
                }
            }

            rs = pst.executeQuery();
            if (callback != null) {
                return callback.executeQuery(rs);
            }
        } finally {
            close(rs);
            close(pst);
            close(connection);
        }
        return null;
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getColumns(ResultSet rs, String sql) {
        String s = StringUtils.substringBefore(sql, "where").trim();
        List<String> columns = columnNameCache.get(s);
        if (columns == null || columns.isEmpty()) {
            columns = new ArrayList<String>();
            try {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
//                    columns.add(metaData.getColumnName(i));
                    columns.add(metaData.getColumnLabel(i));
                }
                columnNameCache.put(s, columns);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return columns;
    }



    public interface MySQLCallback<T> {
        T executeQuery(final ResultSet rs) throws Exception;
    }

    public static Map findFirst(final String sql, Object... args) {
        try {
            return executeQuery(rs -> {
                List<String> columns = getColumns(rs, sql);
                Map<String, Object> data = null;
                if (rs.next()) {
                    data = new HashMap<>();
                    for (int i = 1; i <= columns.size(); i++) {
                        data.put(columns.get(i - 1), rs.getObject(i));
                    }
                }
                return data;
            }, sql, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Map> find(final String sql, Object... args) {
        try {
            return executeQuery(rs -> {
                List<String> columns = getColumns(rs, sql);
                List<Map> data = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int i = 1; i <= columns.size(); i++) {
                        map.put(columns.get(i - 1), rs.getObject(i));
                    }
                    data.add(map);
                }
                return data;
            }, sql, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> find(final Class<T> cls, String sql, Object... args) {
        final String realSql = String.format("select * from %s where %s", cls.getSimpleName(), sql);
        try {
            return executeQuery(rs -> {
                List<String> columns = getColumns(rs, realSql);
                List<T> data = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i <= columns.size(); i++) {
                        map.put(columns.get(i - 1), rs.getObject(i));
                    }
                    data.add(JsonUtils.fromJson(JsonUtils.toJson(map), cls));
                }
                return data;
            }, realSql, args);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static <T> T findById(final Class<T> cls, final long id) {
        return JsonUtils.fromJson(JsonUtils.toJson(findFirst(String.format("select * from %s where id=%s", cls.getSimpleName()), id)), cls);
    }


    public static <T> T findFirst(final Class<T> cls, final String sql, Object... args) {
        return JsonUtils.fromJson(JsonUtils.toJson(findFirst(String.format("select * from %s where %s", cls.getSimpleName(), sql), args)), cls);
    }

    public static <T> long count(String sql, Object... args) throws Exception {
    	final String realSql = String.format("select count(*) as total from %s", sql);
    	Connection connection = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try {
    		connection = getReadOnlyConnection();
    		pst = connection.prepareStatement(realSql, Statement.RETURN_GENERATED_KEYS);
    		int index = 0;

    		if (args != null) {
    			for (Object param : args) {
    				pst.setObject(++index, param);
    			}
    		}
    		rs = pst.executeQuery();
    		while(rs.next()){
    			return rs.getInt("total");
    		}
    	} finally {
    		close(rs);
    		close(pst);
    		close(connection);
    	}
    	return 0l;
    }

    public static List<DataSource> getReadOnlyDataSources() {
        return readOnlyDataSources;
    }

    public static void withTransaction(Execute execute) throws SQLException {
        Connection connection = null;
        try {
            connection = getMasterConnection();
            execute.connection = connection;
            execute.call();
            connection.commit();
        }  catch (Exception e) {
            rollback(connection);
            throw new SQLException(e);
        }  finally {
            close(connection);
        }
    }
}