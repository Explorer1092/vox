package com.voxlearning.utopia.admin.controller.reward.newversion;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.reward.RewardAbstractController;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetCategoryListMapper;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetCategoryTreeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reward/newcategory")
public class CrmRewardCategoryController extends RewardAbstractController {

    @Inject private NewRewardServiceClient newRewardServiceClient;
    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    @RequestMapping(value = "upsert.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage upsert() {
        Long id = getRequestLong("id");
        Long parentId = getRequestLong("parentId", 0);
        String name = getRequestString("name");
        Integer level = getRequestInt("level");
        Boolean display = getRequestBool("display");
        Integer displayOrder = getRequestInt("displayOrder");
        Integer oneLevelCategoryType = getRequestInt("oneLevelCategoryType");
        Integer twoLevelCategoryType = getRequestInt("twoLevelCategoryType");

        String visibleStr = getRequestParameter("visible", "0");
        Integer visible = this.getVisibleInt(visibleStr);

        MapMessage message = MapMessage.successMessage();
        if (StringUtils.isBlank(name)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写分类名称");
            return message;
        }
        if (!Objects.equals(ProductCategory.Level.ONE_LEVEL.getLevel(), level) && !Objects.equals(ProductCategory.Level.TWO_LEVEL.getLevel(), level)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写分类级别");
            return message;
        }

        ProductCategory category = new ProductCategory();
        if (!Objects.equals(id,0L)) {
            category.setId(id);
        }
        category.setLevel(level);
        category.setName(name);
        category.setParentId(parentId);
        category.setVisible(visible);
        if (Objects.equals(ProductCategory.Level.TWO_LEVEL.getLevel(), level)) {
            category.setDisplay(true);
            category.setTwoLevelCategoryType(twoLevelCategoryType);
        } else {
            category.setOneLevelCategoryType(oneLevelCategoryType);
            category.setDisplay(display);
        }
        category.setDisplayOrder(displayOrder);
        category = newRewardServiceClient.upsertCategory(category);
        if (category != null) {
            return message.setInfo("保存成功");
        } else {
            return message.setInfo("保存失败");
        }
    }

    @RequestMapping(value = "delete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage delete() {
        Long id = getRequestLong("id");

        MapMessage message = MapMessage.successMessage();
        List<ProductCategory> productCategoryList = newRewardLoaderClient.loadProductCategoryByParentId(id);

        if (CollectionUtils.isNotEmpty(productCategoryList)) {
            message.setSuccess(false);
            message.setInfo("删除失败，请先删除子分类再删除父分类");
            return message;
        }

        List<Long> productIdList = newRewardLoaderClient.loadProductIdListByCategoryId(id);

        if (CollectionUtils.isNotEmpty(productIdList)) {
            message.setSuccess(false);
            message.setInfo("删除失败，请先删除该分类和所有商品的关联关系");
            return message;
        }

        if (newRewardServiceClient.deleteCategoryById(id)) {
            return message.setInfo("删除成功");
        } else {
            return message.setInfo("删除失败");
        }
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        Long parentId = getRequestLong("parentId");

        MapMessage message = MapMessage.successMessage();
        List<GetCategoryListMapper> mapperList = new ArrayList<>();
        List<ProductCategory> productCategoryList = newRewardLoaderClient.loadProductCategoryByParentId(parentId);
        if (CollectionUtils.isNotEmpty(productCategoryList)) {
            mapperList = productCategoryList.stream().map(category -> {
                GetCategoryListMapper mapper = new GetCategoryListMapper();
                BeanUtils.copyProperties(category, mapper);
                return mapper;
            }).collect(Collectors.toList());
        }
        message.add("categoryList", mapperList);
        return message;
    }

    @RequestMapping(value = "tree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tree() {

        MapMessage message = MapMessage.successMessage();
        List<GetCategoryTreeMapper> mapperList = new ArrayList<>();
        List<ProductCategory> productCategoryList = newRewardLoaderClient.loadProductCategoryByParentId(0L);
        if (CollectionUtils.isNotEmpty(productCategoryList)) {
            mapperList = productCategoryList.stream().map(category -> {
                GetCategoryTreeMapper mapper = new GetCategoryTreeMapper();
                mapper.setId(category.getId());
                mapper.setName(category.getName());
                mapper.setDisplay(category.getDisplay());
                mapper.setDisplayOrder(category.getDisplayOrder());
                mapper.setVisible(this.getVisibleStr(category.getVisible()));
                mapper.setOneLevelCategoryType(category.getOneLevelCategoryType());
                List<ProductCategory> childrenCategory = newRewardLoaderClient.loadProductCategoryByParentId(category.getId());
                if (CollectionUtils.isNotEmpty(childrenCategory)) {
                    mapper.setChildrenCategory(childrenCategory
                            .stream()
                            .map(cc -> {
                                GetCategoryTreeMapper.ChildrenCategory children = mapper.new ChildrenCategory();
                                children.setId(cc.getId());
                                children.setName(cc.getName());
                                children.setDisplay(cc.getDisplay());
                                children.setDisplayOrder(cc.getDisplayOrder());
                                children.setVisible(this.getVisibleStr(cc.getVisible()));
                                children.setTwoLevelCategoryType(cc.getTwoLevelCategoryType());
                                List<ProductTag> tagList = newRewardLoaderClient.loadProductTagByParent(cc.getId(), ProductTag.ParentType.CATEGPRY.getType());
                                if (CollectionUtils.isNotEmpty(tagList)) {
                                    children.setChildrenTrgList(tagList);
                                }
                                return children;
                            }).collect(Collectors.toList()));
                }
                return mapper;
            }).collect(Collectors.toList());
        }
        message.add("categoryTree", mapperList);
        return message;
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long id = getRequestLong("id");
        MapMessage message = MapMessage.successMessage();
        ProductCategory category = newRewardLoaderClient.loadProductCategoryById(id);
        message.add("productCategory", category);
        return message;
    }
}
