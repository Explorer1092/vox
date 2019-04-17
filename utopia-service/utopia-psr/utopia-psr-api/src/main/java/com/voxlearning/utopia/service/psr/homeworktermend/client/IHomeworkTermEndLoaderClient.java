package com.voxlearning.utopia.service.psr.homeworktermend.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.EnglishQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathMentalQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathQuestionBox;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 小学作业期末复习
 *
 * @author xuesong.zhang
 * @since 2016-05-12
 */
public interface IHomeworkTermEndLoaderClient extends IPingable {

    /**
     * @param boxIds 题包id
     * @return map
     */
    Map<String, EnglishQuestionBox> loadEnglishQuestionBoxs(Collection<String> boxIds);

    /**
     * @param boxIds 题包id
     * @return map
     */
    @Deprecated
    Map<String, MathMentalQuestionBox> loadMathMentalQuestionBoxs(Collection<String> boxIds);

    /**
     * @param boxIds 题包ids
     * @return map
     */
    Map<String, MathQuestionBox> loadMathQuestionBoxs(Collection<String> boxIds);

    /**
     * 推送英语题
     *
     * @param unitIds 单元数组(required)
     * @param userId  用户id(optional)
     * @return 题包数组
     */
    List<EnglishQuestionBox> pushEnglishQuestionBoxes(List<Long> unitIds, Long userId);

    /**
     * 推送数学应试题
     *
     * @param unitIds   单元数组(required)
     * @param teacherId 老师id(required)
     * @return
     */
    List<MathQuestionBox> pushMathQuestionBoxes(List<String> unitIds, Long teacherId);


    /**
     * 推送期末复习单元数学应试题
     *
     * @param unitIds       单元数组(required)
     * @param teacherId     老师id(required)
     */
    List<MathQuestionBox> pushMathQuestionBoxesByTermEndUnit(List<String> unitIds, Long teacherId);

    /**
     * 推送数学口算题
     *
     * @param unitIds   单元数组(required)
     * @param teacherId 老师id(required)
     * @return
     */
    List<MathMentalQuestionBox> pushMathMentalQuestionBoxes(List<String> unitIds, Long teacherId);


    /**
     * 推送期末复习单元数学应试题
     *
     * @param unitIds 单元id数组(required)
     * @param teacherId 老师id(required)
     * @return
     */
    List<MathMentalQuestionBox> pushMathMentalQuestionBoxesByTermEndUnit(List<String> unitIds, Long teacherId);

}
