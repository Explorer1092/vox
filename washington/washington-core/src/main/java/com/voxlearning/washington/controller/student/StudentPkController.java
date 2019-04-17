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

package com.voxlearning.washington.controller.student;

import com.voxlearning.washington.support.AbstractGameSupportController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/pk")
public class StudentPkController extends AbstractGameSupportController {

//    @Inject private AsyncPkServiceClient asyncPkServiceClient;
//
//    @Inject private IntegralLoaderClient integralLoaderClient;
//
//    @Inject private LoadHomeworkHelper loadHomeworkHelper;
//
//    @RequestMapping(value = "index.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    public String index(Model model) {
//        return "redirect:/student/index.vpage";
//
////        StudentDetail student = currentStudentDetail();
////        Date compareDate = DateUtils.stringToDate("2016-12-05 00:00:00");
////        if (student.getClazz() == null || student.getCreateTime() == null || student.getCreateTime().after(compareDate)) {
////            return "redirect:/student/index.vpage";
////        }
////
////        // 判断是否需要弹出输入密码的弹窗
////        model.addAttribute("passwordPopup", StringUtils.isBlank(getWebRequestContext().getCookieManager().getCookie("lupld", "")));
////
////        // 判断是否需要展示活动条
////        model.addAttribute("wxbinded", CollectionUtils.isNotEmpty(parentLoaderClient.loadStudentParents(student.getId())));
////
////        // 学生ugc基础数据收集
////        boolean mobileBinded = false;
////        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(student.getId());
////        if (userAuthentication != null) {
////            mobileBinded = userAuthentication.isMobileAuthenticated();
////        }
////        model.addAttribute("needCollectInfo", mobileBinded);
////
////        if (studentLoaderClient.isStudentForbidden(student.getId())) {
////            model.addAttribute("stuforbidden", true);
////        }
////
////        return "studentv3/pk/index";
//    }
//
//    /**
//     * 用户完成作业后提交完成 pk数据
//     */
//    @RequestMapping(value = "homeworkfinished/{homeworkId}/{clazzId}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    @Deprecated
//    public MapMessage homeworkfinished(@SuppressWarnings("UnusedParameters") @PathVariable("homeworkId") String homeworkId, @PathVariable("clazzId") Long clazzId) {
//        MapMessage mesg = new MapMessage();
//        mesg.setSuccess(true);
//        return mesg;
//    }
//
//    /**
//     * *******************新版 pk controller*********************
//     */
//
//    @RequestMapping(value = "fight/login.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pkLogin() {
//        User user = currentUser();
//        MapMessage response = pkLoaderClient.login(user);
//        if (response.isSuccess()) {
//            try {
//                BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
//                response.add("starCount", babelRole.getStarCount());//星星数
//            } catch (Exception e) {
//                logger.error("Get babelRole {} star error", user.getId());
//                response.add("starCount", 0);//星星数
//            }
//            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
//            response.add("paymentPasswordFlag", StringUtils.isNotBlank(ua.getPaymentPassword()));
//
//            BabelVitalityResponse bvr = babelVitalityServiceClient.getCurrentBalance(currentUserId());
//            if (bvr.isSuccess()) {
//                response.add("babelVitality", bvr.getBalance());
//            } else {
//                response.add("babelVitality", 0);
//            }
//
//            String key = "STUDENT_VIEW_TRANSFORMCAREER:" + currentUserId();
//            Boolean viewTransformCareer = washingtonCacheSystem.CBS.persistence.load(key);
//            if (null == viewTransformCareer) {
//                viewTransformCareer = true;
//            }
//            response.add("newFuncFlag", viewTransformCareer);
//        }
//        boolean homeworkUndoneFlag = loadHomeworkHelper.hasUndoneHomework(currentStudentDetail());
//        response.add("homeworkUndoneFlag", homeworkUndoneFlag);
//        response.put("transferCareerBean", 100);
//
//        response.put("allBabelNpc", babelLoaderClient.loadAvailableNpcs().values());
//        response.put("allPetEgg", babelLoaderClient.loadAvailablePets().values());
//        response.put("myEggList", babelLoaderClient.loadRolePet(currentUserId()).getPetList());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/loaddefination.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage loadPetDefination() {
//        return pkLoaderClient.loadPetDefination().add("allBabelNpc", babelLoaderClient.loadAvailableNpcs().values());
//    }
//
//    @RequestMapping(value = "fight/registration.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pkRegistration(String data) {
//        User student = currentUser();
//        if (StringUtils.isEmpty(data)) {
//            logger.error("PK REGISTRATION: 获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        if (map == null) {
//            logger.error("PK REGISTRATION: 获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        String career = (String) map.get("career");
//        String gender = (String) map.get("gender");
//        if (null == career) {
//            logger.error("PK REGISTRATION: 没有选定职业");
//            return MapMessage.errorMessage().setInfo("没有选定职业");
//        }
//        if (StringUtils.isEmpty(gender)) {
//            logger.error("PK REGISTRATION: 没有选定性别");
//            return MapMessage.errorMessage().setInfo("没有选定性别");
//        }
//
//        Career _career = Career.valueOf(career);
//        Gender _gender = Gender.valueOf(gender);
//
//        MapMessage response = pkServiceClient.createRole(student, _gender, _career);
//        try {
//            BabelRole babelRole = babelLoaderClient.loadRole(currentUserId());
//            response.add("starCount", babelRole.getStarCount());//星星数
//        } catch (Exception e) {
//            logger.error("Get role start error(Registration)", e);
//            response.add("starCount", 0);//星星数
//        }
//        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
//        response.add("paymentPasswordFlag", StringUtils.isNotBlank(ua.getPaymentPassword()));
//        response.put("transferCareerBean", 100);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "fight/candidate.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage getCandidate(String data) {
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        MapMessage response = pkLoaderClient.getCandidate(studentDetail.getId(), studentDetail.getClazzId());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @SuppressWarnings("unchecked")
//    @RequestMapping(value = "fight/pk.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pk(String data) {
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        // 获取当前用户信息
//        StudentDetail user = currentStudentDetail();
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        Long opponentId = NumberUtils.toLong(map.get("opponentId").toString());
//        if (null == opponentId || 0 == opponentId) {
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        MapMessage response = MapMessage.errorMessage();
//        if (opponentId <= 360L) {
//            response = pkServiceClient.pkWithNpc(user, user.getClazzId(), opponentId);
//        } else {
//            if (Objects.equals(opponentId, user.getId())) {
//                return MapMessage.errorMessage().add("error", "Cannot pk with yourself.");
//            }
//            response = pkServiceClient.pk(user, user.getClazzId(), opponentId);
//        }
//        setSecurityKey(response);
//        return response;
//    }
//
//    @SuppressWarnings("unchecked")
//    @RequestMapping(value = "fight/pkhistoryattack.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pkHistoryAttack(String data) {
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String battleType = (String) map.get("battleType");
////        if (StringUtils.isEmpty(battleType)) {
////            return MapMessage.errorMessage().setInfo("获取参数失败");
////        }
//        MapMessage response = pkLoaderClient.getBattleReports(currentUserId(), battleType);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "fight/pkrank.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pkGetRank(String data) {
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        MapMessage response = pkLoaderClient.getWeekRank(studentDetail, studentDetail.getClazzId());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/roleinfos.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage roleInfos(String data) {
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        if (null == map) {
//            logger.error("roleInfos 没有参数");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String userIds = (String) map.get("userIds");
//        if (StringUtils.isEmpty(userIds)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String[] userIdArray = userIds.split(",");
//        if (0 == userIdArray.length) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        Set<Long> userIdSet = new HashSet<>();
//        for (String id : userIdArray) {
//            try {
//                userIdSet.add(Long.parseLong(id));
//            } catch (NumberFormatException e) {
//                logger.warn("Invoke roleInfos error userId :" + id);
//            }
//        }
//        return pkLoaderClient.getRoleInfos(userIdSet);
//    }
//
//    /**
//     * 说明：在竞技场点击商城。
//     * <p>
//     * 需要参数名称：s， 类型：json。
//     * json中包含：type，商品种类（HAIR-0,EXPRESSION-1,CLOTHES-2,WEAPON-3,BACK-4,ACCESSORIES-5）， 我需要数字即可
//     * pageNumber, 第几页，从0开始
//     * pageSize, 每页几条数据
//     * sortField, 排序字段，默认是createTimestamp
//     */
//    @RequestMapping(value = "fight/goshopping.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public Object goShopping(String data) {
//        User user = currentUser();
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String productType = (String) map.get("productType");
//
//        if (null == productType) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        PkProductType pkProductType = PkProductType.valueOf(productType);
//        MapMessage response = pkLoaderClient.goShopping(user, pkProductType);
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 说明：购买商品的接口。
//     * <p>
//     * 需要参数名称：s， 类型：json。
//     * json中包含：productId, 将要购买的商品的id
//     */
//    @RequestMapping(value = "fight/purchase.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage purchase(String data) {
//        User user = currentUser();
//        if (StringUtils.isEmpty(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String productIds = (String) map.get("productIds");
//        String paymentPassword = (String) map.get("paymentPassword");
//
//        if (StringUtils.isEmpty(productIds)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String[] productIdArray = productIds.split(",");
//        MapMessage response = asyncPkServiceClient.getAsyncPkService()
//                .purchase2(user, paymentPassword, productIdArray)
//                .getUninterruptibly();
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 说明：这个接口是在竞技场点击我的角色。
//     * <p>
//     * 需要参数名称：s， 类型：json。
//     * json中包含：c，职业代码，int类型那个
//     * g，性别代码，bg那个
//     */
//    @RequestMapping(value = "fight/baginfo.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public Object bagInfo() {
//        User user = currentUser();
//        MapMessage response = pkLoaderClient.getBag(user);
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 删除背包中时装
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/deletefashion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage deleteFashion(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String fashionIds = (String) map.get("fashionIds");
//        if (StringUtils.isEmpty(fashionIds)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String[] fashionIdArray = fashionIds.split(";");
//        MapMessage response = pkServiceClient.deleteFashion(user, true, fashionIdArray);
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 选择好要穿的时装并穿上
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/changefashion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage changeFashion(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String fashionIds = (String) map.get("fashionIds");
//        if (StringUtils.isEmpty(fashionIds)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String[] fashionIdArray = fashionIds.split(";");
//        MapMessage response = pkServiceClient.changeFashion(user, fashionIdArray);
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 删除背包中武器装备
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/sellequipment.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage sellEquipment(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String ids = (String) map.get("ids");
//        if (StringUtils.isEmpty(ids)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        String[] idArray = ids.split(";");
//        Map<String, Set<String>> idMap = new LinkedHashMap<>();
//
//        for (int i = 0; i < idArray.length; i++) {
//            if (!idArray[i].contains(",")) {
//                logger.error("参数结构错误");
//                return MapMessage.errorMessage().setInfo("参数结构错误");
//            }
//            String equipmentOriginalId = idArray[i].split(",")[0];
//            String equipmentId = idArray[i].split(",")[1];
//            if (!idMap.containsKey(equipmentOriginalId)) {
//                idMap.put(equipmentOriginalId, new LinkedHashSet<String>());
//            }
//            idMap.get(equipmentOriginalId).add(equipmentId);
//        }
//
//        BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
//        if (babelRole == null) {
//            logger.error("BABEL ROLE {} NOT LOADED", user.getId());
//            return MapMessage.errorMessage().setInfo("无法加载通天塔角色");
//        }
//
//        MapMessage response = pkServiceClient.deleteEquipment(user.getId(), idMap);
//        if (response.isSuccess()) {
//            Integer incomeStar = (Integer) response.get("incomeStar");
//            String desp = new StringBuilder("出售PK武装(").append(idMap).append(")").toString();
//            MapMessage message = babelServiceClient.useStar(
//                    babelRole,
//                    -incomeStar,
//                    BabelStarChange.PK_SELL_EQUIPMENT,
//                    desp
//            );
//            if (!message.isSuccess()) {
//                return message;
//            }
//            babelRole = (BabelRole) message.get("role");
//            int currentStar = babelRole.getStarCount();
//            return response.add("currentStar", currentStar);
//        }
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 选择好要装备武器装备并装备上
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/changeequipment.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage changeEquipment(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String ids = (String) map.get("ids");
//        if (StringUtils.isEmpty(ids)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//
//        if (!ids.contains(";")) {
//            String[] params = ids.split(",");
//            if (params.length != 2) {
//                return MapMessage.errorMessage().setInfo("参数结构错误");
//            }
//            MapMessage response = pkServiceClient.changeEquipment(user.getId(), params[0], params[1]);
//            setSecurityKey(response);
//            return response;
//        }
//
//        String[] idArray = ids.split(";");
//
//        Map<String, Set<String>> idMap = new LinkedHashMap<>();
//
//        for (int i = 0; i < idArray.length; i++) {
//            if (!idArray[i].contains(",")) {
//                logger.error("参数结构错误");
//                return MapMessage.errorMessage().setInfo("参数结构错误");
//            }
//            String equipmentOriginalId = idArray[i].split(",")[0];
//            String equipmentId = idArray[i].split(",")[1];
//            if (!idMap.containsKey(equipmentOriginalId)) {
//                idMap.put(equipmentOriginalId, new LinkedHashSet<String>());
//            }
//            idMap.get(equipmentOriginalId).add(equipmentId);
//        }
//        MapMessage response = pkServiceClient.changeEquipment(user.getId(), idMap);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/viewtransformcareer.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage viewTransformCareer() {
//        Long userId = currentUserId();
//        if (null == userId || 0 == userId) {
//            return MapMessage.errorMessage().setInfo("没有登录");
//        }
//        String key = "STUDENT_VIEW_TRANSFORMCAREER:" + userId;
//        washingtonCacheSystem.CBS.persistence.add(key, 0, Boolean.FALSE);
//
//        return MapMessage.successMessage();
//    }
//
//    @RequestMapping(value = "/transformcareer.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage transformCareer(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String career = (String) map.get("career");
//        if (StringUtils.isEmpty(career) || null == Career.valueOf(career)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        String transferCareerType = (String) map.get("transferCareerType");
//        if (StringUtils.isEmpty(career) || null == TransferCareerType.valueOf(transferCareerType)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        String paymentPassword = (String) map.get("paymentPassword");
//        MapMessage response = asyncPkServiceClient.getAsyncPkService()
//                .transformCareer2(user, Career.valueOf(career),
//                        TransferCareerType.valueOf(transferCareerType), paymentPassword)
//                .getUninterruptibly();
//        setSecurityKey(response);
//
//        return response;
//    }
//
//    @RequestMapping(value = "/grantspringgift.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantSpringGift() {
//
//        Long userId = currentUserId();
//        Role role = pkLoaderClient.loadRole(userId);
//        if (null == role) {
//            return MapMessage.errorMessage().setErrorCode("100000").setInfo("角色不存在。");
//        }
//        SpringGift gift = SpringGift.getInstance(userId, role.getGender());
//        if (null == gift) {
//            return MapMessage.errorMessage().setInfo("礼物不存在");
//        }
//
//        MapMessage response = pkServiceClient.grantSpringGift(role, currentUser().getUserType());
//        if (response.isSuccess()) {
//            BabelRole babelRole = babelLoaderClient.loadRole(userId);
//            if (null != babelRole) {
//                //通天塔星星
//                MapMessage message = babelServiceClient.useStar(babelRole, -gift.getStar(),
//                        BabelStarChange.PK_CHRISTMAS, "PK新年好礼");
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantChristmasGift add babel star {} error", userId);
//                }
//                if (message.isSuccess() && message.containsKey("role")) {
//                    response.add("starCount", ((BabelRole) message.get("role")).getStarCount());//星星数
//                }
//                if (null == babelLoaderClient.loadBag(userId)) {//确保通天塔背包存在，防止出现精力卡发不到身上的情况
//                    MapMessage.errorMessage().setErrorCode("100062").setInfo("通天塔背包获取失败。");
//                }
//                //通天塔精力卡
//                message = babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.VITALITY_REFILL, -gift.getEnergy()));
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantChristmasGift add babel vitality {} error", userId);
//                }
//            } else {
//                logger.error("Pk grantChristmasGift babelRole {} not loaded", userId);
//            }
//        }
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 激活技能
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/activeskill.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage activeSkill(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String skillId = (String) map.get("skillId");
//        if (StringUtils.isEmpty(skillId)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//
//        BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
//        if (babelRole == null) {
//            logger.error("BABEL ROLE {} NOT LOADED", user.getId());
//            return MapMessage.errorMessage().setInfo("无法加载通天塔角色");
//        }
//
//        SkillConfig skill = pkLoaderClient.getSkillById2(skillId);
//        int needStar = skill.getOpenStar();
//        MapMessage message = babelServiceClient.useStar(
//                babelRole,
//                needStar,
//                BabelStarChange.PK_ACTIVE_SKILL,
//                "激活PK技能：" + skill.getName());
//        if (!message.isSuccess()) {
//            return MapMessage.errorMessage().setInfo("星星数量不够技能开启");
//        }
//        babelRole = (BabelRole) message.get("role");
//        int leftStat = babelRole.getStarCount();
//        MapMessage response = pkServiceClient.activateSkills(user.getId(), skillId).add("starCount", leftStat);
//        setSecurityKey(response);
//        return response;
//    }
//
//    /**
//     * 升级技能
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "fight/upgradeskilllevel.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage upgradeSkillLevel(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String skillId = (String) map.get("skillId");
//        if (StringUtils.isEmpty(skillId)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//
//        BabelRole babelRole = babelLoaderClient.loadRole(user.getId());
//        if (babelRole == null) {
//            logger.error("BABEL ROLE {} NOT LOADED", user.getId());
//            return MapMessage.errorMessage().setInfo("无法加载通天塔角色");
//        }
//
//        SkillConfig skill = pkLoaderClient.getSkillById2(skillId);
//        int needStar = skill.getUpgradeStar();
//
//        //先验证星星够不够，不够就不走后续流程了
//        if (babelRole.getStarCount() < needStar) {
//            return MapMessage.errorMessage().setInfo("星星数量不够技能升级");
//        }
//
//        //星星够，先去升级，升级成功再扣星星
//        //之前是不论怎样，先扣星星。很坑
//        MapMessage upgradeResponse = pkServiceClient.upgradeSkillLevel(user.getId(), skillId);
//        if (!upgradeResponse.isSuccess()) {//升级失败，就不扣星星了
//            return upgradeResponse;
//        }
//        MapMessage message = babelServiceClient.useStar(
//                babelRole,
//                needStar,
//                BabelStarChange.PK_UPGRADE_SKILL,
//                "升级PK技能：" + skill.getName()
//        );
//        if (message.isSuccess()) {//星星扣成功了， 从返回结果中获得扣除后的星星数。如果很倒霉扣星星失败了，那也就放过了。
//            babelRole = (BabelRole) message.get("role");
//        }
//
//        int leftStat = babelRole.getStarCount();
//        MapMessage response = upgradeResponse.add("starCount", leftStat);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "fight/changeskill.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage changeSkill(String data) {
//        User user = currentUser();
//        if (StringUtils.isBlank(data)) {
//            logger.error("获取参数失败");
//            return MapMessage.errorMessage().setInfo("获取参数失败");
//        }
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String sourceId = (String) map.get("sourceId");
//        String targetId = (String) map.get("targetId");
//        if (StringUtils.isEmpty(sourceId) || StringUtils.isEmpty(targetId)) {
//            logger.error("参数不全");
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        if (sourceId.equals(targetId)) {
//            return MapMessage.errorMessage().setInfo("更换的两个技能相同。");
//        }
//        MapMessage response = pkServiceClient.changeSkill(user.getId(), sourceId, targetId);
//        setSecurityKey(response);
//        return response;
//    }
//
//    private boolean checkRequest(String data) {
//        if (RuntimeMode.le(Mode.DEVELOPMENT)) {
//            return true;
//        }
//        Map<String, Object> dataMap = JsonUtils.fromJson(data);
//        if (null == dataMap) {
//            logger.warn(currentUserId() + " PK操作验证Key失败，没有传入SecurityKey.");
//            return false;
//        }
//        if (dataMap.containsKey("securityKey") && StringUtils.isNotEmpty((String) dataMap.get("securityKey"))) {
//            String token = (String) dataMap.get("securityKey");
//            String checkKeyRs = checkKeyForCurrentUser(token, SecureKeyType.PK通天塔通用);
//            if (checkKeyRs.isEmpty()) {
//                return true;
//            }
//            logger.warn(currentUserId() + " PK操作验证Key失败，key:" + token);
//            return false;
//        }
//        return false;
//    }
//
//    private void setSecurityKey(MapMessage response) {
//        String securityKey = genSecureKeyForCurrentUser(SecureKeyType.PK通天塔通用);
//        response.add("securityKey", securityKey);
//    }
//
//    @RequestMapping(value = "/getassembly.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage getAssembly(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage response = pkLoaderClient.getAssembly(currentUserId());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/addassemblyreward.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage addAssemblyReward(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage response = pkServiceClient.addAssemblyReward(currentUserId());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/grantassemblyreward.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantAssemblyReward(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Long userId = currentUserId();
//        MapMessage response = pkServiceClient.grantAssemblyReward(userId);
//        Integer noReceiveCount = (Integer) response.get("noReceiveCount");
//        //通天塔精力卡
//        AssemblyRewardHelper.SingleReward singleReward = AssemblyRewardHelper.getSingleReward();
//        BabelRole babelRole = babelLoaderClient.loadRole(userId);
//        int totalNoReceiveCount = singleReward.getEnergy() * noReceiveCount;
////        int diamond = singleReward.getDiamond() * noReceiveCount;
//        babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.VITALITY_REFILL, -totalNoReceiveCount));
//        //走遍美国钻石
////        miscServiceClient.sendPKComradesAward(OrderProductServiceType.TravelAmerica.name(), userId, diamond);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/grantassemblyachieve.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantAssemblyAchievement(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Long userId = currentUserId();
//        int spaceLeft = pkLoaderClient.getBagEquipmentSpaceLeft(userId);
//        if (spaceLeft <= 0) {
//            return MapMessage.errorMessage().setErrorCode("100018").setInfo("背包空间不足");
//        }
//        MapMessage response = pkServiceClient.grantAssemblyAchievement(userId);
//        if (response.isSuccess()) {
//            BabelRole babelRole = babelLoaderClient.loadRole(userId);
//
//            Integer rewardLv = (Integer) response.get("rewardLv");
//            Career career = (Career) response.get("career");
//            AssemblyAchievementReward achievementReward = AssemblyRewardHelper.getAssemblyAchievementReward(rewardLv, career);
//            MapMessage message = null;
//            if (null != babelRole) {
//                //通天塔星星
//                message = babelServiceClient.useStar(babelRole, -achievementReward.getStar(),
//                        BabelStarChange.PK_ASSEMBLY, "PK战友召集令奖励星星");
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantAssemblyAchievement add babel star {} error", userId);
//                }
//            } else {
//                logger.error("Pk grantAssemblyAchievement babelRole {} not loaded", userId);
//            }
//            //走遍美国钻石
////            miscServiceClient.sendPKComradesAward(OrderProductServiceType.TravelAmerica.name(), userId,
////                    achievementReward.getDiamond());
//        }
//        response.remove("rewardLv");
//        response.remove("career");
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/getsuperscholar.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage getSuperScholar(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage response = pkLoaderClient.getSuperScholar(currentUserId());
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/grantsuperscholar.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantSuperScholar(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map map = JsonUtils.fromJson(data);
//        if (null == map) {
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//
//        String id = (String) map.get("id");
//        Integer receiveOrder = (Integer) map.get("order");
//
//        if (StringUtils.isEmpty(id) || null == receiveOrder) {
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        MapMessage response = pkServiceClient.grantSuperScholar(currentUserId(), id, receiveOrder);
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/grantsuperscholarmonth.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantSuperScholarMonth(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Map map = JsonUtils.fromJson(data);
//        if (null == map) {
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//
//        Integer level = (Integer) map.get("level");
//
//        if (null == level) {
//            return MapMessage.errorMessage().setInfo("参数不全");
//        }
//        Long userId = currentUserId();
//        MapMessage response = pkServiceClient.grantSuperScholarMonth(userId, level);
//
//        if (response.isSuccess()) {
//            SuperScholarMonthReward monthReward = (SuperScholarMonthReward) response.get("monthReward");
//            BabelRole babelRole = babelLoaderClient.loadRole(userId);
//            MapMessage message = null;
//            if (null != babelRole) {
//                //通天塔星星
//                message = babelServiceClient.useStar(babelRole, -monthReward.getStar(),
//                        BabelStarChange.PK_SUPERSCHOLAR, "PK我要当学霸奖励星星");
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantSuperScholarMonth add babel star {} error", userId);
//                }
//                message = babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.VITALITY_REFILL, -monthReward.getEnergy()));
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantSuperScholarMonth add babel energy {} error", userId);
//                }
//            } else {
//                logger.error("Pk grantSuperScholarMonth babelRole {} not loaded", userId);
//            }
//        }
//
//        response.remove("monthReward");
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/grantsuperscholarhistory.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage grantSuperScholarHistory(String data) {
//        if (!checkRequest(data)) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Long userId = currentUserId();
//        MapMessage response = pkServiceClient.grantSuperScholarHistory(userId);
//
//        if (response.isSuccess() && response.containsKey("monthRewardList")) {
//            List<SuperScholarMonthReward> monthRewardList = (List<SuperScholarMonthReward>) response.get("monthRewardList");
//
//            int star = 0;
//            int energy = 0;
//            for (SuperScholarMonthReward monthReward : monthRewardList) {
//                star += monthReward.getStar();
//                energy += monthReward.getEnergy();
//            }
//            BabelRole babelRole = babelLoaderClient.loadRole(userId);
//            MapMessage message = null;
//            if (null != babelRole) {
//                //通天塔星星
//                message = babelServiceClient.useStar(babelRole, -star,
//                        BabelStarChange.PK_SUPERSCHOLAR, "PK我要当学霸奖励星星");
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantSuperScholarHistory add babel star {} error", userId);
//                }
//                message = babelServiceClient.useItem(babelRole, new BabelQuantitiedItem(BabelItem.VITALITY_REFILL, -energy));
//                if (!message.isSuccess()) {
//                    logger.error("Pk grantSuperScholarHistory add babel energy {} error", userId);
//                }
//            } else {
//                logger.error("Pk grantSuperScholarHistory babelRole {} not loaded", userId);
//            }
//            response.remove("monthRewardList");
//        }
//
//        setSecurityKey(response);
//        return response;
//    }
//
//    @RequestMapping(value = "/loadclassmaterole.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage loadClassmateRole() {
//        if (null == currentStudentDetail().getClazzId() || 0L == currentStudentDetail().getClazzId()) {
//            return MapMessage.successMessage().add("classmateBrief", Collections.emptyList());
//        }
//        final String cacheKey = CacheKeyGenerator.generateCacheKey(PkClassmateBrief.class, currentStudentDetail().getClazzId());
//        List<PkClassmateBrief> fromCache = PkCache.getPkCache().load(cacheKey);
//        if (null != fromCache) {
//            for (Iterator<PkClassmateBrief> iter = fromCache.iterator(); iter.hasNext(); ) {
//                PkClassmateBrief pkClassmateBrief = iter.next();
//                if (pkClassmateBrief.userId.equals(String.valueOf(currentUserId()))) {
//                    iter.remove();
//                    break;
//                }
//            }
//            return MapMessage.successMessage().add("classmateBrief", fromCache);
//        }
//
//        List<User> classmateList = userAggregationLoaderClient.loadLinkedStudentsByClazzId(currentStudentDetail().getClazzId(), currentUserId());
//        if (CollectionUtils.isEmpty(classmateList)) {
//            return MapMessage.successMessage().add("classmateBrief", Collections.emptyList());
//        }
//
//        Map<Long, User> userIds = new HashMap<>();
//        for (User user : classmateList) {
//            userIds.put(user.getId(), user);
//        }
//
//        Map<Long, Role> map = pkLoaderClient.loadRoles(new HashSet<>(userIds.keySet()));
//        List<Role> classmateRoleList = CollectionUtils.toLinkedList(map.values());
//        if (CollectionUtils.isEmpty(classmateRoleList)) {
//            return MapMessage.successMessage().add("classmateBrief", Collections.emptyList());
//        }
//        List<PkClassmateBrief> rtn = new ArrayList<>();
//        for (Role role : classmateRoleList) {
//            if (!userIds.containsKey(role.getId())) {
//                continue;
//            }
//            PkClassmateBrief pkClassmateBrief = new PkClassmateBrief();
//            User user = userIds.get(role.getId());
//            pkClassmateBrief.img = getUserAvatarImgUrl(user.fetchImageUrl());
//            pkClassmateBrief.userId = String.valueOf(role.getId());
//            pkClassmateBrief.level = role.getLevel();
//            pkClassmateBrief.username = user.fetchRealname();
//            rtn.add(pkClassmateBrief);
//        }
//        Collections.sort(rtn, new Comparator<PkClassmateBrief>() {
//            @Override
//            public int compare(PkClassmateBrief o1, PkClassmateBrief o2) {
//                int levelCompare = Integer.compare(o2.level, o1.level);
//                int userIdCompare = Long.compare(Long.parseLong(o1.userId), Long.parseLong(o2.userId));
//                return 0 != levelCompare ? levelCompare : userIdCompare;
//            }
//        });
//        PkCache.getPkCache().set(cacheKey, DateUtils.getCurrentToDayEndSecond(), rtn);
//        for (Iterator<PkClassmateBrief> iter = rtn.iterator(); iter.hasNext(); ) {
//            PkClassmateBrief pkClassmateBrief = iter.next();
//            if (pkClassmateBrief.userId.equals(String.valueOf(currentUserId()))) {
//                iter.remove();
//                break;
//            }
//        }
//        return MapMessage.successMessage().add("classmateBrief", rtn);
//    }
//
//    @RequestMapping(value = "/transex.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage transex(String data) {
//        PKTransexRequest req;
//        try {
//            req = PKTransexRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        try {
//            return atomicLockManager.wrapAtomic(this)
//                    .keyPrefix("PK:TRANSEX_LOCK")
//                    .keys(currentUserId())
//                    .expirationInSeconds(10)
//                    .proxy()
//                    .internalTransex(req);
//        } catch (Exception ex) {
//            return MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//        }
//    }
//
//    protected MapMessage internalTransex(PKTransexRequest req) {
//        Role role = pkLoaderClient.loadRole(currentUserId());
//        if (null == role) {
//            return MapMessage.errorMessage();
//        }
//        final int integralCost = req.free ? 0 : 500;
//        if (req.free) {
//            if (null != role.getFreeTransexDate()) {//已经免费变性过了
//                return MapMessage.errorMessage().setErrorCode("100066").setInfo("免费次数不足");
//            }
//        } else {
//            UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
//            if (StringUtils.isNotBlank(ua.getPaymentPassword())) {
//                Password password = Password.of(ua.getPaymentPassword());
//                if (!StringUtils.equals(Password.obscurePassword(req.paymentPassword, password.getSalt()), password.getPassword())) {
//                    return MapMessage.errorMessage().setErrorCode("100001").setInfo("支付密码错误");
//                }
//            }
//            if (integralCost > integralLoaderClient.getIntegralLoader().loadStudentIntegral(currentUserId()).getUsable()) {//学豆不足
//                return MapMessage.errorMessage().setErrorCode("100029").setInfo("学豆不足。");
//            }
//        }
//
//        return pkServiceClient.transex(role, integralCost);
//    }
}
