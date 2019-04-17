package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.IOUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import org.jsoup.helper.Validate;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 补发UserTag给大数据
 * Created by haitaian.gan on 2017/9/25.
 */
@Named
@ScheduledJobDefinition(
        jobName = "补发UserTag给大数据",
        jobDescription = "补发UserTag给大数据",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 20 23 1 1 ?",
        ENABLED = false
)
public class SupplyUserTagAgainJob extends ScheduledJobWithJournalSupport {

    @Inject private UserManagementClient userManagementClient;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String url = "http://v.17zuoye.cn/Prize/chanpin/idList.txt";
        GET downloadSrcRequest = HttpRequestExecutor.defaultInstance().get(url);
        AlpsHttpResponse response = downloadSrcRequest.execute();
        if(response.getStatusCode() != 200){
            logger.error("UserTag:get source file from oss error!");
            return;
        }

        byte[] orgData = response.getOriginalResponse();
        InputStream inputStream = new ByteArrayInputStream(orgData);

        List<String> idsStr = IOUtils.readLines(inputStream,"UTF-8");
        List<Long> ids = idsStr.stream().map(SafeConverter::toLong).collect(Collectors.toList());

        // 100个为一组，中间睡200
        Set<Long> tmpIds = new HashSet<>();
        int index = 1;
        while(index <= ids.size()){
            tmpIds.add(ids.get(index - 1));
            if(index % 100 == 0){
                MapMessage result = userManagementClient.sendTagMessage(tmpIds);
                if(!result.isSuccess()){
                    logger.error("UserTag:supply user tag again error!,detail:{}",result.getInfo());
                    return;
                }
                tmpIds.clear();

                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    // ignore
                }
            }

            if(index % 10000 == 0)
                logger.info("UserTag:supply user tag again in progress,number:{}",index);

            index ++;
        }
    }

}
