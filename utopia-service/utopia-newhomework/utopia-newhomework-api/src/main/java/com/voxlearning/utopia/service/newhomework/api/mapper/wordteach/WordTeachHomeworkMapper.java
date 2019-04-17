package com.voxlearning.utopia.service.newhomework.api.mapper.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: 报告-作业内容
 * @author: Mr_VanGogh
 * @date: 2018/12/20 下午6:40
 */
@Getter
@Setter
public class WordTeachHomeworkMapper implements Serializable{
    private static final long serialVersionUID = -5860614160349062584L;

    private String sectionId;       //章节ID
    private String sectionName;     //章节名称
    private String stoneId;         //题包ID
    private WordExerciseInfo wordExerciseInfo;      //字词训练模块
    private ImageTextRhymeInfo imageTextRhymeInfo;  //图文入韵模块
    private ChineseCharacterCultureInfo chineseCharacterCultureInfo;    //汉字文化模块

    @Getter
    @Setter
    public static class WordExerciseInfo implements Serializable {
        private static final long serialVersionUID = -8675490651932560187L;

        private String moduleName;      //模块名称
        private List<Map<String, Object>> questionMapperList;   //题目详情
    }

    @Getter
    @Setter
    public static class ImageTextRhymeInfo implements Serializable {
        private static final long serialVersionUID = -2895630476993330652L;

        private String moduleName;      //模块名称
        private List<String> titles; //篇章名称
    }

    @Getter
    @Setter
    public static class ChineseCharacterCultureInfo implements Serializable {
        private static final long serialVersionUID = -4859574333503469755L;

        private String moduleName;      //模块名称
        private List<String> courseNames; //课程名称
    }

}
