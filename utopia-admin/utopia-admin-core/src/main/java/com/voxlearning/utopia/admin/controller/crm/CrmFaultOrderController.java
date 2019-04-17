package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.entity.CrmFaultOrder;
import com.voxlearning.utopia.admin.service.crm.CrmFaultOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/20
 * Time: 17:23
 * 用户记录跟踪工单操作
 */

@Controller
@RequestMapping("/crm/faultOrder")
public class CrmFaultOrderController extends CrmAbstractController {

    @Inject
    private CrmFaultOrderService crmFaultOrderService;

    @RequestMapping(value = "addRecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addRecord() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();

        long userId = getRequestLong("userId", -1L);
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String createInfo = getRequestParameter("createInfo", "").replaceAll("\\s", "");
        Integer userType = getRequestInt("userType");
        String sFaultTypes = getRequestParameter("faultType", "").replaceAll("\\s", "");
        String homeworkId = getRequestParameter("homeworkId", "").replaceAll("\\s", "");

        List<CrmFaultOrder> records = new LinkedList<>();
        if(StringUtils.isNotBlank(sFaultTypes)){
            String [] faultTypes = sFaultTypes.split(",");
            for(String faultType : faultTypes){
                CrmFaultOrder crmFaultOrder = new CrmFaultOrder();
                crmFaultOrder.setUserId(userId);
                crmFaultOrder.setUserName(userName);
                crmFaultOrder.setUserType(UserType.of(userType).name());
                crmFaultOrder.setCreateInfo(createInfo);
                crmFaultOrder.setCreator(adminUser.getAdminUserName());
                crmFaultOrder.setFaultType(Integer.parseInt(faultType));
                if(StringUtils.equals("4",faultType)){
                    crmFaultOrder.setExt(homeworkId);
                }
                crmFaultOrder.setStatus(0);
                records.add(crmFaultOrder);
            }
        }

        crmFaultOrderService.save(records);
        MapMessage mapMessage = MapMessage.successMessage("新增跟踪项成功");
        return mapMessage;
    }

    @RequestMapping(value = "closeRecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage closeRecord() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();

        String closeInfo = getRequestParameter("closeInfo", "").replaceAll("\\s", "");
        String id = getRequestParameter("id", "").replaceAll("\\s", "");

        if(StringUtils.isNotBlank(id)){
            CrmFaultOrder currentOrder = crmFaultOrderService.load(id);
            if(currentOrder != null){
                currentOrder.setCloseInfo(closeInfo);
                currentOrder.setCloser(adminUser.getAdminUserName());
                currentOrder.setCloseTime(new Date());
                currentOrder.setStatus(1);

                crmFaultOrderService.close(currentOrder);
            }else{
                MapMessage mapMessage = MapMessage.errorMessage();
                return mapMessage;
            }
        }else{
            MapMessage mapMessage = MapMessage.errorMessage();
            return mapMessage;
        }

        MapMessage mapMessage = MapMessage.successMessage("新增跟踪项成功");
        return mapMessage;
    }


    @RequestMapping(value = "faultorderlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String faultOrderList(Model model) {
        List<String> conditionKeys = Arrays.asList("start", "end", "faultType", "status","page","pageSize");

        Map<String, Object> conditionMap = new HashMap<>();
        for (String key : conditionKeys) {
            String value = getRequestParameter(key, "").replaceAll("\\s", "");
            conditionMap.put(key, value);
        }

        Page<CrmFaultOrder> pageData = getParentSnapshot(conditionMap);
        if(pageData != null){
            List<CrmFaultOrder> faultOrderList = getParentSnapshot(conditionMap).getContent();
            model.addAttribute("faultOrderList", faultOrderList);
            model.addAttribute("totalPage",pageData.getTotalPages());


            if (CollectionUtils.isEmpty(faultOrderList)) {
                if (isRequestPost())
                    getAlertMessageManager().addMessageError("没有记录。");
            }
        }
        model.addAttribute("conditionMap", conditionMap);

        return "crm/faultorder/faultorderlist";
    }


    private Page<CrmFaultOrder> getParentSnapshot(Map<String, Object> params){
        Date startTime = null;
        Date endTime = null;
        int page = 1;
        int pageSize = 10;
        Integer faultType = 0;
        Integer status = -1;
        if(StringUtils.isNotBlank((String)params.get("start"))){
            startTime = DateUtils.stringToDate((String)params.get("start") +" 00:00:00");
        }
        if(StringUtils.isNotBlank((String)params.get("end"))){
            endTime = DateUtils.stringToDate((String)params.get("end")+" 23:59:59");
        }

        if(StringUtils.isNotBlank((String)params.get("page"))){
            page = Integer.parseInt((String)params.get("page"));
        }else{
            params.put("page","1");
        }

        if(StringUtils.isNotBlank((String)params.get("pageSize"))){
            pageSize = Integer.parseInt((String)params.get("pageSize"));
        }

        if(StringUtils.isNotBlank((String)params.get("faultType"))){
            faultType = Integer.parseInt((String)params.get("faultType"));
        }

        if(StringUtils.isNotBlank((String)params.get("status"))){
            status = Integer.parseInt((String)params.get("status"));
        }
//        return new ArrayList<>();
        return crmFaultOrderService.findByPage(null,startTime,endTime,faultType,status,page - 1,pageSize);
    }

}
