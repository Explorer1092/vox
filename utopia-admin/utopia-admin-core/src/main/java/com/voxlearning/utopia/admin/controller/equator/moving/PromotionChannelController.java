package com.voxlearning.utopia.admin.controller.equator.moving;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandPromotionChannel;
import com.voxlearning.utopia.service.wonderland.api.entity.channel.ChannelRelationLabel;
import com.voxlearning.utopia.service.wonderland.client.WonderlandPromotionChannelServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 渠道号管理
 *
 * @author voctrals
 * @version 2018-10-22T17:03:13+08:00
 */
@Controller
@RequestMapping("/equator/pmc")
@Slf4j
public class PromotionChannelController extends AbstractEquatorController {

    @Inject private WonderlandPromotionChannelServiceClient wonderlandPromotionChannelServiceClient;

    // 渠道号管理
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String pmtcnl_mgr() {
        return "equator/pmc/index";
    }

    // 查询渠道号
    @RequestMapping(value = "queryall.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage pmtcnl_queryall() {
        List<ChannelRelationLabel> undone = wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().queryAllLabel();

        String firstLabel = "活动内付费渠道";
        String secondLabel = "活动导产品渠道";
        //保留标签
        List<ChannelRelationLabel> saveLabel = undone
                .stream()
                .filter(e -> StringUtils.equals(e.getLabel(), firstLabel) || StringUtils.equals(e.getLabel(), secondLabel))
                .collect(Collectors.toList());

        //正常标签
        List<ChannelRelationLabel> labels = undone
                .stream()
                .filter(e -> !StringUtils.equals(e.getLabel(), firstLabel) && !StringUtils.equals(e.getLabel(), secondLabel))
                .sorted(Comparator.comparing(ChannelRelationLabel::getCreateDatetime).reversed())
                .collect(Collectors.toList());

        saveLabel.addAll(labels);

        return MapMessage.successMessage()
                .add("channels", wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().queryAll())
                .add("channelLabel", saveLabel);
    }

    // 新建渠道号
    @RequestMapping(value = "upsert.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage pmtcnl_upsert() {
        Map<String, Object> channelJson = JsonUtils.fromJson(getRequestString("channel"));
        WonderlandPromotionChannel channel = new WonderlandPromotionChannel();
        String description = SafeConverter.toString(channelJson.get("description"));
        if (StringUtils.isEmpty(description)) {
            return MapMessage.errorMessage("描述不能为空");
        }
        Long id = SafeConverter.toLong(channelJson.get("id"), Long.MIN_VALUE);
        if (id != Long.MIN_VALUE) {
            channel.setId(id);
        }

        List<String> channels = (List<String>) channelJson.get("label");

        int category = SafeConverter.toInt(channelJson.get("category"));
        channel.setDescription(description);
        channel.setCategory(category);
        channel.setLabel(StringUtils.join(channels, ","));
        WonderlandPromotionChannel result = wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().upsertChannel(channel);
        return MapMessage.successMessage().add("channel", result);
    }

    // 增加渠道标签
    @RequestMapping(value = "addrelationlabel.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addRelationLabel() {
        String label = getRequestString("label");
        if (StringUtils.isBlank(label)) {
            return MapMessage.errorMessage("添加关联标签不能为空");
        }
        List<ChannelRelationLabel> list = wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().queryAllLabel();
        for (ChannelRelationLabel channelRelationLabel : list) {
            if (!Objects.isNull(channelRelationLabel) && StringUtils.equals(channelRelationLabel.getLabel(), label)) {
                return MapMessage.errorMessage("标签不能重复");
            }
        }
        ChannelRelationLabel channelRelationLabel = new ChannelRelationLabel();
        channelRelationLabel.setLabel(label);
        return wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().upsertLabel(channelRelationLabel);
    }

    //  删除渠道标签
    @RequestMapping(value = "removelabel.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteLabel() {
        String labelId = getRequestString("labelId");
        if (StringUtils.isBlank(labelId)) {
            return MapMessage.errorMessage("标签ID不能为空");
        }

        return wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().removeLabel(labelId);
    }

}
