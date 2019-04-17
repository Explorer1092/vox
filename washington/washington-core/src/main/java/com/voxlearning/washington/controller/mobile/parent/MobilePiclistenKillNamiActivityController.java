package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatUserRef;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.utopia.service.piclisten.api.PiclistenKillNamiActivityService;
import com.voxlearning.utopia.service.piclisten.api.mapper.PiclistenKillNamiActivityContext;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author jiangpeng
 * @since 2018-08-22 下午6:54
 **/
@Controller
@RequestMapping(value = "/parentMobile/piclisten_assist/")
public class MobilePiclistenKillNamiActivityController extends AbstractMobileController {


    @ImportService(interfaceClass = PiclistenKillNamiActivityService.class)
    private PiclistenKillNamiActivityService piclistenKillNamiActivityService;

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;

    @RequestMapping(value = "/check_parent_login.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage checkLogin() {
        User user = currentParent();
        if (user != null) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "/select.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage select() {
        PiclistenKillNamiActivityContext context = buildContext();
        String bookIdStr = getRequestString("book_ids");
        List<String> bookIdList = JsonUtils.fromJsonToList(bookIdStr, String.class);
        return piclistenKillNamiActivityService.changeSelectBooks(context, bookIdList);
    }

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage index() {
        PiclistenKillNamiActivityContext context = buildContext();
        String productId = getRequestString("product_id");
        return piclistenKillNamiActivityService.recBooks(context, productId);
    }

