/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 18:42
 * Description: 客服工单接口
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.worksheet;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.worksheet.WorkSheetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

@Controller
@RequestMapping("/mobile/worksheet")
public class WorkSheetController  extends AbstractAgentController {

    @Inject private WorkSheetService workSheetService;
    @RequestMapping(value = "work_sheet_list.vpage")
    @ResponseBody
    public MapMessage worksheetList() {
        Integer taskStatus = getRequestInt("taskStatus");
        return MapMessage.successMessage().add("dataList",workSheetService.getUserWorkSheetList(taskStatus));

    }

    @RequestMapping(value = "work_sheet_info.vpage")
    @ResponseBody
    public MapMessage worksheetInfo() {
        Long sheetId = requestLong("sheetId");
        Map<String,Object> result = workSheetService.getWorkSheetInfo(sheetId);
        return MapMessage.successMessage().add("dataMap",result);
    }


    @RequestMapping(value = "delete_by_id.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage workSheetEventHandler(){
        String idStr = requestString("ids");
        String[] ids = idStr.split(",");
        workSheetService.deleteByIds(Arrays.asList(ids));
        return MapMessage.successMessage();
    }
}
