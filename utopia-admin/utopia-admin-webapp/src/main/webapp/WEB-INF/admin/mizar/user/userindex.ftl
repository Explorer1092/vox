<#import "../../layout_default.ftl" as layout_default />
<#import "../pager.ftl" as pager />
<@layout_default.page page_title="Mizar Manager" page_num=17>
<div id="main_container" class="span9">
    <legend>
        <strong>用户账户管理</strong>
        <a id="add_user" title="添加" href="javascript:void(0);" class="btn btn-success" style="float: right;">
            <i class="icon-plus icon-white"></i> 添加用户
        </a>
    </legend>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
            <ul class="inline">
                <li>
                    <input type="text" id="token" name="token" value="<#if token??>${token!}</#if>" placeholder="输入账户或者手机号">
                </li>
                <li>
                    <button type="submit" id="filter" class="btn btn-primary">
                        <i class="icon-search icon-white"></i> 查  询
                    </button>
                </li>
            </ul>
        </form>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th style="text-align: center; width: 200px;">登录名</th>
                <th style="text-align: center; width: 200px;">真实姓名</th>
                <th style="text-align: center; width: 200px;">手机号码</th>
                <th style="text-align: center; width: 200px;">用户角色</th>
                <th style="width:70px;">状态</th>
                <th style="text-align: center;">备注</th>
                <th style="text-align: center; width: 200px;">操作</th>
            </tr>
            </thead>
            <tbody>
                <#if allUser?? && allUser?has_content>
                    <#list allUser as user>
                    <tr>
                        <td id="account_${user.id}">${user.accountName!}</td>
                        <td id="name_${user.id}" style="text-align: center;">${user.realName!}</td>
                        <td id="mobile_${user.id}" style="text-align: center;">${user.mobile!}</td>
                        <td id="role_${user.id}" style="text-align: center;" data-value="<#if user.userRoles??>${user.userRoles?join(",")}</#if>">
                            <#if user.userRoles?? && user.userRoles?size gt 0>
                                <#list user.userRoles as roleItem><#if roleItem_index!=0>,</#if>${(allRoleMap[roleItem?string].roleName)!''}</#list>
                            </#if>
                        </td>
                        <td><#if user.status == 0>未登录<#elseif user.status == 1>使用中<#else>已关闭</#if></td>
                        <td id="comment_${user.id}">${user.userComment!}</td>
                        <td style="text-align: center;">
                            <a href="javascript:void(0);" name="user_shop" data-uid="${user.id!}" title="关联机构">
                                <i class="icon-th"></i>
                            </a>
                            <a href="javascript:void(0);" name="edit_user" data-uid="${user.id!}" title="编辑">
                                <i class="icon-pencil"></i>
                            </a>
                            <a href="javascript:void(0);" name="reset_pwd" data-uid="${user.id!}" title="重置密码">
                                <i class="icon-cog"></i>
                            </a>
                            <a href="javascript:void(0);" name="close_account" data-uid="${user.id!}" title="关闭账号">
                                <i class="icon-trash"></i>
                            </a>
                        </td>
                    </tr>
                    </#list>
                <#else>
                <tr>
                    <td colspan="7" style="text-align: center;"><strong>No Data Found</strong></td>
                </tr>
                </#if>
            </tbody>
        </table>
    </div></div></div>
</div>
<div id="edit_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加/编辑账户</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <input id="mode" type="hidden" value="add">
        <input id="uid" type="hidden" value="">
        <dl>
            <dt>登录名</dt>
            <dd>
                <input type="text" id="accountName" name="accountName"/>
            </dd>
        </dl>
        <dl>
            <dt>真实姓名</dt>
            <dd>
                <input type="text" id="realName" name="realName"/>
            </dd>
        </dl>
        <dl>
            <dt>手机号码</dt>
            <dd>
                <input type="text" id="mobile" name="mobile"/>
            </dd>
        </dl>
        <dl>
            <dt>用户角色</dt>
            <dd>
                <#list allRoleMap?keys as item>
                    <label class="checkbox"><input class="role-checkbox" data-value="${item!}" type="checkbox"/>${allRoleMap[item?string].roleName!}</label>
                </#list>
            </dd>
        </dl>
        <dl>
            <dt>备注</dt>
            <dd>
                <input type="text" id="userComment" name="userComment"/>
            </dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取  消</button>
        <button class="btn btn-primary" id="save_user_btn">保  存</button>
    </div>
</div>
<div id="user_shop_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>用户机构编辑</h3>
    </div>
    <input id="shop_uid" type="hidden" value="">
    <div class="modal-body dl-horizontal">
        <div style="max-height: 300px; overflow-y:auto;">
            <table class="table table-condensed">
                <thead>
                <tr>
                    <th>机构ID</th>
                    <th>机构名称</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="shopList"></tbody>
            </table>
        </div>
        <div style="height: 200px; text-align: center;">
            <textarea id="shopId" style="width: 90%; resize: none;" rows="5" placeholder="输入机构ID，用英文逗号分隔"></textarea>
            <button class="btn btn-primary" id="save_user_shop">保  存</button>
        </div>
    </div>
