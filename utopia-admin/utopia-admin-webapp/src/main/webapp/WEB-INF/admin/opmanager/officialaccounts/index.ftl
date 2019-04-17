<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="公众号管理平台" page_num=9>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link  href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>

<div id="main_container" class="span9">
    <legend>
        <strong>公众号管理</strong>
        <#if isTopAuditor>
            <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right" id="add_accounts_btn">添加公众号
            </button>
        </#if>
    </legend>
    <form id="activity-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/opmanager/officialaccounts/index.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="90px">公众号ID</th>
                        <th>公众号名称</th>
                        <th>状态</th>
                        <th>允许用户主动关注</th>
                        <th>创建时间</th>
                        <th width="290px">配置</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if accountsPage?? && accountsPage.content?? >
                            <#list accountsPage.content as accounts >
                            <tr data-sadminusers="${accounts.seniorAdminUsers}" data-gadminusers="${accounts.generalAdminUsers!''}" >
                                <td>${accounts.id!}</td>
                                <td><a href="accountdetail.vpage?accountId=${accounts.id}">${accounts.name!}</a></td>
                                <td>${(accounts.status == "Online")?string('上线','下线')}</td>
                                <td>${accounts.followLimit?string('是','否')}</td>
                                <td>${accounts.createDatetime}</td>
                                <td>
                                    <button type="button" class="btn btn-default btn-xs" name="show-detail" >信息</button>
                                    <button type="button" class="btn btn-default btn-xs" name="config-admins" >管理员</button>
                                    <a href="accountconfig.vpage?accountId=${accounts.id!}" type="button" class="btn">投放策略</a>
                                </td>
                                <td>
                                    <#switch accounts.status>
                                        <#case "Online">
                                            <button name="offline-btn" class="btn btn-danger" ${(isTopAuditor ||(accounts.seniorAdminUsers?contains(currUser)))?string('','disabled="disabled"')}>下线</button>
                                            <#break>
                                        <#case "Offline">
                                            <button name="online-btn" class="btn btn-success" ${(isTopAuditor ||(accounts.seniorAdminUsers?contains(currUser)))?string('','disabled="disabled"')}>上线</button>
                                            <#break>
                                        <#default>
                                    </#switch>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
<!-- 配置投放策略 -->
<div id="config-putin-policy" class="modal fade hide" aria-hidden="true" style="display:none">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 class="modal-title">公众号-配置投放策略</h3>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <ul class="nav nav-tabs" id="putin-type" role="tablist">
                <li role="presentation">
                    <a href="#all" id="all-tab" role="tab" data-toggle="tab" aria-controls="all" aria-expanded="false">全部用户</a>
                </li>
                <li role="presentation">
                    <a href="#region" id="region-tab" role="tab" data-toggle="tab" aria-controls="region" aria-expanded="false">指定地区用户</a>
                </li>
            </ul>
            <div class="tab-content" id="putin-tab-content">
                <div class="tab-pane" role="tabpanel" id="all" aria-labelledby="all-tab">asdfsdf</div>
                <div class="tab-pane" role="tabpanel" id="region" aria-labelledby="region-tab">
                    <div id="regionTree" class="sampletree" style="width:60%; height: 410px; float: left; display: inline;"></div>
                    <div style="width:40%; height: 500px; float:right; display: inline;">
                        &nbsp;&nbsp;筛选 <input name="filter_region" type="text" class="input-small" id="filter_region" placeholder="筛选条件...">
                        <button name="delete_region_filter" id="delete_region_filter">×</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button id="save-account-admins" type="button" class="btn btn-primary">更新策略</button>
        </div>
    </div>
</div>

