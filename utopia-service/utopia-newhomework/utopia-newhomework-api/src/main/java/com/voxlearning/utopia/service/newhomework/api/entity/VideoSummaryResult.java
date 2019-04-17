package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tanguohong on 2016/11/25.
 */
@Getter
@Setter
public class VideoSummaryResult implements Serializable {

    private static final long serialVersionUID = 7669706728388865908L;

    private String videoId;                 // 视频id
    private String videoName;               // 视频名称
    private String videoSummary;            // 视频简介
    private Integer videoSeconds;           // 视频时长
    private String coverUrl;                // 封面图片
    private String videoUrl;                // 视频地址
    private List<String> solutionTricks;    // 解题技巧
    private Integer questionCount;          // 题数
    private Integer finishQuestionCount;    // 完成题数
    private Boolean finished;               // 是否已做完
    private String processResultUrl;        // 上传答题结果url
    private String questionUrl;             // 获取试题url
    private String completedUrl;            // 获取答案url
}
