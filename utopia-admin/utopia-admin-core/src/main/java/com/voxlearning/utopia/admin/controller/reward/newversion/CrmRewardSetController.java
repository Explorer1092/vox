package com.voxlearning.utopia.admin.controller.reward.newversion;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.reward.RewardAbstractController;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductSet;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTagRef;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetSetListMapper;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetSetTreeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-15 18:31
 **/
@Controller
@RequestMapping("/reward/newset")
public class CrmRewardSetController extends RewardAbstractController {
    @Inject
    private NewRewardServiceClient newRewardServiceClient;
    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    @RequestMapping(value = "upsert.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage upsert() {
        Long id = getRequestLong("id");
        String name = getRequestParameter("name","");
        Boolean display = getRequestBool("display");
        Integer displayOrder = getRequestInt("displayOrder");

        String visibleStr = getRequestParameter("visible", "0");
        Integer visible = this.getVisibleInt(visibleStr);

        MapMessage message = MapMessage.successMessage();
        if (StringUtils.isBlank(name)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写分类名称");
            return message;
        }

        ProductSet set = new ProductSet();
        set.setName(name);
        set.setDisplay(display);
        set.setDisplayOrder(displayOrder);
        set.setVisible(visible);
        if (id != 0L && id != null) {
            set.setId(id);
        }
        set = newRewardServiceClient.upsertSet(set);
        if (set != null) {
            return message.setInfo("保存成功");
        } else {
            return message.setInfo("保存失败");
        }
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        Long id = getRequestLong("id");

        MapMessage message = MapMessage.successMessage();
        List<ProductTag> productTagList = newRewardLoaderClient.loadProductTagByParent(id, ProductTag.ParentType.SET.getType());

        if (CollectionUtils.isNotEmpty(productTagList)) {
            message.setSuccess(false);
            message.setInfo("删除失败，请先删除子标签！");
            return message;
        }

        Set<Long> tagIdSet = productTagList.stream().map(ProductTag::getId).collect(Collectors.toSet());

        Map<Long, List<ProductTagRef>> productCategoryRefs = newRewardLoaderClient.loadProductTagRefByTagIdList(tagIdSet);

        if (MapUtils.isNotEmpty(productCategoryRefs)) {
            message.setSuccess(false);
            message.setInfo("删除失败，请先删除该类目集合下标签和所有商品之间的关联关系");
            return message;
        }

        if (newRewardServiceClient.deleteSetById(id)) {
            return message.setInfo("删除成功");
        } else {
            return message.setInfo("删除失败");
        }
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {

        MapMessage message = MapMessage.successMessage();
        List<GetSetListMapper> mapperList = new ArrayList<>();
        List<ProductSet> productSetList = newRewardLoaderClient.loadAllProductSet();
        if (CollectionUtils.isNotEmpty(productSetList)) {
            mapperList = productSetList.stream().map(category -> {
                GetSetListMapper mapper = new GetSetListMapper();
                BeanUtils.copyProperties(category, mapper);
                return mapper;
            }).collect(Collectors.toList());
        }
        message.add("setList", mapperList);
        return message;
    }

    @RequestMapping(value = "tree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tree() {
        MapMessage message = MapMessage.successMessage();
        List<GetSetTreeMapper> mapperList = new ArrayList<>();
        List<ProductSet> productSetList = newRewardLoaderClient.loadAllProductSet();
        if (CollectionUtils.isNotEmpty(productSetList)) {
            mapperList = productSetList.stream().map(category -> {
                GetSetTreeMapper mapper = new GetSetTreeMapper();
                BeanUtils.copyProperties(category, mapper);
                String visible = this.getVisibleStr(category.getVisible());
                mapper.setVisible(visible);
                List<ProductTag> tagList = newRewardLoaderClient.loadProductTagByParent(mapper.getId(), ProductTag.ParentType.SET.getType());
                if (CollectionUtils.isNotEmpty(tagList)) {
                    mapper.setChildrenTrgList(tagList);
                }
                return mapper;
            }).collect(Collectors.toList());
        }
        message.add("setTree", mapperList);
        return message;
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long id = getRequestLong("id");
        MapMessage message = MapMessage.successMessage();
        ProductSet productSet = newRewardLoaderClient.loadProductSetById(id);
        message.add("productSet", productSet);
        return message;
    }
}
