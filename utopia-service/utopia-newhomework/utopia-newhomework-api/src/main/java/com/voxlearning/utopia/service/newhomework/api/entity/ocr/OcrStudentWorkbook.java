package com.voxlearning.utopia.service.newhomework.api.entity.ocr;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 独立拍照学生练习册
 * @author majianxin
 * @version V1.0
 * @date 2019/3/22
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-homework")
@DocumentCollection(collection = "ocr_student_workbook")
@UtopiaCacheExpiration(86400)
@UtopiaCacheRevision("20190322")
public class OcrStudentWorkbook implements Serializable {
    private static final long serialVersionUID = -8670865041896589155L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private Long studentId;             // 学生ID
    private String backCoverImgUrl;     // 练习册背面图片地址
    public Boolean disabled;            // 默认false，删除true
    @DocumentCreateTimestamp
    private Date createTime;            // 记录创建时间
    @DocumentUpdateTimestamp
    private Date updateTime;            // 记录更新时间


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OcrStudentWorkbook.class, id);
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(OcrStudentWorkbook.class,
                new String[]{"SID"},
                new Object[]{studentId});
    }
}
