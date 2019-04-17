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
 * 结构需要与TeacherTask保持一致
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_platform")
@DocumentTable(table = "VOX_TEACHER_TASK_LOG")
@UtopiaCacheRevision("20181023")
public class TeacherTaskLog extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument{

    private static final long serialVersionUID = -5027953634720094438L;

    @UtopiaSqlColumn(name = "TEACHER_ID")   private Long teacherId;     //老师ID
    @UtopiaSqlColumn(name = "TYPE")         private String type;        //任务类型 @See TeacherTaskTpl.Type
    @UtopiaSqlColumn(name = "NAME")         private String name;        //任务名称
    @UtopiaSqlColumn(name = "TPL_ID")       private Long tplId;         //任务模板ID vox_teacher_task_tpl的主键ID
    @UtopiaSqlColumn(name = "STATUS")       private String status;      //任务状态
    @UtopiaSqlColumn(name = "EXPIRE_DATE")  private Date expireDate;    //任务过期时间
    @UtopiaSqlColumn(name = "RECEIVE_DATE") private Date receiveDate;   //任务领取时间
    @UtopiaSqlColumn(name = "CANCEL_DATE")  private Date cancelDate;    //任务取消时间
    @UtopiaSqlColumn(name = "FINISHED_DATE")private Date finishedDate;  //任务完成时间


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey("TEACHER_ID",teacherId)};
    }

}
