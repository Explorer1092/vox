package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.dao.mongo.AgentAppSuggestDao;
import com.voxlearning.utopia.agent.persist.entity.AgentAppSuggest;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 天玑建议
 * Created by yaguang.wang on 2016/8/4.
 */
@Named
@Deprecated
public class AgentAppSuggestService {
    @Inject
    private AgentAppSuggestDao agentAppSuggestDao;
    @Inject
    private BaseOrgService baseOrgService;

    //查询时间
    public List<AgentAppSuggest> loadByTime(Date startDate, Date endDate) {
        List<AgentAppSuggest> all = agentAppSuggestDao.findAll();
        return all.stream().filter(p -> endDate == null || p.getCreateTime().before(endDate)).filter(p -> startDate == null || p.getCreateTime().after(startDate)).collect(Collectors.toList());
    }

    public MapMessage saveSuggest(AuthCurrentUser user, String content) {
        if (user == null || user.getUserId() == null) {
            return MapMessage.errorMessage("用户信息错误,请重新登录后重试");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("请填写建议内容");
        }
        if (content.length() > 500) {
            return MapMessage.errorMessage("内容超过500个字,建议拆成2个提交");
        }
        Long userId = user.getUserId();
        List<AgentAppSuggest> suggests = agentAppSuggestDao.findByDate(DateUtils.getTodayStart(), DateUtils.getTodayEnd());
        suggests = suggests.stream().filter(p -> Objects.equals(p.getProponentId(), user.getUserId())).collect(Collectors.toList());
        if (suggests.size() > 5) {
            return MapMessage.errorMessage("您今天已经提了很多的建议，我们会尽快处理，如果您的建议非常紧急,请联系您的上级主管");
        }
        String regionGroupName = null;
        String departmentName = null;
        Long departmentId = null;
        Long regionGroupId = null;
        List<AgentGroup> groups = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(groups)) {
            if (user.isBusinessDeveloper() || user.isCityManager()) {
                departmentId = groups.get(0).getId();
                departmentName = groups.get(0).getGroupName();
                AgentGroup group = baseOrgService.getParentGroup(departmentId);
                regionGroupId = group.getId();
                regionGroupName = group.getGroupName();
            } else {
                regionGroupName = groups.get(0).getGroupName();
                regionGroupId = groups.get(0).getId();
            }
        }
        AgentAppSuggest suggest = new AgentAppSuggest();
        suggest.setProponentId(user.getUserId());
        suggest.setProponentName(user.getRealName());
        suggest.setSuggestContent(content);
        suggest.setRegionGroupName(regionGroupName);
        suggest.setRegionGroupId(regionGroupId);
        suggest.setDepartmentName(departmentName);
        suggest.setDepartmentId(departmentId);
        agentAppSuggestDao.insert(suggest);
        return MapMessage.successMessage();
    }
}
