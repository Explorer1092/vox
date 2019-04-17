package com.voxlearning.utopia.admin.controller.thirdparty.qiyukf;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.service.config.api.QiyukfService;
import com.voxlearning.utopia.service.config.api.entity.QiyukfInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 七鱼客服配置信息管理
 *
 * @author Wenlong Meng
 * @since Mar 12, 2019
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/qiyukf/config")
public class QiyukfInfoConfigController extends CrmAbstractController {

    //local variables
    @ImportService(interfaceClass = QiyukfService.class)
    private QiyukfService qiyukfService;

    //Logic
    /**
     * 七鱼客服配置页
     *
     * @return
     */
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "/site/qiyukf/qiyukf_config_list";
    }

    /**
     * 七鱼客服配置列表，根据appKey查询
     *
     * @param appKey
     * @return
     */
    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Object list(String appKey) {
        List<QiyukfInfo> qiyukfInfos = qiyukfService.loadByAppkey(appKey);
        return MapMessage.successMessage().set("data", qiyukfInfos);
    }

    /**
     * 七鱼客服配置appKey列表
     *
     * @return
     */
    @RequestMapping(value = "appKeys.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Object appKeys() {
        List<String> qiyukfInfos = qiyukfService.loadAppkeys();
        return MapMessage.successMessage().set("data", qiyukfInfos);
    }

    /**
     * 保存七鱼客服配置信息
     *
     * @param appKey
     * @param questionType
     * @param name
     * @param csGroupId
     * @param qtype
     * @param robotId
     * @return
     */
    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save(String appKey, String questionType, String name, int csGroupId, int qtype, int robotId) {
        QiyukfInfo qiyukfInfo = new QiyukfInfo();
        qiyukfInfo.setName(name);
        qiyukfInfo.setAppKey(appKey);
        qiyukfInfo.setQuestionType(questionType);
        qiyukfInfo.setCsGroupId(csGroupId);
        qiyukfInfo.setQtype(qtype);
        qiyukfInfo.setRobotId(robotId);
        qiyukfInfo.setStatus(0);
        qiyukfService.save(qiyukfInfo);
        return MapMessage.successMessage();
    }


    /**
     * 修改状态七鱼客服配置信息
     *
     * @param questionType
     * @param appKey
     * @param status
     * @return
     */
    @RequestMapping(value = "/updateStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStatus(String appKey, String questionType, int status) {
        QiyukfInfo qiyukfInfo = qiyukfService.load(appKey, questionType);
        qiyukfInfo.setStatus(status);
        qiyukfService.save(qiyukfInfo);
        return MapMessage.successMessage();
    }

    /**
     * 删除七鱼客服配置信息
     *
     * @param questionType
     * @param appKey
     * @return
     */
    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete(String appKey, String questionType) {
        qiyukfService.delete(appKey, questionType);
        return MapMessage.successMessage();
    }

}
