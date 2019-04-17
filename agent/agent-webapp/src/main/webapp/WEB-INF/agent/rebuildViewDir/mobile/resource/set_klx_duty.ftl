<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设置快乐学职务" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<div class="flow" id="parentDiv" style="background-color:#f1f2f5;">

</div>
<script type="text/html" id="contentDiv">
<%if(res){%>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/createaffairteacher.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}&isAffair=<%if(res.data.isAffairTeacher){%>true<%}else{%>false<%}%>">
    <div class="examineTitle">
        <div class="time" <%if(res.data.isAffairTeacher){%>style="color:#ff7d5a"<%}%>><%if(!res.data.isAffairTeacher){%>非教务老师<%}else{%>教务老师<%}%></div>
    教务老师
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可批量添加老师账号、建班、添加学生、打散换班等，开通后，该校普通老师“班级管理”菜单功能将失效；每校限一名；
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/change_school_quiz_bank_administrator_view.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}">
    <div class="examineTitle">
        <div class="time" style="color:#ff7d5a"><%if(res.data.schoolQuizBankAdministrator){%>校本题库管理员<%}%></div>
        校本题库管理员
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可管理该校校本题库，添加或删除普通老师查看校本题库，每校单个学科限一名；
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/set_klx_exam_manager_view.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}&isExamManager=<%if(res.data.isExamManager){%>true<%}else{%>false<%}%>">
    <div class="examineTitle">
        <div class="time" <%if(res.data.isExamManager){%>style="color:#ff7d5a"<%}%>><%if(!res.data.isExamManager){%>非考试管理员<%}else{%>考试管理员<%}%></div>
        考试管理员
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可创建全科考试，并负责流水阅卷各学科的任务分配、异常卡处理、阅卷进度管理等工作；
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/set_school_master_view.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}&isSchoolMaster=<%if(res.data.isSchoolMaster){%>true<%}else{%>false<%}%>">
    <div class="examineTitle">
        <div class="time" <%if(res.data.isSchoolMaster){%>style="color:#ff7d5a"<%}%>><%if(!res.data.isSchoolMaster){%>非校长<%}else{%>校长<%}%></div>
        校长
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可查看整个学校多学科考试分析报告
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/change_klx_grade_manager_view.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}">
    <div class="examineTitle">
        <div class="time" style="color:#ff7d5a"> <%=res.data["gradeManagerMap"].managedGradeStr%> </div>
        年级主任
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可创建多学科考试分析，并具有查看已设置年级的全年级多学科考试分析报告；
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/change_klx_subject_leader_view.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}">
    <div class="examineTitle">
        <div class="time" style="color:#ff7d5a"><%=res.data["subjectLeaderMap"].clazzLevelStr%> </div>
        学科组长
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc;width:93%;display: inline-block;">
            可查看已设置年级的全年级学情分析报告；
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
<div class="examineNotice-box opendNewPage" style="cursor: pointer;" data-info="/mobile/resource/teacher/classmanagelist.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}">
    <div class="examineTitle">
        <div class="time" <%if(res.data["classManagerMap"].classManagerFlag){%>style="color:#ff7d5a"<%}%>><%if(!res.data["classManagerMap"].classManagerFlag){%>非班主任<%}else{%>班主任<%}%></div>
        班主任
    </div>
    <div class="examineSide">
        <div class="subTitle" style="color:#94b7dc">
            可查看所带班的多学科考试分析报告
        </div>
        <div style="float: right;width:5%"> > </div>
    </div>
</div>
    <%}%>
    <#--<%%>-->
</script>
<script>
    $(document).ready(function () {
        reloadCallBack();
    });
    $.get("klx_duty_data.vpage?schoolId=${schoolId!0}&teacherId=${teacherId!0}",function(res){
        if(res.success){
            $("#parentDiv").html(template("contentDiv", {res:res}));
        }
    });
    $(document).on("click",'.opendNewPage',function () {
        openSecond($(this).data("info"));
    });
//    $('.subTitle').each(function(){
//        var pattern = $(this).html().trim();
//        $(this).html(pattern.replace(/\n/g, '<br />'));
//    });
//    $(document).ready(function(){
//        var notifyIdsStr = "";
//        $("input[name='js-ipt']").each(function(){
//            notifyIdsStr += $(this).val() + ",";
//        });
//        $.post("readNoticeList.vpage",{notifyIds:notifyIdsStr},function(){
//
//        });
//    });
</script>
</@layout.page>
