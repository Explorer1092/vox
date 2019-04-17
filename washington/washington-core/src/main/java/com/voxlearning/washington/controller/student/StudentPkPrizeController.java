/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

/**
 * Created by Sadi.Wan on 2015/2/2.
 */
@Controller
@RequestMapping("/student/pk/prize")
public class StudentPkPrizeController extends AbstractGameSupportController {
//    /**
//     * 获取奖品列表
//     *
//     * @return
//     */
//    @RequestMapping(value = "getprizelist.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage getPrizeList() {
//        PkStoredPrize pkStoredPrize = pkLoaderClient.loadStoredPrize(currentUserId());
//        if(null == pkStoredPrize || pkStoredPrize.getPrizeDetailMap().isEmpty()){
//            return MapMessage.successMessage().add("prizeList", Collections.emptyList());
//        }
//        List<PkPrizeDetail> pkPrizeDetailIterator = new ArrayList<>(pkStoredPrize.getPrizeDetailMap().values());
//        List<PkStoredPrizeInfo> pkStoredPrizeInfoList = new ArrayList<>();
//        for(int  i = pkPrizeDetailIterator.size() - 1; i >= 0 ; i--){
//            PkPrizeDetail pkPrizeDetail = pkPrizeDetailIterator.get(i);
//            if(null == pkPrizeDetail || pkPrizeDetail.isExchanged()){
//                continue;
//            }
//            PkStoredPrizeInfo pkStoredPrizeInfo = new PkStoredPrizeInfo();
//            pkStoredPrizeInfo.fillFrom(pkPrizeDetail);
//            pkStoredPrizeInfo.acTime = DateUtils.dateToString(pkPrizeDetail.getGetTime());
//            pkStoredPrizeInfoList.add(pkStoredPrizeInfo);
//        }
//        return MapMessage.successMessage().add("prizeList",pkStoredPrizeInfoList);
//    }
//
//    /**
//     * 测试发奖
//     *
//     * @return
//     */
//    @RequestMapping(value = "ttt.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage TTT() {
//        return pkServiceClient.grantPkPrize(currentUserId(),"TEST_1");
//    }
//
//    /**
//     * 兑换奖品
//     *
//     * @return
//     */
//    @RequestMapping(value = "exchangeprize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage exchangePrize(String data) {
//        PKExchangePrizeRequest req = null;
//        try{
//            req = PKExchangePrizeRequest.parseRequest(data);
//        }catch(Exception e){
//            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
//        }
//        PkStoredPrize pkStoredPrize = pkLoaderClient.loadStoredPrize(currentUserId());
//        PkPrizeDetail pkPrizeDetail = pkStoredPrize.getPrizeDetailMap().get(req.oid);
//        if(null == pkPrizeDetail){
//            return MapMessage.errorMessage().setErrorCode("100056").setInfo("不存在的奖品。");
//
//        }
//        if(pkPrizeDetail.isExchanged()){
//            return MapMessage.errorMessage().setErrorCode("100058").setInfo("奖品已兑换。");
//        }
//        MapMessage setExchange;
//        switch(pkPrizeDetail.getPrizeType()){
//            case BABEL_STAR:
//                setExchange = pkServiceClient.setExternalPrizeExchanged(pkPrizeDetail);
//                if(!setExchange.isSuccess()){
//                    logger.warn("USER set exchange failed. data:{}",data);
//                    return MapMessage.errorMessage().setErrorCode("100057").setInfo("发奖失败，请联系客服");
//                }
////                MapMessage incStarMsg = babelServiceClient.useStar(babelLoaderClient.loadRole(currentUserId()),-pkPrizeDetail.getCount(), BabelStarChange.PK_PRIZE_EXCHANGE,"领取pk奖品,id:" + pkPrizeDetail.getOid());
//                MapMessage incStarMsg = babelServiceClient.useStar(babelLoaderClient.loadRole(currentUserId()),-pkPrizeDetail.getCount(), BabelStarChange.SELL_PK_PET,"领取pk奖品,id:" + pkPrizeDetail.getOid());
//                if(!incStarMsg.isSuccess()){
//                    logger.warn("USER exchange BABEL_STAR failed.try to rollback. data:{}",data);
//                    pkServiceClient.setPrizeNotExchanged(currentUserId(),pkPrizeDetail.getOid());
//                    return MapMessage.errorMessage().setErrorCode("100057").setInfo("发奖失败,请稍后重试");
//                }
//                pkServiceClient.removeExchangedExternalPrize(pkPrizeDetail);
//                break;
//            case BABEL_PET:
//                BabelRolePet babelRolePet = babelLoaderClient.loadRolePet(currentUserId());
//                if(null == babelRolePet){
//                    return MapMessage.errorMessage().setErrorCode("100060").setInfo("查询宠物蛋失败");
//                }
//                int petId = 0;
//                try{
//                    petId = Integer.parseInt(pkPrizeDetail.getIdList().get(0));
//                }catch(Exception e){
//                    return MapMessage.errorMessage().setErrorCode("100060").setInfo("查询宠物蛋失败");
//                }
//                setExchange = pkServiceClient.setExternalPrizeExchanged(pkPrizeDetail);
//                if(!setExchange.isSuccess()){
//                    logger.warn("USER set exchange failed. data:{}",data);
//                    return MapMessage.errorMessage().setErrorCode("100057").setInfo("发奖失败,请稍后重试");
//                }
//                MapMessage incPet = babelServiceClient.changePetCount(currentUserId(),petId,pkPrizeDetail.getCount());
//                if(!incPet.isSuccess()){
//                    logger.warn("USER exchange BABEL_PET failed.try to rollback. data:{}",data);
//                    pkServiceClient.setPrizeNotExchanged(currentUserId(),pkPrizeDetail.getOid());
//                    return MapMessage.errorMessage().setErrorCode("100057").setInfo("发奖失败，请稍后重试");
//                }
//                pkServiceClient.removeExchangedExternalPrize(pkPrizeDetail);
//                break;
//            case PK_EQUIPMENT:
//            case PK_VITALITY:
//            case PK_PET:
//            case PK_FASHION:
//            case PK_EXP:
//                return pkServiceClient.exchangeInternalPrize(pkPrizeDetail);
//        }
//        return MapMessage.successMessage();
//    }
}
