package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.bean.showtable.TableShowAble;
import com.voxlearning.utopia.agent.constants.AgentConfigSchoolLogType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/3/27.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_SCHOOL_CONFIG_LOG")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170327")
public class AgentSchoolConfigLog extends AbstractDatabaseEntity implements ExportAble, TableShowAble {

    private static final long serialVersionUID = 2746457521619100635L;

    @UtopiaSqlColumn Date operatingTime;                 // 操作时间
    @UtopiaSqlColumn Long regionId;                      // 大区ID
    @UtopiaSqlColumn String regionName;                  // 大区名
    @UtopiaSqlColumn Long departmentId;                  // 部门ID
    @UtopiaSqlColumn String departmentName;              // 部门名称
    @UtopiaSqlColumn String handlers;                    // 操作人
    @UtopiaSqlColumn Long handlerId;                     // 操作人ID
    @UtopiaSqlColumn Integer operationType;              // 操作类型
    @UtopiaSqlColumn Long schoolId;                      // 学校ID
    @UtopiaSqlColumn String schoolName;                  // 学校名称
    @UtopiaSqlColumn Integer level;                      // 学校阶段
    @UtopiaSqlColumn String sourceResponsible;           // 原负责人
    @UtopiaSqlColumn Long sourceResponsibleId;           // 原负责人ID
    @UtopiaSqlColumn String newResponsible;              // 新负责人
    @UtopiaSqlColumn Long newResponsibleId;              // 新负责人ID

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        initResult(result);
        return result;
    }

    @Override
    public List<Object> getShowTableInfo() {
        List<Object> result = new ArrayList<>();
        initResult(result);
        return result;
    }

    private void initResult(List<Object> result) {
        result.add(DateUtils.dateToString(this.getOperatingTime()));
        result.add(this.getRegionName());
        result.add(this.getDepartmentName());
        result.add(this.getHandlers());
        result.add(AgentConfigSchoolLogType.typeOf(this.getOperationType()) == null ? "" : AgentConfigSchoolLogType.typeOf(this.getOperationType()).getTypeName());
        result.add(this.getSchoolId());
        result.add(this.getSchoolName());
        result.add(SchoolLevel.safeParse(this.getLevel()).getDescription());
        result.add(this.getSourceResponsible());
        result.add(this.getNewResponsible());
    }
}
