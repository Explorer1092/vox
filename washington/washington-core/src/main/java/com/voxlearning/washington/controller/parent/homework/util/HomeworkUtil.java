package com.voxlearning.washington.controller.parent.homework.util;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ScoreLevel;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业工具类
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-16
 */
public class HomeworkUtil {

    /**
     * 获取难度中文名称
     *
     * @param name
     * @return
     */
    public static String levelCName(String name){
        return levels.get(name);
    }
    private static Map<String, String> levels = new HashMap<>();
    static{
        levels.put("BASE", "基础");
        levels.put("CRUX", "提升");
    }

    public static String generatorDayID(Object ... o){
        return DateUtils.dateToString(new Date(), "yyyyMMdd") + "_" + generatorID(o);
    }

    /**
     * ID生成: 参数o以"_"间隔，若无参数则返回随机值see {@link RandomUtils#nextObjectId()}
     *
     * @return
     */
    public static String generatorID(Object ... o){
        return o != null && o.length > 0 ? Arrays.stream(o).map(e -> e.toString()).collect(Collectors.joining("_")): RandomUtils.nextObjectId();
    }

    /**
     * 成绩转等级
     *
     * @param s
     * @return
     */
    public static ScoreLevel score2Level(double s){
        return ScoreLevel.processLevel((int)s);
    }

}
