<#import "../layout_new_no_group.ftl" as layout>
<@layout.page  title="${className!}">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">${className!}</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">姓名</div>
            <div class="side-fr side-time side-bWidth" style="padding:0;">注册日期</div>
        </div>
    </li>
    <#if studentList?has_content>
        <#list studentList as student>
            <li>
                <div class="side-fl" style="width: 40%;">${student.name!'未知'}
                    <span class="id
                         <#if student.authStates?? && student.authStates=="auth"> mobileCRM-V2-globalTag-orange
                         <#elseif student.authStates?? && student.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                         </#if> mobileCRM-V2-globalTag">
                         <#if student.authStates?? && student.authStates=="auth">证
                         <#elseif student.authStates?? && student.authStates=="doubleAuth">双
                         </#if>
                    </span>
                </div>
                <div class="side-fr side-total ">${student.registerTimeStr!}</div>
            </li>
        </#list>
    <#else>
        <li>
            <div class="side-fl side-small" style="width: 50%;">
                未找到相应数据
            </div>
        </li>
    </#if>
</ul>
</@layout.page>