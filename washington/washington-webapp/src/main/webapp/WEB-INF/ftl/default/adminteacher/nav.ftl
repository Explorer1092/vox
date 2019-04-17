<#--多个父级页面共用nav，根据ftl模板字段idType(角色)和pageType(页面)来确定不同的链接-->
<#assign
    jumpLink1 = "/${idType!'schoolmaster'}/generaloverview.vpage"
    jumpLink2 = "/${idType!'schoolmaster'}/learninganalysis.vpage"
    jumpLink3 = "/${idType!'schoolmaster'}/systemtest.vpage"
    jumpLink4 = "/schoolmaster/activityReport/list.vpage"/>

<div class="contentNav">
    <ul class="navUl">
        <li class="<#if pageType == 'generaloverview'>active</#if>"><a href="${jumpLink1!''}"><i class="pic"></i>总体概览</a></li>
        <li class="<#if pageType == 'learninganalysis'>active</#if>"><a href="${jumpLink2!''}"><i class="pic pic02"></i>学情分析</a></li>
        <li class="special <#if pageType == 'systemtest'>active</#if>"><a href="${jumpLink3!''}"><i class="pic pic03"></i>测评</a></li>
        <li class="<#if pageType == 'activityreport'>active</#if>"><a class="activityReport" href="${jumpLink4!''}"><i class="pic pic05"></i>趣味活动</a></li>
        <#if idType == 'rstaff' && currentUser.subject == 'ENGLISH'>
            <li><a class="trackTTS" href="/tts/listening.vpage"><i class="pic pic04"></i>听力卷TTS</a></li>
        </#if>
    </ul>
    <a href="/${idType!'schoolmaster'}/admincenter.vpage" class="setBtn <#if pageType == 'admincenter'>active</#if>" title="个人中心"><i></i></a>
</div>