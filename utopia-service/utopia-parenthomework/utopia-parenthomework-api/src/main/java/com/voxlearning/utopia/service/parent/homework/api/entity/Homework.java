package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 作业表
 * @author chongfeng.qi
 * @date 2018-11-16
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class Homework implements Serializable {

    private static final long serialVersionUID = -3773766181796336377L;
    @DocumentId
    private String id; //ID

    private String actionId; //批次标识，同一批次作业相同

    @Deprecated
    private Long fromUserId; //作业发布者 see publisherId
    private String publisherId;//发布者id

    private Integer grade; //年级

    private String subject; //学科，参照Subject枚举name

    private String homeworkTag; //作业标签，参照HomeworkTag

    private String type; //作业类型，参照NewHomeworkType

    private Long duration; //预计时间，单位秒

    private String remark; //备注

    private String source;//布置作业来源:parent--家长通

    private String bizType;//业务类型：EXAM--同步习题；MENTAL--口算

    private Boolean checked; //是否检查

    private Boolean disabled; //是否可用

    private Boolean includeSubjective; //是否包含主观题

    private Date startTime; //开始日期，格式为yyyy-MM-dd HH:mm:ss

    private Date endTime; //结束日期，格式为yyyy-MM-dd HH:mm:ss

    private Date checkedTime; //检查日期，格式为yyyy-MM-dd

    private String checkHomeworkSource; //检查作业端标识

    private Map<String, Object> additions; // 扩展信息

    private Integer questionCount; // 题数

    private Double score; // 分数

    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd HH:mm:ss

    @DocumentUpdateTimestamp
    private Date updateTime; //更新日期，格式为yyyy-MM-dd HH:mm:ss


}
