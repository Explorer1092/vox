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
<div class="mobileCRM-V2-tab">
    <div  class="active" onclick="changeTab(this, 'auth')">已认证</div>
    <div onclick="changeTab(this, 'twoHomework')">完成2以上套</div>
    <div onclick="changeTab(this, 'oneHomework')">完成1套 </div>
    <div onclick="changeTab(this, 'noneHomework')">未完成  </div>
</div>
<#if studentMap??>
    <ul class="mobileCRM-V2-list" id="auth" >
        <li>
             <div class="box link-ico">
                 <div class="side-fl side-time">姓名</div>
                 <div class="side-fr side-time side-width" style="width: 30%;">认证时间</div>
            </div>
        </li>
        <#if studentMap["2"]??>
            <#assign studentList = studentMap["2"]>
            <#list studentList as item>
            <li>
                <div class="side-fl" style="line-height: 1.5rem;"  >${item.name!''}
                    <span class="id" style="
                        <#if item.authStates=="证">background-color: #ff8260;
                        <#elseif item.authStates=="双">background-color: #67cd67;
                        </#if> display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                        <#if item?? && item.authStatus?? && item.authStatus?length gt 0>${item.authStates}</#if>
                    </span>
                </div>
                <div class="side-fr side-orange side-width" style="width: 30%;">
                    ${item.latestAuthHwTimeStr!0}
                </div>
            </li>
            </#list>
        <#else>
            <li>
                <div class="side-fl" style="line-height: 1.5rem;"  >
                    未找到相应数据
                </div>
            </li>
        </#if>
    </ul>

    <ul class="mobileCRM-V2-list" id="twoHomework" style="display: none">
        <li>
            <div class="box link-ico">
                <div class="side-fl side-time">姓名</div>
                <div class="side-fr side-time side-width" style="width: 30%;">最近完成</div>
            </div>
        </li>
    <#if studentMap["5"]??>
        <#assign studentList = studentMap["5"]>
        <#list studentList as item>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >${item.name!''}
                <span class="id" style="
                    <#if item.authStates=="证">background-color: #ff8260;
                    <#elseif item.authStates=="双">background-color: #67cd67;
                    </#if> display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                    <#if item?? && item.authStatus?? && item.authStatus?length gt 0>${item.authStates}</#if>
                </span>
            </div>
            <div class="side-fr side-orange side-width" style="width: 30%;">
                ${item.latestHcaHwTimeStr!0}
            </div>
        </li>
        </#list>
    <#else>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >
                未找到相应数据
            </div>
        </li>
    </#if>
    </ul>

    <ul class="mobileCRM-V2-list" id="oneHomework" style="display: none">
        <li>
            <div class="box link-ico">
                <div class="side-fl side-time">姓名</div>
                <div class="side-fr side-time side-width" style="width: 30%;">最近完成</div>
            </div>
        </li>
    <#if studentMap["6"]??>
        <#assign studentList = studentMap["6"]>
        <#list studentList as item>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >${item.name!''}
                <span class="id" style="
                    <#if item.authStates=="证">background-color: #ff8260;
                    <#elseif item.authStates=="双">background-color: #67cd67;
                    </#if> display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                    <#if item?? && item.authStatus?? && item.authStatus?length gt 0>${item.authStates}</#if>
                </span>
            </div>
            <div class="side-fr side-orange side-width" style="width: 30%;">
                ${item.latestHcaHwTimeStr!0}
            </div>
        </li>
        </#list>
    <#else>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >
                未找到相应数据
            </div>
        </li>
    </#if>
    </ul>

    <ul class="mobileCRM-V2-list" id="noneHomework" style="display: none">
        <li>
            <div class="box link-ico">
                <div class="side-fl side-time">姓名</div>
                <div class="side-fr side-time side-width" style="width: 30%;">注册日期</div>
            </div>
        </li>
    <#if studentMap["7"]??>
        <#assign studentList = studentMap["7"]>
        <#list studentList as item>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >${item.name!''}
                <span class="id" style="
                    <#if item.authStates=="证">background-color: #ff8260;
                    <#elseif item.authStates=="双">background-color: #67cd67;
                    </#if> display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                    <#if item?? && item.authStatus?? && item.authStatus?length gt 0>${item.authStates}</#if>
                </span>
            </div>
            <div class="side-fr side-orange side-width" style="width: 30%;">
                ${item.registerTimeStr!0}
            </div>
        </li>
        </#list>
    <#else>
        <li>
            <div class="side-fl" style="line-height: 1.5rem;"  >
                未找到相应数据
            </div>
        </li>
    </#if>
    </ul>
</#if>
<script>
    function changeTab(obj, id){
        $("#auth").hide();
        $("#twoHomework").hide();
        $("#oneHomework").hide();
        $("#noneHomework").hide();
        $("#" + id).show();
        var jobj = $(obj);
        jobj.siblings().removeAttr("class");
        jobj.attr("class", "active");
    }
</script>
</@layout.page>