package com.voxlearning.utopia.agent.controller.mobile.honeycomb;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombService;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombDataView;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombTargetUserCount;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombRankingView;
import com.voxlearning.utopia.agent.view.honeycomb.HoneycombTargetUserDetail;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/mobile/honeycomb")
public class MobileHoneycombController extends AbstractAgentController {

    @Inject
    private HoneycombService honeycombService;

    /**
     * 蜂巢订单柱状图
     * @return
     */
    @RequestMapping(value = "order_chart_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orderChartInfo() {

        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        Date date = getRequestDate("date", "yyyyMMdd");
        if(date == null){
            date = new Date();
        }else {
            Integer day = honeycombService.getBeforeDate(date, dateType);
            date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        }

        MapMessage message = MapMessage.successMessage();
        List<Map<String,Object>> dataList =  honeycombService.getChartData(date, dateType, getCurrentUserId());
        message.add("dataList", dataList);
        return message;
    }

    /**
     * 蜂巢数据统计
     * @return
     */
    @RequestMapping(value = "data_overview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dataStatistics() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月
        return MapMessage.successMessage().add("dataMap",honeycombService.dataOverview(date, dateType, getCurrentUserId()));
    }

    /**
     * 蜂巢数据明细
     * @return
     */
    @RequestMapping(value = "statistics_data_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage statisticsDataList(){
        String activityId = getRequestString("courseId");//课程ID

        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        Long id = getRequestLong("id",0);
        Integer idType = getRequestInt("idType",1);
        Integer dimension = getRequestInt("dimension",1);
        int sumType = getRequestInt("sumType", 1);     // 1：合计  2： 人均

        MapMessage mapMessage = MapMessage.successMessage();

        List<Long> groupIds = new ArrayList<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            groupIds.addAll(honeycombService.getSubGroupIds(id));
        }else if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(id);
            if (CollectionUtils.isNotEmpty(groupUserList)){
                groupIds.add(groupUserList.stream().map(AgentGroupUser::getGroupId).findFirst().orElse(null));
            }
        }
        List<HoneycombTargetUserCount> zeroOrderUserCountList = honeycombService.getZeroOrderUserCount(activityId, groupIds, date, dateType);
        Integer zeroOrderBdNum = zeroOrderUserCountList.stream().map(HoneycombTargetUserCount::getTargetUserCount).reduce(0, (a, b) -> a + b);
        mapMessage.put("zeroOrderBdNum",SafeConverter.toInt(zeroOrderBdNum));
        mapMessage.put("date",honeycombService.showFormatDate(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")),dateType));
        mapMessage.put("beforeDate",honeycombService.getBeforeDate(date,dateType));
        mapMessage.put("afterDate",honeycombService.getAfterDate(date,dateType));

        List<HoneycombDataView> dataList = honeycombService.statisticsDataList(activityId,date,dateType,id,idType,dimension);
        if(sumType == 1){
            mapMessage.put("dataList", dataList);
        }else if(sumType == 2) { // 人均的情况
            List<Map<String, Object>> avgDataList = honeycombService.calAvgDataList(dataList);
            return MapMessage.successMessage().add("dataList", avgDataList);
        }


        return mapMessage;
    }

    /**
     * 商品列表
     * @return
     */
    @RequestMapping(value = "product_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage productList(){
        int pageNo = getRequestInt("pageNo");
        int pageSize = getRequestInt("pageSize");
        return honeycombService.productList(pageNo,pageSize);
    }



    //排行榜
    @RequestMapping("ranking_list.vpage")
    @ResponseBody
    public MapMessage rankingList(){
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        int rankingType = getRequestInt("rankingType", 1);   // 1:专员榜   2：分区榜
        Integer topN = getRequestInt("topN", 10);
        boolean withAvatar = getRequestBool("withAvatar");

        List<HoneycombRankingView> dataList = honeycombService.getRankingList(date,  dateType, rankingType, topN);
        if(rankingType == 1){  // 专员排行时，补充部门信息
            dataList.forEach(p -> {
                List<AgentGroup> groupList = baseOrgService.getUserGroups(p.getId());
                if(CollectionUtils.isNotEmpty(groupList)){
                    AgentGroup group = groupList.get(0);
                    p.setGroupId(group.getId());
                    p.setGroupName(group.getGroupName());
                }
            });
        }

        String dateStr = honeycombService.showFormatDate(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")), dateType);

        if(rankingType == 1 && withAvatar){
            List<Map<String, Object>> rankingList = new ArrayList<>();
            dataList.forEach(p -> {
                Map<String, Object> data = BeanMapUtils.tansBean2Map(p);
                AgentUser user = baseOrgService.getUser(p.getId());
                data.put("avatar", user == null ? "" : user.getAvatar());
                rankingList.add(data);
            });
            return MapMessage.successMessage().add("dataList", rankingList).add("dateStr", dateStr);
        }else {
            return MapMessage.successMessage().add("dataList", dataList).add("dateStr", dateStr);
        }
    }


    // 零订单专员数
    @RequestMapping("target_user_count.vpage")
    @ResponseBody
    public MapMessage targetUserCount(){

        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        String activityId = getRequestString("activityId");

        Long groupId = getRequestLong("groupId");
        if(groupId < 1){
            List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(getCurrentUserId());
            if(CollectionUtils.isNotEmpty(managedGroupIds)){
                groupId = managedGroupIds.get(0);
            }
        }

        if(groupId < 1){
            return MapMessage.successMessage();
        }

        List<Long> groupIds = honeycombService.getSubGroupIds(groupId);
        List<HoneycombTargetUserCount> dataList = honeycombService.getZeroOrderUserCount(activityId, groupIds, date, dateType);
        return MapMessage.successMessage().add("dataList", dataList);
    }


    @RequestMapping("target_user_list.vpage")
    @ResponseBody
    public MapMessage getTargetUserList(){

        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月

        String activityId = getRequestString("activityId");

        Long groupId = getRequestLong("groupId");

        List<HoneycombTargetUserDetail> dataList = honeycombService.getZeroOrderUserList(activityId, groupId, date, dateType);
        return MapMessage.successMessage().add("dataList", dataList);
    }

    /**
     * 排名信息
     * @return
     */
    @RequestMapping("ranking_info.vpage")
    @ResponseBody
    public MapMessage rankingInfo(){
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Integer dateType = getRequestInt("dateType",1); // 1： 日  2：周  3：月
        return MapMessage.successMessage().add("ranking",honeycombService.rankingInfo(date,dateType,getCurrentUserId()));
    }

}
