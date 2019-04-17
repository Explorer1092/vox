package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by tanguohong on 14-7-7.
 */
@Data
public class ReadingDraftSentenceTemp implements Serializable {

    private static final long serialVersionUID = -8015749189099009365L;
    private String dialogRole; // 角色名
    private Integer paragraph; //段落
    private String entext; // 英文内容
    private String cntext; // 中文内容
    private Integer rank; // 内容排序
    private String audioUri;  // 音频文件URI
    private String picUri; // 图片文件URI
}
