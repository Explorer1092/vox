package com.voxlearning.utopia.service.newhomework.api.entity.report;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@DocumentTable(table = "VOX_WEEK_PUSH_TEACHER")
@DocumentConnection(configName = "homework")
public class WeekPushTeacher implements Serializable {

    private static final long serialVersionUID = -7259273434872564845L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("TEACHER_ID")
    private Long teacherId;          //老师ID
    @DocumentField("SUBJECT_KEY")
    private Integer subject_key;     //学科key
    @DocumentField("CREATE_TIME")
    private Date createTime;


    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(WeekPushTeacher.class, id);
    }

}
