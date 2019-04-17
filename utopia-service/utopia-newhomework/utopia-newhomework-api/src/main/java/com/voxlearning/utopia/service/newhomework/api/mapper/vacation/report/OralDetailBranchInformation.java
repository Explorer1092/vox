package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class OralDetailBranchInformation implements Serializable {

    private static final long serialVersionUID = 390148996054311580L;
    private String score; //小题分数
    private int realScore; //实际分数
    private List<String> userVoiceUrls; //音频地址
}