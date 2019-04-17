package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.dubbing.api.DubbingRawService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingRaw;
import com.voxlearning.utopia.service.email.api.EmailService;
import com.voxlearning.utopia.service.email.api.client.PlainEmailCreator;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/12
 */
@Named
@ScheduledJobDefinition(
        jobName = "为教研导出配音素材数据",
        jobDescription = "手动运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 0  * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoExportDubbingRawJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = DubbingRawService.class)
    private DubbingRawService dubbingRawService;
    @ImportService(interfaceClass = EmailService.class)
    private EmailService emailService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String head = "配音ID" + "~" + "配音名称" + "~" + "配音地址" + "~" + "配音字幕源地址" + "\n";
        PlainEmailCreator plainEmailCreator = new PlainEmailCreator(emailService);
        plainEmailCreator.cc("chao.zhu@17zuoye.com");
        plainEmailCreator.to("chao.zhu@17zuoye.com");
        plainEmailCreator.subject("配音素材数据");
        String body = "";

        List<DubbingRaw> dubbingRaws = dubbingRawService.exportDubbingRaw().stream().filter(e -> e.getFinishSync() == null && e.getIsSync()).collect(Collectors.toList());
        for (DubbingRaw dubbing : dubbingRaws) {
            body += dubbing.getId() + "~" + dubbing.getVideoName() + "~" + dubbing.getVideoUrl() + "~" + dubbing.getSrtUrl() + "\n";
        }
        String finalBody = head + body;
        plainEmailCreator.body(finalBody);
        plainEmailCreator.send();
    }
}
