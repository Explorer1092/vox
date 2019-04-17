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
@RequestMapping("/student/pk/pet")
public class StudentPkPetController extends AbstractGameSupportController {

//    @Inject private IntegralLoaderClient integralLoaderClient;
//
//    /**
//     * 开始孵化宠物
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "hatchstart.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage hatchStart(String data) {
//        PKHatchStartRequest req = null;
//        try {
//            req = PKHatchStartRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:HATCHSTART_LOCK:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("GET_HATCHSTART_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            BabelRolePet babelRolePet = babelLoaderClient.loadRolePet(currentUserId());
//            if (null == babelRolePet) {
//                return MapMessage.errorMessage().setErrorCode("100025").setInfo("宠物蛋不足。");
//            }
//            RolePet rp = babelRolePet.fetchRolePets().get(req.eggId);
//            if (null == rp || rp.getCount() <= 0) {
//                return MapMessage.errorMessage().setErrorCode("100025").setInfo("宠物蛋不足。");
//            }
//            resp = pkServiceClient.hatchPet(currentUserId(), req.eggId, req.phId);
//            if (resp.isSuccess()) {
//                MapMessage petChangeRs = babelServiceClient.changePetCount(currentUserId(), req.eggId, -1);
//                if (!petChangeRs.isSuccess()) {
//                    return MapMessage.errorMessage().setErrorCode("100025").setInfo("宠物蛋不足。");
//                }
//                resp.add("myEggList", ((BabelRolePet) petChangeRs.get("rolePet")).getPetList());
//            } else {
//                return resp;
//            }
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm({}).exception is :{}", data, e);
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//        return resp;
//    }
//
//    /**
//     * 用学豆直接完成孵化
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "finishhatchwithintegral.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage finishHatchWithIntegral(String data) {
//        PKFinishHatchWithIntegralRequest req = null;
//        try {
//            req = PKFinishHatchWithIntegralRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:HATCHFINISH_LOCK:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("GET_HATCHSTART_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            MapMessage check = tryFinishTrainWithIntegral(data);
//            if (!check.isSuccess()) {
//                return check;
//            }
//
//            if (!paywordCheck(currentUser(), req.paymentPassword)) {
//                return MapMessage.errorMessage().setErrorCode("100001").setInfo("支付密码错误。");
//            }
//
//            int integralCost = (Integer) check.get("integralCost");
//            PkPetBag pkPetBag = (PkPetBag) check.get("bag");
//            PkHatcher hatcher = pkPetBag.getHatcher().get(req.phId);
//            resp = pkServiceClient.finishHatchWithIntegral(currentUserId(), req.phId, pkPetBag.getPetMap().get(hatcher.getHatchingPet()), integralCost);
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//        return resp;
//    }
//
//    /**
//     * 孵化完成，点击领取
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "finishhatch.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage finishHatch(String data) {
//        PKFinishHatchRequest req = null;
//        try {
//            req = PKFinishHatchRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        MapMessage resp = MapMessage.errorMessage();
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher hatcher = pkPetBag.getHatcher().get(req.phId);
//        if (null == hatcher) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isEmpty(hatcher.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100027").setInfo("孵化器/训练器空闲。");
//        }
//        if (hatcher.getHfTime().after(new Date())) {
//            return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//        }
//        resp = pkServiceClient.finishHatch(currentUserId(), req.phId, pkPetBag.getPetMap().get(hatcher.getHatchingPet()));
//        return resp;
//    }
//
//    /**
//     * 把宠物带到身上
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "equippet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage equipPet(String data) {
//        PKEquipPetRequest req = null;
//        try {
//            req = PKEquipPetRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkRolePet rolePet = pkPetBag.getPetMap().get(req.petUid);
//        if (null == rolePet) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (rolePet.getQuality() < 1) {//还是蛋呢，还想往身上带啊？
//            return MapMessage.errorMessage().setErrorCode("100033").setInfo("不可携带孵化中的宠物。");
//        }
//
//        if (pkPetBag.getCurEquipPid().equals(req.petUid)) {
//            return MapMessage.successMessage().add("petBag", pkPetBag);
//        }
//        return pkServiceClient.changeEquipPet(currentUserId(), pkPetBag.getCurEquipPid(), req.petUid);
//    }
//
//    /**
//     * 卸下宠物
//     *
//     * @return
//     */
//    @RequestMapping(value = "unequippet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage unequipPet() {
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        if (StringUtils.isEmpty(pkPetBag.getCurEquipPid())) {
//            return MapMessage.errorMessage().setErrorCode("100034").setInfo("没有携带宠物。");
//        }
//        if (pkPetBag.getCurEquipPid().equals("")) {
//            return MapMessage.successMessage().add("petBag", pkPetBag);
//        }
//        return pkServiceClient.changeEquipPet(currentUserId(), pkPetBag.getCurEquipPid(), "");
//    }
//
//    /**
//     * 赞孵化中宠物
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "hatchlike.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage hatchLike(String data) {
//        PkHatchLikeRequest req = null;
//        try {
//            req = PkHatchLikeRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        long targetUserId = NumberUtils.toLong(req.userId);
//        if (targetUserId <= 0L) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        StudentDetail targetDetail = studentLoaderClient.loadStudentDetail(targetUserId);
//        if (null == targetDetail || targetUserId == currentUserId().longValue() || !(Objects.equals(currentStudentDetail().getClazzId(), targetDetail.getClazzId()))) {
//            return MapMessage.errorMessage().setErrorCode("100032").setInfo("非同班同学。");
//        }
//
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(targetUserId);
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher hatcher = pkPetBag.getHatcher().get(req.phId);
//        if (null == hatcher) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        Date now = new Date();
//        if (!hatcher.getHfTime().after(now)) {
//            return MapMessage.errorMessage().setErrorCode("100028").setInfo("孵化/训练已经完成。");
//        }
//        if (StringUtils.isEmpty(hatcher.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100027").setInfo("孵化器/训练器空闲。");
//        }
//        if (hatcher.getLikedUser() > 0L) {
//            return MapMessage.errorMessage().setErrorCode("100031").setInfo("孵化器/训练器已有人赞过。");
//        }
//
//        BabelPet babelPet = null;
//        try {
//            babelPet = babelLoaderClient.loadPets().get(hatcher.getCurStId());
//        } catch (Exception e) {
//            logger.error("NO babel npc found for hatcherPet {}.Exception {}", hatcher.getHatchingPet(), e);
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        if (null == babelPet) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:HATCH_LIKE:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("HATCH_LIKE: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            resp = pkServiceClient.hatchLike(targetDetail, hatcher, currentUser(), babelPet.getPetName());
//            if (resp.isSuccess()) {
//                int ranStar = RandomUtils.nextInt(1, 3);
//                MapMessage addStarRs = babelServiceClient.useStar(babelLoaderClient.loadRole(currentUserId()), -ranStar, BabelStarChange.LIKE_PK_PET, new StringBuilder("赞了").append(targetUserId).append("孵化中的宠物").append(hatcher.getHatchingPet()).toString());
//                if (!addStarRs.isSuccess()) {
//                    resp.setErrorCode("100024").setInfo("服务器操作失败");
//                }
//            } else {
//                resp.setErrorCode("100024").setInfo("服务器操作失败");
//            }
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp.setSuccess(false).setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//        return resp;
//    }
//
//
//    /**
//     * 尝试将一个宠物放到训练室，弹出二次确认，显示预计获得经验
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "pktrypettrain.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage pkTryPetTrain(String data) {
//        PKTryPetTrainRequest req = null;
//        try {
//            req = PKTryPetTrainRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        Role myRole = pkLoaderClient.loadRole(currentUserId());
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        MapMessage resp = trainPetPrecheck(myRole, pkPetBag, req.petUid, req.phId);
//        if (!resp.isSuccess()) {
//            return resp;
//        }
//
//        return resp.add("expExpect", pkLoaderClient.calcPetTrainExpExpect(myRole, pkPetBag.getPetMap().get(req.petUid), pkPetBag.getTrainer().get(req.phId)));
//    }
//
//    /**
//     * 将宠物放入训练室
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "pettrainstart.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage petTrainStart(String data) {
//        PKTryPetTrainRequest req = null;
//        try {
//            req = PKTryPetTrainRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        Role myRole = pkLoaderClient.loadRole(currentUserId());
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        MapMessage resp = trainPetPrecheck(myRole, pkPetBag, req.petUid, req.phId);
//        if (!resp.isSuccess()) {
//            return resp;
//        }
//
//        return pkServiceClient.trainPet(myRole, pkPetBag.getPetMap().get(req.petUid), pkPetBag.getTrainer().get(req.phId));
//    }
//
//    private MapMessage trainPetPrecheck(Role myRole, PkPetBag pkPetBag, String petUid, String phId) {
//        if (null == myRole) {
//            return MapMessage.errorMessage().setErrorCode("100000").setInfo("角色不存在。");
//        }
//
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher trainer = pkPetBag.getTrainer().get(phId);
//        if (null == trainer) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isNoneBlank(trainer.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100023").setInfo("孵化器/训练器已被占用。");
//        }
//        PkRolePet pkRolePet = pkPetBag.getPetMap().get(petUid);
//        if (null == pkRolePet) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (pkRolePet.getQuality() == 0) {
//            return MapMessage.errorMessage().setErrorCode("100035").setInfo("不可训练孵化中的宠物。");
//        }
//
//        if (pkRolePet.isEvolving()) {
//            return MapMessage.errorMessage().setErrorCode("100063").setInfo("宠物正在训练中。");
//        }
//
//        if (trainer.getOpenByLevel() > myRole.getLevel()) {
//            return MapMessage.errorMessage().setErrorCode("100026").setInfo("孵化器/训练器未开放。");
//        }
//        return MapMessage.successMessage();
//    }
//
//    /**
//     * 赞训练中宠物
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "pettrainlike.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage petTrainLike(String data) {
//        PkHatchLikeRequest req = null;
//        try {
//            req = PkHatchLikeRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        long targetUserId = NumberUtils.toLong(req.userId);
//        if (targetUserId <= 0L) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        StudentDetail targetDetail = studentLoaderClient.loadStudentDetail(targetUserId);
//        if (null == targetDetail || targetUserId == currentUserId().longValue() || !(Objects.equals(currentStudentDetail().getClazzId(), targetDetail.getClazzId()))) {
//            return MapMessage.errorMessage().setErrorCode("100032").setInfo("非同班同学。");
//        }
//
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(targetUserId);
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher trainer = pkPetBag.getTrainer().get(req.phId);
//        if (null == trainer) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        Date now = new Date();
//        if (!trainer.getHfTime().after(now)) {
//            return MapMessage.errorMessage().setErrorCode("100028").setInfo("孵化/训练已经完成。");
//        }
//        if (StringUtils.isEmpty(trainer.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100027").setInfo("孵化器/训练器空闲。");
//        }
//        if (trainer.getLikedUser() > 0L) {
//            return MapMessage.errorMessage().setErrorCode("100031").setInfo("孵化器/训练器已有人赞过。");
//        }
//
//        BabelNpc babelNpc = null;
//        try {
//            babelNpc = babelLoaderClient.loadAvailableNpcs().get(trainer.getCurStId());
//        } catch (Exception e) {
//            logger.error("NO babel npc found for trainerPet {}.Exception {}", trainer.getHatchingPet(), e);
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        if (null == babelNpc) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        String lock = new StringBuilder("PK:TRAIN_LIKE:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("TRAIN_LIKE: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            resp = pkServiceClient.trainLike(targetDetail, trainer, currentUser(), babelNpc.getName());
//            if (resp.isSuccess()) {
//                int ranStar = RandomUtils.nextInt(1, 3);
//                MapMessage addStarRs = babelServiceClient.useStar(babelLoaderClient.loadRole(currentUserId()), -ranStar, BabelStarChange.LIKE_PK_PET, new StringBuilder("赞了").append(targetUserId).append("训练中的宠物").append(trainer.getHatchingPet()).toString());
//                if (!addStarRs.isSuccess()) {
//                    resp.setErrorCode("100024").setInfo("服务器操作失败");
//                }
//            } else {
//                resp.setErrorCode("100024").setInfo("服务器操作失败");
//            }
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp.setSuccess(false).setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//        return resp;
//    }
//
//    /**
//     * try用学豆直接完成训练
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = {"tryfinishtrainwithintegral.vpage", "tryfinishhatchwithintegral.vpage"}, method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage tryFinishTrainWithIntegral(String data) {
//        boolean isHatch = getRequest().getRequestURL().indexOf("finishhatchwithintegral") > 0;
//        PKFinishHatchWithIntegralRequest req = null;
//        try {
//            req = PKFinishHatchWithIntegralRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        Date now = new Date();
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher trainer = isHatch ? pkPetBag.getHatcher().get(req.phId) : pkPetBag.getTrainer().get(req.phId);
//        if (null == trainer) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isEmpty(trainer.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100027").setInfo("孵化器/训练器空闲。");
//        }
//        if (!trainer.getHfTime().after(now)) {
//            return MapMessage.errorMessage().setErrorCode("100028").setInfo("孵化/训练已完成。");
//        }
//        long timeDiff = trainer.getHfTime().getTime() - now.getTime();
//        int integralCost = new BigDecimal(timeDiff).divide(new BigDecimal(60 * 60 * 1000), 0, BigDecimal.ROUND_UP).intValue() * PkPetConstant.FINISH_HATCH_INTEGRAL_COST_PER_HOUR;
//        if (integralCost > integralLoaderClient.getIntegralLoader().loadStudentIntegral(currentUserId()).getUsable()) {
//            return MapMessage.errorMessage().setErrorCode("100029").setInfo("学豆不足。");
//        }
//        return MapMessage.successMessage().add("integralCost", integralCost).add("bag", pkPetBag);
//    }
//
//    /**
//     * 用学豆直接完成训练
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "finishtrainwithintegral.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage finishTrainWithIntegral(String data) {
//        PKFinishHatchWithIntegralRequest req = null;
//        try {
//            req = PKFinishHatchWithIntegralRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:TRAINFINISH_LOCK:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("GET_TRAINFINISH_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            MapMessage check = tryFinishTrainWithIntegral(data);
//            if (!check.isSuccess()) {
//                return check;
//            }
//
//            if (!paywordCheck(currentUser(), req.paymentPassword)) {
//                return MapMessage.errorMessage().setErrorCode("100001").setInfo("支付密码错误。");
//            }
//
//            int integralCost = (Integer) check.get("integralCost");
//            PkPetBag pkPetBag = (PkPetBag) check.get("bag");
//            PkHatcher trainer = pkPetBag.getTrainer().get(req.phId);
//            resp = pkServiceClient.finishTrainWithIntegral(currentUserId(), trainer, pkPetBag.getPetMap().get(trainer.getHatchingPet()), integralCost);
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//        return resp;
//    }
//
//    /**
//     * 训练完成，点击领取
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "pettrainfinish.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage petTrainFinish(String data) {
//        PKFinishHatchRequest req = null;
//        try {
//            req = PKFinishHatchRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        MapMessage resp = MapMessage.errorMessage();
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher trainer = pkPetBag.getTrainer().get(req.phId);
//        if (null == trainer) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isEmpty(trainer.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100027").setInfo("孵化器/训练器空闲。");
//        }
//        if (trainer.getHfTime().after(new Date())) {
//            return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//        }
//        resp = pkServiceClient.finishTrain(currentUserId(), trainer, pkPetBag.getPetMap().get(trainer.getHatchingPet()));
//        return resp;
//    }
//
//    /**
//     * 升级孵蛋器
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "upgradehatcher.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage upgradeHatcher(String data) {
//        PKUpgradeHatcherRequest req = null;
//        try {
//            req = PKUpgradeHatcherRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher hatcher = pkPetBag.getHatcher().get(req.phId);
//        if (null == hatcher) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isNotEmpty(hatcher.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//        }
//        if (hatcher.getLevel() >= PkPetConstant.HATCHER_MAX_LEVEL) {
//            return MapMessage.errorMessage().setErrorCode("100036").setInfo("孵化器/训练器已达最大等级，不可升级。");
//        }
//
//        if (!paywordCheck(currentUser(), req.paymentPassword)) {
//            return MapMessage.errorMessage().setErrorCode("100001").setInfo("支付密码错误。");
//        }
//
//        int integralCost = hatcher.getLevel() * 200;
//
//        long usableIntegral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(currentUserId()).getUsable();
//        if (usableIntegral < integralCost) {
//            return MapMessage.errorMessage().setErrorCode("100029").setInfo("学豆不足。");
//        }
//        return pkServiceClient.upgradeHatcherLevel(currentUserId(), hatcher, integralCost, "hatcher.");
//    }
//
//    private boolean paywordCheck(User user, String passwordInput) {
//        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
//        if (StringUtils.isBlank(ua.getPaymentPassword())) {
//            return true;
//        }
//        Password password = Password.of(ua.getPaymentPassword());
//        if (!StringUtils.equals(Password.obscurePassword(passwordInput, password.getSalt()), password.getPassword())) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * 升级训练器
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "upgradetrainer.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage upgradeTrainer(String data) {
//        PKUpgradeHatcherRequest req = null;
//        try {
//            req = PKUpgradeHatcherRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkHatcher hatcher = pkPetBag.getTrainer().get(req.phId);
//        if (null == hatcher) {
//            return MapMessage.errorMessage().setErrorCode("100022").setInfo("不存在的孵化器/训练器。");
//        }
//        if (StringUtils.isNotEmpty(hatcher.getHatchingPet())) {
//            return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//        }
//        if (hatcher.getLevel() >= PkPetConstant.HATCHER_MAX_LEVEL) {
//            return MapMessage.errorMessage().setErrorCode("100036").setInfo("孵化器/训练器已达最大等级，不可升级。");
//        }
//
//        if (!paywordCheck(currentUser(), req.paymentPassword)) {
//            return MapMessage.errorMessage().setErrorCode("100001").setInfo("支付密码错误。");
//        }
//
//        int integralCost = hatcher.getLevel() * 200;
//
//        long usableIntegral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(currentUserId()).getUsable();
//        if (usableIntegral < integralCost) {
//            return MapMessage.errorMessage().setErrorCode("100029").setInfo("学豆不足。");
//        }
//        return pkServiceClient.upgradeHatcherLevel(currentUserId(), hatcher, integralCost, "trainer.");
//    }
//
//
//    /**
//     * 尝试卖出宠物，给出卖出价格
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "trysellpet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage trysellpet(String data) {
//        PKTrySellPetRequest req = null;
//        try {
//            req = PKTrySellPetRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        return sellPetPrecheck(pkPetBag, req.petUid);
//    }
//
//    /**
//     * 卖出宠物，返回星星数
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "sellpet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage sellpet(String data) {
//        PKSellPetRequest req = null;
//        try {
//            req = PKSellPetRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:SELLPET_LOCK:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("GET_SELLPET_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//            resp = sellPetPrecheck(pkPetBag, req.petUid);
//            if (!resp.isSuccess()) {
//                return resp;
//            }
//
//            int sellPrice = (Integer) resp.get("starGet");
//
//            MapMessage getStarResp = babelServiceClient.useStar(babelLoaderClient.loadRole(currentUserId()), -sellPrice, BabelStarChange.SELL_PK_PET, "卖出宠物" + req.petUid);
//            if (getStarResp.isSuccess()) {
//                resp = pkServiceClient.removePet(currentUserId(), req.petUid, pkPetBag);
//                BabelRole babelRole = (BabelRole) getStarResp.get("role");
//                resp.add("starGet", sellPrice);
//                resp.add("starTotal", babelRole.getStarCount());
//            } else {
//                resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败。");
//            }
//
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//
//        return resp;
//    }
//
//    private MapMessage sellPetPrecheck(PkPetBag pkPetBag, String petUid) {
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkRolePet pkRolePet = pkPetBag.getPetMap().get(petUid);
//        if (null == pkRolePet) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (pkRolePet.getQuality() < 1) {
//            return MapMessage.errorMessage().setErrorCode("100037").setInfo("不可卖出宠物蛋。");
//        }
//        if (pkRolePet.getQuality() > 5) {
//            return MapMessage.errorMessage().setErrorCode("100014").setInfo("背包数据异常。");
//        }
//        if (pkRolePet.isEvolving()) {
//            return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//        }
//        if (pkRolePet.isLocked()) {
//            return MapMessage.errorMessage().setErrorCode("100038").setInfo("宠物锁定中。");
//        }
//        if (pkRolePet.getPetUid().equals(pkPetBag.getCurEquipPid())) {
//            return MapMessage.errorMessage().setErrorCode("100039").setInfo("不可卖出携带中的宠物。");
//        }
//        int sellPrice = new BigDecimal(pkRolePet.getLevel()).multiply(new BigDecimal(PkPetConstant.PET_SELL_RATE.get(pkRolePet.getQuality()))).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//        return MapMessage.successMessage().add("starGet", sellPrice);
//    }
//
//    /**
//     * 尝试升级宠物技能，返回需要星星数
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "trypetskillupgrade.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage tryPetSkillUpgrade(String data) {
//        PKTryPetSkillUpgradeRequest req = null;
//        try {
//            req = PKTryPetSkillUpgradeRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        BabelRole babelRole = babelLoaderClient.loadRole(currentUserId());
//        if (null == babelRole) {
//            return MapMessage.errorMessage().setErrorCode("100000").setInfo("角色不存在。");
//        }
//
//        return pkServiceClient.upgradePetSkillPrecheck(pkPetBag, babelRole.getStarCount(), req.petUid, req.petSkillId);
//    }
//
//    /**
//     * 用通天塔星星升级宠物技能
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "petskillupgrade.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage petSkillUpgrade(String data) {
//        PKPetSkillUpgradeRequest req = null;
//        try {
//            req = PKPetSkillUpgradeRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        String lock = new StringBuilder("PK:UPGRADE_PET_SKILL_LOCK:").append(currentUserId()).append(":lock").toString();
//        try {
//            atomicLockManager.acquireLock(lock, 10);
//        } catch (CannotAcquireLockException ex) {
//            logger.warn("GET_UPGRADE_PET_SKILL_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        MapMessage resp = MapMessage.errorMessage();
//        try {
//            BabelRole babelRole = babelLoaderClient.loadRole(currentUserId());
//            if (null == babelRole) {
//                return MapMessage.errorMessage().setErrorCode("100000").setInfo("角色不存在。");
//            }
//            resp = pkServiceClient.upgradePetSkill(currentUserId(), babelRole.getStarCount(), req.petUid, req.petSkillId);
//            if (resp.isSuccess()) {
//                int starCost = (Integer) resp.get("starCost");
//                MapMessage consumeStarRs = babelServiceClient.useStar(babelRole, starCost, BabelStarChange.UPGRADE_PK_PET_SKILL, "给宠物" + req.petUid + "升级技能" + req.petSkillId);
//                if (!consumeStarRs.isSuccess()) {
//                    resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//                }
//                BabelRole roleAfter = (BabelRole) consumeStarRs.get("role");
//                resp.add("starTotal", roleAfter.getStarCount());
//            }
//        } catch (Exception e) {
//            logger.error("Excepiton with pararm().exception is :{}", data, e);
//            resp = MapMessage.errorMessage().setErrorCode("100024").setInfo("服务器操作失败");
//        } finally {
//            atomicLockManager.releaseLock(lock);
//        }
//
//        return resp;
//    }
//
//    /**
//     * 锁定宠物
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "lockpet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage lockPet(String data) {
//        PKLockPetRequest req = null;
//        try {
//            req = PKLockPetRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkRolePet rolePet;
//        if ((rolePet = pkPetBag.getPetMap().get(req.petUid)) == null) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (rolePet.isLocked()) {
//            return MapMessage.successMessage().add("petBag", pkPetBag);
//        }
//        return pkServiceClient.setPetLock(currentUserId(), req.petUid, true);
//    }
//
//    /**
//     * 解锁宠物
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "unlockpet.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage unlockPet(String data) {
//        PKLockPetRequest req = null;
//        try {
//            req = PKLockPetRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        PkRolePet rolePet;
//        if ((rolePet = pkPetBag.getPetMap().get(req.petUid)) == null) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (!rolePet.isLocked()) {
//            return MapMessage.successMessage().add("petBag", pkPetBag);
//        }
//        return pkServiceClient.setPetLock(currentUserId(), req.petUid, false);
//    }
//
//    /**
//     * 合成宠物，进化到下一个形态
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "petevolution.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage petEvolution(String data) {
//        PKPetEvolutionRequest req = null;
//        try {
//            req = PKPetEvolutionRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//
//        PkPetBag pkPetBag = pkLoaderClient.loadPkPetBag(currentUserId());
//        if (null == pkPetBag) {
//            return MapMessage.errorMessage().setErrorCode("100020").setInfo("创建宠物背包失败。");
//        }
//        if (req.mPidList.contains(req.evolvPid)) {
//            return MapMessage.errorMessage().setErrorCode("100049").setInfo("材料数量不符。");
//        }
//
//
//        PkRolePet evolvPet = pkPetBag.getPetMap().get(req.evolvPid);
//        if (null == evolvPet) {
//            return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//        }
//        if (evolvPet.getQuality() < 1 || evolvPet.getQuality() >= PkPetConstant.PET_MAX_QUALITY) {
//            return MapMessage.errorMessage().setErrorCode("100047").setInfo("宠物品质不可进化。");
//        }
//        if (evolvPet.getLevel() != PkPetConstant.PET_QUALITY_MAX_LEVEL.get(evolvPet.getQuality())) {
//            return MapMessage.errorMessage().setErrorCode("100050").setInfo("宠物等级不可进化。");
//        }
//        Set<String> petCandidate = new HashSet<>();
//        petCandidate.addAll(req.mPidList);
//        if (petCandidate.size() != PkPetConstant.PET_EVOLV_MCOST.get(evolvPet.getQuality())) {
//            return MapMessage.errorMessage().setErrorCode("100049").setInfo("材料数量不符。");
//        }
//
//        petCandidate.add(req.evolvPid);
//        for (String petUid : petCandidate) {
//            PkRolePet pkRolePet = pkPetBag.getPetMap().get(petUid);
//            if (null == pkRolePet) {
//                return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在这样的宠物。");
//            }
//            if (pkRolePet.isEvolving()) {
//                return MapMessage.errorMessage().setErrorCode("100030").setInfo("孵化/训练尚未完成。");
//            }
//            if (!petUid.equals(req.evolvPid) && petUid.equals(pkPetBag.getCurEquipPid())) {
//                return MapMessage.errorMessage().setErrorCode("100046").setInfo("宠物携带中。");
//            }
//            if (evolvPet.getQuality() != pkRolePet.getQuality()) {
//                return MapMessage.errorMessage().setErrorCode("100048").setInfo("材料品质不符，不可使用。");
//            }
//        }
//        return pkServiceClient.evolvPet(currentUserId(), evolvPet, new HashSet<>(req.mPidList));
//    }
//
//    /**
//     * 去同学家的孵化/训练室
//     *
//     * @return
//     */
//    @RequestMapping(value = {"gotoclassmatehatcher.vpage", "gotoclassmatetrainer.vpage"}, method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage gotoClassmateHatcher() {
//        final boolean isHatcher = getRequest().getRequestURL().indexOf("gotoclassmatehatcher") > 0;
//
//        String errorStr = isHatcher ? "孵化" : "训练";
////        List<User> classmates = getStudentLoader().loadStudentClassmates(currentUserId());
//        StudentDetail studentDetail = currentStudentDetail();
//        List<User> classmates = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(studentDetail.getClazzId(), studentDetail.getId());
//        if (CollectionUtils.isEmpty(classmates)) {
//            return MapMessage.errorMessage().setErrorCode("100051").setInfo("你的同学还没有宠物");
//        }
//        Set<Long> clazzMateUserId = new HashSet<>();
//        Map<Long, User> userMap = new HashMap<>();
//        for (User us : classmates) {
//            clazzMateUserId.add(us.getId());
//            userMap.put(us.getId(), us);
//        }
//        Map<Long, PkPetBag> classmateBags = pkLoaderClient.loadPkPetBags(clazzMateUserId);
//        if (CollectionUtils.isEmpty(classmateBags.values())) {
//            return MapMessage.errorMessage().setErrorCode("100051").setInfo("你的同学还没有宠物");
//        }
//        List<PkPetBag> bagList = new ArrayList<>(classmateBags.values());
//        bagList = bagList.stream().filter(source -> {
//            for (PkHatcher trainer : isHatcher ? source.getHatcher().values() : source.getTrainer().values()) {
//                if (StringUtils.isNoneBlank(trainer.getHatchingPet())) {
//                    return true;
//                }
//                    }
//            return false;
//                }
//        ).collect(Collectors.toList());
//
//        if (CollectionUtils.isEmpty(bagList)) {
//            return MapMessage.errorMessage().setErrorCode("100052").setInfo("你的同学还没有" + errorStr + "宠物");
//        }
//        PkPetBag randomBag = RandomUtils.pickRandomElementFromList(bagList);
//        Role pkRole = pkLoaderClient.loadRole(randomBag.getUserId());
//        PkPetBagInfo pkPetBagInfo = RoleBuildUtil.toBagInfo(randomBag, pkRole);
//        String realname = userMap.get(randomBag.getUserId()).fetchRealname();
//        return MapMessage.successMessage().add("realname", realname).add("petBag", pkPetBagInfo);
//    }
//
//    /**
//     * 帮助列表（我曾经赞过/别人曾经赞过我 的宠物，）
//     *
//     * @param data
//     * @return
//     */
//    @RequestMapping(value = "getpetlikehistory.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage GetPetLikeHistory(String data) {
//        PKGetPetLikeHistoryInfoRequest req = null;
//        try {
//            req = PKGetPetLikeHistoryInfoRequest.parseRequest(data);
//        } catch (Exception e) {
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        return pkLoaderClient.getPetLikeHistory(currentUserId(), req.isHatcher);
//    }
}
