<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="教师查询">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">
            <#if status == "noUse">
            未使用
            <#elseif status == "noAuth">
            使用未认证
            <#elseif status == "authed">
            已认证
            </#if>
            </div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-tab">
    <div data-statu="english" class="js-subjItem active">英语</div>
    <div data-statu="math" class="js-subjItem">数学</div>
    <div data-statu="chinese" class="js-subjItem">语文</div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info">
    <ul class="mobileCRM-V2-list" data-statu="englishItem">
        <#if englishTeacherList?has_content>
            <#list englishTeacherList as engTeacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}&teacherId=${engTeacher.teacherId!}" class="link">
                        <div class="box">
                            <div class="side-fl">${engTeacher.realName!""}</div>
                        </div>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
    <ul class="mobileCRM-V2-list" style="display: none;" data-statu="mathItem">
        <#if mathTeacherList?has_content>
            <#list mathTeacherList as matTeacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}&teacherId=${matTeacher.teacherId!}" class="link">
                        <div class="box">
                            <div class="side-fl">${matTeacher.realName!""}</div>
                        </div>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
    <ul class="mobileCRM-V2-list" data-statu="chineseItem" style="display: none;">
        <#if chineseTeacherList?has_content>
            <#list chineseTeacherList as chiTeacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}&teacherId=${chiTeacher.teacherId!}" class="link">
                        <div class="box">
                            <div class="side-fl">${chiTeacher.realName!""}</div>
                        </div>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
</div>
<script>
    $(document).on("click",".js-subjItem",function(){
        var self = this;
        $(self).addClass("active");
        $(self).siblings("div").removeClass("active");
        var $subjItemNode = $('ul[data-statu = "'+self.dataset.statu+'Item"]');
        $subjItemNode.show();
        $subjItemNode.siblings('ul').hide();
    });
</script>
</@layout.page>

