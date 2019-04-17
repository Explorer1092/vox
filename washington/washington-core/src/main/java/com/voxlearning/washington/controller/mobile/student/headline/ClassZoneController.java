package com.voxlearning.washington.controller.mobile.student.headline;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.zone.api.constant.FieldName;
import com.voxlearning.utopia.service.zone.api.entity.*;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossGroupRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossSelfRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.RewordResponse;
import com.voxlearning.utopia.service.zone.api.entity.boss.StudentInfo;
import com.voxlearning.utopia.service.zone.api.entity.giving.*;
import com.voxlearning.utopia.service.zone.api.entity.plot.DailySertenceModifySignConfig;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.client.ClassCircleServiceClient;
import com.voxlearning.utopia.vo.ActivityRank;
import com.voxlearning.utopia.vo.StudentVO;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_KEY;

/**
 * @author chensn
 * @date 2018-10-23 17:13
 */
@ControllerMetric
@Controller
@RequestMapping(value = "/class/circle")
public class ClassZoneController extends AbstractMobileController {

  @Inject
  private ClassCircleServiceClient classCircleServiceClient;

  /**
   * 每日一句
   */
  @RequestMapping(value = "/daily/sentence.vpage")
  @ResponseBody
  public MapMessage dailySentence() {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    WeekDailySentence dailySentences = null;
    try {
      dailySentences = classCircleServiceClient.getClassCircleService()
          .getDailySentence(currentUserId());
    } catch (Exception e) {
      logger.info("班级圈获取每日一句异常{}", e);
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    StudentDetail studentDetail = currentStudentDetail();

    MapMessage mapMessage = new MapMessage();
    mapMessage.set("dailySentences", dailySentences).
        set("clazz", clazz.getClassName()).
        set("grade", clazz.getClazzLevel().getDescription()).
        set("currentDate", new Date());
    return mapMessage.setSuccess(true);
  }

  /**
   * 每日一句签到
   */
  @RequestMapping(value = "/sign.vpage")
  @ResponseBody
  public MapMessage sign() {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = null;
    try {
      mapMessage = classCircleServiceClient.getClassCircleService()
          .saveUserSignIn(currentUserId());
    } catch (Exception e) {
      logger.info("班级圈签到失败{}", e);
    }
    return mapMessage == null ? MapMessage.successMessage() : mapMessage;
  }

  /**
   * 获取讨论区内容
   */
  @RequestMapping(value = "discuss.vpage")
  @ResponseBody
  public MapMessage loadDiscuss() {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }

    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    List<DiscussZone> usedDiscuss;
    boolean isInBlankList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(currentStudentDetail(), "ClassZoneDiscuss", "BlackList");
    if (currentUserId() == 3921029 || isInBlankList) {
      usedDiscuss = classCircleServiceClient.getClassCircleService()
              .getById(2);
    } else {
      usedDiscuss = classCircleServiceClient.getClassCircleService()
              .findUsedDiscuss();
    }
    if (CollectionUtils.isEmpty(usedDiscuss)) {
      return MapMessage.successMessage("暂时没有讨论区");
    }
    //查询该活动的同班同学
    usedDiscuss = usedDiscuss.stream().map(e -> {
      List <Map <String, String>> users = new ArrayList <>();
      List <DiscussZoneUserRecord> discussRecords = classCircleServiceClient.getClassCircleService()
          .getDiscussRecord(clazz.getId(), e.getId());
      discussRecords.stream().limit(6).forEach(dr -> {
        Map <String, String> userMap = new HashMap <>();
        long userId = SafeConverter.toLong(dr.getId().split("_")[2]);
        userLoaderClient.loadUsersIncludeDisabled(Arrays.asList(userId)).values()
                .stream().findFirst().ifPresent(user -> {
          userMap.put("pic", getUserAvatarImgUrl(user.fetchImageUrl()));
          userMap.put("name", user.fetchRealname());
        });
        users.add(userMap);
      });
      if (users.size() < 6) {
        //小于6个用户 从同班同学取
        List <Long> userIds = studentLoaderClient.loadClazzStudentIds(clazz.getId());
        userIds = userIds.stream().limit(6 - users.size()).collect(Collectors.toList());
        List <Map <String, String>> clazzUsers = userLoaderClient.loadUsersIncludeDisabled(userIds)
            .values().stream().map(clazzUser -> {
              Map <String, String> userMap = new HashMap <>();
              userMap.put("pic", getUserAvatarImgUrl(clazzUser.fetchImageUrl()));
              userMap.put("name", clazzUser.fetchRealname());
              return userMap;
            }).collect(Collectors.toList());
        users.addAll(clazzUsers);
      }
      e.setUser(users);
      return e;
    }).collect(Collectors.toList());
    return MapMessage.successMessage().add("result", usedDiscuss).add("systemTime", new Date());
  }

