package com.voxlearning.utopia.agent.mockexam.dao.utils;

import com.voxlearning.alps.dao.core.hql.Criteria;

/**
 * 持久层工具类
 *
 * @author xiaolei.li
 * @version 2018/8/12
 */
public abstract class DaoUtil {

    /**
     * 等于
     *
     * @param criteria 条件
     * @param key      列名
     * @param value    值
     * @return 条件
     */
    public static Criteria is(Criteria criteria, String key, Object value) {
        if (value != null && !"".equals(value))
            return criteria.where(key).is(value);
        else
            return criteria;

    }

    public static Criteria is(String key, Object value) {
        if (value != null && !"".equals(value))
            return Criteria.where(key).is(value);
        else
            return new Criteria();
    }

    /**
     * 介于之间[buttom, top]
     *
     * @param criteria 条件
     * @param key      列名
     * @param buttom   下限
     * @param top      上限
     * @return 条件
     */
    public static void between(Criteria criteria, String key, Object buttom, Object top) {
        if (buttom != null && top != null)
            criteria.and(key).gte(buttom).lt(top);
        else if (buttom != null)
            criteria.and(key).gte(buttom);
        else if (top != null)
            criteria.and(key).lt(top);
    }

    public static Criteria between(String key, Object buttom, Object top) {
        if (buttom != null && top != null)
            return Criteria.where(key).gte(buttom).lt(top);
        else if (buttom != null)
            return Criteria.where(key).gte(buttom);
        else if (top != null)
            return Criteria.where(key).lt(top);
        return new Criteria();
    }
}
