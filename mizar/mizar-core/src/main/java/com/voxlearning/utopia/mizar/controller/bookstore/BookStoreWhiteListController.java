package com.voxlearning.utopia.mizar.controller.bookstore;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.entity.bookStore.*;
import com.voxlearning.utopia.mizar.utils.HydraCorsairSupport;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yong.he
 */
@Controller
@RequestMapping(value = "/bookstore/manager/whiteList")
public class BookStoreWhiteListController extends AbstractMizarController {

    @Inject
    HydraCorsairSupport hydraCorsairSupport;

    /**
     * 查看白名单列表index
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String whiteList(Model model) {
        Long totalElements = 0L;
        MizarAuthUser user = getCurrentUser();
        Page<XueMizarWhiteListExtendBean> whiteListExtendBeanPage;
        List<XueMizarWhiteListExtendBean> whiteListExtendBeanList = new ArrayList<>();
        int page = getRequestInt("page");
        // 最小从1开始
        page = Math.max(1, page);
        Pageable pageRequest = PageableUtils.startFromOne(page, 20);
        if (!user.isAdmin()) {
            return "bookstore/whiteList";
        }
        //查询白名单列表
        //目前白名单只有一种类型，之后多了再建枚举
        XueMizarWhiteListBean listInfo = hydraCorsairSupport.loadPageMizarWhiteList(0, page, 20);
        if (listInfo != null) {
            whiteListExtendBeanList = listInfo.getWhiteListExtendBeans();
            totalElements = listInfo.getTotalElements();
            whiteListExtendBeanList = convertWhiteList(whiteListExtendBeanList);
        }
        whiteListExtendBeanPage = new PageImpl<>(whiteListExtendBeanList, pageRequest, totalElements);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", whiteListExtendBeanPage.getTotalPages());
        model.addAttribute("whiteListExtendBeanPage", whiteListExtendBeanPage);
        return "bookstore/whiteList";
    }

    /**
     * 新增、修改白名单信息
     */
    @RequestMapping(value = "add.vpage")
    @ResponseBody
    public MapMessage add() {
        Long id = getRequestLong("id");
        //目前白名单只有一种类型，之后多了再建枚举
        int type = 0;
        String content = getRequestString("content");
        String remark = getRequestString("remark");
        String createMizarUserId = getRequestString("createMizarUserId");
        String updateMizarUserId = getRequestString("updateMizarUserId");
        MizarAuthUser currentUser = getCurrentUser();
        //如果不是管理员进行新增修改
        if (!currentUser.isAdmin()) {
            return MapMessage.errorMessage("无权操作");
        }
        MapMessage checkResult = checkContent(content,id);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        if (remark.length() > 100) {
            return MapMessage.errorMessage("说明文案过长");
        }
        if (id <= 0L) {
            //新增
            createMizarUserId = currentUser.getUserId();
            id = null;
        } else {
            XueMizarWhiteListExtendBean whiteListExtendBean = hydraCorsairSupport.loadWhiteListById(id);
            if (whiteListExtendBean == null || ConversionUtils.toLong(whiteListExtendBean.getId()) == 0L) {
                return MapMessage.errorMessage("修改失败，该条白名单信息不存在");
            }
            updateMizarUserId = currentUser.getUserId();
        }
        XueMizarWhiteListExtendBean whiteListExtendBean = new XueMizarWhiteListExtendBean();
        whiteListExtendBean.setId(id);
        whiteListExtendBean.setType(type);
        whiteListExtendBean.setContent(content);
        whiteListExtendBean.setRemark(remark);
        whiteListExtendBean.setCreateMizarUserId(createMizarUserId);
        whiteListExtendBean.setUpdateMizarUserId(updateMizarUserId);
        Boolean result = hydraCorsairSupport.addOrEditWhiteList(whiteListExtendBean);
        if (result) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * 新增修改白名单页面
     */
    @RequestMapping(value = "upsert.vpage", method = RequestMethod.GET)
    public String upsert(Model model) {
        Long id = getRequestLong("id");
        if (ConversionUtils.toLong(id) > 0) {
            XueMizarWhiteListExtendBean whiteListExtendBean;
            whiteListExtendBean = hydraCorsairSupport.loadWhiteListById(id);

            model.addAttribute("isNew", false);
            model.addAttribute("whiteListExtendBean", whiteListExtendBean);
            return "bookstore/whiteView";
        }
        model.addAttribute("isNew", true);
        return "bookstore/whiteView";
    }

    public List<XueMizarWhiteListExtendBean> convertWhiteList(List<XueMizarWhiteListExtendBean> whiteListExtendBeans) {
        List<XueMizarWhiteListExtendBean> tempList = new ArrayList<>();
        if (CollectionUtils.isEmpty(whiteListExtendBeans)) {
            return tempList;
        }
        Set<String> userIds = new HashSet<>();
        Set<String> createMizarUserIds = whiteListExtendBeans.stream().map(XueMizarWhiteListExtendBean::getCreateMizarUserId).collect(Collectors.toSet());
        Set<String> updateMizarUserIds = whiteListExtendBeans.stream().map(XueMizarWhiteListExtendBean::getUpdateMizarUserId).collect(Collectors.toSet());
        userIds.addAll(createMizarUserIds);
        userIds.addAll(updateMizarUserIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return tempList;
        }
        Map<String, MizarUser> userMap = mizarUserLoaderClient.loadUsers(userIds);
        for (XueMizarWhiteListExtendBean extendBean : whiteListExtendBeans) {
            String operationUserName;
            Date operationTime;
            Integer bookStoreNum = 0;
            if (StringUtils.isNotBlank(extendBean.getUpdateMizarUserId())){
                operationUserName = (userMap.get(extendBean.getUpdateMizarUserId()) == null) ? "" : userMap.get(extendBean.getUpdateMizarUserId()).getRealName();
                operationTime = extendBean.getUpdateDateTime();
            }else {
                operationUserName = (userMap.get(extendBean.getCreateMizarUserId()) == null) ? "" : userMap.get(extendBean.getCreateMizarUserId()).getRealName();
                operationTime = extendBean.getCreateDateTime();
            }
            if (StringUtils.isNotBlank(extendBean.getContent())){
                bookStoreNum = extendBean.getContent().split(",").length;
            }
            extendBean.setOperationTime(operationTime);
            extendBean.setOperationUserName(operationUserName);
            extendBean.setBookStoreNum(bookStoreNum);
            tempList.add(extendBean);
        }
        return tempList;
    }

    private MapMessage checkContent(String content,Long id){
        List<Long> bookStoreIds = StringUtils.toList(content,Long.class);
        if(CollectionUtils.isEmpty(bookStoreIds)){
            return MapMessage.errorMessage("内容不可为空");
        }
        if(content.length() > 500){
            return MapMessage.errorMessage("内容过长");
        }
        for (int i = 0; i < bookStoreIds.size(); i++) {
            if (ConversionUtils.toLong(bookStoreIds.get(i)) == 0L){
                return MapMessage.errorMessage("门店ID填写有误，请检查后重新填写。格式错误:" + bookStoreIds.get(i));
            }
        }
        String result = hydraCorsairSupport.checkInvalidBookStoreIds(bookStoreIds);
        if (StringUtils.isNotBlank(result) && !Objects.equals(result, "[]")){
            return MapMessage.errorMessage("门店ID填写有误，请检查后重新填写。书店id不存在:" + result);
        }
        String repeatResult = hydraCorsairSupport.checkRepeatBookStoreIds(bookStoreIds,id);
        if (StringUtils.isNotBlank(repeatResult)){
            return MapMessage.errorMessage("门店ID填写有误，请检查后重新填写。书店id重复:" + repeatResult);
        }

        return MapMessage.successMessage();
    }

}
