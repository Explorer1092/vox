package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校大考信息
 *
 * @author chunlin.yu
 * @create 2018-03-14 17:07
 **/


@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_EXAM_SCHOOL")
@UtopiaCacheExpiration
@UtopiaCacheRevision("201803114")
public class AgentExamSchool extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = 5269790617938213504L;
    @UtopiaSqlColumn
    private Long schoolId;
    @UtopiaSqlColumn
    private String schoolName;
    @UtopiaSqlColumn
    private SchoolLevel schoolLevel;
    @UtopiaSqlColumn
    private Integer provinceCode;
    @UtopiaSqlColumn
    private String provinceName;
    @UtopiaSqlColumn
    private Integer cityCode;
    @UtopiaSqlColumn
    private String cityName;
    @UtopiaSqlColumn
    private Integer month;
}
