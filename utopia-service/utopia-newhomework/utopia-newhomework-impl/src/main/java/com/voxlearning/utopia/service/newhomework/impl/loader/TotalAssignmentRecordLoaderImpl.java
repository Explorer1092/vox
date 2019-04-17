package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.TotalAssignmentRecordLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/3/2
 */
@Named
@Service(interfaceClass = TotalAssignmentRecordLoader.class)
@ExposeService(interfaceClass = TotalAssignmentRecordLoader.class)
public class TotalAssignmentRecordLoaderImpl extends NewHomeworkSpringBean implements TotalAssignmentRecordLoader {
    @Override
    public Map<String, TotalAssignmentRecord> loadTotalAssignmentRecordByContentType(Subject subject, Collection<String> contentIds, HomeworkContentType contentType) {
        if (CollectionUtils.isNotEmpty(contentIds) && contentType != null) {
            Map<String, String> contentId2RecordIdMap = new HashMap<>();
            contentIds.forEach(contentId -> {
                // 试题和试卷要按照docid来查
                if (contentType == HomeworkContentType.QUESTION || contentType == HomeworkContentType.PAPER) {
                    contentId2RecordIdMap.put(contentId, subject + "-" + contentType + "-" + TeacherAssignmentRecord.id2DocId(contentId));
                } else {
                    contentId2RecordIdMap.put(contentId, subject + "-" + contentType + "-" + contentId);
                }
            });
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordDao.loads(new HashSet<>(contentId2RecordIdMap.values()));
            if (MapUtils.isNotEmpty(totalAssignmentRecordMap)) {
                Map<String, TotalAssignmentRecord> recordMap = new HashMap<>();
                contentIds.stream().filter(contentId -> totalAssignmentRecordMap.containsKey(contentId2RecordIdMap.get(contentId)))
                        .forEach(contentId -> recordMap.put(contentId, totalAssignmentRecordMap.get(contentId2RecordIdMap.get(contentId))));
                return recordMap;
            }
            return Collections.emptyMap();
        }
        return Collections.emptyMap();
    }
}
