package com.voxlearning.utopia.agent.bean.monitor;

import com.voxlearning.utopia.agent.bean.datareport.ReportData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 换班记录下载
 * Created by yaguang.wang
 * on 2017/7/28.
 */
@Getter
@Setter
public class ClassAlterTableData extends ReportData {
    private static final long serialVersionUID = -1279925715784157077L;
    private String date;
    private String regionName;
    private String departmentName;
    private String executor;        // 执行人姓名
    private String role;            // 角色
    private Integer untreated;      // 未处理换班数量
    private Integer yesterdayIncrease;  // 昨日新增

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(date);
        result.add(regionName);
        result.add(departmentName);
        result.add(role);
        result.add(executor);
        result.add(untreated);
        result.add(yesterdayIncrease);
        return result;
    }
}
