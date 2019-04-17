package com.voxlearning.utopia.service.parent.homework.impl.util;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.parent.homework.impl.model.BookQuestionNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业工具类
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 20181112
 */
public class HomeworkUtil {

    public static Map<String, String> levels = new LinkedHashMap<>();

    static{
        levels.put("BASE", "基础");
        levels.put("CRUX", "提升");
    }

    //Logic
    /**
     * ID生成,前缀为日期yyyyMMdd_, 后缀参照{@link #generatorID(Object...)}
     *
     * @return
     */
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
     * 从id中获取年月
     *
     * @param id
     * @return
     */
    public static String yyyyMM(String id){
        return id.substring(0,6);
    }

    /**
     * 从id中获取年月日
     *
     * @param id
     * @return
     */
    public static String yyyyMMdd(String id){
        return id.substring(0,8);
    }

    /**
     * sum
     *
     * @param d1
     * @param d2
     * @return
     */
    public static Double sum(Double d1, Double d2){
        return (d1 == null ? 0D : d1) + (d2 == null ? 0D : d2);
    }

    /**
     * sum
     *
     * @param d1
     * @param d2
     * @return
     */
    public static Long sum(Long d1, Long d2){
        return (d1 == null ? 0L : d1) + (d2 == null ? 0L : d2);
    }

    /**
     * sum
     *
     * @param d1
     * @param d2
     * @return
     */
    public static Integer sum(Integer d1, Integer d2){
        return (d1 == null ? 0 : d1) + (d2 == null ? 0 : d2);
    }

    /**
     * 获取难度中文名称
     *
     * @param name
     * @return
     */
    public static String levelCName(String name){
        return levels.get(name);
    }

    /**
     * 单元名字:英语
     *
     * @param c
     * @return
     */
    public static String unitName(NewBookCatalog c) {
        return c.getSubjectId() == Subject.ENGLISH.getId() ? c.getAlias() : c.getName();
    }

    /**
     * 成绩转等级
     *
     * @param s
     * @return
     */
    public static ScoreLevel score2Level(double s){
        return ScoreLevel.processLevel((int)(s+0.5));
    }

    /**
     * 获取对应类型的教材信息
     *
     * @param bookQuestionNode
     * @param bookCatalogType
     * @return
     */
    public static List<BookQuestionNode> get(BookQuestionNode bookQuestionNode, com.voxlearning.utopia.service.parent.homework.impl.model.BookCatalogType bookCatalogType) {
        List<BookQuestionNode> result = new ArrayList<>();
        if(ObjectUtils.anyBlank(bookQuestionNode.getChildNodes())){
            return Collections.EMPTY_LIST;
        }
        if(bookQuestionNode.getChildNodes().get(0).getBookCatalogType() == bookCatalogType){
            return bookQuestionNode.getChildNodes();
        }
        for(BookQuestionNode bqn : bookQuestionNode.getChildNodes()){
            result.addAll(get(bqn, bookCatalogType));
        }
        return result;
    }

}
