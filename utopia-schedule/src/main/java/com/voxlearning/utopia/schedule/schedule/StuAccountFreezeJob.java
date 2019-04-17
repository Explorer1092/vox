package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.consumer.StudentServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fugui
 * @since 2016/8/22
 *
 */
@Named
@ScheduledJobDefinition(
        jobName = "冻结学生",
        jobDescription = "分批冻结学生,手工执行",
        disabled = {Mode.UNIT_TEST,Mode.STAGING},
        cronExpression = "0 0 11 30 7 ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class StuAccountFreezeJob extends ScheduledJobWithJournalSupport {

    @Inject private StudentServiceClient studentServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Long> studentIds = new ArrayList<>();
        String begin = parameters.get("begin") != null ? parameters.get("begin").toString() : null;
        String end = parameters.get("end") != null ? parameters.get("end").toString() : null;
        Integer beginIndex = Integer.parseInt(begin);
        Integer endIndex ;


//        InputStream is = getClass().getResourceAsStream("/stuneedfreeze20160817.txt");
        InputStream is = getClass().getResourceAsStream("/stuneedfreeze20160908.txt"); //redmine#31815  历史遗留账号冻结处理
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String sId = bufferedReader.readLine();
        while (sId!=null){
            studentIds.add(Long.parseLong(sId));
            sId=bufferedReader.readLine();
        }
        bufferedReader.close();

        if(end == null){
            endIndex = studentIds.size();
        }else {
            endIndex = Integer.parseInt(end);
        }
        if(endIndex > studentIds.size()){
            endIndex = studentIds.size();
        }
        progressMonitor.worked(3);


        ISimpleProgressMonitor monitor = progressMonitor.subTask(97, endIndex - beginIndex);
        for(int index = beginIndex; index <endIndex ; index ++){
            Long tempStudentId = studentIds.get(index);
//            studentServiceClient.freezeStudent(tempStudentId,false);//解除冻结
            studentServiceClient.freezeStudent(tempStudentId,true);//进行冻结
            monitor.worked(1);
        }
    }

}
