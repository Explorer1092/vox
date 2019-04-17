package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.AgentAppContentType;
import com.voxlearning.utopia.agent.constants.AgentDataPacketType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelp;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelpType;
import com.voxlearning.utopia.agent.service.selfhelp.AgentSelfHelpService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentSelfHelpController
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Controller
@RequestMapping("/sysconfig/config")
public class AgentSelfHelpController extends AbstractAgentController {

    @Inject
    private AgentSelfHelpService agentSelfHelpService;

    @Inject
    private AgentAppContentPacketService agentAppContentPacketService;

    // list页
    @RequestMapping("list.vpage")
    public String list(Model model){
        return "/sysconfig/matter_contacts/list";
    }

    @RequestMapping("addViewPage.vpage")
    public String addView(Model model){
        List<AgentSelfHelpType> dataList = agentSelfHelpService.getSelfHelpTypeList();
        model.addAttribute("dataList",dataList);
        return "/sysconfig/matter_contacts/add";
    }
    /**
     * 查询或编辑时返回数据
//     * @param model
//     * @return
     */
    @RequestMapping(value = "itemInfo.vpage", method = RequestMethod.GET)
    public String itemInfo(Model model) {
        String itemId = getRequestString("id");
        String type = getRequestString("type");
        List<AgentSelfHelpType> dataList = agentSelfHelpService.getSelfHelpTypeList();
        if (!StringUtils.isBlank(itemId)) {
            AgentSelfHelp selfHelp = agentSelfHelpService.getById(itemId);
            List<Map<String,String>> list = agentSelfHelpService.assemblyPacketInfo(selfHelp);
            model.addAttribute("packets",list);
            model.addAttribute("selfHelp", selfHelp);
        }
        model.addAttribute("dataList",dataList);
        if(type.equals("view")){
            return "/sysconfig/matter_contacts/detail";
        }
        return "/sysconfig/matter_contacts/edit";
    }

    // 保存功能
    @RequestMapping(value = "saveData.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage saveData() {
        String id = getRequestString("id");
        String typeId = getRequestString("typeId");
        if (StringUtils.isBlank(typeId)) {
            return MapMessage.errorMessage("请选择事项类型");
        }
        String title = getRequestString("title");
        if (StringUtils.isBlank(title)) {
            return MapMessage.errorMessage("请填写事项");
        }

        String contact = getRequestString("contact");
        if (StringUtils.isBlank(contact)) {
            return MapMessage.errorMessage("请填写联系人");
        }

        String email = getRequestString("email");
        if (StringUtils.isBlank(email)) {
            return MapMessage.errorMessage("请填写联系人邮箱");
        }

        String wechatGroup = getRequestString("wechatGroup");
//        if (StringUtils.isBlank(wechatGroup)) {
//            return MapMessage.errorMessage("请填写微信群名称");
//        }

        String comment = getRequestString("comment");

        //packetIds 用"，"分割
        String[] array = getRequestString("packetIds").split(",");
        List<String> packetIds = new ArrayList<>();
        for (String e : array) {
            String value = SafeConverter.toString(e);
            if (StringUtils.isNotBlank(value)) {
                packetIds.add(value);
            }
        }
        return agentSelfHelpService.saveData(id,typeId,title,contact,email,wechatGroup,comment,packetIds);
    }

    /**
     * 查询事项联系人列表
     * @return
     */
    @RequestMapping(value = "selfHelpList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage selfHelpList(){
        return agentSelfHelpService.findHelpList();
    }

    /**
     * 删除事项
     * @param id 事项id
     * @return
     */
    @RequestMapping(value = "delSelfHelpItem.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage delSelfHelpItem(@RequestParam("id") String id) {
        agentSelfHelpService.delSelfHelpItem(id);
        return MapMessage.successMessage();
    }
    /**************************************************事项类型接口*********************************************************/

    // 跳列表页
    @RequestMapping(value = "preHelpTypeList.vpage", method = RequestMethod.GET)
    public String preHelpTypeList(Model model){
        return "/sysconfig/matter_contacts/mattertype";
    }

    /**
     * 获取类型列表
     * @return
     */
    @RequestMapping(value = "typeList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage typeList(){
        MapMessage message = MapMessage.successMessage();
        List<AgentSelfHelpType> dataList = agentSelfHelpService.getSelfHelpTypeList();
        message.add("dataList",dataList);
        return message;
    }

    @RequestMapping(value = "delType.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage delType(@RequestParam("typeId") String id) {
        MapMessage result = new MapMessage();
        List<AgentSelfHelp> selfHelps = agentSelfHelpService.findByType(id);
        if(selfHelps != null && selfHelps.size() > 0){
            result.setSuccess(false);
            result.setInfo("事项类型被使用，无法删除!");
            return result;
        }
        agentSelfHelpService.delType(id);
        result.setSuccess(true);
        return result;
    }


    // 保存功能
    @RequestMapping(value = "saveTypeData.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public MapMessage saveTypeData() {
        String typeId = getRequestString("id");

        String typeName = getRequestString("typeName");
        Integer sortId = getRequestInt("sortId");
        if (StringUtils.isBlank(typeName)) {
            return MapMessage.errorMessage("请填写类型名称");
        }
        if (sortId == 0){
            return MapMessage.errorMessage("请填写排序ID");
        }

        return agentSelfHelpService.saveItemType(typeId,typeName,sortId);
    }


    @RequestMapping(value = "getAllPacketType.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllPacketType(){
        MapMessage result = MapMessage.successMessage();
        List<AgentAppContentPacket> list = agentAppContentPacketService.loadByContentType(AgentAppContentType.DATA_PACKET);
        Map<AgentDataPacketType,List<AgentAppContentPacket>> groupMap = list.stream().collect(Collectors.groupingBy(p -> p.getDatumType(),Collectors.toList()));
        List<Map<String,Object>> resultList = new ArrayList<>();
        groupMap.forEach((k,v) ->{
            Map<String,Object> map = new HashMap<>();
            map.put("packetTypeId",k.getId());
            map.put("packetTypeName",k.getDesc());
            //分组后每个包类型对应的list 简化字段用
            List<Map<String,Object>> list1 = new ArrayList<>();
            v.forEach(item ->{
                Map<String,Object> m1 = new HashMap<>();
                m1.put("packetId",item.getId());
                m1.put("packetTitle",item.getContentTitle());
                list1.add(m1);
            });
            map.put("packetList",list1);
            resultList.add(map);
        });
        result.put("data",resultList);
        return result;
    }
}
