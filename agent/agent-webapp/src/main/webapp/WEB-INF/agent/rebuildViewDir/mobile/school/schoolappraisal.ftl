<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="学校鉴定" pageJs="schoolappraisal" footerIndex=4 >
    <@sugar.capsule css=['school','photo_pic']/>
<div class="flow" style="background:#fff">
    <div class="item" style="margin:0 0 0 1rem;padding:0 1rem 0 0;">
        学校
        <div style="width:10rem;<#if name??><#else>text-align:right</#if>" class="inner-right js-name">
            <#if cname??>${cname!''}<#else>请填写</#if>
        </div>
        <input  type="hidden" name="schoolName" id="schoolName" value="<#if cname??>${cname!''}</#if>" data-einfo="请填写学校名称">
    </div>
    <div class="schoolParticular-box">
        <div class="particular-image">
            <div class="left"><p class="p-1">照片 <a class="schooleImageDetail" onclick="openSecond('/mobile/school_clue/photodesc.vpage')"></a></p></div>
            <div class="right photoSchoolShortIcon <#if cname??><#--仅当选择完学校后，才能选择照片-->js-photo</#if>" id="photoShow">
                <a href="javascript:void(0);">
                    <img src="<#if photoUrl??>${(photoUrl)!''}${shortIconTail}<#else ></#if>">
                </a>
            </div>
        </div>
        <div class="particular-image">
            <div class="left">位置</div>
            <div class="right"><#if address??>${address!''}</#if></div>
        </div>
    </div>
</div>
<script>
    var type = "${type!''}";
</script>
</@layout.page>