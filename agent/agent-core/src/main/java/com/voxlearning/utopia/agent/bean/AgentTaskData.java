package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.data.UserRecordSnapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/5/10.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskData implements Comparable, Serializable {

    private static final long serialVersionUID = 1761087125494246345L;
    //列表页的属性
    private String taskDetailId;        //任务详情ID
    private String taskType;            //任务类型
    private String stats;               //任务状态
    private String endDate;             //截止日期
    private String title;               //任务主题
    private String taskTarget;          //任务对象
    private String taskTargetSchool;    //所在学校名称

    //详情页的属性
    private Long taskTargetId;          //任务对象ID
    private Long createId;              //创建人Id
    private String createName;          //创建人名称
    private String createDate;          //创建日期
    private String content;             //任务内容
                                        //任务类型
                                        //任务主题
                                        //截止日期
                                        //任务状态
                                        //所在学校名称

    List<UserRecordSnapshot> userRecords;// 用户工作记录

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
