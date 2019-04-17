<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择" pageJs="chooseTeacher" footerIndex=4>
<@sugar.capsule css=['new_base','school']/>
<style>
    .btn-stroke{font-size:.6rem!important;}
</style>
    <a href="javascript:void(0)" class="inner-right js-submitTehBtn" style="display:none;">确定</a>
<div class="flex gap-line js-subTab c-flex c-flex-4" style="margin-bottom: .6rem;">
    <a class="item the" href="javascript:void(0);" data-type="english">英语</a>
    <a class="item" href="javascript:void(0);" data-type="math">数学</a>
    <a class="item" href="javascript:void(0);" data-type="chinese">语文</a>
    <a class="item" href="javascript:void(0);" data-type="other">其他</a>
</div>
<div class="c-main js-TeacherCon">
    <div class="item">
        <div class="englishCon">
            <#if english?size gt 0>
                <#list english as teacher>
                    <div class="btn-stroke fix-padding <#if (teacher.checked)!false>orange</#if>"  data-tid="${teacher.teacherId}">${teacher.teacherName!""}
                    <#if teacher.subTeacherList?has_content && teacher.subTeacherList?size gt 0>(<#list teacher.subTeacherList as sub>${sub.subject}<#if sub_has_next>、</#if></#list>)</#if>
                    </div>
                </#list>
            </#if>
        </div>
        <div class="mathCon" style="display: none;">
            <#if math?size gt 0>
                <#list math as teacher>
                    <div class="btn-stroke fix-padding <#if (teacher.checked)!false>orange</#if>"  data-tid="${teacher.teacherId}">${teacher.teacherName!""}
                        <#if teacher.subTeacherList?has_content && teacher.subTeacherList?size gt 0>(<#list teacher.subTeacherList as sub>${sub.subject}<#if sub_has_next>、</#if></#list>)</#if>
                    </div>
                </#list>
            </#if>
        </div>
        <div class="chineseCon" style="display: none;">
            <#if chinese?size gt 0>
                <#list chinese as teacher>
                    <div class="btn-stroke fix-padding <#if (teacher.checked)!false>orange</#if>"  data-tid="${teacher.teacherId}">${teacher.teacherName!""}
                        <#if teacher.subTeacherList?has_content && teacher.subTeacherList?size gt 0>(<#list teacher.subTeacherList as sub>${sub.subject}<#if sub_has_next>、</#if></#list>)</#if>
                    </div>
                </#list>
            </#if>
        </div>
        <div class="otherCon" style="display: none;">
            <#if other?size gt 0>
                <div style="clear:both;">
                    <#if schoolLevel == "MIDDLE" || schoolLevel == "HIGH">
                        <div style="width:100%;height: 1.5rem;line-height:1.5rem;font-size:.7rem">非教学老师</div>
                    </#if>
                    <#list other as teacher>
                        <div class="btn-stroke fix-padding <#if (teacher.checked)!false>orange</#if>"  data-tid="${teacher.teacherId}">${teacher.teacherName!""}</div>
                    </#list>
                </div>
                <div style="clear: both;">
                    <#if schoolLevel == "MIDDLE" || schoolLevel == "HIGH">
                        <div style="width:100%;height: 1.5rem;line-height:1.5rem;font-size:.7rem">已注册的其他科目老师</div>
                        <#if otherSubject?? && otherSubject?size gt 0>
                            <#list otherSubject as teacher>
                                <div class="btn-stroke fix-padding <#if (teacher.checked)!false>orange</#if>"  data-tid="${teacher.teacherId}">${teacher.teacherName!""}</div>
                            </#list>
                        </#if>
                    </#if>
                </div>
            </#if>
        </div>
    </div>
</div>

<script>
    var backUrl = '${backUrl!""}';
</script>
</@layout.page>
