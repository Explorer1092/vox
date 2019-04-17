package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 七鱼在线数据
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_qiyu_session_record")
@UtopiaCacheRevision("20181023")

public class QiYuSessionRecord implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = -4775869009469002151L;

    @DocumentId
    private Long id; // 回话id

    private Long startTime; // 会话开始时间

    private Long endTime; // 会话结束时间

    private Integer sType; // 会话类型 0=正常会话.1(2)=离线 留言，3=排队超时

    private String category; // 会话分类

    private String evaluation; // 满意度值

    private Integer evaluationType; // 满意度类型

    private String evaluationRemark; // 满意度评价内容

    private Integer relatedType; // 关联会话类型 0=无关联,1=从机器人转接过来,2=机器人会话转接人工,3=历史会话发起,4=客服间转接,5=被接管

    private Long relatedId; // 被关联的会话 id

    private Integer interaction; // 会话交互类型

    private String closeReason; // 会话被关闭原因

    private String fromGroup; // 会话来自分流组名

    private String fromStaff; // 会话来自哪个客服

    private Long inQueueTime; // 排队开始时间点

    private Long queueTime; // 排队时长

    private Long visitRange; // 与上一次来访的时间差

    private Integer vipLevel; // vip级别

    private Long staffId; // 客服 id

    private String staffName; // 客服名字

    private String userId; // 访客 id

    private String fromIp; // 访客来源 ip

    private String fromPage; // 来源页

    private String fromTitle; // 来源页标题

    private String fromType; // 来源类型

    private String foreignId; // 17id

    private String mobile; // 手机号

    private Integer staffMessageNum; // 客服发送消息的总数量

    private Integer userMessageNum; // 访客发送消息的总数量

    private Integer replayAvgTime; // 平均会话响应时间

    private Date createTime;

    public void setsType(Integer sType) {
        this.sType = sType;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
