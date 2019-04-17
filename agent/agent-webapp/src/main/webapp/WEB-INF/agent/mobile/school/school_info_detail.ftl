<#import "../layout_new_no_group.ftl" as layout>
<@layout.page title="">
    <@sugar.capsule css=['','school','photo_pic']/>
<#assign schoolClass2 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int + (classCount6!0)?int>
    <#assign schoolClass1 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int>
    <#assign schoolClass3 =  (classCount6!0)?int + (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
    <#assign schoolClass4 =  (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
    <#assign schoolClass5 =  (classCount10!0)?int + (classCount11!0)?int + (classCount12!0)?int >
<#assign schoolMain1 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int>
    <#assign schoolMain2 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int + (studentCount6!0)?int>
    <#assign schoolMain3 = (studentCount6!0)?int + (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
    <#assign schoolMain4 = (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
    <#assign schoolMain5 = (studentCount10!0)?int + (studentCount11!0)?int + (studentCount12!0)?int>
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
</style>
<div class="head fixed-head">
    <a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">学校详情</span>
    <a href="javascript:void(0)" class="inner-right js-submit"><#if canOperation?? && canOperation><#if AuthenticationState?? && AuthenticationState == "已鉴定"><#else>提交</#if></#if></a>
</div>
<div class="flow">
    <div class="item" style="font-size: 1rem">
        地区
        <div class="inner-right js-place" id="regionName">
            <#if regionName??>${regionName!''}</#if>
        </div>
        <input type="hidden" name="regionCode" id="regionCode" value="<#if regionCode??>${regionCode!''}</#if>" data-einfo="请选择地区">
    </div>
    <div class="item" style="position:relative;font-size: 1rem">
        阶段
        <div class="inner-right js-stage">
            <#if phase??><#if phase==1>小学</#if><#if phase==2>初中</#if><#if phase==4>高中</#if><#if phase==5>学前</#if></#if>
        </div>
    </div>
    <div class="item"  style="font-size: 1rem;height:4.5rem">
        名称
        <div class="inner-right js-name" style="width:10rem;height:4.5rem;<#if name??><#else>text-align:right</#if>">
            <#if name??>${name!''}<#else>请填写</#if>
        </div>
        <input type="hidden" name="schoolName" id="schoolName" value="<#if name??>${name!''}</#if>" data-einfo="请填写学校名称">
    </div>
    <p style="padding:0.4rem 0;background:#f6f6f6;text-align:center;font-size: 65%;">选填以下信息，审核通过后，该学校将会被同时鉴定通过</p>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" style="font-size: .75rem;">
        <ul class="mobileCRM-V2-list">
            <li>
                <div class="box">
                    <div class="side-fl" style="margin:10px 0;font-size:1rem">照片
                        <a class="schooleImageDetail" href="/mobile/school_clue/photodesc.vpage"></a></div>
                    <div class="side-fr photoSchoolShortIcon" id="photoShow" style="width:48px;height: 48px;background-size:100% 100%;"><img src="<#if photoUrl??>${(photoUrl)!''}<#--${shortIconTail}--></#if>"></div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl"  style="font-size: 1rem">位置</div>
                    <input type="text" readonly="readonly" placeholder="通过照片直接获取，无需填写" name="address"
                           style="width: 89%;font-size:.75rem"
                           id="address" value="<#if address??>${(address)!''}</#if>">
                </div>
            </li>
        </ul>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" >
    <ul class="mobileCRM-V2-list" <#if phase?? && phase == 4>hidden</#if>>
        <li>
            <div class="link link-ico">
                <div class="side-fl">学制</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time" name="schoolingLengthDisplay">
                    <#if schoolLength??>
                        <#if schoolLengthList??>
                            <#list schoolLengthList as length>
                                <#if schoolLength?? && schoolLength == length.code>
                                ${length.name!""}
                                </#if>
                            </#list>
                        <#else>
                            请选择
                        </#if>
                    </#if>
                </div>
            </div>
        </li>


        <li id="englishStartGrade_li"
            <#if (phase?? && phase == 2) || (phase?? && phase == 4)>hidden</#if>>
            <div class="link link-ico">
                <div class="side-fl">英语起始年级</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time side-english" name="englishStartGradeDisplay">
                    <#if englishStartGrade??&& englishStartGrade == 1>一年级
                    <#elseif englishStartGrade??&& englishStartGrade == 2>二年级
                    <#elseif englishStartGrade??&& englishStartGrade == 3>三年级
                    <#elseif englishStartGrade??&& englishStartGrade == 4>四年级
                    <#elseif englishStartGrade??&& englishStartGrade == 5>五年级
                    <#elseif englishStartGrade??&& englishStartGrade == 6>六年级
                    <#else>
                    </#if>
                </div>
            </div>
        </li>

        <li>
            <div>年级分布<span style="font-size: 65%;text-align:center">（请准确填写各年级的班级数和学生人数）</span></div>
        </li>
        <li>
            <div class="school_length">
                <#if phase?? && phase == 1>
                    <#if (schoolLength)?? && schoolLength =="1">
                            <ul class="showLength01">
                                <li><a data_value="1">小学一年级</a><span>共<input class="banClass" value="${classCount1!''}"/>班</span><span>共<input class="allMan" value="${studentCount1!''}"/>人</span></li>
                                <li><a data_value="2">小学二年级</a><span>共<input class="banClass" value="${classCount2!''}"/>班</span><span>共<input class="allMan" value="${studentCount2!''}"/>人</span></li>
                                <li><a data_value="3">小学三年级</a><span>共<input class="banClass" value="${classCount3!''}"/>班</span><span>共<input class="allMan" value="${studentCount3!''}"/>人</span></li>
                                <li><a data_value="4">小学四年级</a><span>共<input class="banClass" value="${classCount4!''}"/>班</span><span>共<input class="allMan" value="${studentCount4!''}"/>人</span></li>
                                <li><a data_value="5">小学五年级</a><span>共<input class="banClass" value="${classCount5!''}"/>班</span><span>共<input class="allMan" value="${studentCount5!''}"/>人</span></li>
                                <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass1!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain1!''}</a>人</></li>
                            </ul>
                        <#else>
                            <ul class="showLength02">
                                <li><a data_value="1">小学一年级</a><span>共<input class="banClass" value="${classCount1!''}"/>班</span><span>共<input class="allMan" value="${studentCount1!''}"/>人</span></li>
                                <li><a data_value="2">小学二年级</a><span>共<input class="banClass" value="${classCount2!''}"/>班</span><span>共<input class="allMan" value="${studentCount2!''}"/>人</span></li>
                                <li><a data_value="3">小学三年级</a><span>共<input class="banClass" value="${classCount3!''}"/>班</span><span>共<input class="allMan" value="${studentCount3!''}"/>人</span></li>
                                <li><a data_value="4">小学四年级</a><span>共<input class="banClass" value="${classCount4!''}"/>班</span><span>共<input class="allMan" value="${studentCount4!''}"/>人</span></li>
                                <li><a data_value="5">小学五年级</a><span>共<input class="banClass" value="${classCount5!''}"/>班</span><span>共<input class="allMan" value="${studentCount5!''}"/>人</span></li>
                                <li><a data_value="6">小学六年级</a><span>共<input class="banClass" value="${classCount6!''}"/>班</span><span>共<input class="allMan" value="${studentCount6!''}"/>人</span></li>
                                <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass2!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain2!''}</a>人</></li>
                            </ul>
                    </#if>
                </#if>
                <#if phase?? && phase == 2>
                    <#if (schoolLength)?? && schoolLength =="4">
                        <ul class="showLength04">
                            <li><a data_value="6">六年级</a><span>共<input class="banClass" value="${classCount6!''}"/>班</span><span>共<input class="allMan" value="${studentCount6!''}"/>人</span></li>
                            <li><a data_value="7">七年级</a><span>共<input class="banClass" value="${classCount7!''}"/>班</span><span>共<input class="allMan" value="${studentCount7!''}"/>人</span></li>
                            <li><a data_value="8">八年级</a><span>共<input class="banClass" value="${classCount8!''}"/>班</span><span>共<input class="allMan" value="${studentCount8!''}"/>人</span></li>
                            <li><a data_value="9">九年级</a><span>共<input class="banClass" value="${classCount9!''}"/>班</span><span>共<input class="allMan" value="${studentCount9!''}"/>人</span></li>
                            <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass3!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain3!''}</a>人</></li>
                        </ul>
                    <#else>
                        <ul class="showLength03">
                            <li><a data_value="7">七年级</a><span>共<input class="banClass" value="${classCount7!''}"/>班</span><span>共<input class="allMan" value="${studentCount7!''}"/>人</span></li>
                            <li><a data_value="8">八年级</a><span>共<input class="banClass" value="${classCount8!''}"/>班</span><span>共<input class="allMan" value="${studentCount8!''}"/>人</span></li>
                            <li><a data_value="9">九年级</a><span>共<input class="banClass" value="${classCount9!''}"/>班</span><span>共<input class="allMan" value="${studentCount9!''}"/>人</span></li>
                            <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass4!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain4!''}</a>人</></li>
                        </ul>
                    </#if>
                </#if>
                <#if phase?? && phase == 4>
                <ul class="showLength05">
                    <li><a data_value="10">高一</a><span>共<input class="banClass" type="tel" value="${classCount10!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount10!''}"/>人</span></li>
                    <li><a data_value="11">高二</a><span>共<input class="banClass" type="tel" value="${classCount11!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount11!''}"/>人</span></li>
                    <li><a data_value="12">高三</a><span>共<input class="banClass" type="tel" value="${classCount12!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount12!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass5!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain5!''}</a>人</></li>
                </ul>
                </#if>
            </div>
        </li>
    </ul>
    <ul class="mobileCRM-V2-list">

        <#if phase?? && phase == 4>
            <li>
                <div>年级分布<span style="font-size: 65%">（请准确填写各年级的班级数和学生人数）</span></div>
            </li>
            <li>
                <div class="school_length">
                    <ul class="showLength05">
                        <li><a data_value="10">高一</a><span>共<input class="banClass" type="tel" value="${classCount10!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount10!''}"/>人</span></li>
                        <li><a data_value="11">高二</a><span>共<input class="banClass" type="tel" value="${classCount11!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount11!''}"/>人</span></li>
                        <li><a data_value="12">高三</a><span>共<input class="banClass" type="tel" value="${classCount12!''}"/>班</span><span>共<input class="allMan" type="tel" value="${studentCount12!''}"/>人</span></li>
                       <#-- <li>高中一年级<span>共<input/>班</span><span>共<input/>人</span></li>
                        <li>高中二年级<span>共<input/>班</span><span>共<input/>人</span></li>
                        <li>高中三年级<span>共<input/>班</span><span>共<input/>人</span></li>-->
                        <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass5!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain5!''}</a>人</></li>
                    </ul>
                </div>
            </li>
        </#if>
    </ul>

</div>
<script>


</script>


</@layout.page>