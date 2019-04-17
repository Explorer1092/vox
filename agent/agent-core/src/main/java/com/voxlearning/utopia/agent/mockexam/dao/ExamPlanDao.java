package com.voxlearning.utopia.agent.mockexam.dao;

import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanQueryParams;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;

import javax.inject.Named;
import java.util.List;

/**
 * 考试计划持久层接口
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Named
public interface ExamPlanDao {

    void insert(ExamPlanEntity entity);

    void update(ExamPlanEntity entity);

    /**
     * 查测评详情
     * @param planId 测评id
     * @return 测评详情
     */
    ExamPlanEntity findById(Long planId);

    /**
     * 查数量
     *
     * @param params 查询参数
     * @return 总记录数
     */
    long count(ExamPlanQueryParams params);

    /**
     * 分页查询
     * @param params 查询参数
     * @param pageInfo 分页信息
     * @return 测评分页记录
     */
    List<ExamPlanEntity> query(ExamPlanQueryParams params, PageInfo pageInfo);

    /**
     * 查询所有
     * @param params
     * @return
     */
    List<ExamPlanEntity> query(ExamPlanQueryParams params);

    /**
     * 查询所有
     * @return
     */
    List<ExamPlanEntity> queryAll();
}
