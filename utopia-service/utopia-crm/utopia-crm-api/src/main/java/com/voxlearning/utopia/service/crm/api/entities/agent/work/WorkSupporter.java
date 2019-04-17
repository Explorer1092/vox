package com.voxlearning.utopia.service.crm.api.entities.agent.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 保存外部人员（教研员，教育局长等）对市场人员工作的支持记录
 *
 * @author song.wang
 * @date 2018/12/13
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_supporter")
@UtopiaCacheRevision("20181214")
public class WorkSupporter implements CacheDimensionDocument {

    private static final long serialVersionUID = -853321194872638316L;
    @DocumentId
    private String id;

    private Long supporterId;
    private String supporterName;
    private Boolean isPresent;      // 是否出席

    private String content;         // 对市场人员的支持内容
    private String result;          // 支持效果

    private Long userId;            // 创建者ID
    private String userName;            // 创建者姓名

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
