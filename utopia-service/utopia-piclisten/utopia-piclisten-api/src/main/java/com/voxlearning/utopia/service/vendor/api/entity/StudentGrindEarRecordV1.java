package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 第一期磨耳朵活动记录
 *
 * @author jiangpeng
 * @since 2016-11-29 下午5:43
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "student_grind_ear_record_17win")
@UtopiaCacheRevision("20180313")
public class StudentGrindEarRecordV1 implements Serializable {
    private static final long serialVersionUID = -618507312701733827L;
    @DocumentId
    private Long id; //学生id

    private List<Date> dateList;

    @DocumentCreateTimestamp
    private Date createDate;

    public Long dayCount(){
        if (CollectionUtils.isEmpty(getDateList()))
            return 0L;
        return getDateList().stream().map(t -> DayRange.newInstance(t.getTime())).distinct().count();
    }
}
