<#if question?has_content>
<ul data-answer="${question.answer}" data-questionid="${question.id}">
    <li>${question.topicContent}</li>
    <#list (question.options)?keys as key>
        <li> ${key}. ${question.options[key]}</li>
    </#list>
</ul>
<#else>
<ul>
    <li>${msg!"未查询相关的试题,请到我的问题中录题"}</li>
</ul>
</#if>
