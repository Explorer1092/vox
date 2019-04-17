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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 作业详细信息表
 * @author chongfeng.qi
 * @date 2018-11-16
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_practice")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class HomeworkPractice implements Serializable {

    private static final long serialVersionUID = -3775876181796339177L;
    @DocumentId
    private String id; //ID

    private List<Practices> practices; //作业内容：题目信息

    @DocumentUpdateTimestamp
    private Date updateTime; //更新日期，格式为yyyy-MM-dd

    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd

}
