package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KpiBudgetReportData
 *
 * @author song.wang
 * @date 2018/2/26
 */
@Setter
@Getter
public class KpiBudgetReportData extends ReportData {
    private Integer month;
    private Integer groupOrUser;                     // 部门，用户   1：部门  2：个人
    private String businessUnitName;                 // 业务部名称
    private String regionName;                       // 大区名称
    private String areaName;                         // 区域名称
    private String cityName;                         // 分区名称
    private String userName;                         // 用户名称
    private String kpiType;                          // 指标名称
    private Integer budget;                          // 预算值

    @Override
    public List<Object> getExportAbleData() {
        List<Object> list = new ArrayList<>();
        list.add(this.month);
        list.add(Objects.equals(this.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER) ? "用户" : "部门");
        list.add(this.businessUnitName);
        list.add(this.regionName);
        list.add(this.areaName);
        list.add(this.cityName);
        list.add(this.userName);
        list.add(this.kpiType);
        list.add(this.budget);
        return list;
    }
}
