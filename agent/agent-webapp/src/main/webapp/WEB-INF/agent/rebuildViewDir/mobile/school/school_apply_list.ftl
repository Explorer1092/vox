<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="审核中的学校信息" pageJs="" footerIndex=4>
    <@sugar.capsule css=['school','photo_pic']/>

    <#--<div class="head fixed-head">-->
        <#--<a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="h-title">审核中的学校信息</span>-->
    <#--</div>-->
<#if list??>
<#list list as schoolList>
    <div class="schoolParticular-box">
        <div class="particular-title"><span class="data">申请日期：${schoolList.createTime!'--'}</span>申请人：${schoolList.recorderName!'--'}</div>
        <div class="particular-image">
            <div class="left"><p class="p-1">照片<i></i></p></div>
            <div class="right">
                <a href="javascript:void(0);">
                    <img src="${schoolList.photoUrl!'--'}" alt="">
                </a>
            </div>
        </div>
        <div class="particular-image">
            <div class="left">位置</div>
            <div class="right">${schoolList.address!'--'}</div>
        </div>
    </div>
</#list>
</#if>
</@layout.page>