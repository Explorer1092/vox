<#import './layout.ftl' as layout>

<@layout.page className='Ucenter' pageJs="second" title="课外乐园首页">
    <#escape x as x?html>
        <#assign topType = "topTitle">
        <#assign topTitle = "趣味学习">
        <#include "./top.ftl" >

        <#if result.success>

            <#if isGraduate!false ><#--是否毕业判断-->
                <div class="parentApp-messageNull">暂时不支持小学毕业账号</div>
            <#else>
                <#assign infos = appList![] tipType = "nomessage" tipText = "敬请期待" >

                <#if infos?size == 0>
                    <#include "tip.ftl">
                <#else>
                    <#include "./shopIcon.ftl">
                    <#assign productText>
                        <#list infos as info>
                        <li>
                            <a href="shoppinginfo.vpage?sid=${sid}&productType=${info.appKey}"
                                <#if trackTypeObj[info.appKey]?exists> data-track="interest|${trackTypeObj[info.appKey]}_click" </#if>
                               class="ui-link doTrack">
                                <h3>
                                    <#if !isNotSupportPc(info.appKey)><span class="funLearning-pc"></span></#if>
                                    <#if isSupportMobile(info.appKey)><span class="funLearning-iphone"></span></#if>
                                ${info.title}
                                </h3>
                                <div class="content">
                                    <div class="font row_left">${info.info}</div>
                                    <div class="img row_right"><img src="${getShopIconSrc(info.appKey)}" alt="${info.appKey}"></div>
                                </div>
                                <div class="bottom_tip" >点击购买</div>
                            </a>
                        </li>
                        </#list>
                    </#assign>
                    <#assign productText = productText?trim>
                    <#if productText?has_content>
                    <h2 class="title_info_box title_info_green_box">让孩子学习更容易</h2>
                    <div style="text-align: center; font-size: 20px; line-height: 50px;">课外乐园的应用均由第三方公司提供，请自愿使用</div>
                    <div class="funLearning-info">
                        <div><i class="iphone"></i>代表可以在手机APP使用该应用</div>
                        <div><i class="pc"></i>代表可以在电脑使用该应用</div>
                    </div>

                    <#--阿分题英语＋数学，双科提升-->
                        <#include "./afenti/listpiece.ftl"/>

                    <div class="homework_history">
                        <ul class="list">
                            <#noescape>${productText}</#noescape>
                        </ul>
                    </div>
                    <#else>
                        <#include "tip.ftl">
                    </#if>
                </#if>
            </#if>
        <#else>
        <p class="hide doAutoTrack" data-track="interest|fail"></p>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>

    </#escape>

</@layout.page>

