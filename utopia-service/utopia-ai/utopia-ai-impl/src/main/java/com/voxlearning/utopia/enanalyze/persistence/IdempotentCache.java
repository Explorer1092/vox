package com.voxlearning.utopia.enanalyze.persistence;

/**
 * 幂等持久层接口
 *
 * @author xiaolei.li
 * @version 2018/7/25
 */
public interface IdempotentCache {

    /**
     * 判断是否存在
     *
     * @param key 幂等参数，根据业务通过摘要算法计算得出
     * @return 是否存在
     */
    boolean exist(String key);

    /**
     * 获取
     *
     * @param key 幂等参数，根据业务通过摘要算法计算得出
     * @return json格式的结果
     */
    String get(String key);

    /**
     * 暂存
     *
     * @param key    key
     * @param value  value
     * @param expire 超时时间(单位：秒）
     */
    void set(String key, String value, long expire);
}
