package com.voxlearning.utopia.service.crm.api.entities.agent.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.FollowUpType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_teacher")
@UtopiaCacheRevision("20181214")
public class WorkRecordTeacher implements CacheDimensionDocument {

    private static final long serialVersionUID = 2329587718604469681L;
    @DocumentId
    private String id;
    private Long schoolId;
    private String schoolName;
    private Long teacherId;
    private String teacherName; // 老师姓名
    private List<Subject> subjects;         // 包含主副账号所对应的学科
    private FollowUpType followUpType;
    private String content;              // 内容
    private String result;               // 结果

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
                newCacheKey(new String[]{"teacherId"}, new Object[]{teacherId})
        };
    }
}
