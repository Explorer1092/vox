<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page footerIndex=4>
<@sugar.capsule css=['team','skin']/>


<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <input type="password" placeholder="请输入原密码" id="oldpass">
        </li>
        <li>
            <input type="password" placeholder="请输入新密码" id="newpass1">
        </li>
        <li>
            <input type="password" placeholder="重复新密码" id="newpass2">
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-submit">
    <input type="submit" value="提&nbsp;交">
</div>

<script type="text/javascript">

    $(function(){
        $('input[type="submit"]').on('click',function(){
            var oldpass = $('#oldpass').val().trim();
            var newpass1 = $('#newpass1').val().trim();
            var newpass2 = $('#newpass2').val().trim();
            if(!check(oldpass,newpass1,newpass2)){
                return false;
            }
            $.post('resetPassword.vpage',{
                password:oldpass,
                newPassword:newpass1
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert("修改成功！请重新登录！");
                    $(window.location).attr('href', '/auth/logout.vpage?client=h5');
                }
            });
        });

    });
    function check(oldpass,newpass1,newpass2) {
        if (oldpass == '') {
            alert("请输入当前密码!");
            $("#oldpass").focus();
            return false;
        }
        if (newpass1 == '') {
            alert("请输入新密码!");
            $("#newpass1").focus();
            return false;
        }
        if (newpass2 === '') {
            alert("请重新输入新密码!");
            $("#newpass2").focus();
            return false;
        }
        if (newpass1 === oldpass) {
            alert("新密码与原密码相同，请重新输入！");
            $("#newpass1").focus();
            return false;
        }
        if (newpass1 !== newpass2) {
            alert("两次输入的新密码不一致，请重新输入！");
            $("#newpass2").focus();
            return false;
        }
        var reg = /^[0-9]*[A-Za-z]*[0-9]*$/;
        if (reg.test(newpass1) && newpass1.length >= 6) {
            return true;
        }
        else {
            alert("密码必须包含6位以上字母和数字");
            return false;
        }
        return true;
    }
</script>
</@layout.page>