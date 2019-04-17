package com.voxlearning.utopia.service.newhomework.api.entity.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_result_extended_info_{}", dynamic = true)
@UtopiaCacheExpiration(259200)
@UtopiaCacheRevision("20180410")
public class SubHomeworkResultExtendedInfo implements Serializable {
    private static final long serialVersionUID = 8052489552074265693L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;
    private Map<String, String> info;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -9039368906116902900L;
        private String day;
        private Subject subject;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public SubHomeworkResultExtendedInfo.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new SubHomeworkResultExtendedInfo.ID(day, subject, hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkResultExtendedInfo.class, id);
    }

    public static String getMentalArithmeticBestMinuteCountKey(){
        return "MENTAL_ARITHMETIC_BESTMINUTECOUNT";
    }
}
