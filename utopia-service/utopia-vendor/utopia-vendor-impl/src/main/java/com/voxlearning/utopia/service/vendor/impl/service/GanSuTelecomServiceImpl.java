package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportGenericService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderAmortizeHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderRefundServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.vendor.api.GansuTelecomService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = GansuTelecomService.class)
@ExposeService(interfaceClass = GansuTelecomService.class)
@Slf4j
public class GanSuTelecomServiceImpl implements GansuTelecomService {

    @Inject
    private UserLoginServiceClient userLoginServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private ParentServiceClient parentServiceClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private UserOrderServiceClient userOrderServiceClient;
    @Inject
    private UserOrderRefundServiceClient userOrderRefundServiceClient;

    @Inject
    private VendorUserLoaderImpl vendorUserLoader;
    @Inject
    private VendorUserServiceImpl vendorUserService;

    @ImportGenericService(interfaceClass = BusinessUserOrderServiceWrapper.class)
    private BusinessUserOrderServiceWrapper businessUserOrderServiceWrapper;

    @Override
    public Map<String, String> openService(String appKey, List<String> mobileList) {

        Map<String, String> procResult = new HashMap<>();

        // 创建家长和学生用户，并且绑定关系
        Map<Long, String> childMobile = createParentAndStudent(appKey, mobileList, procResult);

        // 开通服务
        if (!childMobile.isEmpty()) {
            openServiceForStudents(appKey, childMobile, procResult);
        }

        return procResult;
    }

    @Override
    public Map<String, String> closeService(String appKey, List<String> mobileList) {
        Map<String, String> procResult = new HashMap<>();

        List<String> productIds = getProducts();
        for (String mobile : mobileList) {
            User targetUser = userLoaderClient.loadUserByToken(mobile).stream()
                    .filter(p -> p.isStudent())
                    .findAny().orElse(null);
            if (targetUser == null) {
                procResult.put(mobile, "该手机号没有注册，不能退订服务！");
                continue;
            }

            //查询学生订阅的小U和网络视频订单进行退订
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserOrderList(targetUser.getId()).stream().
                    filter(o -> productIds.contains(o.getProductId())).
                    filter(o -> o.getPaymentStatus() == PaymentStatus.Paid).
                    collect(Collectors.toList());
            for (UserOrder userOrder : userOrderList) {
                Map<String, BigDecimal> items = new HashMap<>();
                List<UserOrderAmortizeHistory> amortizeList =
                        userOrderLoaderClient.loadOrderAmortizeHistory(userOrder.genUserOrderId());

                List<UserOrderAmortizeHistory> userOrderAmortizeHistoryList = amortizeList.stream().
                        filter(o -> o.getPaymentStatus() == PaymentStatus.Paid).
                        collect(Collectors.toList());

                userOrderAmortizeHistoryList.stream().forEach(o -> {
                    items.put(o.getProductItemId(), o.getPayAmount());
                });

                userOrderRefundServiceClient.refund(targetUser.getId(), userOrder.getId(), items,
                        SafeConverter.toString(targetUser.getId()), "退订三方服务");
            }

            vendorUserService.deleteVendorUserRef(targetUser.getId());

            // 记录USER_RECORD
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(targetUser.getId());
            userServiceRecord.setOperatorId(appKey);
            userServiceRecord.setOperatorName(appKey);
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单相关.name());
            userServiceRecord.setOperationContent("退订账号小U和视频服务");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            procResult.put(mobile, "success");
        }

