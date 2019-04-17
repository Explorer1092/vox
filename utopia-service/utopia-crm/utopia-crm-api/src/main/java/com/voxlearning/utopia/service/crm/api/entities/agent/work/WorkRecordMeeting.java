package com.voxlearning.utopia.service.crm.api.entities.agent.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmMeetingType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 组会  包括省级，市级，区级，校级
 *
 * @author song.wang
 * @date 2018/12/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_meeting")
@UtopiaCacheRevision("20181214")
public class WorkRecordMeeting implements CacheDimensionDocument {

    private static final long serialVersionUID = -787215576059106532L;
    @DocumentId
    private String id;

    private Long userId;
    private String userName;

    private CrmMeetingType meetingType;          //
    // 省市区级组会专用户
    private String title;                        // 主题
    private Integer attendances;                 // 出席人数
    private List<String> supporterRecordList;             // 教研员，教育局长等资源对本次组会的支持情况 （指向WorkSupporter记录）


//    // 校级组会专用字段
//    private Long schoolId;
//    private String schoolName;
//    private List<String> teacherRecordList;      // 跟进的老师记录ID列表


    //  以下为通用字段
    private String signInRecordId;               // 签到记录ID

    private String lecturerName;                     // 讲师
    private Integer preachingTime;      // 宣讲时长 1.小于15分钟，2。15-60分钟，3大于1个小时
    private Integer form;               //  1.专场，2.插播
    private List<String> photoUrls;     // 现场照片
    private String content;
    private String result;

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
