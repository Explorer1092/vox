package com.voxlearning.utopia.service.mizar.api.entity.hbs;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
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
 * 华杯赛 - 用户实体
 * Created by haitian.gan on 2017/2/15.
 */
@DocumentConnection(configName = "partner_hbs")
@DocumentTable(table = "jo_user")
public class HbsUser implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {

    @Getter @Setter @UtopiaSqlColumn(name = "user_id", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) Long id;
    @Getter @Setter @UtopiaSqlColumn(name = "username") String userName;
    @Getter @Setter @UtopiaSqlColumn(name = "password") String password;

    @DocumentCreateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "register_time") Date createTime;

    @DocumentUpdateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "modify_time") Date updateTime;

    @Getter @Setter @UtopiaSqlColumn(name = "last_login_time") Date lastLoginTime;// 最近一次登陆时间

    @DocumentFieldIgnore
    @Getter
    @Setter
    private HbsContestant contestant;// 该用户关联的参赛选手信息

    @Override
    public long fetchCreateTimestamp() {
        return createTime == null ? 0 : createTime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateTime == null ? 0 : updateTime.getTime();
    }

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(
                HbsUser.class,
                new String[]{"UID"},
                new Object[]{userId});
    }

    public static String ck_uname(String userName) {
        return CacheKeyGenerator.generateCacheKey(
                HbsUser.class,
                new String[]{"UNAME"},
                new Object[]{userName});
    }
}
