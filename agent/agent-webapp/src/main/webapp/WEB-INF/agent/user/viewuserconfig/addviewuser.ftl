<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加/编辑协作账户' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑协作账户</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户名</label>
                        <div class="controls">
                            <input id="account_name" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.accountName!}</#if>"
                                   <#if userId??>disabled</#if>>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">真实姓名</label>
                        <div class="controls">
                            <input id="real_name" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.realName!}</#if>">
                        </div>
                    </div>
                    <#if !userId??>
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">密码</label>
                            <div class="controls">
                                <input id="password" class="input-xlarge focused" type="password" value="">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">确认密码</label>
                            <div class="controls">
                                <input id="re_password" class="input-xlarge focused" type="password" value="">
                            </div>
                        </div>
                    </#if>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户简介</label>
                        <div class="controls">
                            <textarea id="user_comment" class="input-xlarge focused"><#if agentUser??>${agentUser.userComment!}</#if></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户电话</label>
                        <div class="controls">
                            <input id="tel" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.tel!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户邮箱</label>
                        <div class="controls">
                            <input id="email" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.email!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户QQ</label>
                        <div class="controls">
                            <input id="imAccount" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.imAccount!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户地址</label>
                        <div class="controls">
                            <input id="address" class="input-xlarge focused" type="text" value="<#if agentUser??>${agentUser.address!}</#if>">
                        </div>
                    </div>
                    <div class="control-group" id="group_region_tree">
                        <label class="control-label">协作区域</label>
                        <div id="regionTree" class="controls" style="width: 280px;height: 300px">
                        </div>
                        <input type="hidden" name="regionIds" value="" id="regionIds">
                    </div>

                    <div class="form-actions">
                        <button id="add_view_user_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="userId" value="${userId!}">

<script type="text/javascript">
    $(function(){

        $("#regionTree").fancytree({
            extensions: ["filter"],
            source: {
                url: "loadregiontree.vpage?userId=" + $("#userId").val(),
                cache:true
            },
            checkbox: true,
            selectMode: 2
        });

        $('#add_view_user_btn').live('click',function(){
            var accountName = $('#account_name').val().trim();
            var realName = $('#real_name').val().trim();
            var userComment = $('#user_comment').val().trim();
            var password ="";
            var rePassword = "";
            if($('#userId').val() == ''){
                password = $('#password').val().trim();
                rePassword = $('#re_password').val().trim();
            }
            var tel = $('#tel').val().trim();
            var email = $('#email').val().trim();
            var imAccount = $('#imAccount').val().trim();
            var address = $('#address').val().trim();

            if(!checkAddViewUser(accountName,realName,password,rePassword)){
                return false;
            }

            var regions = new Array();
            var regionTree = $("#regionTree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();

            $.map(regionNodes, function(node){
                regions.push(node.key);
            });

            if(regions.length == 0) {
                alert("请选择协作区域!");
                return false;
            }

            $.post('addviewuser.vpage',{
                accountName : accountName,
                realName : realName,
                userComment : userComment,
                password : password,
                tel : tel,
                email : email,
                imAccount : imAccount,
                address : address,
                regions:regions,
                userId: $('#userId').val()
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });
    });

    function checkAddViewUser(accountName, realName, password, rePassword){
        if(accountName.trim() == ''){
            alert("请输入用户名!");
            return false;
        }
        if(realName.trim() == ''){
            alert("请输入真实姓名!");
            return false;
        }

        if ($('#userId').val() == '') {
            if(password.trim() == ''){
                alert("请输入密码!");
                return false;
            }
            if(password.trim() != rePassword.trim()){
                alert("两次输入的密码不匹配！");
                return false;
            }
        }

        return true;
    }

</script>

</@layout_default.page>