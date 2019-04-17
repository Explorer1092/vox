<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='活动介绍' pageJs="activityintroducte">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box">
        <div class="ab01-box-1">
            <a href="javascript:void(0);" class="btn js-bookregistBtn">去预约体验</a>
            <div class="txt">时间：12.22-12.31</div>
        </div>
        <div class="ab01-box-2"><h2 class="title"></h2>年终工作忙，无暇顾及孩子学习？我们为您甄选出学校附近的托管班，帮助孩子巩固知识点，培养习惯，轻松迎接期末考试。</div>
        <div class="ab01-box-3">
            <div class="box">
                <h1>圣诞大礼包，一起作业用户专享</h1>
                <div class="inner">
                    <#if schoolName?has_content && reserveCount?has_content>
                        <h2>${schoolName!""},已有<span>${reserveCount!0}</span>名家长报名</h2>
                    </#if>
                    <#if shop.privileges?has_content>
                        <#list shop.privileges as item>
                            <#if item_index == 0 >
                                <div class="item item-1">${item}</div>
                            </#if>
                            <#if item_index == 1>
                                <div class="item item-2">${item}</div>
                            </#if>
                        </#list>
                    </#if>
                </div>
                <a href="javascript:void(0);" class="link js-ruledescBtn">规则说明</a>
            </div>
        </div>
        <div class="footer">
            <div class="empty"></div>
            <a href="javascript:void(0);" class="active-bottom-know order_icon js-knowclazzinfoBtn">了解托管班</a>
        </div>
    </div>
</div>
</@trusteeMain.page>