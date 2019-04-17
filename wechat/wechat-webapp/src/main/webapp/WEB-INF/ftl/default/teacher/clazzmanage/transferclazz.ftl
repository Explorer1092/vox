<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="转让班级" pageJs="transferclazz">
    <@sugar.capsule css=["clazzManage"] />
    <div class="subjectList" data-bind="visible: subjectList().length > 1" style="display: none;">
        <h2>选择转出学科</h2>
        <div class="labelBox">
            <!-- ko foreach : {data : subjectList, as : '_subs'} -->
            <span class="label" data-bind="text: _subs.value, css: {'active': _subs.checked},click: $root.selectSubjectBtn">--</span>
            <!--/ko-->
        </div>
    </div>

    <div class="addTeacher" data-bind="visible: teachersList" style="display: none;">
        <ul class="cList">
            <!-- ko foreach : {data : teachersList, as : '_teacher'} -->
            <li data-bind="css: {'active': _teacher.checked},click: $root.selectTeacherBtn">
                <span class="select-icon"></span>
                <span class="tea-name" data-bind="text:  _teacher.teacherName()+'('+ _teacher.mainTeacherId() +')'"></span>
                <!--ko if: _teacher.authenticationState-->
                <i class="auth-icon"></i>
                <!--/ko-->
            </li>
            <!--/ko-->
        </ul>
        <#--<a data-bind="click: inviteBtn" href="javascript:void (0);" class="link-tips">
            <div class="icons"></div>
            <div class="text">
                <p class="blue">没找到该老师？</p>
                <p>可能对方未注册，点击邀请老师注册</p>
            </div>
        </a>-->
    </div>
    <div class="cFooter">
        <div class="footerInner">
            <div class="btnBox">
                <a href="javascript:void(0)" class="btn" data-bind="visible: teacherId() != 0, click: saveBtn" style="display: none;">确定</a>
                <a href="javascript:void(0)" class="btn btn-disabled" data-bind="visible: teacherId() == 0">确定</a>
            </div>
        </div>
    </div>
    <script>
        var subjectWeight={"CHINESE":0,"MATH":1,"ENGLISH":2};
        function sortSubject(a,b){
            return subjectWeight[a.name]-subjectWeight[b.name];
        }
        var subjects=${subjects![]};
        subjects=subjects.sort(sortSubject);
        var transferclazzMap = {
            subjects :subjects
        }
    </script>
</@activityMain.page>