<#import "../layout_default.ftl" as layout_default>

<@layout_default.page page_title='Auth Info'>
<form class="form-signin form-horizontal" method="post" action="?" style="max-width: 500px;">
    <h2 class="form-signin-heading text-center">Edit Password</h2>
    <div class="control-group">
        <label class="control-label" for="inputPassword">当前密码</label>
        <div class="controls">
            <input type="password" name="currentPassword" id="inputPassword" placeholder="Current Password" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="inputNewPassword">新密码</label>
        <div class="controls">
            <input type="password" name="newPassword" id="inputNewPassword" placeholder="New Password" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="inputRepeatPassword">重复密码</label>
        <div class="controls">
            <input type="password" name="confirmPassword" id="inputRepeatPassword" placeholder="Repeat Password" />
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <button type="submit" class="btn btn-large btn-primary">提交</button>
        </div>
    </div>
</form>
</@>
