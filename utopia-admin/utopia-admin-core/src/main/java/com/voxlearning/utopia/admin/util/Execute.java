package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import java.sql.*;
import java.util.*;

public abstract class Execute {

    public Connection connection;
    public abstract void call() throws Exception;

    public int executeUpdate(String sql, Object... params) throws SQLException {
        return executeUpdate(connection, sql, params);
    }

    public int[] executeBatch(String sql, List<Object[]> paramsList) throws SQLException {
        return executeBatch(connection, sql, paramsList);
    }

    public int[] executeBatchByOne(String sql, Object ... params) throws SQLException {
        return executeBatchByOne(connection, sql, params);
    }

    public long executeUpdateResponse(String sql, Object ... params) throws SQLException {
        return executeUpdateResponse(connection, sql, params);
    }
    public <T> List<T> find(final Class<T> cls, String sql, Object... params) throws SQLException {
    	return find(connection, cls, sql, params);
    }
//    public long save(Object object) throws Exception {
//        Class cls = object.getClass();
//        String tableName = cls.getSimpleName();
//        List setKeys = Arrays.asList(cls.getDeclaredFields()).stream().map(Field::getName).collect(Collectors.toList());
//
//        Long id;
//        if (setKeys.contains("id") && (id = (Long)cls.getDeclaredField("id").get(object)) != null) {
//            List<String> signs = new ArrayList<>();
//            List<Object> values = new ArrayList<>();
//            for (Object key : setKeys) {
//                if (!Objects.equals(key, "id") && DB.containsColumn(tableName, (String) key)) {
//                    signs.add(key + "=?");
//                    Object value = cls.getDeclaredField((String)key).get(object);
//                    values.add(DB.isDatetimeColumn(tableName, key.toString()) && value instanceof Long ? new Date((Long)value) : value);
//                }
//            }
//
//            String sql = "update " + tableName + " set " + StringUtils.join(signs, ",") + " where id=" + id;
//            return executeUpdate(sql, values.toArray(new Object[values.size()]));
//        } else {
//            List<String> signs = new ArrayList<>();
//            List<String> keys = new ArrayList<>();
//            List<Object> values = new ArrayList<>();
//            for (Object key : setKeys) {
//                if (!Objects.equals(key, "id") && DB.containsColumn(tableName, (String) key)) {
//                    signs.add("?");
//                    keys.add((String)key);
//                    Object value = cls.getDeclaredField((String)key).get(object);
//                    values.add(DB.isDatetimeColumn(tableName, key.toString()) && value instanceof Long ? new Date((Long)value) : value);
//                }
//            }
//
//            String sql = "insert into " + tableName + "(" + StringUtils.join(keys, ",") + ")value(" + StringUtils.join(signs, ",") + ")";
//            Logger.info(String.format("insert sql: %s", sql));
//            return executeUpdateResponse(sql, values.toArray(new Object[values.size()]));
//        }
//    }

    public static int executeUpdate(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 0;
            for (Object param : params) {
                pst.setObject(++index, param);
            }
            return pst.executeUpdate();
        } finally {
            ShenszDB.close(pst);
        }
    }

    public int[] executeBatch(Connection connection, String sql, List<Object[]> paramsList) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (Object[] params : paramsList) {
                int index = 0;
                for (Object param : params) {
                    pst.setObject(++index, param);
                }
                pst.addBatch();
            }
            return pst.executeBatch();
        } finally {
            ShenszDB.close(pst);
        }
    }

    public int[] executeBatchByOne(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (Object param : params) {
                pst.setObject(1, param);
                pst.addBatch();
            }
            return pst.executeBatch();
        } finally {
            ShenszDB.close(pst);
        }
    }

    public long executeUpdateResponse(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 0;
            for (Object param : params) {
                pst.setObject(++index, param);
            }
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        } finally {
            ShenszDB.close(pst);
        }
    }

    public static <T> List<T> find(Connection connection, final Class<T> cls, String sql, Object... params) throws SQLException {
    	PreparedStatement pst = null;
        try {
        	   final String realSql = String.format("select * from %s where %s", cls.getSimpleName(), sql);
        	   pst = connection.prepareStatement(realSql, Statement.RETURN_GENERATED_KEYS);
        	   int index = 0;
               for (Object param : params) {
                   pst.setObject(++index, param);
               }
               ResultSet rs = pst.executeQuery();
               List<T> data = new ArrayList<>();
               List<String> columns = ShenszDB.getColumns(rs, sql);
               while (rs.next()) {
                   Map<String, Object> map = new HashMap<>();
                   for (int i = 1; i <= columns.size(); i++) {
                       map.put(columns.get(i - 1), rs.getObject(i));
                   }
                   data.add(JsonUtils.fromJson(JsonUtils.toJson(map), cls));
               }
               return data;
        } finally {
            ShenszDB.close(pst);
        }
    }
}
