<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='修改密码'>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 修改密码</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form id="add_product_form" class="form-horizontal" method="post" action="addproduct.vpage" enctype="multipart/form-data" >
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">当前密码</label>
                        <div class="controls">
                            <input id="password" class="input-xlarge focused" type="password" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">新密码</label>
                        <div class="controls">
                            <input id="newPassword1" class="input-xlarge focused" type="password" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">确认新密码</label>
                        <div class="controls">
                            <input id="newPassword2" class="input-xlarge focused" type="password" value="">
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="add_reset_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="/index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<script type="text/javascript">
    $(function(){
        $('#add_reset_btn').live('click',function(){
            var password = $('#password').val().trim();
            var newPassword1 = $('#newPassword1').val().trim();
            var newPassword2 = $('#newPassword2').val().trim();

            if(!check(password,newPassword1,newPassword2)){
                return false;
            }
            $.post('resetPassword.vpage',{
                password:password,
                newPassword:newPassword1
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert("修改成功！请重新登录！");
                    $(window.location).attr('href', '/auth/logout.vpage');
                }
            });
        });

    });
    function check(password,newPassword1,newPassword2){
        if(password == ''){
            alert("请输入当前密码!");
            return false;
        }
        if(newPassword1 == ''){
            alert("请输入新密码!");
            return false;
        }
        if(newPassword2 == ''){
            alert("请输入确认新密码!");
            return false;
        }
        if(newPassword1 != newPassword2){
            alert("两次输入的密码不一致!");
            return false;
        }
        return true;
    }
</script>

</@layout_default.page>                                                   