<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="教师查询">
<div class="mobileCRM-V2-header">
    <div class="inner" name="enTeacher">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">${subject.value!''}老师</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-tab">
    <div name="status" method="nouse">未使用</div>
    <div name="status" method="noauth">使用未认证</div>
    <div name="status" method="auth" class="active">已认证</div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info" style="display:block;" id="auth" name="teacherList">
    <#if authedTeacherList??>
        <ul class="mobileCRM-V2-list">
            <#list authedTeacherList as authTeacher>
                <#if authTeacher??>
                    <li>
                        <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}_auth&teacherId=${authTeacher.teacherId!}" class="link">
                            <div class="side-fl"><#if authTeacher.realName?has_content>${authTeacher.realName!''}<#else >(暂无姓名)</#if></div>
                            <#if authTeacher.ambassador>
                                <div class="state-box">
                                    <div class="blue">校园大使</div>
                                </div>
                            </#if>
                            <#--<div class="side-fr side-info side-num">认证学生<span class="num">${authTeacher.authStudentCount!}</span>人</div>-->
                        </a>
                    </li>
                </#if>
            </#list>
         </ul>
    </#if>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info" style="display:none;" id="noauth" name="teacherList">
    <#if noAuthTeacherList??>
        <ul class="mobileCRM-V2-list">
            <#list noAuthTeacherList as noAuthTeacher>
                <#if noAuthTeacher??>
                    <li>
                        <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}_noauth&teacherId=${noAuthTeacher.teacherId!}" class="link">
                            <div class="side-fl"><#if noAuthTeacher.realName?has_content>${noAuthTeacher.realName!''}<#else >(暂无姓名)</#if></div>
                            <#if noAuthTeacher.ambassador>
                                <div class="state-box">
                                    <div class="blue">校园大使</div>
                                </div>
                            </#if>
                            <#--<div class="side-fr side-info side-num">认证学生<span class="num">${noAuthTeacher.authStudentCount!}</span>人</div>-->
                        </a>
                    </li>
                </#if>
            </#list>
        </ul>
    </#if>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info" style="display:none;" id="nouse" name="teacherList">
    <#if noUseTeacherList??>
        <ul class="mobileCRM-V2-list">
            <#list noUseTeacherList as noUseTeacher>
                <#if noUseTeacher??>
                    <li>
                        <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=school_${schoolId!}_${subject!''}_nouse&teacherId=${noUseTeacher.teacherId!}" class="link">
                            <div class="side-fl"><#if noUseTeacher.realName?has_content>${noUseTeacher.realName!''}<#else >(暂无姓名)</#if></div>
                            <#if noUseTeacher.ambassador>
                                <div class="state-box">
                                    <div class="blue">校园大使</div>
                                </div>
                            </#if>
                            <#--<div class="side-fr side-info side-num">认证学生<span class="num">${noUseTeacher.authStudentCount!}</span>人</div>-->
                        </a>
                    </li>
                </#if>
            </#list>
        </ul>
    </#if>
</div>
<script type="text/javascript">
     var curUrl =location.search.substr(1).toLowerCase();
     var parameterMap ={};
     var paraArr = curUrl.split("&");
     var tempArr;
     for(var i=0;i<paraArr.length;i++){
         tempArr = paraArr[i].split("=");
         parameterMap[tempArr[0]] = tempArr[1];
     }
     var key = curUrl.toLowerCase()
     $("div[name='status']").click(function(){
         var ele  = $(this);
         var type = $('#' + ele.attr("method"));
         $("div[name='status']").removeClass("active");
         ele.addClass("active");
         $("div[name='teacherList']").hide();
         type.show();
     });
</script>
</@layout.page>
