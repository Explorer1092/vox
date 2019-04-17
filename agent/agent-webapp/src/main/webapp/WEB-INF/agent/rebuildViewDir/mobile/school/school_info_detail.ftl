<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校详情" pageJs="schoolDetail">
    <@sugar.capsule css=['school','photo_pic']/>
<style>
    .fixLength{
        display:block;
    }
    .mobileCRM-V2-info{
        font-size:.75rem
    }
    .noLength{
        display:none;
    }
    .school_length input{
        width:10%;
        font-size:.75rem;
        text-align:center;
    }
    .school_length span{
        margin-left:13%;
    }
    .school_length ul li {
        width:100%;
        text-align:center;
    }
     .nav.tab-head.c-flex.c-flex-5{
         display: none;
     }
</style>
    <#if size?has_content && size?number gt 0>
        <#if size?has_content>
            <div class="item">
                <div id="school_apply" class="schoolParticular-point">当前学校信息有${size}条申请记录正在审核中
                    <div class="inner-right" style="margin-right:1rem">&nbsp;</div>
                </div>
            </div>
        </#if>
    </#if>
<div class="flow" style="background:#fff">
    <div class="item">
        地区
        <div class="right js-place" id="regionName">
            <#if regionName??>${regionName!''}</#if>
        </div>
    </div>
    <div class="item" style="position:relative;">
        阶段
        <div class="right js-stage">
            <#if phase_value??><#if phase_value==1>小学</#if><#if phase_value==2>初中</#if><#if phase_value==4>高中</#if><#if phase_value==5>学前</#if></#if>
        </div>
    </div>
    <div class="item" style="">
        名称<div style="width:10rem;<#if name??><#else>text-align:right</#if>" class="right js-name">
        <#if cname??>${cname!''}</#if>
    </div><input  type="hidden" name="schoolName" id="schoolName" value="<#if cname??>${cname!''}</#if>" data-einfo="请填写学校名称">
    </div>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" style="font-size: .75rem;">
        <ul class="mobileCRM-V2-list">
            <li>
                <div class="box">
                    <div class="side-fl" style="margin:10px 0;color:#50546d">照片
                        <a class="schooleImageDetail" onclick="openSecond('/mobile/school_clue/photodesc.vpage')"></a></div>
                    <div class="side-fr photoSchoolShortIcon" id="photoShow" style="background-size:100% 100%;"><img src="<#if photoUrl??>${(photoUrl)!''}<#--${shortIconTail}--></#if>"></div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl" style="color:#50546d">位置</div>
                    <input type="text" readonly="readonly" placeholder="通过照片直接获取，无需填写" name="address"
                           style="width: 89%;font-size:.75rem;color:#50546d"
                           id="address" value="<#if address??>${(address)!''}</#if>">
                </div>
            </li>
        </ul>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" style="margin-top:.5rem">
    <ul class="mobileCRM-V2-list" <#if phase_value?? && phase_value == 5 >hidden</#if>>
        <li>
            <div class="link link-ico">
                <div class="side-fl" style="color:#50546d">学制</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time" name="schoolingLengthDisplay" style="color:#50546d">
                    ${eduSystemName!"请选择"}
                   <#-- <#if schoolLength??>
                        <#if schoolLengthList??>
                            <#list schoolLengthList as length>
                                <#if schoolLength?? && schoolLength == length.code>
                                ${length.name!""}
                                </#if>
                            </#list>
                        <#else>
                            请选择
                        </#if>
                    </#if>-->
                </div>
            </div>
        </li>
        <li id="englishStartGrade_li"
            <#if phase_value?? && (phase_value == 2 ||  phase_value == 4 || phase_value == 5)>hidden</#if>>
            <div class="link link-ico">
                <div class="side-fl" style="color:#50546d">英语起始年级</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time side-english" name="englishStartGradeDisplay" style="color:#50546d">
                    <#if englishStartGrade??>
                        <#if  englishStartGrade == 1>一年级
                        <#elseif englishStartGrade == 2>二年级
                        <#elseif  englishStartGrade == 3>三年级
                        <#elseif  englishStartGrade == 4>四年级
                        <#elseif  englishStartGrade == 5>五年级
                        <#elseif  englishStartGrade == 6>六年级
                        <#else>
                        </#if>
                    </#if>
                </div>
            </div>
        </li>
    </ul>
</div>
<div class="mobileCRM-V2-list schoolParticular-edit">
    <div class="edit-title">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
    <div class="school_length edit-list">
        <ul class="showLength01 schoolGrade">
            <#if gradeDataList?? && gradeDataList?size gt 0>
                <#assign clazzNum = 0 ,studentNum = 0>
                <#list gradeDataList as list>
                    <#assign clazzNum = (list.clazzNum!0) + clazzNum>
                    <#assign studentNum = (list.studentNum!0) + studentNum>
                    <li><div class="level" data_value="${list.grade!0}">${list.gradeDesc!''}</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${list.clazzNum!0}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${list.studentNum!0}"/>人</div></li>
                </#list>
            </#if>
            <li><div>合计</div><div>共<input value="${clazzNum!0}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${studentNum!0}">人</div></li>
        </ul>
    </div>
    <div class="img_info" style="width:100%;height:100%;background:rgba(0 ,0 ,0 ,1);display: none;position:fixed;top:0;text-align: center;">
        <img style="max-width:100%;height:auto" src="<#if photoUrl??>${(photoUrl)!''}?x-oss-process=image/auto-orient,1<#--${shortIconTail}--></#if>">
    </div>
</div>
<script defer>
$(document).on('click','#school_apply',function(){
    openSecond("/mobile/school_clue/apply_list.vpage?schoolId=${schoolId!0}");
});
$(document).on('click','#photoShow',function(){
    $('.img_info').show();
});
$(document).on('click','.img_info',function(){
    $('.img_info').hide();
});
    $('input').attr('readonly','readonly');

var url = "/mobile/school_clue/updateschool.vpage?schoolId=${schoolId!0}";
</script>


</@layout.page>