package com.voxlearning.utopia.service.crm.api.entities.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * CrmTeacherClue
 *
 * @author song.wang
 * @date 2016/8/6
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_teacher_clue")
public class CrmTeacherClue implements Serializable {

    @DocumentId
    private String id;
    private CrmClueType type;
    private Long schoolId;
    private String schoolName;
    private Long teacherId;
    private String teacherName;
    private String subject;
    private String creator;
    private String receiver;// 接收者
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;


}
