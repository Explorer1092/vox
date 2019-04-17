package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 用于保存应用类部分的作业结构
 *
 * @author xuesong.zhang
 * @since 2016-06-24
 */
@Getter
@Setter
public class NewHomeworkApp implements Serializable {

    private static final long serialVersionUID = 8249184859254891802L;

    private Integer categoryId;                         // 练习类型id（VOX_PRACTICE_TYPE中的）
    private Long practiceId;                            // 应用id（VOX_PRACTICE_TYPE中的主键）
    private String lessonId;                            // 我是一个很奇怪的属性。
    private String sectionId;                           // 章节ID(字词讲练)

    private String pictureBookId;                       // 绘本id（阅读绘本）
    private String videoId;                             // 视频id（难重点专项）
    private String questionBoxId;                       // 题包id（语文读背）
    private String dubbingId;                           // 配音id（趣味配音）
    private String stoneDataId;                         // 石头堆id（以后多个作业形式都会用到，目前只有口语交际、字词讲练形式）
    private QuestionBoxType questionBoxType;            // 题包类型（朗读题、背诵题）
    private List<NewHomeworkQuestion> oralQuestions;    // 口语题部分（阅读绘本特有属性）
    private Boolean containsDubbing;                    // 是否包含配音（新绘本阅读特有属性）
    private OralCommunicationContentType oralCommunicationContentType;  // 口语交际应用类型

    private List<NewHomeworkQuestion> easyQuestions;    //课外拓展任务2，简单练习
    private List<NewHomeworkQuestion> hardQuestions;    //课外拓展任务2，挑战练习

    private String courseId;                             //巩固学习课程ID
    private Integer courseOrder;                        //课程顺序-用于巩固学习课程排序
    private List<ErrorQuestion> errorQuestions;         //巩固课程对应的错题List
    private List<ErrorKpoint> errorKpoints;             //巩固课程对应的知识点（纸质拍照特有）

    //====================实验相关属性====================
    private String experimentGroupId;       //实验组ID
    private String experimentId;            //实验ID
    //====================实验相关属性====================

    //====================字词讲练相关属性====================
    private List<NewHomeworkQuestion> wordExerciseQuestions;       //字词训练
    private List<ImageTextRhymeHomework> imageTextRhymeQuestions;  //图文入韵
    private List<String> chineseCharacterCultureCourseIds;         //汉字文化
    //====================字词讲练相关属性====================

    // ###### 公共属性 ###### //
    private List<NewHomeworkQuestion> questions;        // 应试题部分

    private DiagnosisSource diagnosisSource; //诊断来源

    public List<NewHomeworkQuestion> fetchQuestions() {
        List<NewHomeworkQuestion> questions = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(this.questions)) {
            questions.addAll(this.questions);
        }
        return questions;
    }

    public boolean containsDubbing() {
        return SafeConverter.toBoolean(containsDubbing);
    }

    public enum DiagnosisSource {
        SyncDiagnosis,           //同步诊断
        CalcDiagnosis,           //口算速算诊断
        OcrDiagnosis;        //拍照诊断

        public static DiagnosisSource of(String name) {
            try {
                return valueOf(name);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
