/**
 * Author:   xianlong.zhang
 * Date:     2018/10/18 16:42
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.controller.worksheet;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheet;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheetLog;
import com.voxlearning.utopia.agent.service.worksheet.WorkSheetService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

@Controller
@RequestMapping(value = "/agent/worksheet")
public class AgentWorkSheetController  extends AbstractAgentController {

    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private WorkSheetService workSheetService;
    // 接收客服工单
    @RequestMapping(value = "work_sheet_event_handler.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage workSheetEventHandler(@RequestBody Map<String,Object> paramMap){

        if(MapUtils.isEmpty(paramMap)){
            return MapMessage.errorMessage("事件内容不能为空");
        }
//        emailServiceClient.createPlainEmail()
//                .body(JsonUtils.toJson(paramMap))
//                .subject("客服工单推送【" + RuntimeMode.current().getStageMode() + "】")
//                .to("xianlong.zhang@17zuoye.com;")
//                .send();

        try{
            Integer event = SafeConverter.toInt(paramMap.get("event"),-2);
            if(event == null){
                return MapMessage.errorMessage("事件类型不能为空");
            }else if(event == 0){
                WorkSheet workSheet = new WorkSheet();
                try{
                    BeanUtilsBean2.getInstance().copyProperties(workSheet, paramMap);
                }catch (Exception e){
                    logger.error("workSheetEventHandler workSheet error", e);
                }
                workSheetService.saveInsertEvent(workSheet);
            }else{
                WorkSheetLog workSheetLog = new WorkSheetLog();
                try{
                    BeanUtilsBean2.getInstance().copyProperties(workSheetLog, paramMap);
                }catch (Exception e){
                    logger.error("workSheetEventHandler workSheetLog error", e);
                }
                workSheetService.saveOtherEvent(workSheetLog);
            }
        }catch (Exception e){
            emailServiceClient.createPlainEmail()
                    .body(JsonUtils.toJson(paramMap))
                    .subject("客服工单推送【" + RuntimeMode.current().getStageMode() + "】")
                    .to("xianlong.zhang@17zuoye.com;")
                    .send();
            logger.error("接收工单推送接口异常:",e);
        }


        return MapMessage.successMessage();
    }
}
