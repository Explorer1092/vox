<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="重置密码" pageJs="resetPwd">
    <@sugar.capsule css=["clazzManage"] />
    <div class="addTeacher">
        <ul class="cList">
            <li>
                <label class="label-name">新密码</label>
                <input data-bind="value: passwordValue" type="password" placeholder="请输入新的登录密码" class="pwd-ipt">
            </li>
            <li>
                <label class="label-name">确认新密码</label>
                <input data-bind="value: passwordConfirmValue" type="password" placeholder="请再次输入新的登录密码" class="pwd-ipt">
            </li>
        </ul>
    </div>
    <div class="cFooter">
        <div class="footerInner">
            <div class="btnBox">
                <a data-bind="click: passwordSaveBtn" href="javascript:void(0)" class="btn">确定</a>
            </div>
        </div>
    </div>

</@activityMain.page>