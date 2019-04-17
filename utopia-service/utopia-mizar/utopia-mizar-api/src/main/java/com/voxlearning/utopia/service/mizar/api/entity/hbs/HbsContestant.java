package com.voxlearning.utopia.service.mizar.api.entity.hbs;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;


/**
 * 华杯赛 - 选手实体
 * Created by haitian.gan on 2017/2/15.
 */
@DocumentConnection(configName = "partner_hbs")
@DocumentTable(table = "hbs_application")
public class HbsContestant implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {

    @Getter @Setter @UtopiaSqlColumn(name = "id", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) Long id;
    @Getter @Setter @UtopiaSqlColumn(name = "stu_id") private Long userId;
    @Getter @Setter @UtopiaSqlColumn(name = "s_name") private String name;
    @Getter @Setter @UtopiaSqlColumn(name = "paper_no") private String idCardNo;
    @Getter @Setter @UtopiaSqlColumn(name = "patriarch_phone") private String phoneNumber;
    @Getter @Setter @UtopiaSqlColumn(name = "award1") private String preContestResult;
    @Getter @Setter @UtopiaSqlColumn(name = "award2") private String finalContestResult;

    @DocumentUpdateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "modify_time") private Date updateTime;

    @DocumentFieldIgnore
    @Getter
    @Setter
    private HbsUser user;// 此参赛选手关联的系统用户信息

    @Override
    public long fetchCreateTimestamp() {
        return 0;
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateTime == null ? 0 : updateTime.getTime();
    }

    public static String ck_uid(Long uid) {
        return CacheKeyGenerator.generateCacheKey(
                HbsContestant.class,
                new String[]{"UID"},
                new Object[]{uid});
    }

    public static String ck_mobile(String mobile) {
        return CacheKeyGenerator.generateCacheKey(
                HbsContestant.class,
                new String[]{"MOBILE"},
                new Object[]{mobile});
    }

    public static String ck_idcardno(String idCardNo) {
        return CacheKeyGenerator.generateCacheKey(
                HbsContestant.class,
                new String[]{"ID_CARD_NO"},
                new Object[]{idCardNo});
    }

}
