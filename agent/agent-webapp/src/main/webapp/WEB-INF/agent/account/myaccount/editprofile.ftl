<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='编辑个人信息' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 编辑个人信息</h2>
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
                            <input id="account_name" class="input-xlarge focused" type="text" value="<#if user??>${user.accountName!}</#if>" disabled>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">真实姓名</label>
                        <div class="controls">
                            <input id="real_name" class="input-xlarge focused" type="text" value="<#if user??>${user.realName!}</#if>" disabled>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户简介</label>
                        <div class="controls">
                            <textarea id="user_comment" class="input-xlarge focused"><#if user??>${user.userComment!}</#if></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户电话</label>
                        <div class="controls">
                            <input id="tel" class="input-xlarge focused" type="text" value="<#if user??>${user.tel!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户邮箱</label>
                        <div class="controls">
                            <input id="email" class="input-xlarge focused" type="text" value="<#if user??>${user.email!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户QQ</label>
                        <div class="controls">
                            <input id="imAccount" class="input-xlarge focused" type="text" value="<#if user??>${user.imAccount!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户地址</label>
                        <div class="controls">
                            <input id="address" class="input-xlarge focused" type="text" value="<#if user??>${user.address!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户行名称</label>
                        <div class="controls">
                            <input id="bank_name" class="input-xlarge focused" type="text" value="<#if user??>${user.bankName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户人姓名</label>
                        <div class="controls">
                            <input id="bank_hostname" class="input-xlarge focused" type="text" value="<#if user??>${user.bankHostName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">银行帐号</label>
                        <div class="controls">
                            <input id="bank_account" class="input-xlarge focused" type="text" value="<#if user??>${user.bankAccount!}</#if>">
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="add_sys_user_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="userId" value="${userId!}">
<input type="hidden" id="schoolMap" value='${schoolMap!}'>
<input type="hidden" id="affiliationSchools" value='${affiliationSchools!}'>

<script type="text/javascript">
    $(function(){

        $('#add_sys_user_btn').live('click',function(){
            var userComment = $('#user_comment').val().trim();

            var tel = $('#tel').val().trim();
            var email = $('#email').val().trim();
            var imAccount = $('#imAccount').val().trim();
            var address = $('#address').val().trim();

            var bankName = $('#bank_name').val().trim();
            var bankHostname = $('#bank_hostname').val().trim();
            var bankAccount = $('#bank_account').val().trim();

            $.post('editprofile.vpage',{
                userComment : userComment,
                tel : tel,
                email : email,
                imAccount : imAccount,
                address : address,
                bankName : bankName,
                bankHostname : bankHostname,
                bankAccount : bankAccount,
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

    });
</script>

</@layout_default.page>