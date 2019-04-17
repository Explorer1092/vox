package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20190315")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface NewTeacherResourceService {

    MapMessage loadSubjectClazzLevel(Long teacherId);

    MapMessage loadBookList(Long teacherId, Integer subjectId, Integer clazzLevelId, Integer levelTerm);

    MapMessage loadResource(Long teacherId, Integer subjectId, String bookId, Integer source, Integer page, Integer pageSize);

    VersionedBufferData<List<NewTeacherResourceWrapper>> loadNewTeacherResourceWrapperBufferData(long version);

    void resetNewTeacherResourceWrapperBuffer();

    NewTeacherResource loadDetailById(String id);

    MapMessage loadDetailMsgById(String id, Long teacherId);

    void incrReadCount(String id, Long incr);

    void incrCollectCount(String id, Long incr);

    void incrParticipateNum(String id, Long incr);

    void incrFinishNum(String id, Long incr);

    MapMessage collect(String id, Long teacherId);

    MapMessage disableCollect(String id, Long teacherId);

    MapMessage receiveResource(String id, Long teacherId);

    MapMessage shareParent(Long teacherId, String resourceId);

    Boolean getShareParent(Long teacherId, String resourceId);
}