<!-- 配置管理员的窗口 -->
<div id="config-admin-dialog" class="modal fade hide" aria-hidden="true" style="display:none">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 class="modal-title">公众号-配置管理员</h3>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="form-horizontal">
                <form id="config-admins-frm" action="save.vpage" method="post">
                    <div class="control-group">
                        <label class="col-sm-2 left-compact-label">高级管理员:</label>
                    <#--<div class="">-->
                        <#--<select name="s-admin-users" class="admin-select" multiple="multiple">
                        </select>-->
                        <input class="admin-select" type="text" name="s-admin-users" >
                    <#--</div>-->
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 left-compact-label">普通管理员:</label>
                        <div class="">
                            <#--<select name="g-admin-users" class="admin-select" multiple="multiple">
                            </select>-->
                                <input class="admin-select" type="text" name="g-admin-users" >
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <#if isTopAuditor>
                <button id="save-account-admins" type="button" class="btn btn-primary">提交</button>
            </#if>
        </div>
    </div>
</div>

<!-- 添加公众号的窗口 -->
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog" >
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加/配置公众号</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="add-account-frm" action="save.vpage" method="post" role="form">
                    <div class="form-group" style="height:800px;display: none">
                        <label class="col-sm-2 control-label"><strong>公众号ID</strong></label>
                        <div class="controls">
                            <input type="text" id="accountId" >
                        </div>
                    </div>
                    <div class="form-group has-feedback">
                        <label>公众号账户</label>
                        <input class="form-control" type="text" pattern="[A-z]*" id="accountsKey"
                               maxlength="20" data-error="必填，并且全英文" required>
                        <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">公众号名称</label>
                        <input class="form-control" type="text" id="name" maxlength="10" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label>公众号说明</label>
                        <textarea class="form-control" id="instruction" rows="5" cols="10" maxlength="200" required></textarea>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">每日最多发布数</label>
                        <input class="form-control" type="text" id="maxPublishNumsD" maxlength="5" pattern="[0-9]*" data-error="必须是数字" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">每月最多发布数</label>
                        <input class="form-control" type="text" id="maxPublishNumsM" maxlength="7" pattern="[0-9]*" data-error="必须是数字" required>
                        <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" id="followLimit" width="25px">
                                允许用户主动关注
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" id="paymentBlackLimit" width="25px">
                                增加付费黑名单限制
                            </label>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <#if isTopAuditor>
                <button id="save-account-submit" type="submit" class="btn btn-primary">提交</button>
                </#if>
            </div>
        </div>
    </div>
</div>

<style>

    label{
        font-weight: 700;
    }

    .checkbox label{
        font-weight: 400;
    }

    .list-unstyled {
        padding-left: 0;
        list-style: none;
    }

    .help-block {
        display: block;
        margin-top: 5px;
        margin-bottom: 10px;
        color: #a94442;
    }

    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }

    .form-checkbox {
        padding-top: 3px;
    }

    .left-compact-label{
        padding-top:3px;
        width:90px;
        float:left;
        text-align: left;
    }

    .admin-select{
        width:410px;
    }

    .form-control {
        display: block;
        width: 95%;
        height: 34px;
        padding: 6px 12px;
        font-size: 14px;
        line-height: 1.42857143;
        color: #555;
        background-color: #fff;
        background-image: none;
        border: 1px solid #ccc;
        border-radius: 4px;
        -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
        box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
        -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
        -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
        transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
        margin-bottom:0px;
    }

    ul, ol {
        padding: 0;
        margin: 0 0 0px 0px;
    }

