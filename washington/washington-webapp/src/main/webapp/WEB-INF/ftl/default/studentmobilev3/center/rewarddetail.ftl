<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-yolk"
title="详情"
pageJs=["reward"]
pageJsFile={"reward" : "public/script/mobile/student/studentmobilev3"}
pageCssFile={"reward" : ["public/skin/mobile/student/app/prizecenter/css/skin"]}
>
<div class="courseDetails-box">
    <#if detail.image?has_content>
        <img src="<@app.avatar href='/'/>${(detail.image)!}">
    <#else>
        <img data-bind="attr : {src : '<@app.avatar href=''/>'}"/>
    </#if>
</div>
<div class="courseDetails-column">
    <div class="cud-title">${(detail.productName)!'--'}</div>
    <div class="cud-content">
        <div class="cIcon"><i></i>${(detail.discountPrice)!'--'}</div>
    </div>
    <div class="detailDes">${(detail.description)!'--'}</div>
</div>
<div class="courseDetails-footer">
    <div class="inner">

        <div class="tips" style="display: none;">加入心愿池的奖励可以到“心愿池”查看哦</div>
        <div class="innerBoxHeight">
            <div class="innerBox">
                <#if detail.productType == "JPZX_SHIWU">
                    <div class="default_btn orange_btn gray_btn">暂不支持兑换，请到电脑端兑换</div><!--灰色按钮gray_btn：“使用中”-->
                <#else>
                    <#if categories[0]  == 'HEAD_WEAR' >
                        <#if (inuse)!false>
                            <div class="default_btn orange_btn gray_btn">使用中</div>
                        <#else>
                            <#if (exchanged)!false>
                                <div class="default_btn orange_btn" data-bind="click: shopDressOn.bind($data,${(detail.id)!''})">更换装扮</div>
                            <#else>
                                <#if (inWishList)!false>
                                    <div class="default_btn" data-bind="click:delectWishClick.bind($data,${(detail.id)!''},${wishOrderId!''})">移出心愿池</div>
                                <#else>
                                    <div class="default_btn" data-bind="click: addWishClick.bind($data,${(detail.id)!''},'button_virtual_prize_wish_list_click')">加入心愿池</div>
                                </#if>
                                <#if usableIntegral lt (detail.discountPrice) >
                                    <div class="default_btn orange_btn gray_btn">学豆不足，无法兑换</div>
                                <#else>
                                    <div class="default_btn orange_btn" data-bind="click: shopSubmit.bind($data,${(detail.id)!''})">兑换并装扮</div>
                                </#if>
                            </#if>
                        </#if>
                    <#elseif categories[0]  == 'MINI_COURSE' || categories[0]  == 'CHOICEST_ARTICLE' >
                        <#if (exchanged)!false>
                            <a href="javascript:;" data-bind="click: courseVideoClick.bind($data,'${courseVideoUrl!'#'}','${categories[0]!'#'}')" class="default_btn orange_btn">开始学习</a>
                        <#else>
                            <#if (inWishList)!false>
                                <div class="default_btn" data-bind="click:delectWishClick.bind($data,${(detail.id)!''},${wishOrderId!''})">移出心愿池</div>
                            <#else>
                                <div class="default_btn" data-bind="click: addWishClick.bind($data,${(detail.id)!''}, 'button_class_wish_list_click')">加入心愿池</div>
                            </#if>
                            <#if usableIntegral lt (detail.discountPrice) >
                                <div class="default_btn orange_btn gray_btn">学豆不足，无法兑换</div>
                            <#else>
                                <div class="default_btn orange_btn" data-bind="click: videoSubmit.bind($data,${(detail.id)!''})">我要学习</div>
                            </#if>
                        </#if>
                    <#elseif categories[0]  == 'COUPON' >
                        <#if (exchanged)!false>
                            <a href="javascript:;" data-bind="click: couponDetail.bind($data,${(detail.id)!''})" class="default_btn orange_btn">查看优惠券</a>
                        <#else>
                            <#if (inWishList)!false>
                                <div class="default_btn" data-bind="click:delectWishClick.bind($data,${(detail.id)!''},${wishOrderId!''})">移出心愿池</div>
                            <#else>
                                <div class="default_btn" data-bind="click: addWishClick.bind($data,${(detail.id)!''}, 'button_class_wish_list_click')">加入心愿池</div>
                            </#if>
                            <#if (usableIntegral lt (detail.discountPrice))!false>
                                <div class="default_btn orange_btn gray_btn">学豆不足，无法兑换</div>
                            <#elseif (cantExchange)!false >
                                <div class="default_btn orange_btn gray_btn">请绑定家长手机后兑换</div>
                            <#else>
                                <div class="default_btn orange_btn" data-bind="click: changeCoupon.bind($data,${(detail.id)!''},'${(detail.productName)!''}')">兑换优惠券</div>
                            </#if>
                        </#if>
                    </#if>
                </#if>
            </div>
            </div>
        <#if categories[0]  == 'HEAD_WEAR' >
            <a href="/view/mobile/student/center/myprivilege?new_page=blank" class="changemore">查看已兑换装扮</a>
        </#if>
    </div>

