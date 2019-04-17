package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 14-7-7.
 */
@Data
public class ReadingDraftTemp implements Serializable {

    private static final long serialVersionUID = -6820405933781377255L;
    private String id;  // 阅读理解ID
    private String cname; // 中文标题
    private String ename; // 英文标题
    private Integer style; // 体裁
    private Integer difficultyLevel; // 适用年级
    private Integer templateId = 0; // 显示模版 0:无图，1：圆图，2：方图
    private List<Map<String, Object>> points;
    private Integer colorId; // 显示色调
    private String coverUri; // 封皮图片URI
    private String coverUri1; // 封皮图片URI1
    private String coverUri2; // 封皮图片URI2
    private String coverUri3; // 封皮图片URI3
    private Long ugcAuthor; // 上传人
    private String status = "draft";  // 状态，draft:草稿，verifying:审核中,published:发布，offline:冻结
    private Integer editStep = 1;
    private List<Object> readingPages = new ArrayList<>(); //阅读理解页面
    private List<ReadingDraftQuestionTemp> readingQuestions = new ArrayList<>();


}
