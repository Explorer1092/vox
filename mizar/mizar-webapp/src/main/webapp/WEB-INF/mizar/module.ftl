<#macro page title="" pageJs=[] pageJsFile={} pageCssFile={} leftMenu = "品牌管理">
    <#import "layout/webview.layout.ftl" as layout/>
    <@layout.page
    title=title
    pageJs=pageJs
    pageJsFile=pageJsFile
    pageCssFile={"mizar" : ["public/skin/css/skin"]}
    >
    <#assign baseInfo={
    "headerText":"一起作业",
    "tagText":"开放平台",
    "copyRight":"Copyright © 2011-${.now?string('yyyy')} 17ZUOYE Corporation. All Rights Reserved."
    }/>
<#--<div class="container">-->
    <div class="topBar">
        <div class="inner clearfix">
            <div class="info2">
                <div id="user" class="user"><a href="javascript:void(0);"></a></div>
                <div id="userDetail" class="userDetail">

                        <#if currentUser?? >
                            <a href="javascript:void(0);">${(currentUser.realName)!}（${(currentUser.accountName)!}）</a>
                        </#if>

                            <a class="logout" href="/auth/logout.vpage">退出</a>

                    </ul>
                </div>
                <div class="menu" id="menu"><a href="javascript:void(0);"></a></div>
            </div>
            <div class="info">
                <#if currentUser?? >
                    <a href="javascript:void(0);">${(currentUser.realName)!}（${(currentUser.accountName)!}）</a>
                    &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;
                </#if>
                <a class="logout" href="/auth/logout.vpage">退出</a>
            </div>
            <a href="/" class="logo">${baseInfo['headerText']!}</a>
            <span class="tag">${baseInfo['tagText']!}</span>
        </div>
    </div>
    <div class="wrapper">
            <#if !currentUser.isTangramJury()>
            <#include 'menu.ftl'>
            </#if>
            <div class="side-content">
                <#nested />
            </div>
        </div>
    </div>
    <div class="footBar">
        <div class="inner">
            <div class="copyright">${baseInfo['copyRight']!}</div>
        </div>
    </div>
<#--</div>-->
    </@layout.page>
</#macro>
