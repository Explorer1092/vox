package com.voxlearning.utopia.entity.level;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;

@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_EXP")
public class TeacherExp extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = 9156956634370359612L;

    @DocumentField private Long id;
    @DocumentField private Long teacherId;
    @DocumentField private Integer exp;

}
