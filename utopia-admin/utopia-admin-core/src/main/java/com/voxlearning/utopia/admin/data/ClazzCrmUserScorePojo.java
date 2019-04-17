package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/22
 */
@Data
public class ClazzCrmUserScorePojo {
    private String userName;
    private Long userId;
    private String productName;
    //子产品名
    private String productItemName;
    //每个单元的成绩
    private List<String> lessonScoreList;
    //完成率
    private String completeRate;
    //定级
    private String grading;
    //是否发送电子教材
    private Boolean showPlay;
    private String bookId;
    //完课点评率，主动服务
    private String servicedRate;

}
