package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * AgentSchoolBudget
 *
 * @author song.wang
 * @date 2017/8/3
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_school_budget")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170807")
public class AgentSchoolBudget implements CacheDimensionDocument {

    private static final long serialVersionUID = 8325458085644699399L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Boolean disabled;

    private Long schoolId;            // 学校ID
    private Integer month;            // 月份（格式：201709）
    private AgentSchoolPermeabilityType permeability;            // 渗透情况： 低渗，中渗，高渗，超高渗
    private Integer sglSubjIncBudget;             // 小单新增预算
    private Integer sglSubjLtBfBudget;            // 小单长回预算
    private Integer sglSubjStBfBudget;            // 小单短回预算
    private Integer engBudget;                    // 英语月活预算
    private Integer mathAnshIncBudget;            // 数扫（扫描数学答题卡）的新增预算
    private Integer mathAnshBfBudget;             // 数扫（扫描数学答题卡）的回流预算

/*    public void initData(){
        this.sglSubjIncBudget = 0;
        this.sglSubjLtBfBudget = 0;
        this.sglSubjStBfBudget = 0;
        this.engBudget = 0;
        this.mathAnshIncBudget = 0;
        this.mathAnshBfBudget = 0;
    }*/

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("sid", schoolId),
                newCacheKey("m", month),
                newCacheKey(new String[]{"sid", "m"}, new Object[]{schoolId, month})
        };
    }
}
