package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RecordData implements Serializable {

    private static final long serialVersionUID = -7314871150125807460L;
    private String score;//得分信息
    private Long userId; //用户ID
    private String userName;//用户名字
    private List<String> voiceUrls; //音频地址
    private String voiceScoringMode;//模式

}