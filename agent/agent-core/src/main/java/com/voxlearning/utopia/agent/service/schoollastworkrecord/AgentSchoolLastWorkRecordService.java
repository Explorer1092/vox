package com.voxlearning.utopia.agent.service.schoollastworkrecord;

import com.voxlearning.utopia.agent.dao.mongo.schoollastworkrecord.AgentSchoolLastWorkRecordDao;
import com.voxlearning.utopia.agent.persist.entity.schoollastworkrecord.AgentSchoolLastWorkRecord;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 学校与最近一次工作记录
 */
@Named
public class AgentSchoolLastWorkRecordService {
    @Inject
    private AgentSchoolLastWorkRecordDao agentSchoolLastWorkRecordDao;

    public Map<Long,AgentSchoolLastWorkRecord> getLastVisitTimeBySchoolIds(Collection<Long> schoolIds){
        return agentSchoolLastWorkRecordDao.loadBySchoolIds(schoolIds);
    }

    public void updateLastVisitTime(String workRecordId,Long schoolId,Long userId,String userName){
        AgentSchoolLastWorkRecord schoolLastWorkRecord = agentSchoolLastWorkRecordDao.loadBySchoolId(schoolId);
        if (schoolLastWorkRecord != null){
            schoolLastWorkRecord.setWorkRecordId(workRecordId);
            schoolLastWorkRecord.setLastVisitTime(new Date());
            schoolLastWorkRecord.setUserId(userId);
            schoolLastWorkRecord.setUserName(userName);
            agentSchoolLastWorkRecordDao.upsert(schoolLastWorkRecord);
        }else {
            schoolLastWorkRecord = new AgentSchoolLastWorkRecord();
            schoolLastWorkRecord.setWorkRecordId(workRecordId);
            schoolLastWorkRecord.setSchoolId(schoolId);
            schoolLastWorkRecord.setLastVisitTime(new Date());
            schoolLastWorkRecord.setUserId(userId);
            schoolLastWorkRecord.setUserName(userName);
            agentSchoolLastWorkRecordDao.insert(schoolLastWorkRecord);
        }
    }
}
