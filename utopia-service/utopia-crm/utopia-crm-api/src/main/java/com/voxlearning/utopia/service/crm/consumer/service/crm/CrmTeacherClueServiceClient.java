package com.voxlearning.utopia.service.crm.consumer.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmTeacherClueService;

import java.util.Collection;

/**
 * CrmTeacherClueServiceClient
 *
 * @author song.wang
 * @date 2017/12/6
 */
public class CrmTeacherClueServiceClient implements CrmTeacherClueService {

    @ImportService(interfaceClass = CrmTeacherClueService.class)
    private CrmTeacherClueService remoteReference;

    @Override
    public void inserts(Collection<CrmTeacherClue> teacherClues) {
        if(CollectionUtils.isEmpty(teacherClues)){
            return;
        }
        remoteReference.inserts(teacherClues);
    }

    @Override
    public CrmTeacherClue replace(CrmTeacherClue teacherClue) {
        return remoteReference.replace(teacherClue);
    }
}