    @RequestMapping(value = "/create_assist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createAssist() {
        PiclistenKillNamiActivityContext context = buildContext();
        if (!context.isLogin()) {
            return noLoginResult;
        }
        String productId = getRequestString("product_id");
        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("产品 id 丢失！");
        }
        try {
            return AtomicLockManager.getInstance()
                    .wrapAtomic(piclistenKillNamiActivityService)
                    .keyPrefix("piclistenKillNamiCreateAssist")
                    .keys(context.getLoginUser().getId())
                    .proxy()
                    .createAssist(context, productId);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，稍后再试！");
        }
    }

    @RequestMapping(value = "/assist_members.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage assistMembers() {
        PiclistenKillNamiActivityContext context = buildContext();
        if (!context.isLogin()) {
            return noLoginResult;
        }
        String assistId = getRequestString("assist_id");
        if (StringUtils.isBlank(assistId)) {
            return MapMessage.errorMessage("助力 id 错误");
        }
        return piclistenKillNamiActivityService.assistMembers(context, assistId);
    }

    @RequestMapping(value = "/assist_detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage assistDetail() {
        String assistId = getRequestString("assist_id");
        if (StringUtils.isBlank(assistId)) {
            return MapMessage.errorMessage("助力 id 丢啦！");
        }
        String openId = getRequestString("open_id");

        return piclistenKillNamiActivityService.assistDetail(assistId, openId);
    }


    @RequestMapping(value = "/do_assist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage doAssist() {
        String assistId = getRequestString("assist_id");
        if (StringUtils.isBlank(assistId)) {
            return MapMessage.errorMessage("助力 id 丢啦！");
        }
        String openId = getRequestString("open_id");
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId 丢啦！");
        }
        try {
            return AtomicLockManager.getInstance()
                    .wrapAtomic(piclistenKillNamiActivityService)
                    .keyPrefix("piclistenKillNamiDoAssist")
                    .keys(openId)
                    .proxy()
                    .doAssist(assistId, openId);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，稍后再试！");
        }
    }

    @RequestMapping(value = "/publisher_list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage publisherList() {
        String subjectStr = getRequestString("subject");
        Subject subject = Subject.safeParse(subjectStr);
        return piclistenKillNamiActivityService.publisherList(subject);
    }

    @RequestMapping(value = "/book_list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookList() {
        String subjectStr = getRequestString("subject");
        Subject subject = Subject.safeParse(subjectStr);
        if (subject != Subject.CHINESE && subject != Subject.ENGLISH) {
            return MapMessage.errorMessage("学科错误");
        }
        String publisherName = getRequestString("publisher_name");
        if (StringUtils.isBlank(publisherName)) {
            return MapMessage.errorMessage("出版社错误");
        }
        Integer clazzLevel = getRequestInt("clazz_level", 3);
        String sys = getRequestParameter("sys", "ios");

        return piclistenKillNamiActivityService.bookList(subject, publisherName, clazzLevel, sys);
    }

    @RequestMapping(value = "/purchase_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage purchaseInfo() {
        PiclistenKillNamiActivityContext context = buildContext();
        if (!context.isLogin() || !context.loginUserIsParent()) {
            return noLoginResult;
        }
        String orderId = getRequestString("order_id");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("订单id丢失");
        }
        return piclistenKillNamiActivityService.purchaseInfo(context, orderId);
    }

    @RequestMapping(value = "/send_coupon.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendCoupon() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        String productId = getRequestString("product_id");
        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("product_id不能为空");
        }
        Long shareRecord = piclistenKillNamiActivityService.loadShareRecord(parent.getId(), productId);
        if (shareRecord != 0L) {
            return MapMessage.successMessage();
        } else {
            MapMessage mapMessage = piclistenKillNamiActivityService.sendCouponForShare(productId, parent.getId());
            if (mapMessage.isSuccess()) {
                piclistenKillNamiActivityService.addShareRecord(parent.getId(), productId);
            }
            return mapMessage;
        }
    }


    @RequestMapping(value = "/send_new_coupon.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendNewCoupon() {
        Long parentId = getParentIdForWechatOrUser();
        if (parentId == 0L) {
            return noLoginResult;
        }
        Long shareRecord = piclistenKillNamiActivityService.loadNewRecord(parentId);
        if (shareRecord != 0L) {
            return MapMessage.successMessage();
        } else {
            MapMessage mapMessage = piclistenKillNamiActivityService.sendNewCoupon(parentId);
            if (mapMessage.isSuccess()) {
                piclistenKillNamiActivityService.addNewRecord(parentId);
            }
            return mapMessage;
        }
    }

    @RequestMapping(value = "/new_book_list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage newBookList() {
//        User user = currentParent();
//        if (user == null) {
//            return noLoginResult;
//        }
        int clazzLevel = getRequestInt("clazz_level");
        PiclistenKillNamiActivityContext context = buildContext();
        String publisherName = getRequestString("publisher_name");
        if (StringUtils.isBlank(publisherName)) {
            return MapMessage.errorMessage("出版社错误");
        }
        String sys = getRequestParameter("sys", "ios");
        Boolean isPackage = getRequestBool("is_package");
        Boolean hasClazzLevel = getRequestBool("has_clazz_level");
        Boolean isSameSubject = getRequestBool("is_same_subject");
        return piclistenKillNamiActivityService.newBookList(publisherName, clazzLevel, sys, context, isPackage, hasClazzLevel, isSameSubject);
    }

    @RequestMapping(value = "/buyer_count.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage buyerCount() {
        Long buyerCount = picListenCommonService.loadPicListenPurchaseCount();
        return MapMessage.successMessage().add("buyer_count", buyerCount);
    }


    @RequestMapping(value = "/has_pop_up.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage hasPopUp() {
        Long parentId = getParentIdForWechatOrUser();
        if (parentId == 0L) {
            return MapMessage.successMessage();
        }
        Long shareRecord = piclistenKillNamiActivityService.loadNewRecord(parentId);
        if (shareRecord != 0L) {
            return MapMessage.errorMessage();
        } else {
            return MapMessage.successMessage();
        }
    }

    private PiclistenKillNamiActivityContext buildContext() {
        User user = currentUser();
        boolean isLogin = user != null;
        StudentDetail studentDetail = null;
        if (isLogin) {
            if (user.isParent()) {
                long studentId = getRequestLong("sid");
                if (studentId != 0) {
                    studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                }
            } else if (user.isStudent()) {
                studentDetail = StudentDetail.class.cast(user);
            }
        }
        return new PiclistenKillNamiActivityContext(user, studentDetail, getRequestString("sys"), getCdnBaseUrlStaticSharedWithSep());
    }


    private long getParentIdForWechatOrUser() {
        User parent = currentParent();
        Long parentId;
        if (parent == null) {
            String openId = getRequestString("open_id");
            if (StringUtils.isBlank(openId)) {
                return 0L;
            }
            WechatUserRef wechatUserRef = dpWechatLoader.getWechatUserRef(openId);
            if (wechatUserRef == null) {
                return 0L;
            }
            parentId = wechatUserRef.getUserId();
        } else {
            parentId = parent.getId();
        }

        return parentId;
    }
}
