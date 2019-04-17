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
 * WorkRecordSchool
 *
 * @author song.wang
 * @date 2018/12/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_school")
@UtopiaCacheRevision("20181214")
public class WorkRecordSchool implements CacheDimensionDocument {

    private static final long serialVersionUID = -2155574087211566198L;
    @DocumentId
    private String id;

    private Long userId;
    private String userName;

    private Long schoolId;
    private String schoolName;

    private String signInRecordId;               // 签到记录ID

    private Integer visitType;               // 1:学校会议  2：拜访老师 3：直播展位推广

    // 拜访老师 ------------------ start
    private String title;                    // 主题
    // 拜访老师 ------------------ end

    private List<String> teacherRecordList;      // 跟进的老师记录ID列表
    private List<String> outerResourceRecordList;     // 跟进的上层资源的ID列表

    // 学校会议 ------------------ start
    private String lecturerName;                     // 讲师
    private Integer preachingTime;      // 宣讲时长 1.小于15分钟，2。15-60分钟，3大于1个小时
    private Integer meetingForm;               //  1.专场，2.插播
    private List<String> photoUrls;     // 现场照片
    // 学校会议 ------------------ end

    private Integer visitCountLte30; // 学校在进校日30天内被拜访了几次

    private String content;                  // 拜访内容
    private String result;                   // 拜访结果

    private Date workTime;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("uid", userId),
                newCacheKey("sid", schoolId)
        };
    }
}
