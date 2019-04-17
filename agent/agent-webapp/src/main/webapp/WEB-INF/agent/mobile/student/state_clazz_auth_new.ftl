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
    <div onclick="changeTab(this, 'auth')">已认证学生</div>
    <div class="active" onclick="changeTab(this, 'unauth')">未认证学生</div>
</div>
<div id="auth" style="display: none">
    <div class="mobileCRM-V2-box mobileCRM-V2-info">
        <ul class="mobileCRM-V2-list">
            <#if studentAuthList?has_content>
                <#list studentAuthList as item>
                    <li>
                        <a href="javascript:;" class="box">
                            <div class="side-fl">${item.name!''}
                                <span class="id
                                    <#if item.authStates?? && item.authStates=="auth"> mobileCRM-V2-globalTag-orange
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                                    </#if> mobileCRM-V2-globalTag">
                                    <#if item.authStates?? && item.authStates=="auth">证
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">双
                                    </#if>
                                </span>
                            </div>
                        </a>
                    </li>
                </#list>
            <#else>
                <li class="mobileCRM-V2-list">
                    <a href="javascript:;" class="box side-small">
                        未找到相应数据
                    </a>
                </li>
            </#if>
        </ul>
    </div>
</div>

<div id="unauth">
    <#if studentUnAuthThreeHwsList?has_content>
        <div class="mobileCRM-V2-noteBar">累计完成3套以上的学生</div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <#list studentUnAuthThreeHwsList as item>
                    <li>
                        <a href="javascript:;" class="box">
                            <div class="side-fl">${item.name!''}
                                <span class="id
                                    <#if item.authStates?? && item.authStates=="auth"> mobileCRM-V2-globalTag-orange
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                                    </#if> mobileCRM-V2-globalTag">
                                    <#if item.authStates?? && item.authStates=="auth">证
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">双
                                    </#if>
                                </span>
                            </div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if studentUnAuthTwoHwsList?has_content>
        <div class="mobileCRM-V2-noteBar">累计完成2套的学生</div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <#list studentUnAuthTwoHwsList as item>
                    <li>
                        <a href="javascript:;" class="box">
                            <div class="side-fl">${item.name!''}
                                <span class="id
                                    <#if item.authStates?? && item.authStates=="auth"> mobileCRM-V2-globalTag-orange
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                                    </#if> mobileCRM-V2-globalTag">
                                    <#if item.authStates?? && item.authStates=="auth">证
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">双
                                    </#if>
                                </span>
                            </div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if studentUnAuthOneHwsList?has_content>
        <div class="mobileCRM-V2-noteBar">累计完成1套的学生</div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <#list studentUnAuthOneHwsList as item>
                    <li>
                        <a href="javascript:;" class="box">
                            <div class="side-fl">${item.name!''}
                                <span class="id
                                    <#if item.authStates?? && item.authStates=="auth"> mobileCRM-V2-globalTag-orange
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                                    </#if> mobileCRM-V2-globalTag">
                                    <#if item.authStates?? && item.authStates=="auth">证
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">双
                                    </#if>
                                </span>
                            </div>
                        </a>
                    </li>
                </#list>

            </ul>
        </div>
    </#if>
    <#if studentUnAuthUnHwsList?has_content>
        <div class="mobileCRM-V2-noteBar">至今未完成作业学生</div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <#list studentUnAuthUnHwsList as item>
                    <li>
                        <a href="javascript:;" class="box">
                            <div class="side-fl">${item.name!''}
                                <span class="id
                                    <#if item.authStates?? && item.authStates=="auth"> mobileCRM-V2-globalTag-orange
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">  mobileCRM-V2-globalTag-green
                                    </#if> mobileCRM-V2-globalTag">
                                    <#if item.authStates?? && item.authStates=="auth">证
                                    <#elseif item.authStates?? && item.authStates=="doubleAuth">双
                                    </#if>
                                </span>
                            </div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if noList??>
    <ul class="mobileCRM-V2-list">
        <li class="mobileCRM-V2-list">
            <a href="javascript:;" class="box side-small">
                未找到相应数据
            </a>
        </li>
    </ul>
    </#if>
</div>
<script>
    function changeTab(obj, id) {
        $("#auth").hide();
        $("#unauth").hide();
        $("#" + id).show();
        var jobj = $(obj);
        jobj.siblings().removeAttr("class");
        jobj.attr("class", "active");
    }
</script>
</@layout.page>