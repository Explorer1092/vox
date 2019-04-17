package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkStudentAuthDict;
import com.voxlearning.utopia.service.newhomework.api.service.HomeworkDictService;
import lombok.Getter;

import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/15
 */
public class HomeworkDictServiceClient implements HomeworkDictService {

    @Getter
    @ImportService(interfaceClass = HomeworkDictService.class)
    private HomeworkDictService remoteReference;

    @Override
    public List<HomeworkDict> fetchHomeworkDictList() {
        return remoteReference.fetchHomeworkDictList();
    }

    @Override
    public HomeworkDict findHomeworkDict(String id) {
        return remoteReference.findHomeworkDict(id);
    }

    @Override
    public MapMessage deleteHomeworkDict(String id) {
        return remoteReference.deleteHomeworkDict(id);
    }

    @Override
    public MapMessage upsertHomeworkDict(HomeworkDict dict) {
        return remoteReference.upsertHomeworkDict(dict);
    }

    @Override
    public List<HomeworkStudentAuthDict> fetchHomeworkStudentAuthDictList() {
        return remoteReference.fetchHomeworkStudentAuthDictList();
    }

    @Override
    public HomeworkStudentAuthDict findHomeworkStudentAuthDict(Long id) {
        return remoteReference.findHomeworkStudentAuthDict(id);
    }

    @Override
    public MapMessage deleteHomeworkStudentAuthDict(Long id) {
        return remoteReference.deleteHomeworkStudentAuthDict(id);
    }

    @Override
    public MapMessage upsertHomeworkStudentAuthDict(HomeworkStudentAuthDict dict) {
        return remoteReference.upsertHomeworkStudentAuthDict(dict);
    }
}
