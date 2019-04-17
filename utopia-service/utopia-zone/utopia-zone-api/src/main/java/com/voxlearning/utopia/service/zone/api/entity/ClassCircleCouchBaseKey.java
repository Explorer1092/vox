package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动记录
 * @author chensn
 * @date 2018-10-30 16:02
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_couchbase_key")
public class ClassCircleCouchBaseKey implements Serializable {

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private String couchBaseKey;

    public static String  generateId(String couchBaseKey) {
        return couchBaseKey;
    }


}
