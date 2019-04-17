package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by jiang wei on 2016/7/26.
 */

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-bigdata")
@DocumentCollection(collection = "vb_ugc_student_result")
public class CrmUgcStudentResult implements Serializable {

    private static final long serialVersionUID = 518781565825333616L;


    @DocumentId
    private String id;
    @DocumentField("school_id")
    private String schoolId;
    @DocumentField("school_name")
    private String schoolName;
    @DocumentField("picture_url")
    private String pictureUrl;
    @DocumentField("student_id")
    private Long studentId;
    private String description;
    private Double blat;
    private Double blon;
    private Integer status;
    @DocumentField("rele_id")
    private Integer releId;
}
