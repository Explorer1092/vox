<#import "../module.ftl" as com>
<@com.page>
<h4>
    找回密码
</h4>
<#--<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li><s></s><i>2</i><b>验证信息</b></li>
    <li><s></s><i>3</i><b>重置密码</b></li>
    <li><s></s><i>4</i><b>成功</b></li>
</ul>-->
<div style="height: 80px;"> </div>
<ul id="inId" class="formList">
    <li id="_tipssLi" class="inp">
        <b class="tit" style="width: 215px;">一起作业学号、绑定的手机</b><input id="17number" name="" type="text" value="">
        <a href="/ucenter/forgotaccount.vpage" class="w-btn w-btn-light w-btn-mini" >已注册忘记账号</a>
        <a href="/index.vpage?ref=register" class="w-btn w-btn-light w-btn-mini" >未注册</a>
    </li>
    <li class="inp">
        <b class="tit" style="width: 215px;">请输入验证码</b><input id="captchaCode" type="text"  value="" style="width:80px" >
        &nbsp;
        <img id='captchaImage' onclick="refreshCaptcha()"/>&nbsp;
        看不清？<a href="javascript:refreshCaptcha();" class="clrblue">换一个</a>
    </li>
    <li class="btn" style="margin: 0 70px 0 0;">
        <#--<a href="/ucenter/resetnavigation.vpage" class="w-btn w-btn-small w-btn-green">返回</a>-->
        <a href="javascript:next();" class="w-btn w-btn-small">下一步</a>
    </li>
</ul>
<div style="clear: both; height: 40px;"></div>

<script type="text/javascript">
    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha?" + $.param({
            'module': 'resetpwd',
            'token': '${captchaToken}',
            't': new Date().getTime()
        }));
    }

    function next(userType){
        $.post('/ucenter/resetpwdstart.vpage', {'account': $("#17number").val(), 'captchaToken': '${captchaToken}', 'captchaCode': $('#captchaCode').val() }, function( data ) {
            if(data.success) {
                if(!$17.isBlank(data.needSelect) && data.needSelect){
                    var users = data.users;
                    var alertWidth = users.length * 250 + (users.length - 1) * 10 + 50;
                    $.prompt($('[data-template="T:选择身份"]').html(), {
                        title: "选择身份",
                        buttons: {},
                        position: {width: alertWidth},
                        loaded: function () {
                            // 循环控制种类显示，1是老师，8是教务老师，3是学生
                            for (var i = 0, len = users.length; i < len; i++) {
                                if (users[i] === 1) {
                                    $('.JS-mainNav').eq(1).find('li').eq(0).css('display', 'inline-block');
                                } else if (users[i] === 8) {
                                    $('.JS-mainNav').eq(1).find('li').eq(1).css('display', 'inline-block');
                                } else if (users[i] === 3) {
                                    $('.JS-mainNav').eq(1).find('li').eq(2).css('display', 'inline-block');
                                }
                            }
                        },
                        close : function(){
                            refreshCaptcha();
                        }
                    });
                    return false;
                }

                location.href = "resetpwdstep.vpage?" + $.param({
                    'step': 'step2', 'token': data.token
                });
            }else{
                $17.alert(data.info ? data.info : "没有找到相关用户信息", function(){
                    refreshCaptcha();
				});
            }
        });
    }

    function selectCharacter(type){
        $.post('/ucenter/resetpwdstart.vpage', {
            account : $("#17number").val(),
            captchaToken: '${captchaToken}',
            captchaCode: $('#captchaCode').val(),
            userType :  type
        }, function( data ) {
            if(data.success){
                location.href = "resetpwdstep.vpage?" + $.param({
                    'step': 'step2', 'token': data.token
                });
            }else{
                $17.alert(data.info ? data.info : "没有找到相关用户信息", function(){
                    refreshCaptcha();
                });
            }
        });
    }

    refreshCaptcha();

    setModeHeight("#signup_form_box", 700, 267);
    $(window).resize(function () {
        setModeHeight("#signup_form_box", 700, 267);
    });
    function setModeHeight(id, h ,n) {
        var _winHeight = $(window).height();
        var _defHeight = h || 700;
        var _defLessCount = n || 0;

        if(_winHeight <= _defHeight){
            $(id).height(_defHeight - _defLessCount);
        }else{
            $(id).height( _winHeight - _defLessCount);
        }
    }
</script>

<#--此处有坑，使用$.porompt将此模板显示时，会造成dom中存在两份，使用选择器时需注意-->
<#--过来改个功能，怕牵一发而动全身，so 懒得改了-->
<div data-template="T:选择身份" style="display: none;">
    <div class="register_box chooseNav" style="width: auto;">
        <div class="reg_type" style="margin: 0;">
            <ul class="mainNav JS-mainNav">
                <#--此处根据后端返回的来进行判断，1是老师，3是学生，8是教务老师-->
                <li class="teacher" style="float:none; display: none;"><a href="javascript:selectCharacter(1);" class="mytc" title="老师"><strong>我是<span>老师</span></strong>请选择这里</a></li>
                <li class="educationteacher" style="margin: 0 0 0 5px; display: none;"><a href="javascript:selectCharacter(8);" class="mypt " title="学生"><strong>我是<span>教务老师</span></strong>请选择这里</a></li>
                <li class="student" style="margin: 0 0 0 5px; display: none;"><a href="javascript:selectCharacter(3);" class="mypt " title="学生"><strong>我是<span>学生</span></strong>请选择这里</a></li>
            </ul>
        </div>
    </div>
</div>
<#--安全联盟代码添加 Task #32243-->
<div style="overflow: hidden; width: 0;height: 0;">
    <a key="57d13eecefbfb021949e82f3" logo_size="83x30" logo_type="realname" href="http://www.anquan.org">
        <script src="//static.anquan.org/static/outer/js/aq_auth.js"></script>
    </a>
</div>
</@com.page>