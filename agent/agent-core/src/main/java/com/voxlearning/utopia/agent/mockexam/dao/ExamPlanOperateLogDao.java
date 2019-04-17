package com.voxlearning.utopia.agent.mockexam.dao;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanOperateLogEntity;

import java.util.List;

/**
 * 测评状态持久层接口
 *
 * @Author: peng.zhang
 * @Date: 2018/8/9 15:40
 */
public interface ExamPlanOperateLogDao {


    /**
     * 新增
     *
     * @param entity
     */
    void insert(ExamPlanOperateLogEntity entity);

    /**
     * 查询
     *
     * @param planId
     * @return
     */
    List<ExamPlanOperateLogEntity> findByPlanId(Long planId);
}