</div>

<div class="dressUp-pop" id="dressUpPop" style="display:none;">
    <div class="inner">
        <div class="close" data-bind="click: closePop"></div>
        <div class="courseDetails-box">
            <div class="courseDetails-box" style="margin-top:0.2rem;">
                <#if headWearImgUrl?has_content>
                    <div class="orn01">
                        <img class="ornFaceWear" src="<@app.avatar href='${headWearImgUrl!}'/>">
                    </div>
                </#if>
                <img class="ornFaceImg" src="<@app.avatar href='${currentUser.fetchImageUrl()!}'/>">
            </div>
        </div>
        <div class="courseDetails-column">
            <div class="cud-title">头像装扮：${(detail.productName)!'--'}</div>
            <div class="cud-content">
                <div class="cInfo fr">有效期${(detail.expiryDate)!'--'}天</div>
                <div class="cIcon"><i></i>${(detail.discountPrice)!'--'}</div>
            </div>
        </div>
        <div class="dru-btn">
            <div class="default_btn orange_btn" data-bind="click:WantUsedClick">我要使用</div>
        </div>
    </div>
</div>

<div class="dressUp-pop secondSurePop" id="videoUpPop" style="display: none;">
    <div class="inner innerh">
        <p>确定要花费${(detail.discountPrice)!'--'}学豆，获得这个学习机会？<br /><i>兑换成功后不能退换，<#if categories[0]  == 'MINI_COURSE'>课程永久免费观看<#else>家长将收到相关课程提醒</#if></i></p>
        <div class="dru-btn">
            <div class="default_btn" data-bind="click:closeStudyPop">取消</div>
            <div class="default_btn orange_btn" data-bind="click:WantStudyClick">我要获得</div>
        </div>
    </div>
</div>
<div style="display:none;" class="dressUp-pop secondSurePop" id="successStudy">
    <div class="inner">
        <p class="oneLine">兑换成功，请到“能量箱”中查看详情</p>
        <div class="dru-btn">
            <div class="default_btn" data-bind="click:closeStudy2Pop">稍后再说</div>
            <div data-bind="click: goDetailClick" class="default_btn orange_btn">查看详情</div>
        </div>
    </div>
</div>
<div class="dialog_mask dialog_show" style="display:none;"></div>
<div class="dialog_box dialog_show" style="display:none;">
    <div class="dialog_hd">
        <strong class="dialog_title">提示</strong>
    </div>
    <div class="dialog_bd">确定要移出心愿池？</div>
    <div class="dialog_ft">
        <a href="javascript:;" class="dialog_default" data-bind="click: dialogDefaultClick">取消</a>
        <a href="javascript:;" class="dialog_primary" data-bind="click: dialogPrimaryClick">确定</a>
    </div>
</div>

<div class="dressUp-pop secondSurePop" id="changeCouponPop" style="display: none;">
    <div class="inner">
        <p class="oneLine" style="text-align: center;height: 1.3rem;">确定要花费${(detail.discountPrice)!'--'}学豆，兑换吗？<br>
            <i>兑换成功后无法退换</i>
        </p>
        <div class="dru-btn">
            <div class="default_btn" data-bind="click:closeChangeCouponPop">取消</div>
            <div class="default_btn orange_btn" data-bind="click:sureChangeCouponClick">我要兑换</div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var initMode = "RewardDetailMode";
    var dataNum = {
        productid:${(detail.id)!0},
        productName:"${(detail.productName)!}",
        price:${(detail.discountPrice)!0},
        skuid:"${(detail.skus[0].id)!0}",
        headWearId:"${(detail.relateVirtualItemId)!0}",
        categories:"${categories[0]}"
    };
    var expiryDate = "${(detail.expiryDate)!'--'}";
</script>
</@layout.page>