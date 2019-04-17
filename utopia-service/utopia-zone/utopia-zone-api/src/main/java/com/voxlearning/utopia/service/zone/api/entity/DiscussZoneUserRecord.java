package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 讨论区用户记录
 * @author chensn
 * @date 2018-10-23 14:27
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_discuss_record_{}",dynamic = true)
public class DiscussZoneUserRecord implements Serializable {
    private static final long serialVersionUID = -1917446794433569966L;
    //discussId_clazzId_userId
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        final DiscussZoneUserRecord discussZoneUserRecord = (DiscussZoneUserRecord) obj;
        if (this == discussZoneUserRecord) {
            return true;
        } else {
            return (this.id.equals(discussZoneUserRecord.getId()));
        }
    }
}
