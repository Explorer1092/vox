package com.voxlearning.utopia.agent.bean.resource;

import com.voxlearning.athena.bean.LoadGradePerformanceData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 老师详情页作业记录
 * Created by yaguang.wang on 2016/11/30.
 */
@Getter
@Setter
@NoArgsConstructor
public class HomeWorkInfo implements Serializable {
    private static final long serialVersionUID = -2907312046280126709L;

    private Long clazzId;           // 班级ID
    private String clazzName;       // 班级名称
    private Date assignDate;        // 布置作业时间
    private Boolean hwCheckStatus;  // 作业的检查状态 true 已检查  false 未检查
    private Boolean disabled;       // 是否删除
    private Integer sumClassStuNum; // 班级总人数
    private Integer finishHwStuNum; // 完成作业人数
    private Long oldTeacherId;      // 原来布置作业的老师的ID
    private String oldTeacherName;  // 原来布置作业的老师名称
    private Integer schoolLevel;    // 老师是中学还是小学 1.小学,2.中学
    private List<String> types;     //作业类型 ObjectiveConfigType
    private Boolean specifiedType;  //是否指定类型作业
    private Long groupId;           //班组ID
}
