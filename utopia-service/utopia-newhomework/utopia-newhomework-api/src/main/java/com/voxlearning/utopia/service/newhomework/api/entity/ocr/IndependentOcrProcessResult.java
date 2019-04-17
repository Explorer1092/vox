package com.voxlearning.utopia.service.newhomework.api.entity.ocr;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生独立拍照
 * --按学期分库, 按学生尾号分表
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-homework-{}-{}", dynamic = true)
@DocumentCollection(collection = "independent_ocr_process_result_{}", dynamic = true)
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20190325")
@DocumentIndexes({
        @DocumentIndex(def = "{'ocrMentalImageDetail.img_url':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'studentId':1,'disabled':1}", background = true)
})
public class IndependentOcrProcessResult implements Serializable {
    private static final long serialVersionUID = -2020118453294491841L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private Long userId;                                // 用户ID(学生端和studentId相同)
    private Long studentId;                             // 学生ID
    @DocumentCreateTimestamp
    private Date createAt;                              // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                              // 修改时间
    private Boolean disabled;                           // 是否删除
    private String clientType;                          // 客户端类型:ios,android
    private String clientName;                          // 客户端名称:17Student,17Parent
    public OcrMentalImageDetail ocrMentalImageDetail;   // 答题详情数据结构

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(IndependentOcrProcessResult.class, id);
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                IndependentOcrProcessResult.class,
                new String[]{"studentId"},
                new Object[]{studentId}
        );
    }

    public static String ck_ImgUrl(String imgUrl) {
        return CacheKeyGenerator.generateCacheKey(
                IndependentOcrProcessResult.class,
                new String[]{"ocrMentalImageDetail.img_url"},
                new Object[]{imgUrl}
        );
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId", "studentId"})
    public static class ID implements Serializable {
        private static final long serialVersionUID = 3151021384501806973L;
        private String randomId = RandomUtils.nextObjectId();
        private String time;
        private Long studentId;

        public ID(Date createTime, Long studentId) {
            this.time = Long.toString(createTime.getTime());
            this.studentId = studentId;
        }

        @Override
        public String toString() {
            return randomId + "-" + time + "-" + studentId;
        }
    }
}
