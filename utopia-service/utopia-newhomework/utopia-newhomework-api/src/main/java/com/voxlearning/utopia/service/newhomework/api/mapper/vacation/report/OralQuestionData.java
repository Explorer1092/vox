package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class OralQuestionData implements Serializable {

    private static final long serialVersionUID = -4397860439463158581L;
    private String text;   //文案
    private String audio;  //音频
}