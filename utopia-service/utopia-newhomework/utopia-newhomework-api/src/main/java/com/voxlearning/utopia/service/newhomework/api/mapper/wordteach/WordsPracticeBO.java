package com.voxlearning.utopia.service.newhomework.api.mapper.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description: 字词讲练题小题包包
 * @author: Mr_VanGogh
 * @date: 2018/11/26 下午5:22
 */
@Getter
@Setter
public class WordsPracticeBO implements Serializable {
    private static final long serialVersionUID = 3233357342031296243L;
    private Map<String, Object> wordExerciseMap;
    private Map<String, Object> imageTextMap;
    private Map<String, Object> chineseCharacterCultureMap;
}