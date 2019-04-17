package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.ambush.api.VicePositionLoader;
import com.voxlearning.galaxy.service.ambush.api.VicePositionService;
import com.voxlearning.galaxy.service.ambush.api.VicePositionTypeLoader;
import com.voxlearning.galaxy.service.ambush.api.entity.VicePosition;
import com.voxlearning.galaxy.service.ambush.api.entity.VicePositionType;
import com.voxlearning.utopia.admin.controller.diamond.AbstractPositionController;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.*;

/**
 * @author feng.guo
 * @since 2019-03-29
 */
@Controller
@RequestMapping(value = "/site/diamond")
public class VicePositionController extends AbstractPositionController {
    @Inject
    private CrmImageUploader crmImageUploader;
    @ImportService(interfaceClass = VicePositionLoader.class)
    private VicePositionLoader vicePositionLoader;
    @ImportService(interfaceClass = VicePositionService.class)
    private VicePositionService vicePositionService;
    @ImportService(interfaceClass = VicePositionTypeLoader.class)
    private VicePositionTypeLoader vicePositionTypeLoader;

    /**
     * 金刚位首页
     */
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String positionIndex(Model model) {
        //分类
        String classify = getRequestString("classify");
        //主标题
        String mainTitle = getRequestString("mainTitle");
        //是否上架
        String undercarriage = getRequestParameter("undercarriage", null);
        Boolean undercarr = null;
        if (StringUtils.isNotBlank(undercarriage)) {
            undercarr = Boolean.parseBoolean(undercarriage);
        }

        List<VicePosition> vicePositions = new LinkedList<>();
        List<VicePosition> vicePositionList = vicePositionLoader.loadVicePositionList(null, mainTitle, undercarr, classify);
        if (CollectionUtils.isNotEmpty(vicePositionList)) {
            vicePositionList.stream().forEach(vicePosition -> {
                VicePositionType vicePositionType = vicePositionTypeLoader.searchVicePositionType(null, null, vicePosition.getClassify());
                if (null != vicePositionType) {
                    vicePosition.setClassify(vicePositionType.getChName());
                }
                vicePositions.add(vicePosition);
            });
        }
        model.addAttribute("positionList", vicePositions);

        //查询分类
        searchTypes(model);

        return "/site/diamond/index";
    }

    /**
     * 但数据金刚位信息查询
     */
    @RequestMapping(value = "/position.vpage", method = RequestMethod.GET)
    public String searchVicePosition(Model model) {
        //主键ID
        String _id = getRequestString("id");
        //是否上架
        boolean undercarriage = getRequestBool("undercarriage");

        //查询分类
        searchTypes(model);

        //查询配置内容
        VicePosition vicePosition = vicePositionLoader.loadVicePositionById(_id, null, undercarriage);
        if (null != vicePosition) {
            String classify = vicePosition.getClassify();
            if (StringUtils.isNotBlank(classify)) {
                VicePositionType vicePositionType = vicePositionTypeLoader.searchVicePositionType(null, null, classify);
                if (null != vicePositionType) {
                    vicePosition.setClassify(vicePositionType.getChName());
                }
            }
        }
        model.addAttribute("vicePosition", null==vicePosition?new VicePosition():vicePosition);
        return "/site/diamond/audiamond";
    }

    /**
     * 查询分类
     */
    public void searchTypes(Model model) {
        //获取分类
        List<String> typeList;
        List<VicePositionType> vicePositionTypes = vicePositionTypeLoader.loadVicePositionType(null, null);
        if (CollectionUtils.isEmpty(vicePositionTypes)) {
            typeList = Collections.EMPTY_LIST;
        } else {
            typeList = vicePositionTypes.stream()
                    .map(VicePositionType::getChName)
                    .distinct()
                    .collect(Collectors.toList());
        }
        model.addAttribute("types", typeList);
    }

