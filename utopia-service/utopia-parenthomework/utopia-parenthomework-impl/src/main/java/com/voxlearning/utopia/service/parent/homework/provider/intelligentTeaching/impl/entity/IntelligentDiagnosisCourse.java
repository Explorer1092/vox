package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 讲练测课程
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "intelligent_diagnosis_course")
public class IntelligentDiagnosisCourse implements Serializable {

    private static final long serialVersionUID = -2022210401815265192L;

    @DocumentId
    private String id;
    @DocumentField("online_status")
    private Integer onStatus;
    @DocumentField("status")
    private Integer status;

}
