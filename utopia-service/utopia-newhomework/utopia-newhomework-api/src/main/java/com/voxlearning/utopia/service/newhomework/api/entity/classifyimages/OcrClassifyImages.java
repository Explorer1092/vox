package com.voxlearning.utopia.service.newhomework.api.entity.classifyimages;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 5:25 PM
 * \* Description: 纸质口算鉴黄替换图片记录数据
 * \
 */
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_CLASSIFY_IMAGES")
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class OcrClassifyImages implements Serializable, PrimaryKeyAccessor<Long>, TimestampTouchable {

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    @UtopiaSqlColumn
    private Date createDatetime;
    @UtopiaSqlColumn
    private String homeworkId; //作业id/创建时间毫秒值(独立拍照)/独立拍照我的练习册ID
    @UtopiaSqlColumn
    private Long userId; //学生id或家长ID
    @UtopiaSqlColumn
    private String processId;
    @UtopiaSqlColumn
    private String originalImageUrl; //学生原来上传的图片
    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }
    @DocumentUpdateTimestamp
    @UtopiaSqlColumn
    private Date updateDatetime;   //更新时间
    @Override
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(OcrClassifyImages.class, id);
    }

}
