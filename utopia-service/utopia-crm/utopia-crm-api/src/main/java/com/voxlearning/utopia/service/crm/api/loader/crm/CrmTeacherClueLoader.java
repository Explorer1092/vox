package com.voxlearning.utopia.service.crm.api.loader.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CrmTeacherClueLoader
 *
 * @author song.wang
 * @date 2017/12/6
 */
@ServiceVersion(version = "20171206")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface CrmTeacherClueLoader extends IPingable {

    List<CrmTeacherClue> findBySchoolId(Long schoolId, CrmClueType type, Date startDate, Date endDate);

    List<CrmTeacherClue> findByTeacherIds(Collection<Long> teacherIds, CrmClueType type, Date startDate, Date endDate);

    List<CrmTeacherClue> findByType(CrmClueType type, Date startDate, Date endDate);
}
