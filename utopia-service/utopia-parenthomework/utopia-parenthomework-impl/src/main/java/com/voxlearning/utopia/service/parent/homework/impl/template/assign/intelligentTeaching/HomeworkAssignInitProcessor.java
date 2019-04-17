package com.voxlearning.utopia.service.parent.homework.impl.template.assign.intelligentTeaching;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * 讲练测布置布置作业初始化
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Named("IntelliagentTeaching.HomeworkAssignInitProcessor")
public class HomeworkAssignInitProcessor implements HomeworkProcessor {
    @Inject
    private HomeworkAssignLoader assignHomeworkService;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        // 学生基本信息
        StudentInfo studentInfo = assignHomeworkService.loadStudentInfo(studentId);
        if (studentInfo == null) {
            hc.setMapMessage(MapMessage.errorMessage("学生信息有误"));
            LoggerUtils.info("assignHomework.error", "no student", param);
            return;
        }
        hc.setStudentInfo(studentInfo);
        List<String> boxIds = ObjectUtils.get(() -> (List<String>)(param.getData().get("boxIds")));
        if (CollectionUtils.isEmpty(boxIds)) {
            hc.setMapMessage(MapMessage.errorMessage("无题"));
            LoggerUtils.info("assignHomework.error", "no boxIds", param);
            return;
        }

        QuestionPackage questionPackage = HomeWorkCache.load(CacheKey.BOX, boxIds.get(0));
        if (questionPackage == null) {
            hc.setMapMessage(MapMessage.errorMessage("题包缺失"));
            LoggerUtils.info("assignHomework.error", "no questionPackages", param);
            return;
        }
        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(questionPackage.getUnitId());
        param.setBookId(newBookCatalog.getParentId());
        hc.setUnitId(questionPackage.getUnitId());
        hc.setQuestionPackages(Collections.singletonList(questionPackage));
    }
}
