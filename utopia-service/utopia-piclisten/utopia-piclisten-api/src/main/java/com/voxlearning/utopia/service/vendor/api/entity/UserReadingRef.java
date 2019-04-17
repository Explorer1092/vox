package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author malong
 * @since 2016/12/21
 * 用户和绘本的对应关系:用于我的绘本和同学在读业务
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_USER_READING_REF")
@UtopiaCacheRevision("20161221")
public class UserReadingRef extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 8798825311130142830L;

    @DocumentField("PICTURE_BOOK_ID") private String pictureBookId;
    @DocumentField("USER_ID") private Long userId;
    @DocumentField("SELF_STUDY_TYPE") private SelfStudyType selfStudyType;

    //v1.3新增阅读完成状态
    @DocumentField("READ_FINISH_TIME") private Date readFinishTime;//阅读完成时间点
    @DocumentField("FINISH_STATUS")private Integer finishStatus;//完成状态(1.完成，0.未完成)
    @DocumentField("READ_SECONDS")private Long readSeconds;//阅读时长

    @Override
    public String[] generateCacheDimensions() {
        return new String[] {
                newCacheKey("UID", userId)
        };
    }
}
