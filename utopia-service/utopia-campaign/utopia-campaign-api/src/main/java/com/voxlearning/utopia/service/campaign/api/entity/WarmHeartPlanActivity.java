package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_WARM_HEART_PLAN_ACTIVITY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190326")
@NoArgsConstructor
public class WarmHeartPlanActivity extends AbstractDatabaseEntity implements CacheDimensionDocument {

    private static final long serialVersionUID = 6989490205718509539L;

    public WarmHeartPlanActivity(Long userId) {
        this.id = userId;
    }

    private Date teacherShowActivity;        // 老师查看页面的时间
    private Date teacherClickAssignBtn;      // 老师点击"布置计划"

    private Date studentShowActivity;        // 学生查看页面的时间
    private Date studentClickGoAssignBtn;    // 学生点击"去制定"
    private Date studentAssign;              // 学生"制定计划" (其实是家长制定、跟家长点击"制定计划"同时更)
    private Date studentParentShow;          // 学生家长是否查看页面 (跟家长查看页面时间同时更)

    private Date parentShowActivity;         // 家长查看页面的时间
    private Date parentClickGoAssign;        // 家长点击"去制定"
    private Date parentAssign;               // 家长点击"制定计划"

    private Boolean teacherSendMsg1;
    private Boolean teacherSendMsg2;

    private Boolean studentSendMsg1;
    private Boolean studentSendMsg2;
    private Boolean studentSendMsg3;
    private Boolean studentSendMsg4;

    private Boolean parentSendMsg1;
    private Boolean parentSendMsg2;
    @Deprecated
    private Boolean parentSendMsg3;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(WarmHeartPlanActivity.class, this.id)
        };
    }
}
