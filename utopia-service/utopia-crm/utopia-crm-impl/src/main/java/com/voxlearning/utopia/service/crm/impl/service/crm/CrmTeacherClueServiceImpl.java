package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmTeacherClueService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.CrmTeacherClueDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * CrmTeacherClueServiceImpl
 *
 * @author song.wang
 * @date 2017/12/6
 */
@Named
@Service(interfaceClass = CrmTeacherClueService.class)
@ExposeService(interfaceClass = CrmTeacherClueService.class)
public class CrmTeacherClueServiceImpl implements CrmTeacherClueService {
    @Inject
    private CrmTeacherClueDao crmTeacherClueDao;

    @Override
    public void inserts(Collection<CrmTeacherClue> teacherClues) {
        if(CollectionUtils.isEmpty(teacherClues)){
            return;
        }
        crmTeacherClueDao.inserts(teacherClues);
    }

    @Override
    public CrmTeacherClue replace(CrmTeacherClue teacherClue) {
        if(teacherClue == null){
            return null;
        }
        return crmTeacherClueDao.replace(teacherClue);
    }
}
