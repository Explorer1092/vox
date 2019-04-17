<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page footerIndex=4 title="修改密码">
    <@sugar.capsule css=['team']/>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="c-head fixed-head return_show_false" style="display: none;">
        <span>修改密码</span>
    </div>
    <ul class="mobileCRM-V2-list">
        <li>
            <input type="password" placeholder="请输入原密码" style="font: 0.8rem/1.2rem 'Microsoft YaHei';" id="oldpass">
        </li>
        <li>
            <input type="password" placeholder="请输入新密码" style="font: 0.8rem/1.2rem 'Microsoft YaHei';" id="newpass1">
        </li>
        <li>
            <input type="password" placeholder="重复新密码" style="font: 0.8rem/1.2rem 'Microsoft YaHei';" id="newpass2">
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-submit" style="font-size:12px;">
    <input type="submit" style="font: 1rem/2rem 'Microsoft YaHei';background: #ff7d5a;" value="提&nbsp;交">
</div>

<script type="text/javascript">
    $(document).ready(function () {
        var return_show = getUrlParam("return_show") || false;
        var setTopBar = {
            show : return_show
        };
        setTopBarFn(setTopBar);
        if(return_show){
            $(".return_show_false").hide();
        }else{
            $(".return_show_false").show();
        }
    });
    $(function () {
        $('input[type="submit"]').on('click', function () {
            var oldpass = $('#oldpass').val().trim();
            var newpass1 = $('#newpass1').val().trim();
            var newpass2 = $('#newpass2').val().trim();
            if (!check(oldpass, newpass1, newpass2)) {
                return false;
            }
            $.post('resetPassword.vpage', {
                password: oldpass,
                newPassword: newpass1
            }, function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    alert("修改成功！请重新登录！");
//                    $(window.location).attr('href', '/auth/logout.vpage?client=h5');
                    do_external('innerJump',JSON.stringify({name:'go_login'}));
                }
            });
        });
    });
    function check(oldpass, newpass1, newpass2) {
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
    $(".headerBack").click(function () {
        window.location.href = "/mobile/work_record/setting.vpage";
    });
</script>
</@layout.page>