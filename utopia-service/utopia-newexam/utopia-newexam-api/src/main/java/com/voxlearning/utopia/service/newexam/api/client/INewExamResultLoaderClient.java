package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;

import java.util.Collection;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
public interface INewExamResultLoaderClient extends IPingable {
    /**
     * 获取本次考试学生结果
     *
     * @param newExamId 考试id
     * @param userId     用户id
     * @return NewExamResult
     */
    NewExamResult loadNewExamResult(String newExamId, Long userId);

    /**
     * 获取本次考试所有学生结果
     * @param newExamId 考试id
     * @param userIds 用户id列表
     * @return
     */
    Map<Long, NewExamResult> loadNewExamResult(String newExamId, Collection<Long> userIds);

    /**
     * 根据ids获取获取考试结果
     */
    Map<String, NewExamResult> loadNewExamResults(Collection<String> newExamResultIds);

}
