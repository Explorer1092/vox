package com.voxlearning.utopia.service.crm.impl.loader.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmTeacherClueLoader;
import com.voxlearning.utopia.service.crm.impl.dao.crm.CrmTeacherClueDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * CrmTeacherClueLoaderImpl
 *
 * @author song.wang
 * @date 2017/12/6
 */
@Named
@Service(interfaceClass = CrmTeacherClueLoader.class)
@ExposeService(interfaceClass = CrmTeacherClueLoader.class)
public class CrmTeacherClueLoaderImpl implements CrmTeacherClueLoader{

    @Inject private CrmTeacherClueDao crmTeacherClueDao;

    @Override
    public List<CrmTeacherClue> findBySchoolId(Long schoolId, CrmClueType type, Date startDate, Date endDate) {
        return crmTeacherClueDao.findBySchoolId(schoolId, type, startDate, endDate);
    }

    @Override
    public List<CrmTeacherClue> findByTeacherIds(Collection<Long> teacherIds, CrmClueType type, Date startDate, Date endDate) {
        return crmTeacherClueDao.findByTeacherIds(teacherIds, type, startDate, endDate);
    }

    @Override
    public List<CrmTeacherClue> findByType(CrmClueType type, Date startDate, Date endDate) {
        return crmTeacherClueDao.findByType(type, startDate, endDate);
    }
}