</style>
<script type="text/javascript">

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#activity-query").submit();
    }

    $(function () {
        // 初始化表单
        $("form#add-account-frm").validator();

        // 预加载平台的用户
        /*$.get("/management/user/getadminusers.vpage",{},function(data){
            var adminSelect = $("form#config-admins-frm select.admin-select");

            $.each(adminSelect,function(index,_adminSelect){
                var $select = $(_adminSelect);
                $.each(data.users,function(index,user){
                    $select.append("<option value='"+user.adminUserName+"'>"+ user.adminUserName +"</option>")
                });

                $select.select2({
                    "multiple":true
                });
            });
        });*/

        $("button#save-account-admins").click(function(){
            var gAdminUsers = $("input[name=g-admin-users]").val();
            var sAdminUsers = $("input[name=s-admin-users]").val();
            var accountId = $("#config-admin-dialog").data("accountid");

            // 高级管理员不能为空
            if(!sAdminUsers || sAdminUsers.trim() == ""){
                alert('高级管理员不能为空');
                return;
            }

            $.post("updateadminusers.vpage",
                    {"accountId":accountId,
                        "gAdminUsers":gAdminUsers,
                        "sAdminUsers":sAdminUsers},
                    function(data){
                        if(data.success){
                            alert("更新成功！");
                            $("#config-admin-dialog").modal("hide");

                            window.location.reload();
                        }
                    });
        });

        $(document).on('click', 'button[name=online-btn]', function () {
            var $this = $(this);
            var accountName = $this.parent().siblings().eq(1).find("a").html();
            if(confirm("是否要上线"+ accountName +"公众号")){
                $.post('accountonline.vpage', {accountId: $this.parent().siblings().first().html()}, function (res) {
                    if (res.success) {
                        //$this.removeClass('on-btn').addClass('off-btn').html('下线');
                        alert("上线成功!");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                });
            }
        });

        $(document).on('click', 'button[name=offline-btn]', function () {
            var $this = $(this);
            var accountName = $this.parent().siblings().eq(1).find("a").html();
            if(confirm("是否要下线"+accountName+"公众号，下线后用户在前台将无法使用此公众号")){
                $.post('accountoffline.vpage', {accountId: $this.parent().siblings().first().html()}, function (res) {
                    if (res.success) {
                        //$this.removeClass('off-btn').addClass('on-btn').html('上线');
                        alert("下线成功!");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                });
            }
        });

        $("form#add-account-frm").validator().on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // 校验不通过
                return;
                // handle the invalid form...
            } else {
                e.preventDefault();
                // everything looks good!
                var frm = $("form#add-account-frm");

                var postData = {};
                $.each($("input,textarea",frm),function(index,field){
                    var _f = $(field);

                    if(_f.attr("type") == "checkbox"){
                        postData[_f.attr("id")] = _f.is(":checked") ? true : false;
                    }else
                        postData[_f.attr("id")] = _f.val();
                });

                $.post("save.vpage", postData,
                        function(data){
                            $("#add_dialog").modal("hide");
                            if(data.success) {
                                alert("保存成功");
                            }
                            else
                                alert(data.info);

                            window.location.reload();
                        }
                );
            }
        });

        $("button#save-account-submit").click(function(){

            var frm = $("form#add-account-frm");
            /*frm.validator('validate');
            return;*/
            frm.submit();
        });

        $("button[name='show-detail']").click(function(){
            var $this = $(this);
            $.get("getaccountinfo.vpage",{accountId: $this.parent().siblings().first().html()}, function(data){
                if(data.success){
                    $("#add_dialog").modal("show");
                    var account = data.account;

                    mapForm(function(f,isCheck){
                        if(isCheck){
                            f.prop("checked",account[f.attr("id")]);
                        }else
                            f.val(account[f.attr("id")]);
                    });

                    $("#add_dialog input#accountId").val(account.id);
                    $("form#add-account-frm").validator('validate');
                }
            });
        });

        $("button[name='config-admins']").click(function(){
            var $this = $(this);
            var accountId = $this.parent().siblings().first().html();
            var accountName = $this.parent().siblings().eq(1).html();
            var $row = $(this).parents("tr");

            $("input[name=s-admin-users]").val($row.data("sadminusers"));
            $("input[name=g-admin-users]").val($row.data("gadminusers"));

            var $adminDialog = $("#config-admin-dialog");
            $adminDialog.modal("show");
            $adminDialog.data("accountid",accountId);

            $("h3.modal-title",$adminDialog).html(accountName + "-配置管理员");
        });

        $("button#add_accounts_btn").click(function(){
            $("#add_dialog input#accountId").val(0);
            // 清空
            mapForm(function(field,isCheck){
                if(isCheck)
                    field.attr("checked",false);
                else
                    field.val('');
            });

            $("#add_dialog").modal("show");
        });

        function mapForm(func){
            var frm = $("form#add-account-frm");
            $.each($("input,textarea",frm),function(index,field){
                var _f = $(field);
                func(_f,_f.attr("type") == "checkbox");
            });
        }

    });
</script>
</@layout_default.page>