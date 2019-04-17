package com.voxlearning.utopia.agent.mockexam.dao;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 考卷持久层接口
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Repository
public interface ExamPaperDao {

    /**
     * 新增
     *
     * @param entity 实体
     */
    void insert(ExamPaperEntity entity);

    /**
     * 更新
     *
     * @param entity 实体
     */
    void update(ExamPaperEntity entity);

    /**
     * 根据id查询
     *
     * @param id 主键
     * @return 实体
     */
    ExamPaperEntity findById(Long id);

    /**
     * 根据试卷id查询
     *
     * @param paperId 试卷id
     * @return 实体
     */
    ExamPaperEntity findByPaperId(String paperId);

    /**
     * 根据试卷id查询
     *
     * @param paperIds 试卷id集合
     * @return 实体
     */
    Map<String, ExamPaperEntity> findByPaperId(Set<String> paperIds);

    /**
     * 查询总数
     *
     * @param params 查询参数
     * @return 总数
     */
    long count(ExamPaperQueryParams params);

    /**
     * 查询
     *
     * @param params 查询条件
     * @return 一页数据
     */
    List<ExamPaperEntity> query(ExamPaperQueryParams params);

    /**
     * 分页查询
     *
     * @param params   查询条件
     * @param pageInfo 分页信息
     * @return 一页数据
     */
    List<ExamPaperEntity> query(ExamPaperQueryParams params, PageInfo pageInfo);

    /**
     * 查询所有
     *
     * @return
     */
    List<ExamPaperEntity> queryAll();

}
