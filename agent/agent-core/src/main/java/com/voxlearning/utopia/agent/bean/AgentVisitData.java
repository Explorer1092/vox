package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 陪访列表的数据
 * Created by yaguang.wang on 2016/7/29.
 */
@Getter
@Setter
public class AgentVisitData implements Serializable {
    private static final long serialVersionUID = -5103770734460938934L;
    private String schoolName;      //学校姓名
    private String partnerSuggest;  //陪访建议
    private String partnerName;     //陪访对象
    private Date workTime;          //陪访时间
}
