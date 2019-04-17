<#assign schoolClass2 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int + (classCount6!0)?int>
<#assign schoolClass1 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int>
<#assign schoolClass3 =  (classCount6!0)?int + (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
<#assign schoolClass4 =  (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
<#assign schoolClass5 =  (classCount11!0)?int + (classCount12!0)?int + (classCount13!0)?int >
<#assign schoolClass6 =  (classCount51!0)?int + (classCount52!0)?int + (classCount53!0)?int + (classCount54!0)?int >
<#assign schoolClass7 =  (classCount9!0)?int + (classCount11!0)?int + (classCount12!0)?int + (classCount13!0)?int >
<#assign schoolMain1 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int>
<#assign schoolMain2 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int + (studentCount6!0)?int>
<#assign schoolMain3 = (studentCount6!0)?int + (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
<#assign schoolMain4 = (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
<#assign schoolMain5 = (studentCount11!0)?int + (studentCount12!0)?int + (studentCount13!0)?int>
<#assign schoolMain6 = (studentCount51!0)?int + (studentCount52!0)?int + (studentCount53!0)?int+ (studentCount54!0)?int>
<#assign schoolMain7 = (studentCount9!0)?int+ (studentCount11!0)?int + (studentCount12!0)?int + (studentCount13!0)?int>
<div class="flow" style="background:#fff">
    <p style="padding:0.4rem 0 0.4rem 0.8rem;background:#f6f6f6;color:#9199bb;text-align:left;font-size: 60%;">选填（申请字典表使用）</p>

    <#if phase_value?has_content>
        <div class="item englishStartGrade"  style="margin:0 0 0 1rem;padding:0 1rem 0 0;border:0" <#if phase_value == 1 || phase_value == 0>hidden</#if>>
                英语起始年级
                <div class="inner-right js-english">
                    <#if englishStartGrade??&& englishStartGrade == 1>一年级
                    <#elseif englishStartGrade??&& englishStartGrade == 3>三年级
                    <#else>请选择
                    </#if>
                </div>
            <#if locked?? && !locked>
                <select name="" id="englishStartGrade" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                    <option value="0">请选择</option>
                    <option value="1" <#if englishStartGrade??&& englishStartGrade == 1>selected</#if>>一年级</option>
                    <option value="3" <#if englishStartGrade??&& englishStartGrade == 3>selected</#if>>三年级</option>
                </select>
            </#if>
            </div>
    <#else>
        <div class="item englishStartGrade"  style="margin:0 0 0 1rem;padding:0 1rem 0 0;border:0">
            英语起始年级
            <div class="inner-right js-english">
                <#if englishStartGrade??&& englishStartGrade == 1>一年级
                <#elseif englishStartGrade??&& englishStartGrade == 3>三年级
                <#else>请选择
                </#if>
            </div>
        <#if locked?? && !locked>
            <select name="" id="englishStartGrade" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                <option value="0" <#if englishStartGrade??&& englishStartGrade == 0>selected</#if>>请选择</option>
                <option value="1" <#if englishStartGrade??&& englishStartGrade == 1>selected</#if>>一年级</option>
                <option value="3" <#if englishStartGrade??&& englishStartGrade == 3>selected</#if>>三年级</option>
            </select>
        </#if>
        </div>
    </#if>
</div>
    <div class="mobileCRM-V2-list schoolParticular-edit">
        <div class="edit-title js-distribution" <#if phase_value?has_content><#else>hidden</#if>>年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
        <div class="school_length edit-list">
            <ul class="showLength01 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="1">一年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount1!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount1!''}"/>人</div></li>
                <li><div class="level" data_value="2">二年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount2!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount2!''}"/>人</div></li>
                <li><div class="level" data_value="3">三年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount3!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount3!''}"/>人</div></li>
                <li><div class="level" data_value="4">四年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount4!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount4!''}"/>人</div></li>
                <li><div class="level" data_value="5">五年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount5!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount5!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass1!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" style="color:#636880" disabled="disabled" value="${schoolMain1!''}">人</div></li>
            </ul>
            <ul class="showLength02 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="1">一年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount1!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount1!''}"/>人</div></li>
                <li><div class="level" data_value="2">二年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount2!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount2!''}"/>人</div></li>
                <li><div class="level" data_value="3">三年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount3!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel"value="${studentCount3!''}"/>人</div></li>
                <li><div class="level" data_value="4">四年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount4!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount4!''}"/>人</div></li>
                <li><div class="level" data_value="5">五年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount5!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount5!''}"/>人</div></li>
                <li><div class="level" data_value="6">六年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount6!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount6!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass2!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain2!''}">人</div></li>
            </ul>
            <ul class="showLength04 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="6">六年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount6!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount6!''}"/>人</div></li>
                <li><div class="level" data_value="7">七年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount7!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount7!''}"/>人</div></li>
                <li><div class="level" data_value="8">八年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount8!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount8!''}"/>人</div></li>
                <li><div class="level" data_value="9">九年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount9!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel"value="${studentCount9!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass3!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain3!''}">人</div></li>
            </ul>
            <ul class="showLength03 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="7">七年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount7!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount7!''}"/>人</div></li>
                <li><div class="level" data_value="8">八年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount8!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount8!''}"/>人</div></li>
                <li><div class="level" data_value="9">九年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount9!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount9!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass4!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain4!''}">人</div></li>
            </ul>
            <ul class="showLength05 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="11">高一</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount11!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount11!''}"/>人</div></li>
                <li><div class="level" data_value="12">高二</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount12!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount12!''}"/>人</div></li>
                <li><div class="level" data_value="13">高三</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount13!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount13!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass5!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain5!''}">人</div></li>
            </ul>
            <ul class="showLength06 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="51">小班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount51!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount51!''}"/>人</div></li>
                <li><div class="level" data_value="52">中班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount52!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount52!''}"/>人</div></li>
                <li><div class="level" data_value="53">大班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount53!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount53!''}"/>人</div></li>
                <li><div class="level" data_value="54">学前班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount54!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount54!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass6!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain6!''}">人</div></li>
            </ul>
            <ul class="showLength07 schoolGrade" style="display: none" <#if phase_value?? && phase_value == 4>hidden</#if>>
                <li><div class="level" data_value="9">九年级</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount9!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount9!''}"/>人</div></li>
                <li><div class="level" data_value="11">高一</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount11!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount11!''}"/>人</div></li>
                <li><div class="level" data_value="12">高二</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount12!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount12!''}"/>人</div></li>
                <li><div class="level" data_value="13">高三</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${classCount13!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${studentCount13!''}"/>人</div></li>
                <li><div>合计</div><div>共<input value="${schoolClass7!''}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" style="color:#636880" value="${schoolMain7!''}">人</div></li>
            </ul>
        </div>
    </div>


