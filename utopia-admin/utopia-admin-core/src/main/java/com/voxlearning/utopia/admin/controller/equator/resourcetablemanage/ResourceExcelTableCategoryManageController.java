package com.voxlearning.utopia.admin.controller.equator.resourcetablemanage;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.configuration.client.ResourceExcelTableServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableCategory;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fugui.chang
 * @since 2018/6/28.
 * 增值equator-资源表管理中的分类管理
 */
@Controller
@RequestMapping("/equator/config/resourcetablemanage/")
public class ResourceExcelTableCategoryManageController extends AbstractEquatorController {
    @Inject
    private ResourceExcelTableServiceClient resourceExcelTableServiceClient;


    //分类页面
    @RequestMapping(value = "categoryindex.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String categoryIndex(Model model) {

        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
        }

        List<ResourceTableCategory> resourceTableCategoryList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly();
        model.addAttribute("resourceTableCategoryList", resourceTableCategoryList);
        return "equator/config/resourcetablemanage/categoryindex";
    }

    //新增一级分类
    @RequestMapping(value = "addfirstcategory.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addFirstCategory() {
        String firstCategory = getRequestString("firstCategory").trim();

        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher matcher = p.matcher(firstCategory);
        String checkFirstCategory = matcher.replaceAll("");
        if (StringUtils.isNotBlank(checkFirstCategory)) {
            return MapMessage.errorMessage("一级分类包含特殊字符" + checkFirstCategory);
        }


        if (StringUtils.isBlank(firstCategory)) {
            return MapMessage.errorMessage("一级分类不能为空");
        }
        if (firstCategory.contains(",")) {
            return MapMessage.errorMessage("一级分类不能包含特殊字符");
        }
        List<ResourceTableCategory> resourceTableCategoryList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly();
        ResourceTableCategory wrongCategory = resourceTableCategoryList.stream().filter(r -> StringUtils.equals(r.getFirstCategory(), firstCategory)).findFirst().orElse(null);
        if (wrongCategory != null) {
            return MapMessage.errorMessage("一级分类" + firstCategory + "已经存在,不能继续新增一级分类" + firstCategory);
        }

        ResourceTableCategory category = new ResourceTableCategory();
        category.setFirstCategory(firstCategory);
        return resourceExcelTableServiceClient.getResourceExcelTableService().insertResourceTableCategory(category);
    }

    //新增二级分类
    @RequestMapping(value = "addsecondcategory.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addSecondCategory() {
        String firstCategory = getRequestString("firstCategory").trim();
        String secondCategory = getRequestString("secondCategory").trim();

        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher matcher = p.matcher(firstCategory);
        String checkFirstCategory = matcher.replaceAll("");

        if (StringUtils.isNotBlank(checkFirstCategory)) {
            return MapMessage.errorMessage("一级分类包含特殊字符" + checkFirstCategory);
        }
        matcher = p.matcher(secondCategory);
        String checkSecondCategory = matcher.replaceAll("");
        if (StringUtils.isNotBlank(checkSecondCategory)) {
            return MapMessage.errorMessage("二级分类包含特殊字符" + checkSecondCategory);
        }


        if (StringUtils.isAnyBlank(firstCategory, secondCategory)) {
            return MapMessage.errorMessage("参数不能为空");
        }
        if (firstCategory.contains(",") || secondCategory.contains(",")) {
            return MapMessage.errorMessage("分类不能包含特殊字符");
        }
        List<ResourceTableCategory> resourceTableCategoryList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly();
        ResourceTableCategory category = resourceTableCategoryList.stream().filter(r -> StringUtils.equals(r.getFirstCategory(), firstCategory)).findFirst().orElse(null);
        if (category == null) {
            return MapMessage.errorMessage("一级分类" + firstCategory + "不存在，不能新增二级分类" + secondCategory);
        }
        if (category.getSecondCategory() != null && category.getSecondCategory().contains(secondCategory)) {
            return MapMessage.errorMessage("一级分类" + firstCategory + "下存在着二级分类" + secondCategory);
        }


        Set<String> secondSet = category.getSecondCategory();
        if (secondSet == null) {
            secondSet = new HashSet<>();
        }
        secondSet.add(secondCategory);
        category.setSecondCategory(secondSet);
        return resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceTableCategory(category);
    }


}
