package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 上报数据
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:34
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "avenger_newexam_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
public class AvengerNewExam implements Serializable {

    private static final long serialVersionUID = -5994946924676051770L;

    private String env;
    private Date timeFiled;

    @DocumentId
    private String id;
    private String homeworkId;
    private SchoolLevel schoolLevel;                            // 猜猜
    private Subject subject;                                    // 学科
    private String title;                                       // 作业名称
    private Date startTime;                                     // 作业起始时间
    private Date endTime;                                       // 作业结束时间
}
