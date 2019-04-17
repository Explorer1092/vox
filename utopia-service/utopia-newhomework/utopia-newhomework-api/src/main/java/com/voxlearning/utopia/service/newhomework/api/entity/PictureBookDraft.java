package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 绘本前端使用结构，源自于老的ReadingDraft
 *
 * @author xuesong.zhang
 * @since 2016-07-18
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PictureBookDraft implements Serializable {
    private static final long serialVersionUID = 1515393316817935734L;

    private String pictureBookId;       // 绘本id
    private String cname;               // 阅读中文名称
    private String ename;               // 阅读英文名称
    private List<String> points;        // 知识点id
    private Integer difficultyLevel;    // 难度级别
    private Integer wordsCount;         // 词汇量
    private Integer recommendTime;      // 预计完成用时
    private Long ugcAuthor;             // UGC作者ID
    private Map<String, Object> content;
}
