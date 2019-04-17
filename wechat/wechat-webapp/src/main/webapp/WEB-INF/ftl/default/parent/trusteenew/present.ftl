<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='活动介绍' pageJs="activityintroducte">
    <@sugar.capsule css=['trusteetwo'] />
    <div class="active-wrap active-bgpink">
        <div class="active03-box">
            <div class="ab01-box-1 ab01-box-add js-bookregistBtn">
                <a href="javascript:void(0);" class="btn">去预约体验</a>
            </div>
            <div class="ab01-box-2"><h2 class="title"></h2>寒假来临，还没放假的您，还在为安置宝贝发愁么？别着急，你可以来这里，和自己学校的小朋友，一起吃饭、休息，一起作业、嬉戏。</div>
            <div class="ab01-box-3">
                <div class="box">
                    <h1>一起作业用户专享</h1>
                    <div class="inner">
                        <#if schoolName?has_content && reserveCount?has_content>
                            <h2>${schoolName},已有<span>${reserveCount}</span>名家长报名</h2>
                        </#if>
                        <#if shop?? && shop.privileges?size gt 0>
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