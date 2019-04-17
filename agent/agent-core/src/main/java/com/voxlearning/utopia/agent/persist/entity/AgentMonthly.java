package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * AgentMonthly
 *
 * @author song.wang
 * @date 2016/8/16
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_monthly")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161109")
public class AgentMonthly implements Serializable {
    @DocumentId
    private String id;
    private Integer month;
    private Long userId;
    private String userName;
    private AgentRoleType userRole;

    private Integer ranking; // 业绩排名

    private AgentMonthlyPerformance myPerformance;// 我的业绩数据

    private Integer visitSchoolCount;// 陪访次数

    private Integer intoSchoolCount;// 进校次数
    private Integer inPlanIntoSchoolCount; // 计划内进校数
    private Integer perMemberIntoSchoolCount;// 人均进校数
    private Integer notIntoSchoolCount;// 未拜访学校数

    private List<AgentMonthlyExcellentGroupAndUser> excellentGroupAndUserList; // 优秀团队和个人
    private List<AgentMonthlyVisitSchoolRanking> visitSchoolRankingList; // 进校排行数据
    private List<AgentMonthlyRecommendBook> recommendBookList;
    private List<AgentMonthlyPerformance> managedUserPerformanceList; // 下属的业绩数据
    private List<AgentMonthlyPerformanceDistribution> performanceDistributionList; // 271 分布数据

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_uid_month(Long userId, Integer month) {
        return CacheKeyGenerator.generateCacheKey(AgentMonthly.class,
                new String[]{"uid", "month"},
                new Object[]{userId, month});
    }


}
