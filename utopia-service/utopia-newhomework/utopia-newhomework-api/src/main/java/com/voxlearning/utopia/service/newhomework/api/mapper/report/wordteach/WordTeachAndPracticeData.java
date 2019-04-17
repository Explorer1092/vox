package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 字词讲练数据
 * @author: Mr_VanGogh
 * @date: 2018/12/14 下午4:33
 */
@Getter
@Setter
public class WordTeachAndPracticeData implements Serializable {
    private static final long serialVersionUID = 1054003847282578534L;

    // 课时ID
    private String sectionId;
    // 课时名称
    private String sectionName;
    // 大包ID
    private String stoneId;
    // 字词训练模块
    private WordExerciseModuleData wordExerciseModuleData;
    // 图文入韵模块
    private ImageTextRhymeModuleData imageTextRhymeModuleData;
    // 汉字文化模块
    private ChineseCharacterCultureModuleData chineseCultureModuleData;
}
