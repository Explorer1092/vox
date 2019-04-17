package com.voxlearning.utopia.entity.level;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;

@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_EXP_HISTORY_{}", dynamic = true)
public class TeacherExpHistory extends AbstractDatabaseEntityWithDisabledField {

    @DocumentField private Long teacherId;
    @DocumentField private Integer exp;
    @DocumentField private String comment;

}
