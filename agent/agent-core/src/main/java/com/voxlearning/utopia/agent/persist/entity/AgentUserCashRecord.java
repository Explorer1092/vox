package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * AgentUserCashRecord
 *
 * @author song.wang
 * @date 2017/1/15
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_user_cash_record")
public class AgentUserCashRecord implements Serializable {
    private static final long serialVersionUID = 8565986036945723635L;

    @DocumentId
    private String id;
    private Long userId;
    private String userName;
    private Long operatorId;
    private String operatorName;
    private Float preCash; // 调整前的预算金额
    private Float afterCash;// 调整前的预算金额
    private Float quantity;// 调整的金额
    private String comment; // 备注
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentUserMaterialBudgetRecord.class, "uid", userId);
    }
}
