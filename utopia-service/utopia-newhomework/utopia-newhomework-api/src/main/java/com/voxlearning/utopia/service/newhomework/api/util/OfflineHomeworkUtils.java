package com.voxlearning.utopia.service.newhomework.api.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/9/19
 */
abstract public class OfflineHomeworkUtils {
    public static String buildMessageContent(OfflineHomework offlineHomework, NewHomework newHomework, NewHomeworkBook newHomeworkBook) {
        return buildMessageContent(offlineHomework, newHomework, newHomeworkBook, "\n");
    }

    public static String buildMessageContent(OfflineHomework offlineHomework, NewHomework newHomework, NewHomeworkBook newHomeworkBook, String separator) {
        if (offlineHomework == null) {
            return "";
        }
        String newHomeworkContent = "";
//        if (newHomework != null && newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
//            String homeworkContent = "在线作业(预计用时:{}分钟)" + separator + "内容:{}" + separator + "{}";
//            List<NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.getPractices();
//            List<String> practiceTypeList = new ArrayList<>();
//            for (int i = 0; i < newHomeworkPracticeContents.size(); i++) {
//                practiceTypeList.add((i + 1) + "." + newHomeworkPracticeContents.get(i).getType().getValue());
//            }
//            Set<String> unitNames = new LinkedHashSet<>();
//            for (ObjectiveConfigType type : newHomeworkBook.getPractices().keySet()) {
//                unitNames.addAll(newHomeworkBook.getPractices().get(type).stream().map(NewHomeworkBookInfo::getUnitName).collect(Collectors.toList()));
//            }
//            String practices = StringUtils.join(practiceTypeList, separator);
//            String units = StringUtils.join(unitNames, ",");
//            newHomeworkContent = StringUtils.formatMessage(homeworkContent, newHomework.processDurationMinutes(), units, practices);
//        }
        String offlineHomeworkContent = "线下作业" + separator + "{}";
        List<OfflineHomeworkPracticeContent> offlineHomeworkPracticeContents = offlineHomework.getPractices();
        List<String> practiceContents = new ArrayList<>();
        for (int i = 0; i < offlineHomeworkPracticeContents.size(); i++) {
            OfflineHomeworkPracticeContent practiceContent = offlineHomeworkPracticeContents.get(i);
            practiceContents.add((i + 1) + "." + practiceContent.toString());
        }
        String practices = StringUtils.join(practiceContents, separator);
        offlineHomeworkContent = StringUtils.formatMessage(offlineHomeworkContent, practices);
        if (StringUtils.isNotBlank(newHomeworkContent)) {
            return newHomeworkContent + separator + offlineHomeworkContent;
        } else {
            return offlineHomeworkContent;
        }
    }
}
