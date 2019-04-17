<#include "constants.ftl">

<#escape top as top?html>

    <#assign topTabHtml = topTabHtml!"" topTitle=topTitle!"" isUseNewTitle=isUseNewTitle!false >
    <#if topType?exists>
        <#if isUseNewTitle><#--使用新的UI2 title-->
            <div class="parentApp-topBar doTop" id="do-head-title-adapt" style="height: 51px;"><#--当客户端版本大于1.6时，remove title -->
                <div class="topBox" style="padding: 20px 0 0;">
                    <#switch topType>
                        <#case "topTab">
                            <div class="topTab">
                                <#noescape >${topTabHtml}</#noescape>
                            </div>
                            <#break>
                        <#case "topTitle">
                            <div class="topHead" style="padding: 2.5px 0 4.5px;font-size: 13px;line-height: 24px;">${topTitle}</div>
                            <#break>
                    </#switch>
                </div>
            </div>
        <#else><#--使用旧的UI2 title-->
            <div class="parentApp-topBar doTop" id="do-head-title-adapt"><#--当客户端版本大于1.6时，remove title -->
                <div class="topBox">
                    <#switch topType>
                        <#case "topTab">
                            <div class="topTab">
                                <#noescape >${topTabHtml}</#noescape>
                            </div>
                            <#break>
                        <#case "topTitle">
                            <div class="topHead">${topTitle}</div>
                            <#break>
                    </#switch>
                </div>
            </div>
        </#if>
    </#if>
</#escape>

