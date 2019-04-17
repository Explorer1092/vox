package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherWorkCommentService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherCommentCourse;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherWorkComment;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 作业点评数据修复
 * @author xuerui.zhang
 * @since 2018/8/20 下午5:12
 */
@Named
@ScheduledJobDefinition(
        jobName = "17学作业点评数据修复",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0 2 * * ? "
)
@ProgressTotalWork(100)
public class AutoFixWorkCommentDataJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = CrmStudyTogetherWorkCommentService.class)
    private CrmStudyTogetherWorkCommentService workCommentService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp, Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        Pageable pageRequest = new PageRequest(0, 100);
        Page<StudyTogetherWorkComment> workComments = workCommentService.queryAllCommentData(pageRequest);
        progressMonitor.worked(10);
        handler(workComments.getContent());
        while (workComments.hasNext()) {
            pageRequest = workComments.nextPageable();
            workComments = workCommentService.queryAllCommentData(pageRequest);
            handler(workComments.getContent());
            progressMonitor.worked(10);
        }
        progressMonitor.done();
    }

    private void handler(List<StudyTogetherWorkComment> content) {
        for (StudyTogetherWorkComment bean : content) {
            String courseId = bean.getCourseId();
            if (StringUtils.isBlank(courseId)) {
                continue;
            }
            StudyTogetherCommentCourse commentCourse = workCommentService.loadCommentCourse(courseId);
            if (null == commentCourse) {
                continue;
            }
            String productId = commentCourse.getProductId();
            Boolean isBuy = bean.getIsBuy();
            if (!isBuy) {
                bean.setBuyType(-1);
            } else if (StringUtils.isBlank(productId)){
                bean.setBuyType(0);
            } else {
                bean.setBuyType(1);
            }
            workCommentService.$updateWorkComment(bean);
        }

    }
}
