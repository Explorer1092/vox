package com.voxlearning.utopia.entity.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 天权的付款依据
 * Created by yaguang.wang on 2016/9/23.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "agent_performance_config")
@UtopiaCacheRevision("20170105")
public class AgentPerformanceConfig implements Serializable {
    private static final long serialVersionUID = -7613876745342155106L;
    @DocumentId
    private String id;
    private Long userId;                        // 用户ID
    private Integer identity;                   // 用户身份
    private String account;                     // 用户帐号
    private Integer settlementMonth;            // 结算月份
    private Integer cityJuniorMeet;             // 市级专场会议
    private Integer countyJuniorMeet;           // 区级专场会议

    private Integer cityMiddleMeet;             // 市级专场会议
    private Integer countyMiddleMeet;           // 区级专场会议
    private Integer interCutJuniorMeet;         // 小学插播组会
    private Integer interCutMiddleMeet;         // 中学插播组会
    private Integer juniorTheMothClue;          // 本月线索数小学
    private Integer middleTheMothClue;          // 本月线索数中学
    //private Double royalties;                   // 市经理UStalk 提成

    private String indicator1Name;              // 指标1名称
    private Double indicator1;                  // 大区结算指标1
    private String indicator2Name;              // 指标1名称
    private Double indicator2;                  // 大区结算指标2
    private Integer paymentsType;               // 结算类型     1.2.3.

    private Boolean disabled;                   // 是否禁用
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ap_all() {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceConfig.class, "ALL");
    }

    public static String ap_u_m(Long userId, Integer settlementMonth) {
        return CacheKeyGenerator.generateCacheKey(AgentPerformanceConfig.class, new String[]{"ap_u", "ap_m"}, new Object[]{userId, settlementMonth});
    }
}
