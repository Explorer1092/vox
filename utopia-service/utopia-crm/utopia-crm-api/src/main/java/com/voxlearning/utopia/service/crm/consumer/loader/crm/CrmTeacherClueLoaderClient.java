package com.voxlearning.utopia.service.crm.consumer.loader.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmTeacherClueLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * CrmTeacherClueLoaderClient
 *
 * @author song.wang
 * @date 2017/12/6
 */
public class CrmTeacherClueLoaderClient implements CrmTeacherClueLoader {

    @ImportService(interfaceClass = CrmTeacherClueLoader.class)
    private CrmTeacherClueLoader remoteReference;

    @Override
    public List<CrmTeacherClue> findBySchoolId(Long schoolId, CrmClueType type, Date startDate, Date endDate) {
        return remoteReference.findBySchoolId(schoolId, type, startDate, endDate);
    }

    @Override
    public List<CrmTeacherClue> findByTeacherIds(Collection<Long> teacherIds, CrmClueType type, Date startDate, Date endDate) {
        return remoteReference.findByTeacherIds(teacherIds, type, startDate, endDate);
    }

    @Override
    public List<CrmTeacherClue> findByType(CrmClueType type, Date startDate, Date endDate) {
        return remoteReference.findByType(type, startDate, endDate);
    }
}