  /**
   * 进入讨论区
   */
  @RequestMapping(value = "saveDiscussRecord.vpage")
  @ResponseBody
  public MapMessage saveDiscussRecord(Integer discussId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }

    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    classCircleServiceClient.getClassCircleService()
        .saveOrUpdateDiscussRecord(currentUser().getId(), clazz.getId(), discussId);
    return MapMessage.successMessage();
  }


  /**
   * 活动列表
   */
  @RequestMapping(value = "activityList.vpage")
  @ResponseBody
  public MapMessage activityList() {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }

    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    List <ClazzActivity> list = classCircleServiceClient.getClazzActivityService()
            .getList(currentUser().getId(), clazz.getSchoolId(), clazz.getId());
    return MapMessage.successMessage().add("result", list).add("systemTime", new Date());
  }

  /**
   * 完成活动
   */
  @RequestMapping(value = "joinActivity.vpage")
  @ResponseBody
  public MapMessage joinActivity(Integer activityId) {

    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    ClazzActivityRecord userRecord = classCircleServiceClient.getClazzActivityService()
        .findUserRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId);
    if (userRecord != null && userRecord.getStatus() == 0) {
      //已经完成返回前端数据
      MapMessage mapMessage = MapMessage.successMessage();
      if (userRecord.getActivityId() == 1) {
        //万圣节活动 返回结果
        mapMessage.add("result", userRecord.getCondition());
      }
      return mapMessage.add("status", 0);
    }
    classCircleServiceClient.getClazzActivityService()
        .addOrUpdateRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId, 1,
            null);

    return MapMessage.successMessage().add("status", 1);
  }

  /**
   * 完成活动
   */
  @RequestMapping(value = "completeActivity.vpage")
  @ResponseBody
  public MapMessage completeActivity(Integer activityId) {

    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    String data = getRequestString("data");
    Map <String, Object> map = null;
    if (StringUtils.isNoneBlank(data)) {
      map = JSON.parseObject(data, Map.class);
    }
    classCircleServiceClient.getClazzActivityService()
        .addOrUpdateRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId, 0,
            map);
    return MapMessage.successMessage();
  }

  /**
   * 保存万圣节活动人数
   */
  @RequestMapping(value = "saveHalloweenCount.vpage")
  @ResponseBody
  public MapMessage saveHalloweenCount(Integer type) {
    classCircleServiceClient.getClazzActivityService().increase(type);

    return MapMessage.successMessage();
  }

  /**
   * 查询万圣节活动人数
   */
  @RequestMapping(value = "queryHalloweenCount.vpage")
  @ResponseBody
  public MapMessage queryHalloweenCount() {
    Map <String, Long> map = classCircleServiceClient.getClazzActivityService().loadLikedCounts();

    return MapMessage.successMessage().set("info", map);
  }

  /**
   * 获取排名前10的同学
   */
  @RequestMapping(value = "querySchoolStudentCount.vpage")
  @ResponseBody
  public MapMessage querySchoolStudentCount(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    Map <String, Object> map = classCircleServiceClient.getClazzActivityService()
            .findBySchooldId(clazz.getSchoolId(), activityId);

    List <StudentVO> list = (List <StudentVO>) map.get("studentVOList");
    list = list.stream().map(e -> {
      User user = userLoaderClient.loadUsersIncludeDisabled(Arrays.asList(e.getUserId())).values().stream().findFirst().orElse(null);
      e.setPic(getUserAvatarImgUrl(user.fetchImageUrl()));
      e.setUserName(user.fetchRealname());
      return e;
    }).collect(Collectors.toList());
    ClazzActivityRecord userRecord = classCircleServiceClient.getClazzActivityService().findUserRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId);
    return MapMessage.successMessage().add("info", map).add("result", userRecord == null ? null : userRecord.getCondition());
  }


  /**
   * 获取当前学生所在班级
   */
  private Clazz getCurrentClazz() {
    return currentStudentDetail() == null ? null : currentStudentDetail().getClazz();
  }

  /**
     * 获取当前学生没有 领取的奖励list
     */
    @RequestMapping(value = "findAwardList.vpage")
    @ResponseBody
    public MapMessage findAwardList(Integer activityId) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = currentUserId();
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        List<RewordResponse> rewordList = classCircleServiceClient.getClassCircleBossService().findRewardList(activityId, studentId, clazz.getId(), clazz.getSchoolId());
        return MapMessage.successMessage().add("value",rewordList).add("systemTime",new Date());

    }

    /**
     *
     * */
    @RequestMapping(value = "userClazzActivityRecord.vpage")
    @ResponseBody
    private MapMessage userClazzActivityRecord(Integer activityId) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        Map<String, Object> map = classCircleServiceClient.getClassCircleBossService().userClazzActivityRecord(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
        if (map == null) {
            return MapMessage.errorMessage().add("data", new HashMap<String, Object>()).add("systemTime", new Date());
        }
        List<ClazzBossSelfRecord> clazzBossSelfRecords = (List<ClazzBossSelfRecord>) map.get("selfList");
        List<ClazzBossGroupRecord> clazzBossGroupRecords = (List<ClazzBossGroupRecord>) map.get("clazzList");
        if (clazzBossSelfRecords == null || clazzBossGroupRecords == null || clazzBossSelfRecords.isEmpty() || clazzBossGroupRecords.isEmpty())
            return MapMessage.errorMessage().add("data", map).add("systemTime", new Date());
        clazzBossSelfRecords.forEach(item -> item.getStuList()
                .forEach(stu -> stu.setPic(getUserAvatarImgUrl(stu.getPic()))));
        return MapMessage.successMessage().add("data", map).add("systemTime", new Date());
    }

    /**
     * 获取用户昨天排名奖励
     * */
    @RequestMapping(value = "findEveryDayReward.vpage")
    @ResponseBody
    private MapMessage findEveryDayReward(Integer activityId) {
        if (studentUnLogin()) {
          return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
          return MapMessage.errorMessage("您还没有加入班级");
        }
        return classCircleServiceClient.getClassCirclePlotService().findEveryDayReward(activityId, currentUserId(),clazz.getSchoolId(),clazz.getId());

    }

    /**
     * 获取班级进度
     * */
    @RequestMapping(value = "findClazzProgressList.vpage")
    @ResponseBody
    private MapMessage findClazzProgressList(Integer activityId) {
        if (studentUnLogin()) {
          return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
          return MapMessage.errorMessage("您还没有加入班级");
        }
        MapMessage mapMessage = classCircleServiceClient.getClassCirclePlotService().findClazzProgressList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
        Object contList = mapMessage.get("contList");
        if (contList != null){
            List<String> imgList = (List<String>) contList;
            if (imgList.size() > 0){
                List<String> newImgList = new ArrayList<>();
                imgList.forEach(item -> newImgList.add(getUserAvatarImgUrl(item)));
                mapMessage.put("contList",newImgList);
            }
        }
        return mapMessage;
    }

    /**
     * 获取个人奖励弹窗列表
     * */
    @RequestMapping(value = "findRewardList.vpage")
    @ResponseBody
    private MapMessage findRewardList(Integer activityId) {
      if (studentUnLogin()) {
        return MapMessage.errorMessage("请重新登录");
      }
      Clazz clazz = getCurrentClazz();
      if (null == clazz) {
        return MapMessage.errorMessage("您还没有加入班级");
      }
      MapMessage mapMessage = classCircleServiceClient.getClassCirclePlotService().findRewardList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
      Object contList = mapMessage.get("contList");
      if (contList != null){
        List<String> imgList = (List<String>) contList;
        if (imgList.size() > 0){
          List<String> newImgList = new ArrayList<>();
          imgList.forEach(item -> newImgList.add(getUserAvatarImgUrl(item)));
          mapMessage.put("contList",newImgList);
        }
      }
      return mapMessage;
    }

    /**
     *  获取同班同学感谢列表
     * */
    @RequestMapping(value = "findThankList.vpage")
    @ResponseBody
    private MapMessage findThankList(Integer activityId) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        return classCircleServiceClient.getClassCirclePlotService().findThankList(activityId,currentUserId(),clazz.getSchoolId(),clazz.getId());
    }

    /**
     *  赠送礼物
     * */
    @RequestMapping(value = "gaveGift.vpage")
    @ResponseBody
    private MapMessage gaveGift(Integer activityId,Long gaveUserId,Integer type) {
      if (studentUnLogin()) {
        return MapMessage.errorMessage("请重新登录");
      }
      Clazz clazz = getCurrentClazz();
      if (null == clazz) {
        return MapMessage.errorMessage("您还没有加入班级");
      }
      return classCircleServiceClient.getClassCirclePlotService().gaveGift(activityId,currentUserId(),gaveUserId,type);
    }

    /**
     *  批量赠送礼物
     * */
    @RequestMapping(value = "batchGaveGift.vpage")
    @ResponseBody
    private MapMessage batchGaveGift(Integer activityId,String gaveUserId,Integer type) {
      if (studentUnLogin()) {
        return MapMessage.errorMessage("请重新登录");
      }
      Clazz clazz = getCurrentClazz();
      if (null == clazz) {
        return MapMessage.errorMessage("您还没有加入班级");
      }
        List<Long> longs = JsonUtils.fromJsonToList(gaveUserId, Long.class);
        return classCircleServiceClient.getClassCirclePlotService().batchGaveGift(activityId,currentUserId(),longs,type);
    }

  // 生成订单并跳转到支付确认页
  @RequestMapping(value = "/order/create.vpage")
  public String submitOrder(Model model) {
    boolean mobile = isMobileRequest(getRequest());
    model.addAttribute("hideTopTitle", getRequestBool("hideTopTitle", false)); // 是否显示h5页面内的顶部title和返回
    model.addAttribute("hideAppTitle", getRequestBool("hideAppTitle", false));
    model.addAttribute("returnUrl", getRequestString("returnUrl"));
    model.addAttribute("appType", getRequestString("appType"));
    String appKey = getRequestString(REQ_APP_KEY);
    if (StringUtils.isNotBlank(appKey)) {
      model.addAttribute("appKey", appKey);
    } else {
      model.addAttribute("appKey", "");
    }

    try {
      // 判断用户登陆状态
      if (studentUnLogin()) {
        if (mobile) {
          model.addAttribute("error", "请使用学生帐号登录");
          return "/paymentmobile/confirm";
        } else {
          return "redirect:/";
        }
      }
      StudentDetail student = currentStudentDetail();
      Clazz clazz = student.getClazz();
      if (clazz == null || clazz.isTerminalClazz()) {
        if (mobile) {
          model.addAttribute("error", "班级信息有误，请联系客服");
          return "/paymentmobile/confirm";
        } else {
          return "redirect:/";
        }
      }

      // 判断产品
      List<String> productIds = Arrays.asList(StringUtils.split(getRequestString("productId"), ","));
      if (CollectionUtils.isEmpty(productIds)) {
        if (mobile) {
          model.addAttribute("error", "请选择产品");
          return "/paymentmobile/confirm";
        } else {
          return "redirect:/";
        }
      }

      Map<String, OrderProduct> products = userOrderLoader.loadAllOrderProduct()
              .stream()
              .filter(OrderProduct::isOnline)
              .filter(p -> !p.isDisabledTrue())
              .filter(p -> productIds.contains(p.getId()))
              .collect(Collectors.toMap(OrderProduct::getId, Function.identity()));

//      if (products.size() != productIds.size()) {
//        if (mobile) {
//          model.addAttribute("error", "你购买的产品已经下架，请重新选择");
//          return "/paymentmobile/confirm";
//        } else {
//          return "redirect:/";
//        }
//      }

//      for (OrderProduct product : products.values()) {

//        // 检查是否有开通记录
//        AppPayMapper paidStatus = userOrderLoader.getUserAppPaidStatus(product.getProductType(), student.getId(), true);
//        if (paidStatus != null && CollectionUtils.isNotEmpty(paidStatus.getValidProducts()) && paidStatus.getValidProducts().contains(product.getId())) {
//          if (mobile) {
//            model.addAttribute("error", "该产品只能购买一次~");
//            return "/paymentmobile/confirm";
//          } else {
//            return "redirect:/";
//          }
//        }
//      }
//      }
      OrderProduct first = products.get(productIds.get(0));
      String name = productIds.size() == 1 ? first.getName() : first.getName() + "等" + productIds.size() + "个自学应用";
      MapMessage order = userOrderService.createAppOrder(student.getId(), first.getProductType(),
              productIds, name, getRequestString("refer"));

      //H5支付灰度变量
      boolean isOpenH5payment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "Order", "H5Pay");
      model.addAttribute("isOpenH5payment", isOpenH5payment);

      if (order.isSuccess()) {
        //暂时写死 不清楚productId是什么，后面还有需求，写好对应关系。
        ClazzActivityRecord userRecord = classCircleServiceClient.getClazzActivityService().findUserRecord(student.getId(), clazz.getSchoolId(), clazz.getId(), 3);
        if (userRecord == null) {
          logger.info("用户购买小U，订单创建成功，但用户记录为空，用户id-{}，订单id-{}", student.getId(), order.get("orderId"));
        } else {
          List<String> orderIds = userRecord.getOrderIds();
          if (CollectionUtils.isEmpty(orderIds)) {
            orderIds = new ArrayList<>();
          }
          orderIds.add((String) order.get("orderId"));
          userRecord.setOrderIds(orderIds);
          classCircleServiceClient.getClazzActivityService().updateRecord(userRecord);
        }
        if (mobile) {
          model.addAttribute("orderId", order.get("orderId"));
          model.addAttribute("productName", name);
          model.addAttribute("amount", order.get("price"));
          model.addAttribute("type", "afenti");

          // 学生是否开启支付权限
          StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(student.getId());
          if (currentUser().fetchUserType() == UserType.PARENT || attribute == null || attribute.fetchPayFreeStatus()) {
            return "/paymentmobile/confirm";
          } else {
            // 获取家长列表
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
            if (CollectionUtils.isNotEmpty(parents)) {
              List<Map<String, Object>> parentMaps = new ArrayList<>();
              for (StudentParent parent : parents) {
                Map<String, Object> p = new HashMap<>();
                p.put("parentId", parent.getParentUser().getId());
                p.put("callName", parent.getCallName());
                parentMaps.add(p);
              }
              model.addAttribute("parentList", parentMaps);
              return "/paymentmobile/authority";
            } else {
              return "/paymentmobile/confirm";
            }
          }
        } else {
          return "redirect:/apps/afenti/order/confirm.vpage?orderId=" + order.get("orderId");
        }
      } else {
        if (mobile) {
          model.addAttribute("error", StringUtils.defaultIfBlank(order.getInfo(), "生成订单失败"));
          return "/paymentmobile/confirm";
        } else {
          return "redirect:/";
        }
      }
    } catch (Exception ex)

    {
      if (mobile) {
        logger.error("Create afenti order error,pid:{}", getRequestString("productId"), ex);
        model.addAttribute("error", "生成订单失败");
        return "/paymentmobile/confirm";
      } else {
        return "redirect:/";
      }
    }
  }

  /**
   *
   * */
  @RequestMapping(value = "rankList.vpage")
  @ResponseBody
  public MapMessage getRankList(Integer activityId, Integer type) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    List<ActivityRank> clazzRank;
    ActivityRank selfActivityRank;
    ClazzActivity activity = classCircleServiceClient.getClazzActivityService().getActivity(activityId);
    if (activity == null || !activity.getIsShowRank()) {
      MapMessage.errorMessage().add("data", Collections.emptyMap()).add("systemTime", new Date()).add("self", Collections.emptyMap()).add("rankName", null);
    }
    if (type == 1) {
      selfActivityRank = new ActivityRank();
      //个人班级榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getRank(activityId, currentUserId());
      ClazzActivityRecord userRecord = classCircleServiceClient.getClazzActivityService()
              .findUserRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId);
      for (int i = 0; i < clazzRank.size(); i++) {
        clazzRank.get(i).setIndex(i + 1);
        User user = userLoaderClient.loadUsersIncludeDisabled(Arrays.asList(clazzRank.get(i).getUserId())).values().stream().findFirst().orElse(null);
        if (user != null) {
          clazzRank.get(i).setPic(getUserAvatarImgUrl(user.fetchImageUrl()));
          clazzRank.get(i).setUserName(user.fetchRealname());
          if (user.getId().equals(currentUserId())) {
            selfActivityRank.setIndex(i + 1);
          }
        }
      }
      if (userRecord != null) {
        selfActivityRank.setUserId(currentUserId());
        selfActivityRank.setPic(getUserAvatarImgUrl(currentUser().fetchImageUrl()));
        selfActivityRank.setUserName(currentUser().fetchRealname());
        if (userRecord.getActivityId() == 3 && userRecord.getBizObject() != null) {
          PlotActivityBizObject bizObject = JsonUtils.fromJson(JsonUtils.toJson(userRecord.getBizObject()), PlotActivityBizObject.class);
          selfActivityRank.setIsVip(bizObject.getVip());
          selfActivityRank.setNum(bizObject.getCurrentHighestDiffiCult() == null ? 0 : bizObject.getCurrentHighestDiffiCult());
        } else {
          selfActivityRank.setNum(userRecord.getScore());
        }

      }

    } else if (type == 2) {
      //个人年级榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getPersonInLevelRank(activityId, currentUserId(), 0, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfPersonInLevelRank(activityId, currentUserId(), 0, null);
      calculateUserPic(clazzRank);
      calculateSelfUserPic(selfActivityRank);
    } else if (type == 3) {
      //班级年级榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getClazzRank(activityId, currentUserId(), 0, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfClazzRank(activityId, currentUserId(), 0, null);
    } else if (type == 4) {
      // 个人全校榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getPersonInSchoolRank(activityId, currentUserId(), 0, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfPersonInSchoolRank(activityId, currentUserId(), 0, null);
      calculateUserPic(clazzRank);
      calculateSelfUserPic(selfActivityRank);
    } else if (type == 5) {

      //班级全校榜 type 5
      clazzRank = classCircleServiceClient.getBrainActivityService().getClazzInSchoolRank(activityId, currentUserId(), 0, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfClazzInSchoolRank(activityId, currentUserId(), 0, null);
    } else if (type == 6) {
      //个人班级每日榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getDailyRank(activityId, currentUserId(), null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfDailyRank(activityId, currentUserId(), null);
      calculateUserPic(clazzRank);
      calculateSelfUserPic(selfActivityRank);
    } else if (type == 7) {
      //个人年级每日榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getPersonInLevelRank(activityId, currentUserId(), 1, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfPersonInLevelRank(activityId, currentUserId(), 1, null);
      calculateUserPic(clazzRank);
      calculateSelfUserPic(selfActivityRank);
    } else if (type == 8) {
      //班级年级每日榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getClazzRank(activityId, currentUserId(), 1, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfClazzRank(activityId, currentUserId(), 1, null);
    } else if (type == 9) {
      // 个人全校每日榜
      clazzRank = classCircleServiceClient.getBrainActivityService().getPersonInSchoolRank(activityId, currentUserId(), 1, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfPersonInSchoolRank(activityId, currentUserId(), 1, null);
      calculateUserPic(clazzRank);
      calculateSelfUserPic(selfActivityRank);
    } else {
      //班级全校每日榜 type
      clazzRank = classCircleServiceClient.getBrainActivityService().getClazzInSchoolRank(activityId, currentUserId(), 1, null);
      selfActivityRank = classCircleServiceClient.getBrainActivityService().getSelfClazzInSchoolRank(activityId, currentUserId(), 1, null);
    }

    return MapMessage.successMessage().add("data", clazzRank).add("systemTime", new Date())
            .add("self", selfActivityRank == null ? Collections.emptyMap() : selfActivityRank)
            .add("rankName", calculateDiscription(activity.getRankDiscription(), type)).add("title", activity.getRankName());

  }

  /**
   * 点赞
   */
  @RequestMapping(value = "rank/like.vpage")
  @ResponseBody
  public MapMessage doRankLike(Integer activityId, Integer rankType, String toObjectId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    return classCircleServiceClient.getBrainActivityService().doRankLike(activityId, currentUserId(), rankType, toObjectId).add("systemTime", new Date());
  }
    /**
     *
     * */
    @RequestMapping(value = "rankReward.vpage")
    @ResponseBody
    public MapMessage getRankListReward() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        return classCircleServiceClient.getBrainActivityService().getReward(currentUserId()).add("systemTime", new Date());
    }

    /**
     *
     * */
    @RequestMapping(value = "rankReward/receive.vpage")
    @ResponseBody
    public MapMessage sendRankListReward(Integer activityId, Integer type) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        return classCircleServiceClient.getBrainActivityService().sendReward(activityId, currentUserId(), type).add("systemTime", new Date());
    }

  @RequestMapping(value = "userCurrentPlotInfo.vpage")
  @ResponseBody
  public MapMessage getUserCurrentPlotInfoList(Integer activityId){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().getUserCurrentPlotInfo(activityId,clazz.getSchoolId(),clazz.getId(),currentUserId());
    return mapMessage.add("systemTime", new Date());

  }

  @RequestMapping(value = "getPlotInfoListByPlotInfoId.vpage")
  @ResponseBody
  public MapMessage getPlotInfoListById(Integer activityId,String plotInfoId){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().getPlotInfoListById(activityId,clazz.getSchoolId(),clazz.getId(),currentUserId(),plotInfoId);
    return mapMessage.add("systemTime", new Date());
  }

  @RequestMapping(value = "updateUserCurrentPlot.vpage")
  @ResponseBody
  public MapMessage updateUserCurrentPlot(Integer activityId,String plotInfoId){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().updateUserCurrentPlot(activityId,clazz.getSchoolId(),clazz.getId(),currentUserId(),plotInfoId);
    return mapMessage.add("systemTime", new Date());

  }

    @RequestMapping(value = "selectPlotActivityPopup.vpage")
    @ResponseBody
    public MapMessage selectPlotActivityPopup(@RequestParam(required = false) Integer activityId,@RequestParam(required = false) Integer plotGroup,@RequestParam(required = false) Integer common){
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().selectPlotActivityPopup(activityId,currentUserId(),plotGroup,common);
        return mapMessage.add("systemTime", new Date());

    }

    @RequestMapping(value = "getPlotActivityPopupSelectResult.vpage")
    @ResponseBody
    public MapMessage selectPlotActivityPopup(@RequestParam(required = false) Integer activityId,@RequestParam(required = false) Integer plotGroup){
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().getPlotActivityPopupSelectResult(activityId,plotGroup);
        return mapMessage.add("systemTime", new Date());
    }

    @RequestMapping(value = "getPlotPopupSelectAward.vpage")
    @ResponseBody
    public MapMessage getPlotPopupSelectAward(@RequestParam(required = false) Integer activityId){
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().getPlotPopupSelectAward(activityId,currentUserId());
        return mapMessage.add("systemTime", new Date());
    }

  /**
   * 去做题接口
   *
   * @param activityId
   * @return
   */
  @RequestMapping(value = "doQuestion.vpage")
  @ResponseBody
  public MapMessage doQuestion(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }

    ClazzActivityRecord userRecord = classCircleServiceClient.getClazzActivityService()
            .findUserRecord(currentUser().getId(), clazz.getSchoolId(), clazz.getId(), activityId);
    String appkey = "AfentiMath";
    if (userRecord != null) {
      if (userRecord.getBizObject() != null) {
        PlotActivityBizObject bizObject = JsonUtils.fromJson(JsonUtils.toJson(userRecord.getBizObject()), PlotActivityBizObject.class);
        appkey = bizObject.getAppkey();
      }
    }
    return MapMessage.successMessage().add("systemTime", new Date()).add("appkey", appkey);

  }

  /**
   * 查询烤箱 托盘  火鸡 用户list
   */
  @RequestMapping(value = "findClazzStudentList.vpage")
  @ResponseBody
  public MapMessage findClazzStudentList(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    //查询烤箱 托盘 火鸡list
    Map<Integer, List<ChickenHelpResponse>> clazzStudentList = classCircleServiceClient
            .getClassCircleGivingService().findClazzStudentList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
    if (clazzStudentList != null){
        for (Integer key : clazzStudentList.keySet()) {
            List<ChickenHelpResponse> chickenHelpResponses = clazzStudentList.get(key);
            if (chickenHelpResponses != null && chickenHelpResponses.size()>0){
                chickenHelpResponses.forEach(item -> {
                    item.setPic(getUserAvatarImgUrl(item.getPic()));
                });
            }
        }
    }
    return MapMessage.successMessage().add("helpList", clazzStudentList);

  }

  /**
   * 查询班级累计进度
   */
  @RequestMapping(value = "findClazzProgress.vpage")
  @ResponseBody
  public MapMessage findClazzProgress(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    //班级累计进度
    ChickenClazzResponse classChickenList = classCircleServiceClient
            .getClassCircleGivingService().findClassChickenList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
    return MapMessage.successMessage().add("clazz", classChickenList);

  }

  /**
   * 查询吃鸡广播 列表
   */
  @RequestMapping(value = "findBroadcastList.vpage")
  @ResponseBody
  public MapMessage findBroadcastList(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    //吃鸡广播
    List<ChickenStudentRecordResponse> eatChickenList = classCircleServiceClient
            .getClassCircleGivingService().findEatChickenList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
    return MapMessage.successMessage().add("data", eatChickenList);

  }

  /**
   * 邀请助力
   */
  @RequestMapping(value = "inviteHelp.vpage")
  @ResponseBody
  public MapMessage inviteHelp(Integer activityId, Integer type) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }

    if (type != 1 && type != 2 && type != 3) {
      return MapMessage.errorMessage("参数异常");
    }

    Boolean aBoolean = classCircleServiceClient
            .getClassCircleGivingService().inviteHelp(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId(), type);
    return MapMessage.successMessage().add("status", aBoolean);

  }

  /**
   * 获取其他同学给的助力弹窗
   */
  @RequestMapping(value = "findChickenStudentList.vpage")
  @ResponseBody
  public MapMessage findChickenStudentList(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    List<ChickenRewardResponse> chickenStudentList = classCircleServiceClient
            .getClassCircleGivingService().findChickenStudentList(activityId, currentUserId(), clazz.getSchoolId(), clazz.getId());
    if (chickenStudentList != null && chickenStudentList.size()>0){
        chickenStudentList.forEach(item -> {
            List<ChickenStudentResponse> studentList = item.getStudentList();
            if (studentList != null && studentList.size()>0){
                studentList.forEach(e ->{
                    e.setStudentPic(getUserAvatarImgUrl(e.getStudentPic()));
                });
            }
        });
    }
    return MapMessage.successMessage().add("data", chickenStudentList);

  }

  /**
   * 获取个人活动进度
   */
  @RequestMapping(value = "getSelfActivityProgress.vpage")
  @ResponseBody
  public MapMessage getSelfActivityProgress(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient
            .getClassCircleGivingService().getSelfActivityProgress(activityId, clazz.getSchoolId(), clazz.getId(), currentUserId());
    return mapMessage.add("systemTime", new Date());

  }

  /**
   * 合成活动物品
   */
  @RequestMapping(value = "composeActivityGoods.vpage")
  @ResponseBody
  public MapMessage composeActivityGoods(Integer activityId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient
            .getClassCircleGivingService().composeActivityGoods(activityId, clazz.getSchoolId(), clazz.getId(), currentUserId());
    return mapMessage.add("systemTime", new Date());

  }

  /**
   * 助力他人
   */
  @RequestMapping(value = "helpOtherUser.vpage")
  @ResponseBody
  public MapMessage helpOtherUser(Integer activityId, String ahId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient
            .getClassCircleGivingService().helpOtherUser(activityId, clazz.getSchoolId(), clazz.getId(), currentUserId(), ahId);
    if (mapMessage.isSuccess()) {
      Object object = mapMessage.get("stu");
      if (object != null) {
        StudentInfo studentInfo = (StudentInfo) object;
        studentInfo.setPic(getUserAvatarImgUrl(studentInfo.getPic()));
        mapMessage.set("stu", studentInfo);
      }
    }
    return mapMessage.add("systemTime", new Date());

  }


  private List<ActivityRank> calculateUserPic(List<ActivityRank> clazzRank) {
    return clazzRank.stream()
            .filter(Objects::nonNull)
            .map(e -> {
              User user = userLoaderClient.loadUsersIncludeDisabled(Arrays.asList(e.getUserId())).values().stream().findFirst().orElse(null);
              if (user != null) {
                e.setPic(getUserAvatarImgUrl(user.fetchImageUrl()));
                e.setUserName(user.fetchRealname());
              }
              return e;
            }).collect(Collectors.toList());
  }

  private void calculateSelfUserPic(ActivityRank selfActivityRank) {
    if (selfActivityRank == null) {
      return;
    }
    selfActivityRank.setUserId(currentUserId());
    selfActivityRank.setPic(getUserAvatarImgUrl(currentUser().fetchImageUrl()));
    selfActivityRank.setUserName(currentUser().fetchRealname());


  }

  @RequestMapping(value = "updateFirstPayStatus.vpage")
  @ResponseBody
  public MapMessage updateFirstPayStatus(Integer activityId){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient.getPlotActivityService().updateClazzActivityRecordFirstBuy(activityId,clazz.getSchoolId(),clazz.getId(),currentUserId());
    return mapMessage.add("systemTime", new Date());

  }
  /**
   * 领取个人奖励吃鸡活动
   */
  @RequestMapping(value = "receiveSelfRank.vpage")
  @ResponseBody
  public MapMessage receiveSelfRank(String activityAwardId) {
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage = classCircleServiceClient
        .getClassCircleGivingService().receiveSelfRank(activityAwardId, currentUserId(),clazz.getSchoolId(),clazz.getId());

    return  mapMessage==null?MapMessage.successMessage():mapMessage;
  }

  /**
   * 领取班级奖励吃鸡活动
   */
  @RequestMapping(value = "receiveClassRank.vpage")
  @ResponseBody
  public MapMessage receiveClassRank(String activityAwardId){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    MapMessage mapMessage =classCircleServiceClient
        .getClassCircleGivingService().receiveClassRank(activityAwardId, currentUserId(),clazz.getSchoolId(),clazz.getId());

    return mapMessage==null?MapMessage.successMessage():mapMessage;
  }

    @RequestMapping(value = "common.vpage")
    @ResponseBody
    public String common(){
        if (studentUnLogin()) {
            return JsonUtils.toJson(MapMessage.errorMessage("请重新登录"));
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return JsonUtils.toJson(MapMessage.errorMessage("您还没有加入班级"));
        }
        Map<String, Object> map = new HashMap<>();
        map.put(FieldName.schoolId,clazz.getSchoolId());
        map.put(FieldName.clazzId,clazz.getId());
        map.put(FieldName.userId,currentUserId());
        HttpServletRequest request = getRequest();
        Enumeration e = request.getParameterNames();
        while(e.hasMoreElements()){
            String field = (String) e.nextElement();
            String value = request.getParameter(field);
            map.put(field,value);
        }
      return classCircleServiceClient.getClazzZoneCommonService().dispatch(map);
    }

  /**
   * 获取用户进入那种学科
   */
  @RequestMapping(value = "getStudentSubject.vpage")
  @ResponseBody
  public MapMessage getStudentSubject(){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    classCircleServiceClient
        .getClassCircleGivingService().increaseCountByStudentId(currentUserId());
    String str =  classCircleServiceClient
        .getClassCircleGivingService().getStudentSubject(currentUserId());
    return MapMessage.successMessage().add("subject",str);
  }

  /**
   * 获取奖励通知弹窗
   */
  @RequestMapping(value = "queryRewardNotice.vpage")
  @ResponseBody
  public MapMessage queryRewardNotice(){
    if (studentUnLogin()) {
      return MapMessage.errorMessage("请重新登录");
    }
    Clazz clazz = getCurrentClazz();
    if (null == clazz) {
      return MapMessage.errorMessage("您还没有加入班级");
    }
    List<ClazzCircleRewardNotice> clazzCircleRewardNotices= classCircleServiceClient
            .getClassCircleGivingService().queryClazzCircleRewardNotice(4,currentUserId(),clazz.getSchoolId(),clazz.getId());
    return MapMessage.successMessage().add("clazzCircleRewardNotices",clazzCircleRewardNotices);
  }

    /**
     * 获取每日一句补签类型
     */
    @RequestMapping(value = "queryModifySignType.vpage")
    @ResponseBody
    public MapMessage queryModifySignType() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        List<DailySertenceModifySignConfig> dailySertenceModifySignConfigs = classCircleServiceClient
                .getClassCircleService().queryDailySertenceModifySignConfigList(currentUserId());
        return MapMessage.successMessage().add("dailySertenceModifySignConfigs", dailySertenceModifySignConfigs);
    }

    /**
     * 每日一句补签接口
     */
    @RequestMapping(value = "modifySign.vpage")
    @ResponseBody
    public MapMessage modifySign(Integer signType) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        classCircleServiceClient
                .getClassCircleService().modifySign(currentUserId(), signType);
        return MapMessage.successMessage();
    }

    /**
     * 获取补签弹窗
     */
    @RequestMapping(value = "querySignNotice.vpage")
    @ResponseBody
    public MapMessage querySignNotice() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        List<SignRecord> signRecords = classCircleServiceClient
                .getClassCircleService().loadModifySignUserCountCache(currentUserId());
        return MapMessage.successMessage().add("signRecords", signRecords);
    }

    /**
     * 更新补签弹窗
     */
    @RequestMapping(value = "updateSignNotice.vpage")
    @ResponseBody
    public MapMessage updateSignNotice(Integer signType) {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        classCircleServiceClient
                .getClassCircleService().updateModifySign(currentUserId(), signType);
        return MapMessage.successMessage();
    }

  private String calculateDiscription(String rankDiscription, Integer type) {
    if (StringUtils.isNoneBlank(rankDiscription)) {
      String[] list = rankDiscription.split(",");
      for (String i : list) {
        String[] typeValue = i.split("\\|");
        if (typeValue.length == 0 || typeValue.length > 2) {
          return "";
        } else {
          if (SafeConverter.toInt(typeValue[0]) == type) {
            return typeValue[1];
          }
        }

      }
    }
    return "";
  }

}
