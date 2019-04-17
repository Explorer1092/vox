package com.voxlearning.utopia.service.crm.api.entities.agent.work;

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
 * 资源拓维
 * @author deliang.che
 * @since  2019/1/11
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_resource_extension")
@UtopiaCacheRevision("20190111")
public class WorkRecordResourceExtension implements CacheDimensionDocument {

    private static final long serialVersionUID = 5702519414178452965L;
    @DocumentId
    private String id;

    private List<String> teacherRecordIds;                 // 老师记录ID列表
    private List<String> outerResourceRecordIds;           //上层资源记录ID列表

    private Integer visitIntention;    // 拜访目的  1：初次接洽  2：客情维护 3：促进组会 4：寻求介绍

    private String signInRecordId;               // 签到记录ID
    private List<String> photoUrls;              // 拜访照片
    private String content;     //拜访过程
    private String result;      //达成结果

    private Long userId;
    private String userName;
    private Date workTime;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
        };
    }
}
