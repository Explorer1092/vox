package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jiang wei on 2016/7/26.
 */

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-bigdata")
@DocumentCollection(collection = "vb_rec_school_result")
public class CrmRecSchool implements Serializable, TimestampTouchable {


    private static final long serialVersionUID = -7409920989532201772L;
    @DocumentId
    private String id;
    @DocumentField("province_id")
    private Integer provinceId;
    @DocumentField("city_id")
    private Integer cityId;
    @DocumentField("county_id")
    private Integer countyId;
    @DocumentField("name")
    private String schoolName;
    @DocumentField("blat")
    private Double blat;
    @DocumentField("blon")
    private Double blon;
    @DocumentField("addr")
    private String addr;
    @DocumentField("verify")
    private String verify;
    @DocumentField("verify_mode")
    private String verifyMode;
    @DocumentField("auditor")
    private String auditor;
    @DocumentField("update_time")
    private Date updateTime;
    @DocumentField("status")
    private String status;
    @DocumentField("audit_result")
    private Integer auditResult;
    @DocumentField("id")
    private Integer schoolId;
    @DocumentField("dt")
    private String dt;
    @DocumentFieldIgnore
    private String provinceName;
    @DocumentFieldIgnore
    private String cityName;
    @DocumentFieldIgnore
    private String countyName;


    @Override
    public void touchUpdateTime(long timestamp) {
        updateTime = new Date(timestamp);
    }
}
