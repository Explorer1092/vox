package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;


/**
 * 大考合同扩展类
 *
 * @author deliang.che
 * @date 2018-05-02
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_exam_contract_extend")
@UtopiaCacheRevision("20180502")
public class AgentExamContractExtend implements CacheDimensionDocument {
    @DocumentId
    private String id;                                          //ID
    private Long contractId;                                    //合同ID

    private List<AgentExamContractSplitSetting> splitSettingList;//分成设置list

    private List<String> imageUrlList;                          //合同照片URL

    private Boolean disabled;

    @DocumentCreateTimestamp
    protected Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("cid", this.contractId)
        };
    }
}
