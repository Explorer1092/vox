package com.voxlearning.utopia.agent.service.indicator.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.support.offline.ChannelOfflineIndicator;
import com.voxlearning.utopia.agent.service.indicator.support.offline.MarketOfflineIndicator;
import com.voxlearning.utopia.agent.service.indicator.support.offline.OfflineIndicatorFactory;
import com.voxlearning.utopia.agent.service.indicator.support.online.ChannelOnlineIndicator;
import com.voxlearning.utopia.agent.service.indicator.support.online.MarketOnlineIndicator;
import com.voxlearning.utopia.agent.service.indicator.support.online.OnlineIndicatorFactory;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * IndicatorFactorySelector
 *
 * @author song.wang
 * @date 2018/11/7
 */
@Named
public class IndicatorFactorySelector {

    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    private AgentGroupSupport agentGroupSupport;

    @Inject
    private MarketOnlineIndicator marketOnlineIndicator;
    @Inject
    private ChannelOnlineIndicator channelOnlineIndicator;

    @Inject
    private MarketOfflineIndicator marketOfflineIndicator;
    @Inject
    private ChannelOfflineIndicator channelOfflineIndicator;



    public OnlineIndicatorFactory fetchOnlineIndicatorFactory(Long id, Integer dataType){
        return fetchOnlineIndicatorFactory(getSelectorMode(id, dataType));
    }

    public OnlineIndicatorFactory fetchOnlineIndicatorFactory(Integer selector){
        if(selector == 1){
            return marketOnlineIndicator;
        }else {
            return channelOnlineIndicator;
        }
    }

    public OfflineIndicatorFactory fetchOfflineIndicatorFactory(Long id, Integer dataType){
        return fetchOfflineIndicatorFactory(getSelectorMode(id, dataType));
    }

    public OfflineIndicatorFactory fetchOfflineIndicatorFactory(Integer selector){
        if(selector == 1){
            return marketOfflineIndicator;
        }else {
            return channelOfflineIndicator;
        }
    }

    // 1: 市场   2：渠道
    public Integer getSelectorMode(Long id, Integer dataType){
        int selector = 1;
        Long groupId = null;
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP) || Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED)){
            groupId = id;
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                groupId = groupIdList.get(0);
            }
        }
        if(groupId != null) {

            AgentGroup group = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
            if(group != null){
                if(StringUtils.contains(group.getGroupName(), "业务")){
                    selector = 1;
                }else if(StringUtils.contains(group.getGroupName(), "渠道")){
                    selector = 2;
                }
            }else {
                GroupWithParent groupWithParent = agentGroupSupport.generateGroupWithParent(groupId);
                while(groupWithParent != null){
                    if(StringUtils.contains(groupWithParent.getGroupName(), "渠道")){
                        selector = 2;
                        break;
                    }
                    groupWithParent = groupWithParent.getParent();
                }
            }
        }
        return selector;
    }
}
