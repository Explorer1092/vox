package com.voxlearning.utopia.service.psr.entity.termreport;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by dongxue.zhao on 2017/7/21.
 */


@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-athena")
@DocumentCollection(collection = "bigdata_termreport_daily_before20170901")

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161027")
public class termReportDaily_before20170901 implements Serializable {
    private static final long serialVersionUID = 1942386367780883624L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("group_id")
    private String groupId;
    @DocumentField("updated_at")
    private Date updatedAt;
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("unit_id")
    private String unit_id;
    @DocumentField("layout_times")
    private String layout_times;
    @DocumentField("student_infos")
    private List<StudentGroupUnitReport> student_infos;
}

