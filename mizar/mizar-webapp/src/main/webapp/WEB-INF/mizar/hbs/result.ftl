<#import "./hbslayout.ftl" as layout/>
<@layout.page
title="华罗庚金杯数学竞赛"
pageCssFile={"hbs" : ["/public/skin/css/hbs/skin"]}
>
<h4>华罗庚金杯少年数学邀请赛获奖情况查询</h4>
<div class="inquiryInfo">
    <div class="innerBox">
        <div class="infoBox">
            <div class="titleBox">
                <p>届数</p>
                <p class="bg-color">身份证号码</p>
                <p>手机号码</p>
                <p class="bg-color">初赛成绩</p>
                <p>决赛成绩</p>
            </div>
            <div class="valueBox">
                <div class="inputBox">
                    <p>第22届</p>
                </div>
                <div class="inputBox bg-color">
                    <p><#if info?? && info.idCardNo??>${info.idCardNo!}<#else>——</#if></p>
                </div>
                <div class="inputBox">
                    <p><#if info?? && info.phoneNumber??>${info.phoneNumber!}<#else>——</#if></p>
                </div>
                <div class="inputBox bg-color">
                    <#if info?? && info.preContestResult?? && info.preContestResult != "">
                        <p>${info.preContestResult!}</p>
                    <#else>
                        <p style="font-size: 13px;">未通过或参赛单位未将您的成绩导入到系统中，具体情况请联系参赛单位</p>
                    </#if>
                </div>
                <div class="inputBox">
                    <#if info?? && info.finalContestResult?? && info.finalContestResult != "">
                        <p>${info.finalContestResult!}</p>
                    <#else>
                        <p style="font-size: 13px;">未获奖或参赛单位未将您的成绩导入到系统中，具体情况请联系参赛单位</p>
                    </#if>
                </div>
            </div>
        </div>
        <div class="gap"></div>
    </div>
</div>
</@layout.page>