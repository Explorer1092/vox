<#import "../layout.ftl" as resetstudentpwd>
<@resetstudentpwd.page title='重置孩子密码' pageJs="resetstudentpwd">
<@sugar.capsule css=['jbox'] />
<#include "../userpopup.ftl">
<div class="title_box">
    <ul style="width: auto">
        <li style="width: auto; max-width: 250px; font-size: 24px;">
            学号：<span style="width: auto; max-width: 160px; text-align: left; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle;" id="studentId" data-bind="text: sNumber"></span>
        </li>
        <li style="font-size: 24px; width: 200px;">
            学豆：<span id="integral" style="width: auto; max-width: 100px; text-align: left; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle;" data-bind="text: sBeans"></span>
        </li>
    </ul>
</div>
<div class="form_main">
    <p class="fix_box">孩子忘记密码了，重置一个新密码</p>
    <form id="form1" action="/ucenter/resetstudentpwd.vpage" method="post">
        <ul class="fm_box">
            <li>
                <input type="password" data-bind="value: newPwd" name="pwd" id="pwd" placeholder="请输入新密码" >
            </li>
            <li>
                <input type="password" data-bind="value: newPwdConfirm" name="confirmpwd" id="confirmpwd" placeholder="请再次输入新密码">
            </li>
        </ul>
        <a data-bind="click: changePwd" href="javascript:void(0);" class="btn_mark btn_mark_block"><span style="color: #F9F9F9;">提 交</span></a>
    </form>
</div>
<script>
    var students = ${json_encode(students)};
</script>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'ucenter_pv_resetpwd'
            })
        })
    }
</script>
</@resetstudentpwd.page>