package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author jiangpeng
 * @since 2017-09-15 下午2:55
 **/
@Getter
@Setter
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentTable(table = "UCT_USER_MINI_PROGRAM_REF")
public class UserMiniProgramRef extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {


    private static final long serialVersionUID = -7145603527264508970L;

    @DocumentField("USER_ID") private Long userId;
    @DocumentField("OPEN_ID") private String openId;
    @DocumentField("SOURCE") private String source;
    @DocumentField("TYPE") private Integer type; //对应 UserMiniProgramType 的 key
    @DocumentField("LAST_LOGIN_DATE") private Date lastLoginDate;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"openId", "type"}, new Object[]{openId,type}),
                newCacheKey(new String[]{"userId", "type"}, new Object[]{userId,type})
        };
    }

    /**
     * 登录状态是否已过期
     * 7天过期
     * @return
     */
    @DocumentFieldIgnore
    public boolean sessionExpire() {
        Date lastLoginDate = this.getLastLoginDate();
        if (lastLoginDate == null)
            return true;
        if (DateUtils.dayDiff(new Date(), lastLoginDate) >= 7)
            return true;
        return false;
    }
}
