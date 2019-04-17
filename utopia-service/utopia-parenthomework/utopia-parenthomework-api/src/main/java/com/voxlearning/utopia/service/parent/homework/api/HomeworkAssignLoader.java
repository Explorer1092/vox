package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 布置作业查询接口
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@ServiceVersion(version = "20190111")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface HomeworkAssignLoader {

    /**
     * 获取教材列表
     * @param subject 科目
     * @param userId 用户id
     * @param clazzLevel 年级
     * @param regionCode 区域id
     * @return
     */
    MapMessage loadBooks(String subject, Long userId, Integer clazzLevel, Integer regionCode, String bizType);

    /**
     * 获取题包
     *
     * @param param 参数
     * @return
     */
    MapMessage loadQuestionBoxes(HomeworkParam param);

    /**
     * 获取学生的基本信息
     *
     * @param studentId 学生id
     * @return
     */
    StudentInfo loadStudentInfo(Long studentId);

    /**
     * 获取题的docId
     * @param boxIds 题包id
     * @param bizType 业务类型
     * @return
     */
    List<String> loadQuestionDocIdByBoxId(Collection<String> boxIds, String bizType);

}
