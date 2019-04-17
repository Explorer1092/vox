package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class ActiveServiceInfo implements Serializable {

    private String serviceType;   // 服务类型
    private String serviceName;
    private Long userId;        // 用户id
    private String userName;    // 姓名
    private String unitId;      // 单元ID
    private String unitName;    // 单元名称
    private Date createDate;    // 创建时间，记录生成时间
    private String status;     // 状态  0:未完成, 1:已完成, 3:未审核
    private Boolean linkStatus; // 为ture 跳转到新页面
    private String userVideoId;
    private String videoUrl;

    private List<RenewType> renewList;

    @Getter
    @Setter
    public static class RenewType implements Serializable {
        private static final long serialVersionUID = 1236678100006415527L;
        private String type;
        private String desc;
        private Boolean status;     // 状态
    }
    public static ActiveServiceInfo valueOf(ChipsActiveServiceRecord record, String userName, StoneUnitData unitDate) {
        ActiveServiceInfo info = new ActiveServiceInfo();
        info.setServiceType(record.getServiceType());
        info.setServiceName(ChipsActiveServiceType.of(record.getServiceType()).getDesc());
        info.setUserId(record.getUserId());
        info.setUserName(userName);
        info.setUnitId(unitDate != null ? unitDate.getId() : "");
        info.setUnitName((unitDate != null && unitDate.getJsonData() != null) ? unitDate.getJsonData().getName() : "");
        info.setCreateDate(record.getCreateDate());
        info.setStatus(calStatus(record));
        info.setLinkStatus((unitDate != null && unitDate.getJsonData() != null && unitDate.getJsonData().getUnit_type() != null && unitDate.getJsonData().getUnit_type() == ChipsUnitType.short_lesson));
        info.setUserVideoId(record.getUserVideoId());
        info.setVideoUrl(record.getVideoUrl());
        return info;
    }

    private static String calStatus(ChipsActiveServiceRecord record) {
        if (ChipsActiveServiceType.of(record.getServiceType()) == ChipsActiveServiceType.SERVICE) {
            if (record.getExamineStatus() != null && !record.getExamineStatus()) {
                return "3";
            } else {
                return record.getServiced() ? "1" : "0";
            }
        } else {
            return record.getServiced() ? "1" : "0";
        }
    }
}
