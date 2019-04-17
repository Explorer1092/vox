<#import "../layout.ftl" as openclassMain>
<@openclassMain.page title='活动介绍' pageJs="ocpresent">
<@sugar.capsule css=['openclass','jbox'] />
<div class="train-wrap bg-pattern">
    <div class="train01-box">
        <div class="inner01">
            <a href="javascript:void(0);" class="btn js-bookOpenClassSocketBtn" data-flag="${(endFlag?string)!'false'}"></a>
            <div class="txt">报名时间 : ${beginDateStr!"1.20"}-${endDateStr!"1.23"}</div>
        </div>
        <div class="inner02">
            <h2 class="title"></h2>
            <#if shop.privileges?has_content>
                <#list shop.privileges as item>
                    <#if item_index == 0 >
                        <p>${item}</p>
                    </#if>
                </#list>
            </#if>
        </div>
        <div class="inner03">
            <h2 class="title"></h2>
            <#assign reserveCountPublic = ((20 - reserveCount!0) <= 1)?string("1", "${(20 - reserveCount!0)}")/>
            <p class="count">还剩<span>${reserveCountPublic!0}</span>个名额</p>
            <p> • 公开课时间：${shop.classDate!""} </p>
            <p> • 公开课地点：${shop.address!""}</p>
            <p> • 公开课收费：30元</p>
            <p> • 主讲老师：${shop.classTeacherName!""}</p>
            <a href="javascript:void(0)" class="link js-openClassDetailBtn">公开课介绍</a>
        </div>
        <div class="footer">
            <a href="javascript:void(0);" class="btn-red fix-footer js-bookOpenClassBtn" data-flag="${(endFlag?string)!'false'}">去报名</a>
        </div>
    </div>
</div>
<script>
    ga('trusteeTracker.send', 'pageview');
</script>
</@openclassMain.page>