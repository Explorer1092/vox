<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="" pageJs="teacherList">
    <@sugar.capsule css=["clazzManage"] />

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
</@activityMain.page>