package com.voxlearning.utopia.agent.mockexam.dao.support;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanQueryParams;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status.*;

/**
 * 考试计划持久层mysql实现
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Repository
public class ExamPlanDaoJdbcImpl extends AlpsStaticJdbcDao<ExamPlanEntity, Long> implements ExamPlanDao {

    @Override
    protected void calculateCacheDimensions(ExamPlanEntity examPlanEntity, Collection<String> collection) {
    }

    @Override
    public void insert(ExamPlanEntity entity) {
        super.insert(entity);
    }

    @Override
    public void update(ExamPlanEntity entity) {
        replace(entity);
    }

    @Override
    @CacheMethod
    public ExamPlanEntity findById(Long planId) {
        Criteria criteria = Criteria.where("ID").is(planId);
        List<ExamPlanEntity> rs = query(Query.query(criteria));
        return rs.stream().findFirst().orElse(null);
    }

    @Override
    public long count(ExamPlanQueryParams params) {
        Query query = buildQuery(params);
        return count(query);
    }

    @Override
    public List<ExamPlanEntity> query(ExamPlanQueryParams params, PageInfo pageInfo) {
        Query query = buildQuery(params);
        if (pageInfo != null) {
            query.limit(pageInfo.getSize()).skip((pageInfo.getPage()) * (pageInfo.getSize()));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        query.with(sort);
        return query(query);
    }

    @Override
    public List<ExamPlanEntity> query(ExamPlanQueryParams params) {
        Query query = buildQuery(params);
        return query(query);
    }

    @Override
    public List<ExamPlanEntity> queryAll() {
        return query();
    }

    /**
     * 构建谓词
     *
     * @param params 查询条件
     * @return 谓词
     */
    static Query buildQuery(ExamPlanQueryParams params) {

        if (params == null)
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, "查询条件为空");

        // 基本筛选条件
        Criteria c1 = new Criteria();
        if (StringUtils.isNotBlank(params.getSubject()))
            c1.and("SUBJECT").is(params.getSubject());
        if (StringUtils.isNotBlank(params.getForm()))
            c1.and("FORM").is(params.getForm());
        if (StringUtils.isNotBlank(params.getType()))
            c1.and("REGION_LEVEL").is(params.getType());
        if (StringUtils.isNotBlank(params.getGrade()))
            c1.and("GRADE").is(params.getGrade());

        Date createStartTime = params.getCreateStartTime();
        Date createEndTime = params.getCreateEndTime();
        if (null != createStartTime || null != createEndTime) {
            if (null != createStartTime && null != createEndTime)
                c1.and("CREATE_DATETIME").gte(createStartTime).lte(createEndTime);
            else if (null != createStartTime)
                c1.and("CREATE_DATETIME").gte(createStartTime);
            else
                c1.and("CREATE_DATETIME").lte(createEndTime);
        }

        Date planStartTime = params.getPlanStartTime();
        Date planEndTime = params.getPlanEndTime();
        if (null != planStartTime || null != planEndTime) {
            if (null != planStartTime && null != planEndTime)
                c1.and("START_TIME").gte(planStartTime).lte(planEndTime);
            else if (null != planStartTime)
                c1.and("START_TIME").gte(planStartTime);
            else
                c1.and("START_TIME").lte(planEndTime);
        }

        Date deadlineStartTime = params.getDeadlineStartTime();
        Date deadlineEndTime = params.getDeadlineEndTime();
        if (null != deadlineStartTime || null != deadlineEndTime) {
            if (null != deadlineStartTime && null != deadlineEndTime)
                c1.and("END_TIME").gte(deadlineStartTime).lte(deadlineEndTime);
            else if (null != deadlineStartTime)
                c1.and("END_TIME").gte(deadlineStartTime);
            else
                c1.and("END_TIME").lte(deadlineEndTime);
        }

        if (null != params.getPlanId())
            c1.and("ID").is(params.getPlanId().toString());
        if (StringUtils.isNotBlank(params.getName())) {
            c1.and("NAME").like("%" + params.getName() + "%");
        }
        if (StringUtils.isNotBlank(params.getCreatorName())) {
            c1.and("CREATOR_NAME").like("%" + params.getCreatorName() + "%");
        }
        if (StringUtils.isNotBlank(params.getPaperId()))
            c1.and("PAPERS").like("%" + params.getPaperId() + "%");

        // 数据权限

        // admin或者全国总监
        boolean isAdmin = isAdmin(params.getCurrentUserRole());
        final Long creatorId = params.getCurrentUserId();
        Criteria c2 = new Criteria();
        if (StringUtils.isBlank(params.getStatus())) {
            // 未指定状态 => 状态为[修改中][被拒绝][已撤回]三个状态时，只能提交人可以查看
            if (isAdmin) {
                c2 = Criteria.or(
                        Criteria.where("STATUS").in(Arrays.asList(PLAN_REJECT, PLAN_AUDITING, PAPER_CHECKING, PAPER_REJECT,
                                PAPER_PROCESSING, PAPER_READY, EXAM_PUBLISHED, EXAM_OFFLINE)),
                        Criteria.where("STATUS").in(Arrays.asList(PLAN_WITHDRAW)).and("CREATOR_ID").is(creatorId)
                );
            } else {
                if(SafeConverter.toBoolean(params.getWithCreator())){
                    c2 = Criteria.where("CREATOR_ID").is(creatorId);
                }
            }
        } else {
            // 指定具体状态
            Status status = Status.valueOf(params.getStatus());
            if (isAdmin) {
                if (Arrays.asList(PLAN_WITHDRAW).contains(status)) {
                    c2 = Criteria.where("STATUS").is(status.name()).and("CREATOR_ID").is(creatorId);
                } else {
                    c2 = Criteria.where("STATUS").is(status.name());
                }
            } else {
                c2 = Criteria.where("STATUS").is(status.name());
                if(SafeConverter.toBoolean(params.getWithCreator())){
                    c2.and("CREATOR_ID").is(creatorId);
                }
            }

        }
        if (c1.export().isEmpty())
            return Query.query(c2);
        else if(c2.export().isEmpty())
            return Query.query(c1);
        else
            return Query.query(Criteria.and(c1, c2));
    }

    /**
     * 判断是否为管理员
     *
     * @param roles 角色列表
     * @return 是否为管理员
     */
    static boolean isAdmin(List<Integer> roles) {
        return roles.contains(AgentRoleType.Admin.getId())
                || roles.contains(AgentRoleType.Country.getId())
                || roles.contains(AgentRoleType.MOCK_EXAM_MANAGER.getId());
    }
}
