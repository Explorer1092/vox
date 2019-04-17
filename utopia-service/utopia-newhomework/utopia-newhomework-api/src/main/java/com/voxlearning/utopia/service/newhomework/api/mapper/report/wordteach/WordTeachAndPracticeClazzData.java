package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 字词讲练-班级数据
 * @author: Mr_VanGogh
 * @date: 2018/12/18 上午10:22
 */
@Getter
@Setter
public class WordTeachAndPracticeClazzData implements Serializable {
    private static final long serialVersionUID = 5485468979448536447L;

    // 课时ID
    private String sectionId;
    // 课时名称
    private String sectionName;
    // 大包ID
    private String stoneId;
    //字词训练模块
    private WordExerciseModuleClazzData wordExerciseModuleClazzData;
    //图文入韵模块
    private ImageTextRhymeModuleClazzData imageTextRhymeModuleClazzData;
    //汉字文化模块
    private ChineseCharacterCultureModuleClazzData chineseCharacterCultureModuleClazzData;
}
