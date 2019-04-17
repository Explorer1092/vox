package com.voxlearning.utopia.agent.controller.trainingcenter;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.trainingcenter.AgentTitleColumnService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Set;


/**
 * 培训中心-栏目管理
 * @author deliang.che
 * @since 2018/7/6
 */
@Controller
@RequestMapping(value = "/trainingcenter/column")
public class ColumnController extends AbstractAgentController {

    @Inject private AgentTitleColumnService agentTitleColumnService;

    //跳页面
    @RequestMapping("columnList.vpage")
    public String columnList(Model model){
        return "/trainingcenter/column/columnList";
    }

    // 保存功能
    @RequestMapping(value = "saveColumnData.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage saveColumnData() {
        String id = getRequestString("id");
        Integer level = getRequestInt("level");
        String name = getRequestString("name");
        String parentId = getRequestString("parentId");//父级栏目ID
        Integer sortId = getRequestInt("sortId");     // 排序ID
        if(level < 1 ){
            return MapMessage.errorMessage("请选择级别！");
        }
        if(StringUtils.isBlank(name)){
            return MapMessage.errorMessage("请输入栏目名称！");
        }
        if(level==2 && StringUtils.isBlank(parentId)){
            return MapMessage.errorMessage("选择父级栏目！");
        }
        if(sortId <1 || sortId == null){
            return MapMessage.errorMessage("请输入排序号！");
        }
        return agentTitleColumnService.saveColumn(id,name,level,parentId,sortId);
    }

    /**
     * 根据级别返回栏目
     * @return MapMessage
     */
    @RequestMapping(value = "findColumnListByLevel.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findColumnListByLevel(){
//        Integer level = getRequestInt("level");
        Set<Integer> levels = requestIntegerSet("levels");
        return agentTitleColumnService.findColumnList(levels);
    }

    /**
     * 返回 栏目列表二级联动
     * @return
     */
    @RequestMapping(value = "findLinkageColumnList.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findLinkageColumnList(){
        return agentTitleColumnService.findLinkageColumnList();
    }

    /**
     * 删除栏目
     * @return
     */
    @RequestMapping(value = "deleteColumn.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteColumn(){
        String id = getRequestString("id");
        return agentTitleColumnService.delColumn(id);
    }
}