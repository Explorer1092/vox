package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.afenti.api.context.AfentiGiftStatus;
import com.voxlearning.utopia.service.afenti.api.context.GiftType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author peng.zhang.a
 * @since 16-8-15
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_USER_GIFT_RECORD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160815")
public class AfentiUserGiftRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -6743121440844842239L;

    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private GiftType giftType;
    @UtopiaSqlColumn private Integer beansNum;
    @UtopiaSqlColumn private AfentiGiftStatus status;
    @UtopiaSqlColumn private Subject subject;


    public static AfentiUserGiftRecord newInstance(Long userId, GiftType giftType, Integer beansNum, AfentiGiftStatus status, Subject subject) {
        AfentiUserGiftRecord record = new AfentiUserGiftRecord();
        record.setUserId(userId);
        record.setGiftType(giftType);
        record.setBeansNum(beansNum);
        record.setSubject(subject);
        record.setStatus(status);
        return record;
    }

    public static String ck_us(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(
                AfentiUserGiftRecord.class,
                new String[]{"UID", "SJ"},
                new Object[]{userId, subject});
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AfentiUserGiftRecord.class, id);
    }
}
