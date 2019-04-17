package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;

import java.math.BigDecimal;

/**
 * MathUtils
 *
 * @author song.wang
 * @date 2016/9/23
 */
public class MathUtils {


    public static double doubleAdd(double ... values){
        return doubleAdd(values, 2);
    }

    public static double doubleAdd(double[] values, int newScale){
        return doubleAdd(values, newScale, BigDecimal.ROUND_HALF_UP);
    }

    public static double doubleAdd(double[] values, int newScale, int roundingMode){
        if(ArrayUtils.isEmpty(values)){
            return 0d;
        }
        BigDecimal result = new BigDecimal(Double.toString(0d));
        for(double v : values){
            result = result.add(new BigDecimal(Double.toString(v)));
        }
        return result.setScale(newScale, roundingMode).doubleValue();
    }

    /**
     * 加法，小数位数不限制
     * @param values
     * @return
     */
    public static double doubleAddNoScale(double ... values){
        if(ArrayUtils.isEmpty(values)){
            return 0d;
        }
        BigDecimal result = new BigDecimal(Double.toString(0d));
        for(double v : values){
            result = result.add(new BigDecimal(Double.toString(v)));
        }
        return result.doubleValue();
    }

    public static double doubleSub(double v1,double v2){
        return doubleSub(v1, v2, 2);
    }

    public static double doubleSub(double v1, double v2, int newScale){
        return doubleSub(v1, v2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    public static double doubleSub(double v1,double v2, int newScale, int roundingMode){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).setScale(newScale, roundingMode).doubleValue();
    }

    // 乘法， 保留两位小数
    public static double doubleMultiply(double d1, double d2){
        return doubleMultiply(d1, d2, 2);
    }

    // 乘法， 保留指定位数小数
    public static double doubleMultiply(double d1, double d2, int newScale){
        if(newScale < 0){
            newScale = 0;
        }
        return doubleMultiply(d1, d2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    // 乘法， 保留指定位数小数，及进位模式
    public static double doubleMultiply(double d1, double d2, int newScale, int roundingMode){
        if(newScale < 0){
            newScale = 0;
        }
        return new BigDecimal(Double.toString(d1)).multiply(new BigDecimal(Double.toString(d2))).setScale(newScale, roundingMode).doubleValue();
    }

    /**
     * 乘法，小数位数不限制
     * @param d1
     * @param d2
     * @return
     */
    public static double doubleMultiplyNoScale(double d1, double d2){
        return new BigDecimal(Double.toString(d1)).multiply(new BigDecimal(Double.toString(d2))).doubleValue();
    }

    // 除法， 保留两位小数
    public static double doubleDivide(double d1, double d2){
        return doubleDivide(d1, d2, 2);
    }

    // 除法， 保留指定位数小数
    public static double doubleDivide(double d1, double d2, int newScale){
        if(newScale < 0){
            newScale = 0;
        }
        return doubleDivide(d1, d2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    // 除法， 保留指定位数小数，及进位模式
    public static double doubleDivide(double d1, double d2, int newScale, int roundingMode){
        if(newScale < 0){
            newScale = 0;
        }
        if(d2 == 0){
            return 0;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.divide(b2, newScale, roundingMode).doubleValue();
    }



    public static float floatAdd(float ... values){
        return floatAdd(values, 2);
    }

    public static float floatAdd(float[] values, int newScale){
        return floatAdd(values, newScale, BigDecimal.ROUND_HALF_UP);
    }

    public static float floatAdd(float[] values, int newScale, int roundingMode){
        if(ArrayUtils.isEmpty(values)){
            return 0f;
        }
        BigDecimal result = new BigDecimal(Float.toString(0));
        for(float v : values){
            result = result.add(new BigDecimal(Float.toString(v)));
        }
        return result.setScale(newScale, roundingMode).floatValue();
    }

    public static float floatSub(float v1,float v2){
        return floatSub(v1, v2, 2);
    }

    public static float floatSub(float v1,float v2, int newScale){
        return floatSub(v1, v2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    public static float floatSub(float v1,float v2, int newScale, int roundingMode){
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return b1.subtract(b2).setScale(newScale, roundingMode).floatValue();
    }


    // 乘法， 保留两位小数
    public static float floatMultiply(float d1, float d2){
        return floatMultiply(d1, d2, 2);
    }

    // 乘法， 保留指定位数小数
    public static float floatMultiply(float d1, float d2, int newScale){
        if(newScale < 0){
            newScale = 0;
        }
        return floatMultiply(d1, d2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    // 乘法， 保留指定位数小数，及进位模式
    public static float floatMultiply(float d1, float d2, int newScale, int roundingMode){
        if(newScale < 0){
            newScale = 0;
        }
        return new BigDecimal(Float.toString(d1)).multiply(new BigDecimal(Float.toString(d2))).setScale(newScale, roundingMode).floatValue();
    }

    // 除法， 保留两位小数
    public static float floatDivide(float d1, float d2){
        return floatDivide(d1, d2, 2);
    }

    // 除法， 保留指定位数小数
    public static float floatDivide(float d1, float d2, int newScale){
        if(newScale < 0){
            newScale = 0;
        }
        return floatDivide(d1, d2, newScale, BigDecimal.ROUND_HALF_UP);
    }

    // 除法， 保留指定位数小数，及进位模式
    public static float floatDivide(float d1, float d2, int newScale, int roundingMode){
        if(newScale < 0){
            newScale = 0;
        }
        if(d2 == 0){
            return 0;
        }
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.divide(b2, newScale, roundingMode).floatValue();
    }

    /**
     * double转int,不保留小数位
     * @param d
     * @param roundingMode
     * @return
     */
    public static int doubleToInt(double d,int roundingMode){
        return new BigDecimal(Double.toString(d)).setScale(0, roundingMode).intValue();
    }
}
