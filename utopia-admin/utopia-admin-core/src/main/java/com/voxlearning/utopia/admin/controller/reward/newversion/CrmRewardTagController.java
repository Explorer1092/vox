package com.voxlearning.utopia.admin.controller.reward.newversion;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.reward.RewardAbstractController;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import com.voxlearning.utopia.service.reward.mapper.product.crm.UpSertTagMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/reward/newtag")
public class CrmRewardTagController extends RewardAbstractController {
    @Inject private NewRewardServiceClient newRewardServiceClient;
    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    @RequestMapping(value = "upsert.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage upsert() {
        Long id = getRequestLong("id");
        String name = getRequestParameter("name", "");
        Long parentId = getRequestLong("parentId");
        Integer parentType = getRequestInt("parentType");
        Integer displayOrder = getRequestInt("displayOrder");

        MapMessage message = MapMessage.successMessage();
        if (StringUtils.isBlank(name)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写标签名称");
            return message;
        }
        if (Objects.isNull(parentType)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写父级类型");
            return message;
        }
        if (Objects.isNull(parentId)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写父级ID");
            return message;
        }

        UpSertTagMapper mapper = new UpSertTagMapper();
        if (!Objects.isNull(id) && id !=0L) {
            mapper.setId(id);
        }
        mapper.setName(name);
        mapper.setParentId(parentId);
        mapper.setParentType(parentType);
        mapper.setDisplayOrder(displayOrder);
        message = newRewardServiceClient.upsertTag(mapper);
        return message;
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        Long id = getRequestLong("id");

        MapMessage message = MapMessage.successMessage();
        newRewardServiceClient.deleteProductTagRefByTagId(id);

        if (newRewardServiceClient.deleteTagById(id)) {
            return message.setInfo("删除成功");
        } else {
            return message.setInfo("删除失败");
        }
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        Long parentId = getRequestLong("parentId");
        Integer parentType = getRequestInt("parentType");

        MapMessage message = MapMessage.successMessage();
        List<ProductTag> productTagList = newRewardLoaderClient.loadProductTagByParent(parentId, parentType);
        message.add("tagList", productTagList);
        return message;
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long id = getRequestLong("id");
        MapMessage message = MapMessage.successMessage();
        ProductTag productTag = newRewardLoaderClient.loadProductTagById(id);
        message.add("productTag", productTag);
        return message;
    }
}
