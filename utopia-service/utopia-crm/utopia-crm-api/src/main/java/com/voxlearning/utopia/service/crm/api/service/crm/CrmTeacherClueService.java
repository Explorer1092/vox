package com.voxlearning.utopia.service.crm.api.service.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * CrmTeacherClueService
 *
 * @author song.wang
 * @date 2017/12/6
 */
@ServiceVersion(version = "20171206")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmTeacherClueService  extends IPingable {

    void inserts(Collection<CrmTeacherClue> teacherClues);

    CrmTeacherClue replace(CrmTeacherClue teacherClue);
}
