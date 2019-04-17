package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.ambush.api.VicePositionLoader;
import com.voxlearning.galaxy.service.ambush.api.VicePositionTypeLoader;
import com.voxlearning.galaxy.service.ambush.api.VicePositionTypeService;
import com.voxlearning.galaxy.service.ambush.api.entity.VicePosition;
import com.voxlearning.galaxy.service.ambush.api.entity.VicePositionType;
import com.voxlearning.utopia.admin.controller.diamond.AbstractPositionController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @author feng.guo
 * @since 2019-03-30
 */
@Controller
@RequestMapping(value = "/site/classify")
public class VicePositionTypeController extends AbstractPositionController {

    @ImportService(interfaceClass = VicePositionLoader.class)
    private VicePositionLoader vicePositionLoader;
    @ImportService(interfaceClass = VicePositionTypeLoader.class)
    private VicePositionTypeLoader vicePositionTypeLoader;
    @ImportService(interfaceClass = VicePositionTypeService.class)
    private VicePositionTypeService vicePositionTypeService;

    /**
     * 类型首页
     */
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String loadPositionTypeIndex(Model model) {
        //中文类型名称
        String chName = getRequestString("chName");
        //英文类型名称
        String enName = getRequestString("enName");
        if (StringUtils.isNotBlank(enName)) {
            enName = enName.toUpperCase();
        }

        List<VicePositionType> vicePositionTypes = vicePositionTypeLoader.loadVicePositionType(chName, enName);
        model.addAttribute("positionTypes", vicePositionTypes);

        return "/site/diamond/classifyindex";
    }

    /**
     * 类型内容
     */
    @RequestMapping(value = "/type.vpage", method = RequestMethod.GET)
    public String loadPositionType(Model model) {
        //主键ID
        String _id = getRequestString("id");
        VicePositionType vicePositionType = vicePositionTypeLoader.searchVicePositionType(_id, null, null);
        model.addAttribute("types", null==vicePositionType?new VicePositionType():vicePositionType);

        return "/site/diamond/addclassify";
    }

    /**
     * 添加类型
     */
    @RequestMapping(value = "/add.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage addPositionType(Model model) {
        //中文类型名称
        String chName = getRequestString("chName");
        //英文类型名称
        String enName = getRequestString("enName");
        if (StringUtils.isNotBlank(enName)) {
            enName = enName.toUpperCase();
        }

        try {
            validateParamNotNull("chName", "中文名称不允许为空");
            validateParamNotNull("enName", "英文名称不允许为空");
            //判断是否存在该类型
            VicePositionType vicePositionType = vicePositionTypeLoader.searchVicePositionType(null, null, enName);
            if (null != vicePositionType) {
                return MapMessage.errorMessage("添加失败，此类别已存在");
            }
            if (!isVerify(enName)) {
                return MapMessage.errorMessage("添加失败，英文名称不允许为空");
            } else {
                enName = enName.toUpperCase();
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("添加失败");
        }

        VicePositionType positionType = new VicePositionType();
        positionType.setChName(chName);
        positionType.setEnName(enName);
        positionType.setCreateTime(new Date());
        positionType.setUpdateTime(new Date());
        vicePositionTypeService.addVicePositionType(positionType);

        return MapMessage.successMessage("添加成功");
    }

    /**
     * 修改类型
     */
    @RequestMapping(value = "/update.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage updatePositionType(Model model) {
        //主键ID
        String _id = getRequestString("id");
        //中文类型名称
        String chName = getRequestString("chName");
        //英文类型名称
        String enName = getRequestString("enName");
        if (StringUtils.isNotBlank(enName)) {
            enName = enName.toUpperCase();
        }

        VicePositionType vicePositionType;
        try {
            validateParamNotNull("id", "唯一标识不允许为空");
            validateParamNotNull("chName", "中文类别不允许为空");
            validateParamNotNull("enName", "英文类别不允许为空");
            //判断是否存在该类型
            vicePositionType = vicePositionTypeLoader.searchVicePositionType(_id, null, null);
            if (null == vicePositionType) {
                return MapMessage.successMessage("修改成功");
            }
            if (!isVerify(enName)) {
                return MapMessage.errorMessage("修改失败，英文名称不允许为空");
            } else {
                enName = enName.toUpperCase();
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("修改失败失败");
        }

        vicePositionType.setChName(StringUtils.isBlank(chName)?vicePositionType.getChName():chName);
        vicePositionType.setEnName(StringUtils.isBlank(enName)?vicePositionType.getEnName():enName);
        vicePositionTypeService.updateVicePositionType(vicePositionType);

        return MapMessage.successMessage("修改成功");
    }

    /**
     * 删除分类标识
     */
    @RequestMapping(value = "/del.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage delPositionType(Model model) {
        //主键ID
        String _id = getRequestString("id");

        try {
            validateParamNotNull("id", "唯一标识不允许为空");
            VicePositionType vicePositionType = vicePositionTypeLoader.loadVicePositionTypeById(_id);
            if (null == vicePositionType) {
                return MapMessage.successMessage("删除成功");
            }
            List<VicePosition> vicePositions = vicePositionLoader.loadAllVicePosition();
            if (CollectionUtils.isNotEmpty(vicePositions)) {
                if (vicePositions.stream().filter(vicePosition -> vicePosition.getClassify().equals(vicePositionType.getEnName())).count() > 0) {
                    return MapMessage.errorMessage("存在关联关系，不允许删除！");
                }
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("删除失败");
        }
        vicePositionTypeService.delVicePositionType(_id);

        return MapMessage.successMessage("删除成功");
    }

    public boolean isVerify(String enMainTitle) {
        if (StringUtils.isNotBlank(enMainTitle)) {
            Pattern pattern = compile("^(?!_)(?!.*?_$)[a-zA-Z_]+$");
            Matcher matcher = pattern.matcher(enMainTitle);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
