<#import './layout.ftl' as layout>
<@layout.page className='HomeworkReport bg-fff' pageJs=null title="学校表现" specialCss="skin2" specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
	<title>学校表现</title>
'>
    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "学校表现">
        <#assign isUseNewTitle = true><#--使用新的UI2 title-->
        <script><#--改版的样式，不适用adapt-->
            window.notUseAdapt=true;
        </script>
        <#include "./top.ftl" >

        <#if result.success >

            <#if isBindClazz>

                <#assign isEmpty = result.histories?size == 0>
                <div class="expression-box">
                <#--<#if (result.hasAuthentication!false && !result.closeContributionButton)>-->
                    <#--<div class="e-head">-->
                        <#--<a href="/parentMobile/homework/giveBean.vpage?sid=${sid}" class="contribute-btn doTrack" data-track="school|classbean">贡献班级学豆</a>-->
                        <#--<p class="info"> <#if isEmpty> ${studentName}今天还没有被奖励， </#if> 贡献班级学豆给孩子更多奖励吧！</p>-->
                    <#--</div>-->
                <#--</#if>-->

                <#if isEmpty>
                    <div class="null-box">
                        <div class="no-performance"></div>
                        <div class="null-text">暂无学校表现记录</div>
                    </div>
                <#else>
                    <div class="e-list">
                        <ul>
                            <#list (result.histories![]) as loadSmart>
                                <li>
                                    <#assign integral = loadSmart.integral>
                                    <#if integral?exists>
                                        <div class="right icon-bean">+${loadSmart.integral}</div>
                                    <#else>
                                        <div class="right icon-honor"></div>
                                    </#if>

                                    <div class="left">
                                        <div class="name">${loadSmart.subject.value}课上,${loadSmart.comment}</div>
                                        <div class="time">${loadSmart.createDatetime}</div>
                                    </div>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </#if>
                </div>
            <#else>
                <div class="null-box">
                    <div class="no-performance"></div>
                    <div class="null-text">暂无学校表现记录，请向老师申请添加吧</div>
                </div>
            </#if>
        <#else>
        <p class="hide doAutoTrack" data-track = "school|fail"></p>
        </#if>
        <p class="hide doScrollTrack" data-track = "school|scroll"></p>
    </#escape>
</@layout.page>
