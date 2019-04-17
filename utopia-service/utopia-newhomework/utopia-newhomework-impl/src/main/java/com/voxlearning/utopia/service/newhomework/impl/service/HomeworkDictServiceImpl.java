package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkStudentAuthDict;
import com.voxlearning.utopia.service.newhomework.api.service.HomeworkDictService;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkDictPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkStudentAuthDictPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/15
 */
@Named
@Service(interfaceClass = HomeworkDictService.class)
@ExposeService(interfaceClass = HomeworkDictService.class)
public class HomeworkDictServiceImpl implements HomeworkDictService {

    @Inject private HomeworkDictPersistence homeworkDictPersistence;
    @Inject private HomeworkStudentAuthDictPersistence homeworkStudentAuthDictPersistence;

    @Override
    public List<HomeworkDict> fetchHomeworkDictList() {
        return homeworkDictPersistence.fetchHomeworkDictList();
    }

    @Override
    public HomeworkDict findHomeworkDict(String id) {
        return homeworkDictPersistence.load(id);
    }

    @Override
    public MapMessage deleteHomeworkDict(String id) {
        homeworkDictPersistence.remove(id);
        return MapMessage.successMessage("");
    }

    @Override
    public MapMessage upsertHomeworkDict(HomeworkDict dict) {
        homeworkDictPersistence.upsert(dict);
        return MapMessage.successMessage();
    }

    @Override
    public List<HomeworkStudentAuthDict> fetchHomeworkStudentAuthDictList() {
        return homeworkStudentAuthDictPersistence.fetchHomeworkStudentAuthDictList();
    }

    @Override
    public HomeworkStudentAuthDict findHomeworkStudentAuthDict(Long id) {
        return homeworkStudentAuthDictPersistence.load(id);
    }

    @Override
    public MapMessage deleteHomeworkStudentAuthDict(Long id) {
        homeworkStudentAuthDictPersistence.remove(id);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertHomeworkStudentAuthDict(HomeworkStudentAuthDict dict) {
        if (dict.getId() == null) {
            homeworkStudentAuthDictPersistence.insert(dict);
        } else {
            homeworkStudentAuthDictPersistence.upsert(dict);
        }
        return MapMessage.successMessage();
    }
}