        return procResult;
    }


    @Override
    public Long loadEffectiveUser(String appKey, String date) {

        Date checkDate = DateUtils.stringToDate(date, "yyyy-MM");
        if (checkDate == null || StringUtils.isEmpty(appKey)) {
            return 0L;
        }

        date = date.replace("-", "");

        long count = vendorUserLoader.queryEffectiveUser(appKey, date);

        return count;
    }

    private Map<Long, String> createParentAndStudent(String appKey, List<String> mobileList, Map<String, String> procResult) {

        Map<Long, String> childMobile = new HashMap<>();

        for (String mobile : mobileList) {
            // 判断手机号是否应注册了
            User parentUser = userLoaderClient.loadUserByToken(mobile).stream()
                    .filter(p -> p.isParent())
                    .findAny().orElse(null);

            String password = RandomUtils.randomString(6);
            if (parentUser == null) {
                // 注册家长
                NeonatalUser parentNeonatalUser = new NeonatalUser();
                parentNeonatalUser.setUserType(UserType.PARENT);
                parentNeonatalUser.setRoleType(RoleType.ROLE_PARENT);
                parentNeonatalUser.setPassword(password);
                parentNeonatalUser.setMobile(mobile);
                parentNeonatalUser.setCode("17abzy");
                parentNeonatalUser.setWebSource(appKey);
                MapMessage parentResult = userServiceClient.registerUser(parentNeonatalUser);
                parentUser = (User) parentResult.get("user");
            }

            List<User> stuUserList = userLoaderClient.loadUserByToken(mobile).stream()
                    .filter(p -> p.isStudent())
                    .collect(Collectors.toList());

            Long studentId = getStudentForOpen(appKey, mobile, password, stuUserList);

            // 绑定家长关系
            parentServiceClient.bindExistingParent(studentId, parentUser.getId(),
                    false, CallName.其它监护人.name());

            childMobile.put(studentId, mobile);
        }

        return childMobile;
    }

    private Long getStudentForOpen(String appKey, String mobile, String password, List<User> stuUserList) {
        if (CollectionUtils.isEmpty(stuUserList)) {
            // 注册学生
            NeonatalUser studentNeonatalUser = new NeonatalUser();
            studentNeonatalUser.setUserType(UserType.STUDENT);
            studentNeonatalUser.setRoleType(RoleType.ROLE_STUDENT);
            studentNeonatalUser.setPassword(password);
            studentNeonatalUser.setMobile(mobile);
            studentNeonatalUser.setCode("17abzy");
            studentNeonatalUser.setWebSource(appKey);
            MapMessage studentResult = userServiceClient.registerUser(studentNeonatalUser);
            User studentUser = (User) studentResult.get("user");
            return studentUser.getId();
        } else if (stuUserList.size() == 1) {
            return stuUserList.get(0).getId();
        } else {
            List<Long> userIdList = stuUserList.stream().map(u -> u.getId()).collect(Collectors.toList());
            Map<Long, Date> loginTime = userLoginServiceClient.getUserLoginService()
                    .findUserLastLoginTime(userIdList);
            Long studentId =0L;

            if (loginTime != null && CollectionUtils.isNotEmpty(loginTime.keySet())) {
                Date lastLoginDate = null;
                for (Long userId : loginTime.keySet()) {
                    if (lastLoginDate == null) {
                        lastLoginDate = loginTime.get(userId);
                        studentId = userId;
                        continue;
                    }

                    if (lastLoginDate.before(loginTime.get(userId))) {
                        lastLoginDate = loginTime.get(userId);
                        studentId = userId;
                    }
                }
            } else {
                Collections.sort(stuUserList);
                studentId = stuUserList.get(stuUserList.size() - 1).getId();
            }
            return studentId;
        }
    }

    /**
     * 给幼儿开通服务(小U智能同步练 90天（语数外）、精品网络课程)
     *
     * @param appKey
     * @param childMobile
     * @param procResult
     */
    private void openServiceForStudents(String appKey, Map<Long, String> childMobile, Map<String, String> procResult) {

        // 要开通的产品列表
        List<String> productIdList = getProducts();
        String productType = userOrderLoaderClient.loadOrderProductById(productIdList.get(0)).getProductType();

        for (Long studentId : childMobile.keySet()) {

            // 创建订单
            MapMessage appOrder = userOrderServiceClient.createAppOrder(studentId, productType, productIdList, "");
            if (!appOrder.isSuccess()) {
                procResult.put(childMobile.get(studentId), appOrder.getInfo());
                continue;
            }

            // 完成后台支付处理
            businessUserOrderServiceWrapper.processUserOrderPayment(SafeConverter.toString(appOrder.get("orderId")),
                    BigDecimal.ZERO,
                    "",
                    "");


            // 存储用户关系
            vendorUserService.saveVendorUserRef(appKey, studentId, productIdList);

            procResult.put(childMobile.get(studentId), "success");

            // 记录USER_RECORD
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(appKey);
            userServiceRecord.setOperatorName(appKey);
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单相关.name());
            userServiceRecord.setOperationContent("创建账号并且开通小U和视频服务");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
    }

    private List<String> getProducts() {
        List<String> productIdList = new ArrayList<>();
        productIdList.add("5876f5067445fb1be06c25ac");
        productIdList.add("58784bb4e92b1b99cf707f8a");
        productIdList.add("5878536fe92b1b99cf708083");
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            productIdList.add("5c2ed0a8ced6cf384b02ee53");
        } else {
            productIdList.add("5c2dc447ac7459afb4f5446a");
        }
        return productIdList;
    }


}
