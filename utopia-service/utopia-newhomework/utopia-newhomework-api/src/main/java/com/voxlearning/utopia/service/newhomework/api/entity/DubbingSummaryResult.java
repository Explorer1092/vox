package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Getter
@Setter
public class DubbingSummaryResult implements Serializable {

    private static final long serialVersionUID = -3537883155388602035L;

    private String dubbingId;                       // 配音id
    private String dubbingName;                     // 配音名
    private String videoUrl;                        // 视频url
    private String coverUrl;                        // 缩略图url
    private Boolean synthetic;                      // 配音是否合成成功
    private Boolean skipUploadVideo;                // 是否提交失败跳过合成
    private List<Map<String, Object>> keyWords;     // 词汇
    private List<Map<String, Object>> keyGrammars;  // 语法
    private List<String> topics;                    // 话题
    private Integer level;                          // 年级
    private String clazzLevel;                      // 年级
    private String albumName;                       // 专辑名称
    private String videoSummary;                    // 视频简介
    private String processResultUrl;                // 提交答案地址
    private String questionUrl;                     // 获取题目地址
    private String completedUrl;                    // 获取答案地址
    private Boolean finished;                       // 是否已做完
    private Double score;                           // 得分
    private Boolean isSong;                         // 是否是歌曲类型
}
