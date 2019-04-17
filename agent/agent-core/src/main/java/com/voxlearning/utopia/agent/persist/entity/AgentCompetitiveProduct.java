package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.PaymentModeType;
import com.voxlearning.utopia.agent.constants.UsageScenarioType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 竞品实体类
 * @author deliang.che
 * @date 2018/3/8
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_competitive_product")
public class AgentCompetitiveProduct implements CacheDimensionDocument {

    @DocumentId
    private String id;                              //ID
    private Long schoolId;                          //学校ID
    private String name;                            //竞品名称
    private Integer studentNum;                     //覆盖学生数量
    private Integer intoTime;                       //竞品进入时间（格式：201803）
    private List<UsageScenarioType> usageScenario;  //使用场景
    private Integer ifSchoolPay;                    //学校是否付费（0：否 1：是）
    private List<PaymentModeType> paymentMode;      //付费模式
    private String  remark;                         //备注
    @DocumentCreateTimestamp
    protected Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("sid", this.schoolId)
        };
    }

}
