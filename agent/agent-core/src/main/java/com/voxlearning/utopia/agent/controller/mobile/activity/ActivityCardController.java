package com.voxlearning.utopia.agent.controller.mobile.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.activity.ActivityCardRedeemCodeService;
import com.voxlearning.utopia.agent.service.activity.ActivityCardService;
import com.voxlearning.utopia.agent.service.activity.AgentActivityService;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.view.activity.ActivityCardRecordView;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 礼品卡发送
 */

@Controller
@RequestMapping("/mobile/activity_card")
public class ActivityCardController extends AbstractAgentController {

    @Inject
    private ActivityCardService cardService;
    @Inject
    private AgentActivityService agentActivityService;
    @Inject
    private ActivityCardRedeemCodeService cardRedeemCodeService;
    @Inject
    private BaseExcelService baseExcelService;

    @ResponseBody
    @RequestMapping("give.vpage")
    public MapMessage giveCard(){

        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        String cardNo = getRequestString("cardNo");
        Long cardUserId = getRequestLong("cardUserId");
        Long cardTime = getRequestLong("cardTime");
        Date cardDate = new Date(cardTime);
        return cardService.addCardRecord(activityId, cardNo, cardUserId, cardDate, getCurrentUserId());
    }

    @RequestMapping(value = "importCardAndredeemCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importLogisticExcel() {
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceFile");
        return cardRedeemCodeService.cvExcel2ActivityCardRedeemCode(workbook);
    }

    @ResponseBody
    @RequestMapping("record_list.vpage")
    public MapMessage recordList(){
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        Long userId = getRequestLong("userId");
        if(userId < 1){
            userId = getCurrentUserId();
        }

        List<ActivityCardRecordView> recordList =  cardService.getRecordList(activityId, userId);
        Map<Integer, List<ActivityCardRecordView>> dayDataMap = recordList.stream().collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getBusinessTime(), "yyyyMMdd"))));
        Map<String, List<ActivityCardRecordView>> dataMap = new LinkedHashMap<>();
        if(MapUtils.isNotEmpty(dayDataMap)) {
            List<Integer> dayList = dayDataMap.keySet().stream().sorted((o1, o2) -> (o2 - o1)).collect(Collectors.toList());
            for (Integer day : dayList) {
                String key = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyy-MM-dd");
                List<ActivityCardRecordView> dataList = dayDataMap.get(day);
                dataList.sort((o1, o2) -> o2.getBusinessTime().compareTo(o1.getBusinessTime()));
                dataMap.put(key, dataList);
            }
        }

        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    @RequestMapping(value = "parent_count.vpage")
    @ResponseBody
    public MapMessage getParentCount() {
        String activityId = getRequestString("activityId");
        if(agentActivityService.getActivity(activityId) == null){
            return MapMessage.errorMessage("不存在该活动！");
        }

        MapMessage message = MapMessage.successMessage();
        Map<String, Object> dataMap = cardService.getParentCountData(activityId, getCurrentUserId());
        message.putAll(dataMap);
        return message;
    }


}
