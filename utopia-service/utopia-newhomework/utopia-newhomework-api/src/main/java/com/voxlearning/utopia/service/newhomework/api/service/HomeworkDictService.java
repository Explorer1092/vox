package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkStudentAuthDict;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/15
 */
@ServiceVersion(version = "20190115")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface HomeworkDictService extends IPingable {

    List<HomeworkDict> fetchHomeworkDictList();

    HomeworkDict findHomeworkDict(String id);

    MapMessage deleteHomeworkDict(String id);

    MapMessage upsertHomeworkDict(HomeworkDict dict);

    List<HomeworkStudentAuthDict> fetchHomeworkStudentAuthDictList();

    HomeworkStudentAuthDict findHomeworkStudentAuthDict(Long id);

    MapMessage deleteHomeworkStudentAuthDict(Long id);

    MapMessage upsertHomeworkStudentAuthDict(HomeworkStudentAuthDict dict);
}
