package com.voxlearning.utopia.service.newhomework.api.mapper.report.reading;

import com.voxlearning.utopia.api.constant.VoiceEngineType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class WordRecognitionAndReadingDetail  implements Serializable {
    private static final long serialVersionUID = 6698092365720817006L;
    private Integer order;//段落次序:第一段
    private boolean standard;
    private String questionId;
    private String pinYinMark;//拼音
    private String chineseWordContent;//字
    private VoiceEngineType engineName;//引擎名称
    private double engineScore;//引擎打分
    private Long categoryId;
    private List<String> voices = new LinkedList<>();
}
