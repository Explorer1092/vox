package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 老师子任务模板(字典)，一个任务对应多个子任务。子任务之间可能会有依赖关系
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_TASK_SUB")
public class TeacherTaskSubTpl extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = 4999312244316017679L;

    @UtopiaSqlColumn(name = "MASTER_ID") private Long masterId;         // 父任务
    @UtopiaSqlColumn(name = "DESC") private String desc;
    @UtopiaSqlColumn(name = "EXPRESSION") private String expression;    // 表达式

}
