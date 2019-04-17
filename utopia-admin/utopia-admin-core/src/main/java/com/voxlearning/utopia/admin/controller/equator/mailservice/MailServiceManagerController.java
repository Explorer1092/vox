package com.voxlearning.utopia.admin.controller.equator.mailservice;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.common.api.enums.mailbox.MailType;
import com.voxlearning.equator.service.configuration.api.entity.material.Material;
import com.voxlearning.equator.service.configuration.client.ResourceConfigServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceStaticFileInfo;
import com.voxlearning.equator.service.mailbox.api.client.GrowingWorldMailClient;
import com.voxlearning.equator.service.mailbox.api.response.MailResponse;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * equator邮件服务相关
 *
 * @author lei.liu
 * @version 18-8-14
 */
@Controller
@RequestMapping(value = "equator/mailservice")
public class MailServiceManagerController extends AbstractEquatorController {

    @Inject
    private GrowingWorldMailClient growingWorldMailClient;
    @Inject
    private ResourceConfigServiceClient resourceConfigServiceClient;

    /**
     * 首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        MapMessage mapMessage = growingWorldMailClient.getAllNoticeMail();
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", mapMessage.getInfo());
        } else {
            MailResponse mailResponse = (MailResponse) mapMessage.get("mailResponse");
            model.addAttribute("mailList", mailResponse.getMailList());
        }

        return "equator/mail/index";
    }

    /**
     * 邮件列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String mailList(Model model) {

        model.addAttribute("mailTypeList", Arrays.stream(MailType.values())
                .map(p -> new MapMessage().set("type", p.name()).set("name", p.getDescription()))
                .collect(Collectors.toList()));

        Long studentId = getRequestLong("studentId");
        String targetMailType = getRequestString("targetMailType");

        if (studentId == 0) {
            return "equator/mail/list";
        }

        model.addAttribute("studentId", studentId);
        model.addAttribute("targetMailType", targetMailType);

        MailType currentMailType = MailType.of(targetMailType);
        if (currentMailType == null) {
            currentMailType = MailType.Reward;
        }

        // 查询邮件列表
        MailResponse mailResponse = growingWorldMailClient.growingWorldMailLoader.loadMailListVo(studentId, currentMailType).getUninterruptibly();
        if (mailResponse != null && CollectionUtils.isNotEmpty(mailResponse.getMailList())) {
            model.addAttribute("mailList", mailResponse.getMailList());
        }

        // 获取道具资源
        Map<String, String> resourceIconMap = resourceConfigServiceClient.getResourceStaticFileInfoFromBuffer()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.equals(e.getFirstCategory(), "NewGrowingWorld"))
                .filter(e -> StringUtils.equals(e.getSecondCategory(), "Resource"))
                .collect(Collectors.toMap(ResourceStaticFileInfo::getResourceName, ResourceStaticFileInfo::getUrl));
        List<Material> materialInfoList = resourceConfigServiceClient.getBuffer(Material.class).loadDataList();
        materialInfoList.forEach(materialInfo -> materialInfo.setIcon(resourceIconMap.getOrDefault(materialInfo.getIcon(), "")));
        Map<String, Material> materialIdInfoCfgMap = materialInfoList.stream().collect(Collectors.toMap(Material::getId, materialInfo -> materialInfo));
        model.addAttribute("materialIdInfoCfgMap", materialIdInfoCfgMap);

        return "equator/mail/list";
    }

    /**
     * 发送通知
     */
    @RequestMapping(value = "notice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage notice() {
        String content = getRequestString("content");
        if (StringUtils.isEmpty(content)) {
            MapMessage.errorMessage().setInfo("内容为空，请输入通知内容。");
        }
        return growingWorldMailClient.sendGrowingWorldNoticeMail(content);
    }
}
