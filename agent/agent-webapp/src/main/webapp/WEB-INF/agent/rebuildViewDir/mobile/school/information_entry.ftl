<style>
    .fixLength{
        display:block;
    }
    .noLength{
        display:none;
    }
    .school_length input{
        width:11%;
        font-size:.75rem;
        text-align:center;
    }
    .school_length span{
        margin-left:13%;
    }

    .mobileCRM-V2-info{
        font-size:.75rem
    }
    .school_length ul li {
        width:100%;
        text-align:center;
    }
</style>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" >
    <p style="padding: 1.2rem 0 0.5rem 0; color: #ff7d5a;">注意：请准确填写以下信息，每月25日后将不能进行补充及修改</p>
    <ul class="mobileCRM-V2-list" <#if phase?? && phase == 4>hidden</#if>>

        <li>
            <div class="link link-ico">
                <div class="side-fl"  style="font-size: 1rem">学制</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time" name="schoolingLengthDisplay">
                    <#if schoolLength??>
                        <#else>
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

            <select id="schoolingLength" name="schoolingLength" <#if phase?? && phase == 4>hidden</#if>>
                <option value="0">请选择</option>
            <#if phase?? && phase == 1>
                <option <#if schoolLength?? && schoolLength == 1>selected</#if>
                        value="1">小学五年制
                </option>
                <option <#if phase?? && phase ==2 >hidden</#if>
                        <#if schoolLength?? && schoolLength == 2>selected</#if>
                        value="2">小学六年制
                </option>
            </#if>
            <#if phase?? && phase == 2 >
                <option <#if schoolLength?? && schoolLength == 3>selected</#if>
                        value="3">中学三年制
                </option>
                <option <#if phase?? && phase==1 >hidden</#if>
                        <#if schoolLength?? && schoolLength == 4>selected</#if>
                        value="4">中学四年制
                </option>
            </#if>
            </select>
        </li>


        <li id="englishStartGrade_li"
            <#if phase?? && phase == 2 >hidden</#if>>
            <div class="link link-ico">
                <div class="side-fl"  style="font-size: 1rem">英语起始年级</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <div class="side-fr side-time side-english" name="englishStartGradeDisplay">
                <#if englishStartGrade??&& englishStartGrade == 1>一年级
                <#elseif englishStartGrade??&& englishStartGrade ==2 >二年级
                <#elseif englishStartGrade??&& englishStartGrade ==3>三年级
                <#elseif englishStartGrade??&& englishStartGrade ==4>四年级
                <#elseif englishStartGrade??&& englishStartGrade ==5>五年级
                <#elseif englishStartGrade??&& englishStartGrade ==6>六年级
                <#else>请选择
                </#if>
                </div>
            </div>
            <select id="englishStartGrade" name="englishStartGrade" >
                <option value="0">请选择</option>
                <option
                <#if englishStartGrade??&& englishStartGrade == 1>selected</#if>
                value="1">一年级
                </option>
                <option
                <#if englishStartGrade??&& englishStartGrade == 2>selected</#if>
                value="2">二年级
                </option>
                <option
                <#if englishStartGrade??&& englishStartGrade == 3>selected</#if>
                value="3">三年级
                </option>
                <option
                <#if englishStartGrade??&& englishStartGrade == 4>selected</#if>
                value="4">四年级
                </option>
                <option
                <#if englishStartGrade??&& englishStartGrade == 5>selected</#if>
                value="5">五年级
                </option>
                <option <#if englishStartGrade??&& englishStartGrade == 6>selected</#if>
                        value="6">
                    六年级
                </option>
            </select>
        </li>

        <li>
            <div  style="font-size: 1rem">年级分布<span style="font-size: 65%">（请准确填写各年级的班级数和学生人数）</span></div>
        </li>
        <li>
            <div class="school_length">
                <ul class="showLength01 noLength">
                    <li><a data_value="1">小学一年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount1!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount1!''}"/>人</span></li>
                    <li><a data_value="2">小学二年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount2!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount2!''}"/>人</span></li>
                    <li><a data_value="3">小学三年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount3!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount3!''}"/>人</span></li>
                    <li><a data_value="4">小学四年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount4!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount4!''}"/>人</span></li>
                    <li><a data_value="5">小学五年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount5!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount5!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass1!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain1!''}</a>人</></li>
                </ul>
                <ul class="showLength02 noLength">
                    <li><a data_value="1">小学一年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount1!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount1!''}"/>人</span></li>
                    <li><a data_value="2">小学二年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount2!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount2!''}"/>人</span></li>
                    <li><a data_value="3">小学三年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount3!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount3!''}"/>人</span></li>
                    <li><a data_value="4">小学四年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount4!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount4!''}"/>人</span></li>
                    <li><a data_value="5">小学五年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount5!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount5!''}"/>人</span></li>
                    <li><a data_value="6">小学六年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount6!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount6!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass2!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain2!''}</a>人</></li>
                </ul>
                <ul class="showLength04 noLength">
                    <li><a data_value="6">六年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount6!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount6!''}"/>人</span></li>
                    <li><a data_value="7">七年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount7!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount7!''}"/>人</span></li>
                    <li><a data_value="8">八年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount8!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount8!''}"/>人</span></li>
                    <li><a data_value="9">九年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount9!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount9!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass3!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain3!''}</a>人</></li>
                </ul>
                <ul class="showLength03 noLength">
                    <li><a data_value="7">七年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount7!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount7!''}"/>人</span></li>
                    <li><a data_value="8">八年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount8!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount8!''}"/>人</span></li>
                    <li><a data_value="9">九年级</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" value="${classCount9!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" value="${studentCount9!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass4!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain4!''}</a>人</></li>
                </ul>
                <ul class="showLength05 noLength">
                    <li><a data_value="10">高一</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount10!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" type="tel" value="${studentCount10!''}"/>人</span></li>
                    <li><a data_value="11">高二</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount11!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" type="tel" value="${studentCount11!''}"/>人</span></li>
                    <li><a data_value="12">高三</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount12!''}"/>班</span><span>共<input onkeyup="value=value.replace(/[^\d]/g,'')" class="allMan" type="tel" value="${studentCount12!''}"/>人</span></li>
                    <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass5!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain5!''}</a>人</></li>
                </ul>
            </div>
        </li>
    </ul>
<ul class="mobileCRM-V2-list">

<#if phase?? && phase == 4>
    <li>
        <div style="font-size: 1rem">年级分布<span style="font-size: 65%">（请准确填写各年级的班级数和学生人数）</span></div>
    </li>
    <li>
        <div class="school_length">
            <ul class="showLength05">
                <li class="fixLength"><a data_value="10">高一</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount10!''}"/>班</span><span>共<input class="allMan" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${studentCount10!''}"/>人</span></li>
                <li class="fixLength"><a data_value="11">高二</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount11!''}"/>班</span><span>共<input class="allMan" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${studentCount11!''}"/>人</span></li>
                <li class="fixLength"><a data_value="12">高三</a><span>共<input class="banClass" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${classCount12!''}"/>班</span><span>共<input class="allMan" onkeyup="value=value.replace(/[^\d]/g,'')" type="tel" value="${studentCount12!''}"/>人</span></li>
                <li><a>合计</a><span>共<a style="width:10%;display: inline-block;">${schoolClass5!''}</a>班</span><span>共<a style="width:10%;display: inline-block;">${schoolMain5!''}</a>人</></li>
            </ul>
        </div>
    </li>
</#if>
</ul>

</div>
