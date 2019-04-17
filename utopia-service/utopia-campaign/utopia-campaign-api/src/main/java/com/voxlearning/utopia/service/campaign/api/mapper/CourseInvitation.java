package com.voxlearning.utopia.service.campaign.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.common.FieldValueSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CourseInvitation implements java.io.Serializable {

    @JsonIgnore
    private long oneDayTime = 24 * 60 * 60 * 1000;
//    private long oneDayTime = 10 * 60 * 1000; // 测试10分钟

    private Long userId;           // 用户ID
    private Long courseId;         // 新讲堂课程ID
    @FieldValueSerializer(serializer = "com.voxlearning.alps.lang.mapper.json.StringDateSerializer")
    private Date invitationDate;   // 邀请日期
    private List<Helper> helpers;  // 助力者

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Helper implements java.io.Serializable {
        private String openId;     // 微信 openId
        private String nickName;   // 昵称
        private String imgUrl;     // 头像 url
        private Date createTime;   // 助理时间
    }

    /**
     * 是否已过期 (缓存有效期 48 小时,邀请有效期 24 小时)
     */
    public boolean fetchExpire() {
        if (invitationDate == null) {
            return true;
        }

        long nowTime = new Date().getTime();
        long diff = nowTime - invitationDate.getTime();
        return diff > oneDayTime;
    }

    /**
     * 距离邀请结束还有多少毫秒
     */
    public long fetchCountdown() {
        if (invitationDate == null) {
            return 0;
        }

        long endTime = invitationDate.getTime() + oneDayTime;
        return endTime - new Date().getTime();
    }

    /**
     * 距离邀请缓存过期还有多少秒
     */
    public int fetchCacheExpirationInSeconds() {
        if (invitationDate == null) return 0;

        long expireDate = invitationDate.getTime() + (2 * oneDayTime);
        long diff = expireDate - new Date().getTime();
        return Integer.parseInt(String.valueOf(diff));
    }
}
