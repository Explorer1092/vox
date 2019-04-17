package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.constants.AgentPerformanceStatisticsType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.Setter;

/**
 * AgentPerformanceStatistics 业绩统计
 *
 * @author song.wang
 * @date 2017/3/27
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_PERFORMANCE_STATISTICS")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170327")
public class AgentPerformanceStatistics extends AbstractDatabaseEntity {

    private Integer month; // 月份

    private Long regionGroupId; // 大区部门ID
    private String regionGroupName; // 大区部门名称
    private Long cityGroupId; // 城市部门ID
    private String cityGroupName; // 城市部门名称

    private AgentPerformanceStatisticsType statisticsType; // 统计类型

    private Long groupId; // 部门ID
    private AgentGroupRoleType groupRoleType; // 部门角色

    private Long userId; // 用户ID
    private String userName; // 用户姓名
    private AgentRoleType userRoleType; // 人员角色

    private AgentKpiType performanceKpiType; // 业绩指标
    private Long budget; // 预算
    private Long complete; // 完成
    private Double completeRate; // 完成率

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceStatistics.class, "uid", userId);
    }

    public static String ck_uid_month(Long userId, Integer month) {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceStatistics.class,
                new String[]{"uid", "month"},
                new Object[]{userId, month});
    }

    public static String ck_gid_month(Long groupId, Integer month) {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceStatistics.class,
                new String[]{"gid", "month"},
                new Object[]{groupId, month});
    }

}
