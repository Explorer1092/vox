package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *  快乐学学校每日扫描数据
 * Created by yaguang.wang
 * on 2017/4/7.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolKlxEverydayScanData implements ExportAble{

    private String regionName;              // 大区
    private String cityName;                // 城市
    private String countyName;              // 地区
    private String businessDeveloper;       // 负责的专员
    private Long schoolId;                  // 学校ID
    private String schoolName;              // 学校名称
    private String schoolPhase;             // 学校阶段
    private String schoolLevel;             // 学校等级
    private String subject;                 // 科目
    private String time;                    // 时间
    private List<Integer> scanData;          // 每日扫描数据

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getRegionName());
        result.add(this.getCityName());
        result.add(this.getCountyName());
        result.add(this.getBusinessDeveloper());
        result.add(this.getSchoolId());
        result.add(this.getSchoolName());
        result.add(this.getSchoolPhase());
        result.add(this.getSchoolLevel());
        result.add(this.getSubject());
        result.add(this.getTime());
        if (CollectionUtils.isNotEmpty(scanData)) {
            result.addAll(scanData);
        }
        return result;
    }
}
