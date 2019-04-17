package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentDictSchoolDifficultyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * AgentDictSchool
 *
 * @author song.wang
 * @date 2016/6/24
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_DICT_SCHOOL")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20171031")
public class AgentDictSchool extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 1691841333143404993L;

    @UtopiaSqlColumn Long schoolId;            // 学校ID
    @UtopiaSqlColumn Integer schoolLevel;      // 1:小学  2:中学
    @UtopiaSqlColumn Integer countyCode;       // 区域代码
    @UtopiaSqlColumn String countyName;        // 区域名称


    /**
     * 渗透情况，可以为空
     */
    @UtopiaSqlColumn AgentSchoolPermeabilityType permeabilityType;
    @UtopiaSqlColumn AgentDictSchoolDifficultyType schoolDifficulty;        // 任务难度：枚举类型，目前仅包含S，可为空 AgentDictSchoolDifficultyType
    @UtopiaSqlColumn AgentSchoolPopularityType schoolPopularity;        // 学校等级：枚举类型，目前仅包含A,B,C 可为空 AgentSchoolPopularityType


    @Deprecated @DocumentFieldIgnore Integer engMode; // 英语考核模式：0:模式为空 1:17zuoye模式  2：快乐学2+10模式  3:快乐学2+2模式
    @Deprecated @DocumentFieldIgnore Integer mathMode; // 数学考核模式：0:模式为空 1:17zuoye模式  2：快乐学2+10模式  3:快乐学2+2模式

    @UtopiaSqlColumn Boolean disabled;           // 是否已被删除
    @UtopiaSqlColumn Boolean calPerformance;     // 参与业绩计算

    @JsonIgnore
    public boolean isDisabledTrue() {
        return disabled != null && disabled;
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentDictSchool.class, "ALL");
    }

    public static String ck_sId(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(AgentDictSchool.class, "s_id", schoolId);
    }

    public static String ck_county_code(Integer countyCode) {
        return CacheKeyGenerator.generateCacheKey(AgentDictSchool.class, "county_code", countyCode);
    }

    @Deprecated
    public Long getEngBudget(Integer day) {

        return 0L;
    }

    @Deprecated
    public Long getMathBudget(Integer day) {
        return 0L;
    }


    @JsonIgnore
    public boolean isCalPerformanceTrue() {
        return calPerformance != null && calPerformance;
    }

    @JsonIgnore
    public boolean isKlxModeSchool() {
        return Objects.equals(schoolLevel, 4) || Objects.equals(schoolLevel, 2);
    }
}
