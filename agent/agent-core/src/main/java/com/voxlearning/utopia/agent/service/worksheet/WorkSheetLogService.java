/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 18:40
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.service.worksheet;

import com.voxlearning.utopia.agent.dao.mongo.worksheet.WorkSheetLogDao;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheetLog;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class WorkSheetLogService  extends AbstractAgentService {
    @Inject private WorkSheetLogDao workSheetLogDao;

    public void save(WorkSheetLog workSheetLog){
        workSheetLogDao.insert(workSheetLog);
    }
}
