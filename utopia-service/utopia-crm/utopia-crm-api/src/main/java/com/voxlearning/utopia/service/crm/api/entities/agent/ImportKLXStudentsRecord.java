package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.bean.ImportKLXStudentInfo;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;


/**
 * Created by tao.zang
 * on 2017/4/11.
 * 快乐学学生账号导入操作记录
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "import_klx_students_record")
@UtopiaCacheRevision("170411")
public class ImportKLXStudentsRecord implements CacheDimensionDocument {
    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    private Long operatorId;            // 操作人Id
    private String operatorName;        // 操作人姓名
    private String comments;            // 备注
    private String schoolName;          //操作学校
    private Long   schoolId;            //操作学校id标识
    private List<ImportKLXStudentInfo> importKLXStudentInfoList; //导入快乐学账号信息
    private SystemPlatformType sourceType;  //来源

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }
}
