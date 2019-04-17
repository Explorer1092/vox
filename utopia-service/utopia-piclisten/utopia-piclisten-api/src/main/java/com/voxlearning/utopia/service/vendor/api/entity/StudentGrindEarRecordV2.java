package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 磨耳朵二期活动记录
 *
 * @author jiangpeng
 * @since 2017-01-22 下午1:07
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "student_grind_ear_record_v2")
public class StudentGrindEarRecordV2 implements Serializable {
    private static final long serialVersionUID = -618507312701733827L;
    @DocumentId
    private String id; //学生id

    private List<Date> dateList;

    @DocumentCreateTimestamp
    private Date createDate;
}