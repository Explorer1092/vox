package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PictureBookPlusSummaryResult implements Serializable {

    private static final long serialVersionUID = -1244682299543557518L;

    private String pictureBookId;                       // 绘本id
    private String pictureBookName;                     // 绘本名
    private Integer seconds;                            // 建议阅读时长
    private String level;                               // 级别
    private Integer wordsCount;                         // 词汇总量
    private List<Map<String, Object>> keyWords;         // 关键词
    private String series;                              // 绘本系列
    private List<String> topics;                        // 绘本主题列表
    private List<Map<String, Object>> practiceTypes;    // 阅读任务类型
    private Boolean spellingPb;                         // 是否是拼读绘本
    private String coverUrl;                            // 封面图url
    private String screenMode;                          // 横屏还是竖屏
    private String bookSummary;                         // 绘本简介
    private String appDataUrl;                          // 获取绘本数据地址
    private String processResultUrl;                    // 提交答案地址
    private String questionUrl;                         // 获取题目地址
    private String completedUrl;                        // 获取答案地址
    private String uploadDubbingUrl;                    // 上传配音数据地址
    private Boolean finished;                           // 是否已做完
    private Integer score;                              // 绘本得分
    private String dubbingId;                           // 配音作品id
}
