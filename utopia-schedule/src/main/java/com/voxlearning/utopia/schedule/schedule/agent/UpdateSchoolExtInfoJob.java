package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.data.ChipsRank;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yaguang.wang
 * on 2017/8/31.
 */
@Named
@ScheduledJobDefinition(
        jobName = "更新schoolExtInfo的schoolSize",
        jobDescription = "重新计算schoolExtInfo的schoolSize",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 10 * * ?",
        ENABLED = true
)
public class UpdateSchoolExtInfoJob extends ScheduledJobWithJournalSupport {

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }



    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {


        Set<Long> schoolIds = new HashSet<>();
        if(parameters.containsKey("ids")){
            String ids = SafeConverter.toString(parameters.get("ids"), "");
            String[] idArr = StringUtils.split(ids, ",");
            for(String idStr : idArr){
                Long schoolId = SafeConverter.toLong(idStr);
                if(schoolId > 0){
                    schoolIds.add(schoolId);
                }
            }
        }else {
            String sql = "SELECT DISTINCT(ID) FROM VOX_SCHOOL WHERE DISABLED = false";
            utopiaSql.withSql(sql).queryAll((rs, rowNum) -> {
                schoolIds.add(rs.getLong("ID"));
                return null;
            });
        }


        Integer count = 0;
        for(Long schoolId : schoolIds){
            Map<String, Object> command = new HashMap<>();
            command.put("command", "update_school_ext_info");
            command.put("schoolId", schoolId);
            Message message = Message.newMessage();
            message.withStringBody(JsonUtils.toJson(command));
            agentCommandQueueProducer.getProducer().produce(message);

            count++;
            if(count % 100 == 0){
                Thread.sleep(100);
            }
        }
        progressMonitor.done();
    }
}
