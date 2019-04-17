/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.util;

import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.constant.LpEkStatus;
import com.voxlearning.utopia.service.psr.entity.IrtLowHighStructEx;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by ChaoLi Lee on 14-7-9.
 * 原谅我吧, 太困！！ 命名规范 有空再改吧
 * 因数据库格式发生改变,所以新加一个文件,并提供新的接口
 */
@Slf4j
public class PsrIrtPredictEx {
    private static double g_bad_irt_predict_p;

    private static double g_all_p_w;
    private static double g_cur_p_w;
    private static double g_min_bm_pw;
    private static double g_min_master_pw;
    private static double g_min_bm_right;
    private static double g_min_master_right;

    private static double more_zero;
    private static double less_one;
    private static double bad_param_num;

    private static double g_bad_irt_value;

    private static String __feedback_splitstr;

    private static Map<String, Double> __range_vmap;

    @Getter
    @Setter
    private static double g_predict_weight; // [0.0,1.0]
    private static EkCouchbaseDao ekCouchbaseDao;

    static {
        g_bad_irt_predict_p = -1.0;
        g_all_p_w = 0.35;
        g_cur_p_w = 1 - g_all_p_w;
        g_min_bm_pw = 0.65;
        g_min_master_pw = 0.85;
        g_min_bm_right = 5;
        g_min_master_right = 10;
        double g_old_mw = 0.9;
        double g_new_mw = 1.0 - g_old_mw;

        more_zero = 1.0 / (1.0 + Math.exp(11.55)); //0.00001
        less_one = 1.0 / (1.0 + Math.exp(-11.55)); //0.99999
        bad_param_num = 11.55;

        g_bad_irt_value = -10000.0;

        String __splitstr = "\t";
        String __trtbr_splitstr = ";";
        __feedback_splitstr = ":";

        if (__range_vmap == null) {
            __range_vmap = new ConcurrentHashMap<>();
        }


        // todo 可配置
        g_predict_weight = 0.7;
        ekCouchbaseDao = null;

        __range_vmap.clear();

        reLoadEx();

        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrIrtPredictEx-timeout") {
            @Override
            public void runSafe() {
                __range_vmap.clear();
                log.info("PsrIrtPredictEx reLoadEx on the timer");
                reLoadEx();
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60*60*1000, 60*60*1000);
    }

    private static boolean is_bad_irt_value(double val) {
        return (g_bad_irt_value == val);
    }

    private static boolean is_bad_irt_predict_p(double p) {
        return (g_bad_irt_predict_p == p);
    }

    private static double irt2pl_param_func(double theta_var, double a_var, double b_var, double c_var) {
        //return (-1.7 * a_var * (theta_var - b_var));  // old irt algo
        return (c_var + (1.0-c_var) / (1 + Math.exp(-(a_var*theta_var + b_var))));
    }

    private static double Pij_func(double theta_var, double a_var, double b_var, double c_var) {
        //pij = 1/{1+exp[-1.7*a*(theta-b)]}

        double ret = 0.0;
        double param_num = irt2pl_param_func(theta_var, a_var, b_var, c_var);
        if (Math.abs(param_num) > bad_param_num) {
            if (param_num > 0.0) {
                ret = more_zero;
            } else {
                ret = less_one;
            }
        } else {
            ret = 1.0 / (1.0 + Math.exp(param_num));
        }
        return ret;
    }

    private static String __get_predw_key(double pred_p_in) {
        double base_num = 20.0;

        //乘基数，四舍五入，再用基数取出范围
        Double pred_p = base_num * pred_p_in;

        Integer int_pred_p = pred_p.intValue();

        if (pred_p - int_pred_p.doubleValue() > 0.5)
            pred_p = int_pred_p.doubleValue() + 1.0;
        else
            pred_p = int_pred_p.doubleValue() + 0.0;

        pred_p = pred_p / base_num;

        int_pred_p = pred_p.intValue();

        //转化成key
        if (pred_p == int_pred_p.doubleValue()) {
            return ("predw" + __feedback_splitstr + int_pred_p.toString());
        } else {
            return ("predw" + __feedback_splitstr + pred_p.toString());
        }
    }

    private static String __get_tab_key(double ut, double ia, double ib) {
        double base_num = 10.0;

        //乘基数，四舍五入，再用基数取出范围
        Double value = base_num * ia * (ut - ib);

        Integer int_value = value.intValue();

        if (value - int_value.doubleValue() > 0.5)
            value = int_value.doubleValue() + 1.0;
        else
            value = int_value.doubleValue() + 0.0;

        value = value / base_num;

        int_value = value.intValue();

        //转化成key
        if (value == int_value.doubleValue()) {
            return ("tab" + __feedback_splitstr + int_value.toString());
        } else {
            return ("tab" + __feedback_splitstr + value.toString());
        }
    }

    private static boolean reLoadEx() {
        if (__range_vmap == null) {
            __range_vmap = new ConcurrentHashMap<>();
        }

        if (ekCouchbaseDao == null) {
            log.error("PsrIrtPredict ekCouchbaseDao is null");
            return false;
        }

        List<IrtLowHighStructEx> irtLowHighStructExs = ekCouchbaseDao.getIrtLowHighStructExFromCouchbase();

        if (irtLowHighStructExs == null) {
            log.error("PsrIrtPredict irtLowHighStructExs is null");
            return false;
        }

        String key;
        double value;
        for (IrtLowHighStructEx irtLowHighStrctEx : irtLowHighStructExs) {
            key = irtLowHighStrctEx.getKey();
            value = irtLowHighStrctEx.getValue();

            if (!__range_vmap.containsKey(key)) {
                __range_vmap.put(key, value);
            }
        } // end for

        return true;
    }

