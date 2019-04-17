<#import "../researchstaffv3.ftl" as com>
<@com.page menuType="normal">
<style type="text/css" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    dd span{
        font-size: 12px;
        color: #F06D65;
    }
</style>
<ul class="breadcrumb_vox">
    <li><a href="/rstaff/center/index.vpage">个人信息</a> <span class="divider">/</span></li>
    <li class="active">修改密码</li>
</ul>
<div id="edit_user_info" class="testpaperBox">
    <div class="sAvatar row_vox_left text_center" style="width: 120px; padding-top: 30px;"></div>
    <div class="row_vox_left" style="width: 460px;">
        <dl class="horizontal_vox">
            <dt class="text_big">当前登录密码：</dt>
            <dd>
                <input id="current_pwd" type="password" placeholder="当前登录密码" class="require int_vox"/><br>
                <span></span>
            </dd>

            <dt class="text_big">新的登录密码：</dt>
            <dd>
                <input id="new_pwd" type="password" placeholder="新的登录密码" class="require int_vox"/><br>
                <span></span>
            </dd>
            <dt class="text_big">确认新的登录密码：</dt>
            <dd>
                <input id="new_pwd_a" type="password" placeholder="确认新的登录密码" class="require int_vox"/> <br>
                <span></span>
            </dd>
            <dd>
                <a href="/rstaff/center/index.vpage" class="btn_vox"><strong>取消</strong></a>
                <a id="reset_pwd_but" class="btn_vox btn_vox_primary " href="javascript:void(0);">
                    <strong>提交</strong>
                </a>
            </dd>
        </dl>
    </div>
    <div class="clear"></div>
</div>
<script type="text/javascript">
    $(function(){
        $("#reset_pwd_but").on('click', function(){
            var currentPwd = $("#current_pwd");
            var newPwd = $("#new_pwd");
            var newPwdA = $("#new_pwd_a");

            if($17.isBlank(currentPwd.val())){
                $17.alert("请填写当前登录密码");
                return false;
            }

            if(newPwd.val().length < 6){
                $17.alert("密码不能少于6位");
                return false;
            }

            if(newPwd.val() == "123456" || newPwd.val() == "111111"){
                $17.alert("您设置的新密码过于简单");
                return false;
            }

            if($17.isBlank(newPwdA.val())){
                $17.alert("请填写确认新的登录密码");
                return false;
            }

            if(newPwd.val() != newPwdA.val()){
                $17.alert("两次输入的密码不相同");
                return false;
            }

            var data = {current_password : currentPwd.val(), new_password : newPwd.val()};
            $.post('/ucenter/resetmypw.vpage',data)
                    .done(function(data){
                        if(data.success){
                            $17.alert("密码修改成功",function(){
                                setTimeout(function(){location.href = '/rstaff/center/index.vpage'},200);
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    })
                    .fail(function(){
                        $17.alert('数据提交失败！');
                    });
        });
    });
</script>
</@com.page>