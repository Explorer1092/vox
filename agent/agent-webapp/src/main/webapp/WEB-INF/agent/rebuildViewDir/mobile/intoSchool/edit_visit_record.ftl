<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if schoolRecordId??>
    <#assign header = "修改备忘信息">
<#else>
    <#assign header = "备忘信息">
</#if>
<@layout.page title="${header!''}" pageJs="edit_visit_record" footerIndex=4>
<@sugar.capsule css=['new_base','school']/>
    <a style="display:none;" href="javascript:void(0)" class="inner-right submitBtn <#if schoolRecordId??>js-editSubmit<#else>js-submitSchoolSRecord</#if>">提交</a>
<div class="flow visit-detail">
    <div class="item clearfix js-school-newItem">
        <span class="t-name">学校备忘</span>
        <div class="inner-right">
        <#--<input class="" type="text" value="${vt.visitInfo!""}" placeholder="请填写">-->
            <textarea placeholder="请填写" id="school_memorandum" rows="5" value="${schoolMemorandumInfo!''}"
                      style="display: block;width: 80%;resize: none;line-height: .9rem;font-size: .75rem;word-wrap:break-word;word-break:break-all;border: none;margin-top: .8rem;padding:0 1rem;color: #76797e;">${schoolMemorandumInfo!""}</textarea>
        </div>
    </div>
    <#if visitTeacherList?? && visitTeacherList?size gt 0>
        <#list visitTeacherList as vt>
            <div class="item clearfix js-newItem" data-tid="${vt.teacherId!''}">
                <span class="t-name">${vt.teacherName!""}</span>
                <div class="inner-right">
                    <#--<input class="" type="text" value="${vt.visitInfo!""}" placeholder="请填写">-->
                    <textarea placeholder="请填写" rows="5" value="${vt.visitInfo!""}" style="word-wrap:break-word;word-break:break-all;display: block;width: 80%;resize: none;line-height: .9rem;font-size: .75rem;border: none;padding:0 1rem;margin-top: .8rem;color: #76797e;">${vt.visitInfo!""}</textarea>
                </div>
            </div>
        </#list>
    </#if>
</div>
<script>
    var sid = "${schoolRecordId!0}";
</script>
</@layout.page>