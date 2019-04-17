package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.runtime.RuntimeMode;

import java.util.*;

/**
 * @author jiangpeng
 * @since 2018-08-23 上午11:41
 **/
public class PiclistenKillNamiActivity {

    public static Date startDate;

    public static Date endDate;

    public static Long couponExpireTime;

    public static Long expireRemindTime;

    public static Integer maxAssistCount;

    public static Set<String> activePublisherSet = new HashSet<>();

    public static Map<String, String> couponIdMap = new HashMap<>();

    static {
        if (RuntimeMode.isUsingTestData()) {
            startDate = DateUtils.stringToDate("2018-08-20 00:00:00");
            endDate = DateUtils.stringToDate("2018-10-31 23:59:59");
            couponExpireTime = 1800L;
            maxAssistCount = 0;
            activePublisherSet.add("人教版");
//            activePublisherSet.add("译林版");
            couponIdMap.put("ENGLISH", "5b7e6859ac74593ed4f8e770");
            couponIdMap.put("CHINESE", "5b7e68208edbc80e7c1cd3d1");
            couponIdMap.put("PACKAGE", "5b7e68a98edbc80e7c1cd3d5");
            couponIdMap.put("NEW_COUPON", "5b9f446d8edbc8652fc77049");
            expireRemindTime = 600L;

        } else {
            startDate = DateUtils.stringToDate("2018-09-01 00:00:00");
            endDate = DateUtils.stringToDate("2018-10-31 23:59:59");
            if (RuntimeMode.isStaging()) {
                couponExpireTime = 86400L;
                maxAssistCount = 1;
                activePublisherSet.add("人教版");
                expireRemindTime = 3600L;
            } else {
                couponExpireTime = 86400L;
                maxAssistCount = 1;
                activePublisherSet.add("人教版");
                expireRemindTime = 3600L;
            }

            couponIdMap.put("ENGLISH", "5b7e62125272e555b03aa97b");
            couponIdMap.put("CHINESE", "5b7e5f5a5272e555b03aa51d");
            couponIdMap.put("PACKAGE", "5b7e628c5272e555b03aaa3f");
            couponIdMap.put("NEW_COUPON", "5b9f4e635272e508b36e5bb6");
        }
    }

    public static boolean inPeriod() {
        Date date = new Date();
        return date.after(startDate) && date.before(endDate);
    }

    public static boolean limitAssistCount() {
        return maxAssistCount > 0;
    }
}
