/*
 * 账号登录
 * 登录和绑定孩子。
 */
define(['jquery','$17'], function ($,$17) {
    var $loginBtn = $('#btn_submit'),token = $('#token'), pwd = $('#pwd'),woid = $('#woid');
    var source = $('#source').val();
    var studentId = $('#studentId').val();
    if(source == "ucenter"){
        $("#btn_submit").text('绑定孩子');
        $(".js-parentMobileLogin").hide();
    }
    $loginBtn.on('click', function () {
        if($17.isBlank(token.val())){
            $17.msgTip("账号不能为空");
            return false;
        }
        if($17.isBlank(pwd.val())){
            $17.msgTip("密码不能为空");
            return false;
        }
        if ($loginBtn.hasClass('btn_disable')) return false;
        $loginBtn.addClass('btn_disable').text('数据提交中');
        var postUrl = '', returnUrl = '',postData = {};
        if(source == 'signUp'){
            postUrl = '/signup/parent/login.vpage';
            returnUrl = '/signup/parent/selectparent.vpage';
            postData.j_username = token.val();
            postData.j_password = pwd.val();
            postData.j_woid = woid.val();
        }else if(source == 'ucenter'){
            postUrl = '/parent/ucenter/bindchild.vpage';
            returnUrl = '/parent/ucenter/selectparent.vpage';
            postData.sid = token.val();
            postData.pwd = pwd.val();
        }else{
            postUrl = '/parent/ucenter/bindchild.vpage';
            returnUrl = '/parent/ucenter/selectparent.vpage';
            postData.sid = token.val();
            postData.pwd = pwd.val();
        }
        $.post(postUrl, postData,function(data){
            if(data.success){
                location.href = returnUrl;
            }else{
                $17.msgTip(data.info);
                $17.tongji('家长登录','登录失败',data.info);
            }
            $loginBtn.removeClass('btn_disable').text('登 录');
        }).fail(function(){
            $17.msgTip('登录失败');
            $loginBtn.removeClass('btn_disable').text('登 录');
        });
    });

    //-----------------第二步 选择家长角色-----------------
    var roleListBox = $('#roleListBox'),roleSubmitBtn = $('#roleSubmitBtn'),otherRoleListBox = $('#otherRoleListBox'),
        callNameCode = '';

    //选择角色
    roleListBox.find('a').on('click',function(){
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');
        callNameCode = $this.data('value');
        if(callNameCode == -1){
            otherRoleListBox.show();
            callNameCode = otherRoleListBox.find('select option:selected').val();
        }else{
            otherRoleListBox.hide();
        }

    });

    otherRoleListBox.find('select').on('change',function(){
        callNameCode = $(this).val();
    });

    roleSubmitBtn.on('click',function(){
        if($17.isBlank(callNameCode)){
            $17.msgTip("请选择角色");
            return false;
        }

        if(source == 'signUp'){
            if (roleSubmitBtn.hasClass('btn_disable')) return false;
            roleSubmitBtn.addClass('btn_disable').text('数据提交中');
            $.post('/signup/parent/callname.vpage',{callNameCode : callNameCode},function(data){
                if(data.success){
                    location.href = '/signup/parent/verify.vpage?callNameCode='+callNameCode;
                }else{
                    $17.msgTip(data.info);
                }
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            }).fail(function(){
                $17.msgTip('提交失败');
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            });
        }else if(source == 'ucenter'){
            if (roleSubmitBtn.hasClass('btn_disable')) return false;
            roleSubmitBtn.addClass('btn_disable').text('数据提交中');
            $.post('/parent/ucenter/selectparent.vpage',{callNameCode : callNameCode},function(data){
                if(data.success){
                    location.href = '/parent/ucenter/childreninfo.vpage';
                }else{
                    $17.msgTip(data.info);
                }
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            }).fail(function(){
                $17.msgTip('提交失败');
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            });
        }else if(source == 'validate'){
            if (roleSubmitBtn.hasClass('btn_disable')) return false;
            roleSubmitBtn.addClass('btn_disable').text('数据提交中');
            $.post('/parent/ucenter/selectparentwithstudentid.vpage',{callNameCode : callNameCode, studentId : studentId},function(data){
                if(data.success){
                    location.href = '/parent/ucenter/index.vpage';
                }else{
                    $17.msgTip(data.info);
                }
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            }).fail(function(){
                $17.msgTip('提交失败');
                roleSubmitBtn.removeClass('btn_disable').text('确 定');
            });
        }else{
            location.href = '/parent/ucenter/verify.vpage?callNameCode='+callNameCode;
        }
    });

    var mobileLoginBtn = $("#mobileLoginBtn");
    //家长手机登录
    mobileLoginBtn.on("click",function(){
        location.href = "/signup/parent/verifiedlogin.vpage?returnUrl=/parent/homework/index.vpage";
    })
});