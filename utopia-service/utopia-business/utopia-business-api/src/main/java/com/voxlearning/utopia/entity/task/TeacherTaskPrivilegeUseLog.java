package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by zhouwei on 2018/10/23
 **/
@Getter
@Setter
@DocumentConnection(configName = "hs_platform")
@DocumentTable(table = "VOX_TEACHER_TASK_PRIVILEGE_USE_LOG")
@UtopiaCacheRevision("20181023")
public class TeacherTaskPrivilegeUseLog extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = 258655233472166921L;

    @UtopiaSqlColumn(name = "TEACHER_ID")       private Long teacherId;             //老师ID
    @UtopiaSqlColumn(name = "LEVEL")            private Integer level;              //老师级别
    @UtopiaSqlColumn(name = "PRIVILEGE_ID")     private Integer privilegeId;        //特权ID
    @UtopiaSqlColumn(name = "PRIVILEGE_NAME")   private String privilegeName;       //特权名称
    @UtopiaSqlColumn(name = "COMMENT")          private String comment;             //任务描述

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{};
    }

}
