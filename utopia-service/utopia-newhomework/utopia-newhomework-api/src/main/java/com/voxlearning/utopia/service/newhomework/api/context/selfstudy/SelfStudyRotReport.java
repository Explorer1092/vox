package com.voxlearning.utopia.service.newhomework.api.context.selfstudy;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/6/4
 */
@Data
public class SelfStudyRotReport implements Serializable {
    private static final long serialVersionUID = 5289377817888404765L;

    private Long actor; // 用户ID
    private String verb; // 行为
    private String object;//对象

    private Context context;
    private Boolean result;//回答判别，=grasp, boolean

    private Attachments attachments;// 附件
    private String timestamp;//时间戳
    private Long duration;//持续时间

    /*************************视频课程独有属性*******************/
    private Long coursePos;//行为发生的视频当前所在的秒
    private Long targetPos;//行为的目标位置，比如前拖行为拖到了视频的哪一秒
    /*
     * 退出有两种情况：
     *  用户主动关闭视频   tag取值为：quit
     *  其它情况关闭视频比如：电话呼入   tag取值为：interrupt
     */
    private String videoTag;
    /*************************轻交互课程独有属性*****************/
    /*************************视频课程独有属性*******************/
    private Integer page;
    /*
     * pptTag：退出有三种情况
     *   第一次点击退出         tag取值为：quit
     *   在弹出框里点击确定     tag取值为：sure
     *   在弹出框里点取消      tag取值为：cancel
     */
    private String pptTag;
    /*************************轻交互课程独有属性*****************/


    @Data
    public static class Context implements Serializable{
        private static final long serialVersionUID = 5304831898023866274L;
        private String homeworkId;//作业ID
        private String objectiveConfigType;//作业类型
        private String courseId;//课程ID
        private String courseType;//课程类型
        private List<String> preQuestionIds;//前测题IDs
        private String expGroupId;       //实验组ID
        private String expId;            //实验ID
    }

    @Data
    public static class Attachments implements Serializable {
        private static final long serialVersionUID = -3374520691149296333L;
        private List<List<String>> userAnswers;//答案详情
    }

}
