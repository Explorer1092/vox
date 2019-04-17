<#import '../../layout.ftl' as layout>
<#assign title = "假期作业">
<#assign extraRequireJs = [
    "public/script/parentMobile/activity/holiday"
]>
<@layout.page className='winterVacation' pageJs="windowVacation_isExtraRequireJs" title="${title}" extraRequireJs = extraRequireJs>
    <#include "./commonModule.ftl">
    ${buildAutoTrackTag("vacationhw|vacationhw_open")}
    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "${title}">
        <#include "../../top.ftl" >

        <div class="parentApp-winterHoliday">
            <div class="parentApp-mock-background"></div>
            <div class="inner">
                <#assign
                subject2class = {
                "ENGLISH" : "green",
                "MATH" : "yellow"
                }
                subject2trackTag = {
                "ENGLISH" : "en",
                "MATH" : "math"
                }
                canRewardIntegalCount = 100
                >
                <#list (result![]) as winterVacation>
                    <#assign
                    total = winterVacation.packageCount!0
                    current = winterVacation.finishPackageCount!0
                    subject=(winterVacation.subject!"")?upper_case
                    homeworkType = (winterVacation.homeworkType!"")?upper_case
                    >
                    <div class="item clearfix">
                        <div class="canvasMod">
                            <div class="hd clearfix"><span class="${subject2class[subject]!""}">${winterVacation.title!""}</span></div>
                            <div class="canvas">
                                <canvas class="doProgress" data-all="${total}" data-percent="${current}">
                                    您的浏览器不支持canvas
                                </canvas>
                            </div>
                            <div class="ft">距离结束:<span>${winterVacation.leftDay!0}天${winterVacation.leftHour!0}小时</span></div>
                        </div>
                        <div class="textMod">
                            <div class="beans">全部完成可领${canRewardIntegalCount}学豆奖励</div>
                            <#assign
                            rewarded = winterVacation.rewarded!true
                            isCanReward = !(winterVacation.expired!true) && !rewarded && (total == current)
                            preText = rewarded?string("已", "")
                            trackTag = subject2trackTag[subject]!""
                            >
                            <#if isCanReward>
                                <a href="javascript:;" data-homework_type="${homeworkType}" ${buildTrackData("vacationhw|finishbean_" + trackTag + "_click")} class="btn doTrack doAcceptReward">领取${canRewardIntegalCount}学豆</a>
                            <#else>
                                <a href="javascript:;" class="btn btn-gary">${preText}领取${canRewardIntegalCount}学豆</a>
                            </#if>
                        </div>
                    </div>
                    <#assign comment = (winterVacation.comment!"")?trim>
                    <#if comment != "">
                        <div class="promModule">
                            <div class="promMain">${comment}</div>
                        </div>
                    </#if>
                </#list>

                <#if (result![])?size == 0>
                <div class="parentApp-holidayNull">
                    <div>老师暂未布置假期作业哦</div>
                </div>
                </#if>

                <#if product?exists >
                    <#assign
                    sid = sid!""
                    productKey = product.key!""
                    >
                    <div class="item clearfix" style="opacity: .9; height: auto;">
                        <div class="canvasMod" style="width: auto; padding: ">
                            <div class="hd clearfix"><span class="blue">自选练习</span></div>
                            <div class="imgtext-h clearfix">
                                <#include "../../shopIcon.ftl">
                                <a href="javascript:;" class="img-left"><img src="${getShopIconSrc(productKey)}" alt=""/></a>
                                <div class="text-right">
                                    <h3>${product.name!""}</h3>
                                    <p>${product.info!""}</p>
                                </div>
                            </div>
                            <div class="promoteAction">
                                <a href="/parentMobile/ucenter/shoppinginfolist.vpage?sid=${sid}" class="link">查看更多练习</a>
                                <a href="/parentMobile/ucenter/shoppinginfo.vpage?sid=${sid}&productType=${productKey}" class="btn">为孩子开通</a>
                            </div>
                        </div>
                    </div>
                </#if>
            </div>
            <script>
                PM.no_select_kids = "${(no_select_kids?exists)?string('1', '0')}";
                PM.default_user_image = "${publicDefaultUserImg}";
            </script>
        </div>
    </#escape>
</@layout.page>
