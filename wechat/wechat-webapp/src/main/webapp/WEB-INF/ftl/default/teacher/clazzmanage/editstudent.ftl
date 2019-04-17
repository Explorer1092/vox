<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="班级管理" pageJs="editStudent">
    <@sugar.capsule css=["clazzManage"] />

    <div class="addTeacher">
        <ul class="cList">
            <li>
                <label class="label-name">姓名：</label>
                <span data-bind="text: studentDetail().studentName || studentDetail().studentId">--</span>
            </li>
            <li>
                <label class="label-name">学号：</label>
                <span data-bind="text: studentDetail().studentId">--</span>
            </li>
            <li>
                <label class="label-name">绑定手机：</label>
                <span data-bind="text: studentDetail().mobile || '未绑定'">--</span>
            </li>
        </ul>
    </div>
    <div class="cFooter" data-bind="visible: clazzId">
        <div class="footerInner">
            <div class="btnBox btnBox-2">
                <a href="javascript:void(0)" class="btn btn-grey" data-bind="click: deleteStudentBtn">删除学生</a>
                <a href="javascript:void(0)" class="btn" data-bind="click: resetPasswordBtn">重置密码</a>
            </div>
        </div>
    </div>
</@activityMain.page>