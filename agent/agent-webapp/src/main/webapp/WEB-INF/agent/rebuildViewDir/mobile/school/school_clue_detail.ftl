<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="鉴定学校" pageJs="" footerIndex=4>
    <@sugar.capsule css=['audit']/>
    <#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<style>

</style>
<div class="crmList-box resources-box">
    <#--<div class="res-top fixed-head">-->
        <#--<div class="return js-return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">鉴定学校</span>-->
    <#--</div>-->
    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if schoolClue??>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <div class="adjust-content">
                        <p class="title"><span style="color:#ff7d5a">学校ID：</span>${schoolClue.schoolId!''}</p>
                        <p class="title"><span style="color:#ff7d5a">名称：</span>${schoolClue.cmainName!''}</p>
                    </div>
                </div>
                <div class="schoolParticular-box" style="margin-top:-.5rem">
                    <div class="particular-image">
                        <div class="left"><p class="p-1"><span style="color:#ff7d5a;font-size:.7rem">照片</span> <a class="schooleImageDetail" href="/mobile/school_clue/photodesc.vpage"></a></p></div>
                        <div class="right photoSchoolShortIcon" id="photoShow">
                            <a href="javascript:void(0);">
                                <img src="<#if schoolClue.photoUrl??>${schoolClue.photoUrl!''}${shortIconTail}</#if>">
                            </a>
                        </div>
                    </div>
                    <div class="particular-image">
                        <div class="left"><span style="color:#ff7d5a;font-size:.7rem">位置</span></div>
                        <div class="right"><#if schoolClue.address??>${schoolClue.address!''}</#if></div>
                    </div>
                </div>

            </#if>
        </div>

    <#--已通过-->
    </div>


</div>
</@layout.page>
