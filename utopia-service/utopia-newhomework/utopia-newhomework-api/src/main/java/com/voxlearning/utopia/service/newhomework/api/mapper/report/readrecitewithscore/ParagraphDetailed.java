package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore;

import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ParagraphDetailed implements Serializable {
    private Integer paragraphOrder;//段落次序:第一段
    private boolean paragraphDifficultyType;//段落困难描述： 重点段落
    private boolean standard;
    private String questionId;
    private List<String> voices = new LinkedList<>();
    private String duration;
    private VoiceEngineType voiceEngineType;
    private List<NaturalSpellingSentence> sentences;    // 引擎打分结果
}
