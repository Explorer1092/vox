/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.api.filter.RewardCategoryFilter;
import com.voxlearning.utopia.service.reward.api.filter.RewardTagFilter;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.reward.entity.RewardProductCategoryRef;
import com.voxlearning.utopia.service.reward.entity.RewardTag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author XiaoPeng.Yang
 * @version 0.1
 * @since 14-7-16
 */
@Deprecated
@Controller
@RequestMapping("/reward/category")
public class RewardCategoryController extends RewardAbstractController {

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "categorylist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    private String categoryList(Model model) {
        model.addAttribute("types", RewardProductType.values());
        return "reward/category/categorylist";
    }

    @RequestMapping(value = "categories.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getRewardCategories() {
        RewardProductType productType;
        UserType userType;
        try {
            productType = RewardProductType.valueOf(getRequestString("categoryType"));
            userType = UserType.valueOf(getRequestString("roles"));
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }

        List<RewardCategory> allCategories = crmRewardService.$loadRewardCategories();
        List<RewardCategory> categoryList = RewardCategoryFilter.filter(allCategories, productType, userType);
        return MapMessage.successMessage().add("categoryList", categoryList);
    }

    @RequestMapping(value = "deletecategory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteCategory() {
        long categoryId = getRequestLong("categoryId");
        if (categoryId <= 0) return MapMessage.errorMessage("删除失败");

        boolean del = true; // 是否可以执行删除操作

        // 查询这个分类下的所有产品的库存，如果都没有了就可以删除分类
        List<RewardProductCategoryRef> refs = crmRewardService.$findRewardProductCategoryRefsByCategoryId(categoryId);
        if (CollectionUtils.isNotEmpty(refs)) {
            del = !refs.stream()
                    .map(RewardProductCategoryRef::getProductId)
                    .map(e -> crmRewardService.$findRewardSkusByProductId(e))
                    .flatMap(List::stream)
                    .anyMatch(e -> SafeConverter.toInt(e.getInventorySellable()) > 0);
        }

        // 执行删除
        if (del) {
            boolean ret = crmRewardService.$removeRewardCategory(categoryId);
            return new MapMessage().setSuccess(ret);
        } else {
            return MapMessage.errorMessage("该分类下的产品还有库存，不能删除");
        }
    }

    @RequestMapping(value = "addcategory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCategory() {
        String categoryName = getRequestString("categoryName");
        String categoryType = getRequestString("categoryType");
        String categoryCode = getRequestString("categoryCode");
        boolean studentVisible = getRequestBool("studentVisible");
        boolean display = getRequestBool("display");
        boolean teacherVisible = getRequestBool("teacherVisible");
        boolean juniorVisible = getRequestBool("juniorVisible");
        boolean primaryVisible = getRequestBool("primaryVisible");
        int displayOrder = getRequestInt("displayOrder");
        long categoryId = getRequestLong("categoryId");

        if (StringUtils.isBlank(categoryName)) return MapMessage.errorMessage("操作失败，请正确填写各参数");

        RewardCategory category = new RewardCategory();
        category.setCategoryName(categoryName);
        category.setProductType(categoryType);
        category.setDisplay(display);
        category.setStudentVisible(studentVisible);
        category.setTeacherVisible(teacherVisible);
        category.setParentId(0L);
        category.setDisplayOrder(displayOrder);
        category.setCategoryCode(categoryCode);
        category.setJuniorVisible(juniorVisible);
        category.setPrimaryVisible(primaryVisible);

        if (categoryId > 0) {
            category.setId(categoryId);
        }
        category = crmRewardService.$upsertRewardCategory(category);
        if (category == null) {
            return MapMessage.errorMessage("操作失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "taglist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    private String tagList(Model model) {
        String tagLevel = getRequestParameter("tagLevel", null);
        RewardTagLevel rewardTagLevel = null;
        if (StringUtils.isNotBlank(tagLevel)) {
            try {
                rewardTagLevel = RewardTagLevel.valueOf(tagLevel);
            } catch (Exception ex) {
                rewardTagLevel = null;
            }
        }

        String roles = getRequestParameter("roles", null);
        UserType userType = null;
        if (StringUtils.equals("TEACHER", roles)) {
            userType = UserType.TEACHER;
        } else if (StringUtils.equals("STUDENT", roles)) {
            userType = UserType.STUDENT;
        }

        List<RewardTag> tagList = crmRewardService.$loadRewardTags();
        tagList = RewardTagFilter.filter(tagList, rewardTagLevel, userType);

        model.addAttribute("tagList", tagList);
        model.addAttribute("tagLevel", RewardTagLevel.values());
        return "reward/category/taglist";
    }

    @RequestMapping(value = "upserttag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertTag() {
        String tagId = getRequestParameter("tagId", "");
        String tagName = getRequestParameter("tagName", "");
        String tagLevel = getRequestParameter("tagLevel", "");
        Boolean studentVisible = getRequestBool("studentVisible");
        Boolean disabled = getRequestBool("disabled");
        Boolean teacherVisible = getRequestBool("teacherVisible");

        MapMessage message = new MapMessage();
        if (StringUtils.isBlank(tagName)) {
            message.setSuccess(false);
            message.setInfo("保存失败，请正确填写各参数");
            return message;
        }

        RewardTag tag = new RewardTag();
        if (StringUtils.isNotBlank(tagId)) {
            tag.setId(SafeConverter.toLong(tagId));
        }
        tag.setTeacherVisible(teacherVisible);
        tag.setStudentVisible(studentVisible);
        tag.setDisabled(disabled);
        tag.setTagLevel(tagLevel);
        tag.setTagName(tagName);
        tag = crmRewardService.$upsertRewardTag(tag);
        if (tag != null) {
            return message.setInfo("保存成功");
        } else {
            return message.setInfo("保存失败");
        }
    }

}
