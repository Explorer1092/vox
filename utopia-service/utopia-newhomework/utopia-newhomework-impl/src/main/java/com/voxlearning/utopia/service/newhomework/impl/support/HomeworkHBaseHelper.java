package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultAnswerHBase;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import org.bson.types.ObjectId;

/**
 * @author xuesong.zhang
 * @since 2017/8/16
 */
public class HomeworkHBaseHelper {

    public static HomeworkResultHBase transformJsonData(HomeworkResultHBase homeworkResultHBase) {
        if (homeworkResultHBase == null || StringUtils.isBlank(homeworkResultHBase.getJsonData())) {
            return homeworkResultHBase;
        }
        String jsonData = homeworkResultHBase.getJsonData();
        homeworkResultHBase = JsonUtils.fromJson(jsonData, HomeworkResultHBase.class);
        if (homeworkResultHBase != null) {
            if (StringUtils.isBlank(homeworkResultHBase.getId())) {
                String id = SafeConverter.toString(JsonUtils.convertJsonObjectToMap(jsonData).getOrDefault("_id", ""));
                homeworkResultHBase.setId(id);
            }
            homeworkResultHBase.setJsonData("");
        }
        return homeworkResultHBase;
    }

    public static HomeworkResultAnswerHBase transformJsonData(HomeworkResultAnswerHBase homeworkResultAnswerHBase) {
        if (homeworkResultAnswerHBase == null || StringUtils.isBlank(homeworkResultAnswerHBase.getJsonData())) {
            return homeworkResultAnswerHBase;
        }
        String jsonData = homeworkResultAnswerHBase.getJsonData();
        homeworkResultAnswerHBase = JsonUtils.fromJson(jsonData, HomeworkResultAnswerHBase.class);
        if (homeworkResultAnswerHBase != null) {
            if (StringUtils.isBlank(homeworkResultAnswerHBase.getId())) {
                String id = SafeConverter.toString(JsonUtils.convertJsonObjectToMap(jsonData).getOrDefault("_id", ""));
                homeworkResultAnswerHBase.setId(id);
            }
            homeworkResultAnswerHBase.setJsonData("");
        }
        return homeworkResultAnswerHBase;
    }

    public static HomeworkProcessResultHBase transformJsonData(HomeworkProcessResultHBase homeworkProcessResultHBase) {
        if (homeworkProcessResultHBase == null || StringUtils.isBlank(homeworkProcessResultHBase.getJsonData())) {
            return homeworkProcessResultHBase;
        }
        String jsonData = homeworkProcessResultHBase.getJsonData();
        homeworkProcessResultHBase = JsonUtils.fromJson(jsonData, HomeworkProcessResultHBase.class);
        if (homeworkProcessResultHBase != null) {
            if (StringUtils.isBlank(homeworkProcessResultHBase.getId())) {
                String id = SafeConverter.toString(JsonUtils.convertJsonObjectToMap(jsonData).getOrDefault("_id", ""));
                homeworkProcessResultHBase.setId(id);
            }
            homeworkProcessResultHBase.setJsonData("");
        }
        return homeworkProcessResultHBase;
    }

    public static boolean isHBaseProcessId(String processId, boolean isOpen) {
        if (isOpen && NewHomeworkConstants.FROM_HBASE_SUB_HOMEWORK) {
            if (StringUtils.isBlank(processId)) {
                return false;
            }
            String[] segments = StringUtils.split(processId, "-");
            if (segments.length != 2) {
                return false;
            }
            long time = SafeConverter.toLong(segments[1]);
            return time != 0 && time < NewHomeworkConstants.HBASE_SUB_HOMEWORK_END_TIME.getTime();
        }
        return false;
    }

    public static boolean isHBaseHomeworkId(String homeworkId, boolean isOpen) {
        if (isOpen && NewHomeworkConstants.FROM_HBASE_SUB_HOMEWORK) {
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
                return time < NewHomeworkConstants.HBASE_SUB_HOMEWORK_END_TIME.getTime();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isHBaseResultAnswerId(String resultAnswerId, boolean isOpen) {
        if (isOpen && NewHomeworkConstants.FROM_HBASE_SUB_HOMEWORK) {
            if (StringUtils.isBlank(resultAnswerId)) {
                return false;
            }
            String[] segments = StringUtils.split(resultAnswerId, "|");
            if (segments.length != 6 && segments.length != 5) {
                return false;
            }
            String hid = segments[1];
            return isHBaseHomeworkId(hid, isOpen);
        }
        return false;
    }

    public static boolean isHBaseResultId(String resultId, boolean isOpen) {
        if (isOpen && NewHomeworkConstants.FROM_HBASE_SUB_HOMEWORK) {
            if (StringUtils.isBlank(resultId)) {
                return false;
            }
            String[] segments = StringUtils.split(resultId, "-");
            if (segments.length != 4) {
                return false;
            }
            String hid = segments[2];
            return isHBaseHomeworkId(hid, isOpen);
        }
        return false;
    }
}