    public static void setPredictWeight(double predictWeightP) {
        if (predictWeightP < 0 || predictWeightP > 1)
            predictWeightP = 1.0;

        g_predict_weight = predictWeightP;
    }

    public static void setEkCouchbaseDao(EkCouchbaseDao ekCouchbaseDaoP) {
        if (ekCouchbaseDaoP == null || ekCouchbaseDao != null)
            return;

        ekCouchbaseDao = ekCouchbaseDaoP;
    }

    public static double predictEx(double ut, double ia, double ib, double ic) {
        double min_highv = 0.3;
        double min_lowv = 0.3;

        return (predictEx(ut, ia, ib, ic, min_highv, min_lowv));
    }

    public static double predictEx(double ut, double accuracyRate, double ia, double ib, double ic) {
        double min_highv = 0.3;
        double min_lowv = 0.3;

        if (accuracyRate < 0 || accuracyRate > 1)
            accuracyRate = 1.0;

        if (g_predict_weight < 0 || g_predict_weight > 1)
            g_predict_weight = 1;

        return (predictEx(ut, ia, ib, ic, min_highv, min_lowv) * g_predict_weight + accuracyRate * (1 - g_predict_weight));
    }

    public static double predictEx(double ut, double ia, double ib, double ic, double min_highv, double min_lowv) {
        if (is_bad_irt_value(ut) || is_bad_irt_value(ia) || is_bad_irt_value(ib)) {
            return g_bad_irt_predict_p;
        }

        if (__range_vmap == null) {
            log.warn("PsrIrtPredictEx __range_vmap is null");
            reLoadEx();
        }

        if (__range_vmap.size() <= 0) {
            log.warn("PsrIrtPredictEx __range_vmap size <= 0, reLoadEx start");
            reLoadEx();
        }

        double pred_p = Pij_func(ut, ia, ib, ic);

        String key = __get_predw_key(pred_p);

        if (__range_vmap != null && __range_vmap.containsKey(key)) {
            pred_p -= __range_vmap.get(key);
        } else {
            key = __get_tab_key(ut, ia, ib);
            if (__range_vmap != null && __range_vmap.containsKey(key)) {
                pred_p -= __range_vmap.get(key);
            }
            /*
            else {
                log.warn("PsrIrtPredictEx predictEx theta="
                        + Double.valueOf(ut).toString()
                        + " irtA=" + Double.valueOf(ia).toString()
                        + " irtB=" + Double.valueOf(ib).toString()
                        + " no __range_vmap data");
            }
            */
        }

        if (pred_p <= 0.0)
            pred_p = 0.00001;
        else if (pred_p >= 1.0)
            pred_p = 0.99999;

        return pred_p;
    }

    private static double feedback(double pred_p, double cur_p, double all_p, int cur_s, int all_s) {
        if (pred_p <= 0.0)
            return pred_p;
        else {
            double p = g_all_p_w * all_p + g_cur_p_w * cur_p;
            double deta = (p - pred_p) / pred_p;
            if (deta > 0.1)
                deta = 0.1;
            else if (deta < -0.1)
                deta = -0.1;

            double retp = pred_p * (1 + deta / 2);
            if (retp >= 1.0)
                retp = 0.99999;
            return retp;
        }
    }

    private static LpEkStatus master(double pw, int cur_r) {
        if (cur_r >= g_min_master_right || pw >= g_min_master_pw)
            return LpEkStatus.ek_master;
        else if (cur_r >= g_min_bm_right || pw >= g_min_bm_pw)
            return LpEkStatus.ek_bm;

        return LpEkStatus.ek_ftm;
    }

    private static String __get_t_range(double theta) {
        List<Double> ra = new ArrayList<Double>(Arrays.asList(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0));
        for (int i = 0; i < ra.size(); i++) {
            String strI = ra.get(i).toString();
            int pos = strI.indexOf(".0");
            if (pos >= 0)
                strI = strI.substring(0,pos);

            if (theta < ra.get(i)) {
                if (0 == i)
                    return "t(," + strI + ")";
                else {
                    String strII = ra.get(i-1).toString();
                    pos = strII.indexOf(".0");
                    if (pos >= 0)
                        strII = strII.substring(0,pos);
                    return "t[" + strII + "," + strI + ")";
                }
            }
        }

        return "t[" + ra.get(ra.size() - 1).toString() + ",)";
    }


    private static String __get_tb_range(double theta, double b, double a) {
        double deta = theta - b;

        List<Double> da = new ArrayList<Double>(Arrays.asList(-3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5));

        for (int i = 0; i < da.size(); i++) {
            String strI = da.get(i).toString();
            int pos = strI.indexOf(".0");
            if (pos >= 0)
                strI = strI.substring(0,pos);

            if (deta < da.get(i)) {
                if (0 == i)
                    return "d(," + strI + ")";
                else {
                    String strII = da.get(i-1).toString();
                    pos = strII.indexOf(".0");
                    if (pos >= 0)
                        strII = strII.substring(0,pos);
                    return "d[" + strII + "," + strI + ")";
                }
            }
        }

        return "d[" + da.get(da.size() - 1).toString() + ",)";
    }

}
