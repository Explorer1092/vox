package com.voxlearning.utopia.mizar.controller.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCategory;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机构一级二级分类管理
 * Created by alex on 2016/9/19.
 */
@Controller
@RequestMapping("/config/category")
public class CategoryConfigController extends AbstractMizarController {

    @Inject private MizarLoaderClient mizarLoaderClient;
    @Inject private MizarServiceClient mizarServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String categoryIndex(Model model) {
        String category = getRequestString("category");
        List<MizarCategory> categoryList = mizarLoaderClient.loadAllMizarCategory();
        Set<String> firstCategories = categoryList
                .stream()
                .map(MizarCategory::getFirstCategory)
                .collect(Collectors.toSet());

        if (StringUtils.isNotBlank(category)) {
            categoryList = categoryList.stream()
                    .filter(c -> StringUtils.equals(category, c.getFirstCategory()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("category", category);
        model.addAttribute("firstCategory", firstCategories);
        model.addAttribute("categoryList", splitList(categoryList, 10));
        return "sysconfig/categorylist";
    }

    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveCategory() {
        String cid = getRequestString("cid");
        String first = getRequestString("first");
        String second = getRequestString("second");
        if (StringUtils.isBlank(first) || StringUtils.isBlank(second)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MizarCategory category = new MizarCategory();
        if (ObjectId.isValid(cid)) category.setId(cid);
        category.setFirstCategory(first);
        category.setSecondCategory(second);
        try {
            return mizarServiceClient.saveMizarCategory(category);
        } catch (Exception ex) {
            logger.error("Failed Save Mizar Category!");
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeCategory() {
        String cid = getRequestString("cid");
        if (StringUtils.isBlank(cid) || !ObjectId.isValid(cid)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            return mizarServiceClient.removeMizarCategory(cid);
        } catch (Exception ex) {
            logger.error("Failed Save Mizar Category!");
            return MapMessage.errorMessage();
        }
    }

}
