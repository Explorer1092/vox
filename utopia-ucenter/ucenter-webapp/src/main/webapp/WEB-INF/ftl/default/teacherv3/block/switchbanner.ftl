<#if ((currentTeacherDetail.ktwelve) == 'PRIMARY_SCHOOL')!true>
<div class="switchBox belist" id="leftSwitchBanner">
    <ul>
        <#if (currentTeacherDetail.subject == "ENGLISH")!false>
            <#if pageBlockContentGenerator??>
            ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'RightAdBoxItemsP1')}
            </#if>
        </#if>
        <#if (currentTeacherDetail.subject == "MATH")!false>
            <#if pageBlockContentGenerator??>
            ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'RightAdBoxItemsMath')}
            </#if>
        </#if>
        <#--英语老师 - 数学老师 - 同时显示广告-->
        <#--<li style="display:none;">
            <a href="/ucenter/partner.vpage?url=${ProductConfig.getRewardSiteBaseUrl()}/order" target="_blank">
                <img src="//cdn.17zuoye.com/static/project/teacherGround/t_reward0428.jpg" width="190" height="250">
            </a>
        </li>-->
    </ul>
    <div class="tab"></div>
</div>
    <@ftlmacro.allswitchbox target="#leftSwitchBanner"/>
</#if>