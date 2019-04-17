package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import org.bson.types.ObjectId;

public class NewHomeworkShardMongoHelper {

    public static boolean isShardHomeworkId(String homeworkId, boolean isOpen) {
        if (isOpen) {
            if (StringUtils.isBlank(homeworkId)) {
                return false;
            }
            String[] segments = StringUtils.split(homeworkId, "_");
            if (segments.length != 3) {
                return false;
            }
            String randomId = segments[1];
            try {
                ObjectId objectId = new ObjectId(randomId);
                long time = objectId.getDate().getTime();
                return time > NewHomeworkConstants.SHARD_MONGO_START_TIME.getTime();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isShardProcessId(String processId, boolean isOpen) {
        if (isOpen) {
            if (StringUtils.isBlank(processId)) {
                return false;
            }
            String[] segments = StringUtils.split(processId, "-");
            if (segments.length != 2) {
                return false;
            }
            long time = SafeConverter.toLong(segments[1]);
            return time != 0 && time > NewHomeworkConstants.SHARD_MONGO_START_TIME.getTime();
        }
        return false;
    }

    public static boolean isShardResultAnswerId(String resultAnswerId, boolean isOpen) {
        if (isOpen) {
            if (StringUtils.isBlank(resultAnswerId)) {
                return false;
            }
            String[] segments = StringUtils.split(resultAnswerId, "|");
            if (segments.length != 6 && segments.length != 5) {
                return false;
            }
            String hid = segments[1];
            return isShardHomeworkId(hid, isOpen);
        }
        return false;
    }

    public static boolean isShardAccomplishmentId(String accomplishmentId) {
        if (StringUtils.isBlank(accomplishmentId)) {
            return false;
        }
        String[] segments = StringUtils.split(accomplishmentId, "-");
        if (segments.length != 3) {
            return false;
        }
        String hid = segments[2];
        return NewHomeworkUtils.isShardHomework(hid);
    }

    public static boolean isShardHomeworkResultId(String homeworkResultId) {
        if (StringUtils.isBlank(homeworkResultId)) {
            return false;
        }
        String[] segments = StringUtils.split(homeworkResultId, "-");
        if (segments.length != 4) {
            return false;
        }
        String hid = segments[2];
        return NewHomeworkUtils.isShardHomework(hid);
    }
}
