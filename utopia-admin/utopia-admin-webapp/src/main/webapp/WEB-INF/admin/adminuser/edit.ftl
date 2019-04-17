<#import "../layout_default.ftl" as layout_default />

<@layout_default.page page_title="管理员列表" page_num=2>
    <form class="form-signin form-horizontal" method="post" action="?" style=" max-width: 500px;">
        <h2 class="form-signin-heading text-center">添加新用户</h2>
        <h5 class="text-center">注意：只用来添加客服外包用户，用户名必须以csos开头</h5>
        <#if adminUser.adminUserName?has_content>
        <div class="control-group">
            <label class="control-label">用户名：</label>
            <div class="controls">
                <input type="hidden" name="adminUserName" value="${adminUser.adminUserName}" />
                ${adminUser.adminUserName}
            </div>
        </div>
        <#else>
            <div class="control-group">
                <label class="control-label">用户名：</label>
                <div class="controls">
                    <input type="text" name="adminUserName" value="" placeholder="Username" />
                </div>
            </div>
        </#if>
        <div class="control-group">
            <label class="control-label">密码：</label>
            <div class="controls">
                <input type="text" name="password" type="password"  placeholder="Password"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">姓名：</label>
            <div class="controls">
                <input type="text" name="realName" value="${(adminUser.realName)!''}"  placeholder="Name"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">说明：</label>
            <div class="controls">
                <input type="text" name="comment" value="${(adminUser.comment)!''}"  placeholder="Explain"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">部门：</label>
            <div class="controls">
                <select id="department" name="department">
                <#list departmentList as departmentName>
                    <option value="${departmentName!}"> ${departmentName!}</option>
                </#list>
                </select>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button class="btn btn-large btn-primary" type="submit">提交</button>
            </div>
        </div>
    </form>
</@>