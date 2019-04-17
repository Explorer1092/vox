package com.voxlearning.utopia.service.psr.newhomework.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionBox;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 类题推荐
 *
 * @author xuesong.zhang
 * @since 2016-07-11
 */
public interface ISimilarQuestionLoaderClient extends IPingable {
    /**
     * 用于goal的类题推荐接口
     *
     * @param sourceIds 原题id
     * @param unitId    单元id
     * @param teacher   老师
     * @return Map
     */
    Map<String, List<NewQuestion>> loadGoalSimilarQuestions(Collection<String> sourceIds, String unitId, Teacher teacher);

    /**
     * 用于布置作业的类题推荐接口
     *
     * @param sourceIds sectionId#原题id
     * @param teacher   老师
     * @return Map
     */
    Map<String, List<NewQuestion>> loadSimilarQuestions(Collection<String> sourceIds, Teacher teacher);

    /**
     * 用于布置作业中，同步习题"更多"标签的题目获取
     *
     * @param catalogIds 数学的话是sectionId
     * @return Map
     */
    Map<String, List<NewQuestion>> loadMathSimilarQuestions(Collection<String> catalogIds);

    /**
     * 用于报告中的类题推荐
     *
     * @param sourceQid   原题id
     * @param correctQid  订正题id
     * @param correctRate 原题正确率
     * @param sectionId   课时id
     * @return List
     */
    List<NewQuestion> loadSimilarQuestions(String sourceQid, String correctQid, Integer correctRate, String sectionId);


    /**
     * 小学数学section配置的精品题包
     * 返回结果的key 为 base(基础题包)|solidify(巩固题包)
     * @param catalogIds 数学的话是sectionId
     * @return
     */
    Map<String, List<MathQuestionBox>> loadQuestionPackagesOfSections(Collection<String> catalogIds);


    /**
     * 未布置知识点取题目
     * @param kps KP_开头的表示知识点或知识点特征，TM_表示考法，SM_表示解法
     * @param catalogId unitId
     * @return
     */
    Map<String, List<NewQuestion>> loadQuestionsByKps(Collection<String> kps, String catalogId);


    /* 测试类题和更多题目接口,只返回questionIds,for php dubbo*/
    //TODO 上线之后去掉
    Map<String, List<String>> testMathSimilarQuestions(Collection<String> catalogIds);

    Map<String,List<String>> testSimilarQuestion(String sourceQid,String sectionId);

    Map<String, List<MathQuestionBox>> testQuestionPackagesOfSection(Collection<String> catalogIds);

    Map<String, List<String>> testLoadSimilarQuestions(Collection<String> sourceIds, String unitId);

    Map<String, List<String>> testloadGoalSimilarQuestions(Collection<String> sourceIds, String unitId);

    Map<String, List<String>> testloadQuestionsByKps(Collection<String> kps, String catalogId);

}