    /**
     * 添加金刚位位置信息
     */
    @RequestMapping(value = "/add.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage addPosition(Model model) {
        //排位
        Integer order = getRequestInt("order");
        //标签
        String lable = getRequestString("lable");
        //跳转路径
        String jumpUrl = getRequestString("jumpUrl");
        //附加标签
        String tagText = getRequestString("tagText");
        //图片地址
        String iconUrl = getRequestString("iconUrl");
        //类型
        String classify = getRequestString("classify");
        //主标题
        String mainTitle = getRequestString("mainTitle");
        //展示位置
        String disAddress = getRequestString("disAddress");
        //副标题
        String subheading = getRequestString("subheading");
        //标签颜色
        String labelColor = getRequestString("labelColor");
        //背景图片
        String backImgUrl = getRequestString("backImgUrl");
        //英文主标题
        String enMainTitle = getRequestString("enMainTitle");
        //登录可见
        Boolean loginStatus = getRequestBool("loginStatus");
        //跳转方式
        String functionType = getRequestString("functionType");
        //附加标签颜色
        String tagTextColor = getRequestString("tagTextColor");
        //是否下架
        Boolean undercarriage = getRequestBool("undercarriage");
        //自学习型
        String selfStudyType = getRequestString("selfStudyType");
        //予发布环境地址
        String stagingJumpUrl = getRequestString("stagingJumpUrl");

        try {
            validateParamNotNull("jumpUrl", "跳转地址不允许为空");
            validateParamNotNull("mainTitle", "主标题不允许为空");
            validateParamNotNull("enMainTitle", "英文主标题不允许为空");
            validateParamNotNull("functionType", "跳转地址类型不允许为空");
            VicePosition vicePosition = vicePositionLoader.loadVicePositionById(null, mainTitle, null);
            if (null != vicePosition) {
                return MapMessage.errorMessage("添加失败，配置已存在");
            }
            if (StringUtils.isBlank(classify)) {
                //默认不存在
                classify = "NON";
            } else {
                List<VicePositionType> vicePositionTypes = vicePositionTypeLoader.loadVicePositionType(classify, null);
                if (CollectionUtils.isEmpty(vicePositionTypes)) {
                    classify = "NON";
                } else {
                    classify = vicePositionTypes.get(0).getEnName();
                }
            }
            if (StringUtils.isBlank(disAddress)) {
                disAddress = "SP";
            }
            //设置默认排序
            if (order < 0) {
                order = 1;
            }
            if (!isVerify(enMainTitle)) {
                return MapMessage.errorMessage("添加失败，英文主标题格式已存在");
            } else {
                enMainTitle = enMainTitle.toUpperCase();
            }
            //判断英文主标题是否存在
            VicePosition vicePosition1 = vicePositionLoader.loadVicePositionByEMT(enMainTitle);
            if (null != vicePosition1) {
                return MapMessage.errorMessage("添加失败，英文主标题已存在");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("添加失败失败");
        }

        VicePosition position = new VicePosition();
        position.setLable(lable);
        position.setOrder(order);
        position.setTagText(tagText);
        position.setIconUrl(iconUrl);
        position.setJumpUrl(jumpUrl);
        position.setClassify(classify);
        position.setMainTitle(mainTitle);
        position.setSubheading(subheading);
        position.setDisAddress(disAddress);
        position.setCreateTime(new Date());
        position.setUpdateTime(new Date());
        position.setLabelColor(labelColor);
        position.setBackImgUrl(backImgUrl);
        position.setEnMainTitle(enMainTitle);
        position.setTagTextColor(tagTextColor);
        position.setFunctionType(functionType);
        position.setSelfStudyType(selfStudyType);
        position.setStagingJumpUrl(stagingJumpUrl);
        position.setLoginStatus(null==loginStatus?true:loginStatus);
        position.setUndercarriage(null==undercarriage?true:undercarriage);
        vicePositionService.addVicePosition(position);

        return MapMessage.successMessage("添加成功");
    }

    /**
     * 修改金刚位内容信息
     */
    @RequestMapping(value = "/update.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage updatePosition(Model model) {
        //主键
        String _id = getRequestString("id");
        //排位
        Integer order = getRequestInt("order");
        //标签
        String lable = getRequestString("lable");
        //跳转路径
        String jumpUrl = getRequestString("jumpUrl");
        //附加标签
        String tagText = getRequestString("tagText");
        //图片地址
        String iconUrl = getRequestString("iconUrl");
        //类型
        String classify = getRequestString("classify");
        //主标题
        String mainTitle = getRequestString("mainTitle");
        //展示位置
        String disAddress = getRequestString("disAddress");
        //副标题
        String subheading = getRequestString("subheading");
        //标签颜色
        String labelColor = getRequestString("labelColor");
        //背景图片
        String backImgUrl = getRequestString("backImgUrl");
        //英文主标题
        String enMainTitle = getRequestString("enMainTitle");
        //登录可见
        Boolean loginStatus = getRequestBool("loginStatus");
        //跳转方式
        String functionType = getRequestString("functionType");
        //附加标签颜色
        String tagTextColor = getRequestString("tagTextColor");
        //是否下架
        Boolean undercarriage = getRequestBool("undercarriage");
        //自学习型
        String selfStudyType = getRequestString("selfStudyType");
        //予发布环境地址
        String stagingJumpUrl = getRequestString("stagingJumpUrl");

        VicePosition vicePosition;
        try {
            validateParamNotNull("mainTitle", "主标题");
            validateParamNotNull("id", "唯一标识不允许为空");
            validateParamNotNull("jumpUrl", "跳转地址不允许为空");
            validateParamNotNull("enMainTitle", "英文主标题不允许为空");
            validateParamNotNull("functionType", "跳转地址类型不允许为空");
            vicePosition = vicePositionLoader.loadVicePositionById(_id, null, null);
            if (null == vicePosition) {
                return MapMessage.errorMessage("修改失败，查询不到改配置");
            }
            if (StringUtils.isBlank(classify)) {
                //默认不存在
                classify = "NON";
            } else {
                List<VicePositionType> vicePositionTypes = vicePositionTypeLoader.loadVicePositionType(classify, null);
                if (CollectionUtils.isEmpty(vicePositionTypes)) {
                    classify = "NON";
                } else {
                    classify = vicePositionTypes.get(0).getEnName();
                }
            }
            if (StringUtils.isBlank(disAddress)) {
                disAddress = "SP";
            }
            //设置默认排序
            if (order < 0) {
                order = 1;
            }
            if (!isVerify(enMainTitle)) {
                return MapMessage.errorMessage("修改失败，英文主标题格式已存在");
            } else {
                enMainTitle = enMainTitle.toUpperCase();
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("修改失败");
        }

        vicePosition.setId(_id);
        vicePosition.setOrder(order);
        vicePosition.setLable(lable);
        vicePosition.setTagText(tagText);
        vicePosition.setIconUrl(iconUrl);
        vicePosition.setJumpUrl(jumpUrl);
        vicePosition.setClassify(classify);
        vicePosition.setMainTitle(mainTitle);
        vicePosition.setSubheading(subheading);
        vicePosition.setDisAddress(disAddress);
        vicePosition.setBackImgUrl(backImgUrl);
        vicePosition.setLabelColor(labelColor);
        vicePosition.setUpdateTime(new Date());
        vicePosition.setEnMainTitle(enMainTitle);
        vicePosition.setLoginStatus(loginStatus);
        vicePosition.setFunctionType(functionType);
        vicePosition.setTagTextColor(tagTextColor);
        vicePosition.setUndercarriage(undercarriage);
        vicePosition.setSelfStudyType(selfStudyType);
        vicePosition.setStagingJumpUrl(stagingJumpUrl);
        vicePositionService.updateVicePosition(vicePosition);

        return MapMessage.successMessage("修改成功");
    }

    /**
     * 上架下架
     */
    @RequestMapping(value = "/endisable.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody MapMessage enablePosition(Model model) {
        //主键ID
        String _id = getRequestString("id");
        //是否上架
        boolean undercarriage = getRequestBool("undercarriage");
        //查询配置内容
        VicePosition vicePosition = vicePositionLoader.loadVicePositionById(_id, null, undercarriage);
        if (null != vicePosition) {
            vicePosition.setUndercarriage(!undercarriage);
            vicePositionService.updateVicePosition(vicePosition);
        } else {
            MapMessage.errorMessage("操作失败");
        }

        return MapMessage.successMessage("操作成功");
    }

    /**
     * 删除金刚位内容信息
     */
    @RequestMapping(value = "del.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage delPosition(Model model) {
        //主键ID
        String _id = getRequestString("id");

        try {
            validateParamNotNull("id", "唯一标识不允许为空");
        } catch (Exception e) {
            positionIndex(model);
        }

        //查询配置内容
        VicePosition vicePosition = vicePositionLoader.loadVicePositionById(_id, null, null);
        if (null != vicePosition) {
            vicePositionService.delVicePosition(_id);
        } else {
            MapMessage.errorMessage("删除失败");
        }

        return MapMessage.successMessage("删除成功");
    }

    /**
     * 上传图片
     */
    @RequestMapping(value = "uploadImage.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage uploadImages(@RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = file.getOriginalFilename();
        try {
            String prefix = "vicePosition";
            @Cleanup InputStream inStream = file.getInputStream();
            String filePath = crmImageUploader.upload(prefix, originalFileName, inStream);
            return MapMessage.successMessage().set("filePath", filePath);
        } catch (Exception ex) {
            return MapMessage.errorMessage("上传图片异常,Error:" + ex.getMessage());
        }
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
