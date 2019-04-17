package com.voxlearning.utopia.service.reward.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/2.
 */
@Data
public class HistoryOrderMapper implements Serializable {
    private static final long serialVersionUID = 445394452215893682L;

    private boolean historyOrder;
    private boolean substituteReceive;//是否是待收
    private String subject;
    private String teacherName;
    private String deliverDate;
    private String logisticNo;
    private String companyName;
    private List<Map<String,Object>> orders;
    private String groupId;
    private Date orderTime;// 排序用的时间
    private String rewardInfo;
}