</div>
<script>
    $(function () {
        $('#add_user').on('click', function() {
            $('#accountName').val('');
            $('#realName').val('');
            $('#mobile').val('');
            $('#userComment').val('');
            $('#mode').val("add");
            $('#uid').val("");
            $(".role-checkbox").each(function() {
                $(this).attr("checked", false);
            });
            $('#edit_dialog') .modal('show');
        });

        // 编辑用户信息
        $("a[name=edit_user]").on('click', function () {
            var uid = $(this).data("uid");
            var account = $('#account_'+uid).html();
            var name = $('#name_'+uid).html();
            var mobile = $('#mobile_'+uid).html();
            var comment = $('#comment_'+uid).html();
            var role = $('#role_'+uid).data("value").toString().split(",");
            var roles = $(".role-checkbox");
            if(roles.length > 0){
                $(".role-checkbox").each(function(){
                    for (var i=0; i<role.length; ++i) {
                        if(role[i] == $(this).attr("data-value".toString())) {
                            $(this).attr("checked", true);
                        } else {
                            $(this).attr("checked", false);
                        }
                    }
                });
            }
            $('#accountName').val(account);
            $('#realName').val(name);
            $('#mobile').val(mobile);
            $('#userComment').val(comment);
            $('#mode').val("edit");
            $('#uid').val(uid);
            $('#edit_dialog') .modal('show');
        });

        $('#save_user_btn').on('click', function() {
            var roles = $(".role-checkbox");
            var roleVal = [];
            if(roles.length > 0){
                $(".role-checkbox:checked").each(function(){
                    roleVal.push($(this).attr("data-value"));
                });
            }
             var userData = {
                 mode : $('#mode').val(),
                 uid : $('#uid').val(),
                 accountName : $('#accountName').val(),
                 realName : $('#realName').val(),
                 mobile : $('#mobile').val(),
                 comment : $('#userComment').val(),
                 roles : roleVal.join(',')
             };
             $.post('saveuser.vpage', userData, function(res) {
                if (res.success) {
                    alert("保存成功");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
             });
        });

        // 重置密码
        $("a[name=reset_pwd]").on('click', function () {
            var uid = $(this).data("uid");
            if (!confirm("是否确认重置密码")) {
                return false;
            }
            $.post('resetpwd.vpage', {userId:uid}, function(res) {
                if (res.success) {
                    alert("密码重置成功:" + res.pwd);
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        // 关闭账号
        $("a[name=close_account]").on('click', function () {
            var uid = $(this).data("uid");
            if (!confirm("是否确认关闭账号")) {
                return false;
            }
            $.post('closeaccount.vpage', {userId:uid}, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        // 关联机构
        $("a[name=user_shop]").on('click', function () {
            var uid = $(this).data("uid");
            $('#shop_uid').val(uid);
            var $body =  $('#shopList');
            $body.html('');
            $.get('usershoplist.vpage', {userId:uid}, function(res) {
                if (!res.success) {
                   alert(res.info);
                } else {
                    for(var i = 0; i < res.shopList.length; i++){
                        var info = res.shopList[i];
                        var str = "<tr id=\"shop_info_"+info.shopId+"\"><td class=\"center\">"+info.shopId+"</td>";
                        str += "<td class=\"center\">"+info.shopName+"</td>";
                        str += "<td class=\"center\"><a href=\"javascript:void(0);\" name=\"del_user_shop\" data-sid=\""+info.shopId+"\">解除</a></td></tr>";
                        $body.append(str);
                    }
                    $('#user_shop_dialog').modal('show');
                }
            });
        });

        $('#save_user_shop').on('click', function() {
            var uid = $("#shop_uid").val();
            var shopId = $('#shopId').val();
            if (shopId == '') {
                alert("请填写机构ID");
                return false;
            }
            $.post('saveusershop.vpage', {userId:uid, shopId:shopId}, function(res) {
               if (res.success) {
                   alert("success");
                   var shopId = $('#shopId').val('');
                   var info = res.shop;
                   for(var i = 0; i < info.length; i++) {
                       var str = "<tr><td class=\"center\">" + info[i].shopId + "</td>";
                       str += "<td class=\"center\">" + info[i].shopName + "</td>";
                       str += "<td class=\"center\"><a href=\"javascript:void(0);\" name=\"del_user_shop\" data-sid=\"" + info[i].shopId + "\"></a></td></tr>";
                       $('#shopList').append(str);
                   }
               } else {
                   alert(res.info);
               }

            });
        });

        $(document).on('click', "a[name=del_user_shop]", function() {
            var uid = $("#shop_uid").val();
            var $this = $(this);
            var shopId = $this.data("sid");
            $.post('delusershop.vpage', {userId:uid, shopId:shopId}, function(res) {
                if (!res.success) {
                    alert(res.info);
                } else {
                   $('#shop_info_'+shopId).remove();
                }
            });
        });
    });
</script>
</@layout_default.page>