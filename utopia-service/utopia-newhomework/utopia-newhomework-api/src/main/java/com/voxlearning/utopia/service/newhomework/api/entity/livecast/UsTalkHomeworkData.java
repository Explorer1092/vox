package com.voxlearning.utopia.service.newhomework.api.entity.livecast;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xuesong.zhang
 * @since 2017/8/3
 */
@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_USTALK_HOMEWORK_DATA")
public class UsTalkHomeworkData implements Serializable {

    private static final long serialVersionUID = 7728138706524318211L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    Long id;
    @DocumentField("HOMEWORK_ID") private String homeworkId;
    @DocumentField("STUDENT_ID") private Long studentId;
}
