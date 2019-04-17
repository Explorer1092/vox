<#import "../layout.ftl" as sigup>
<@sigup.page title="登录" pageJs="teacherLogin">
<style>
    html, body, h1, h2, h3, h4, h5, h6, ul, li, p, input, dl, dt, dd{padding: 0; margin: 0;}
    html, body{ width: 100%; }
    body{ height: 100%;}
    html, body, input{font: 28px/180% "微软雅黑", regular; color: #464646; }
    ul, li{ list-style: none; }
    a, input{ outline: none;}
    a{ text-decoration: none; color: #6691fe; }

    /*.logo*/
    h1.logo{height: 90px; margin: 100px 0 45px;}
    /*main*/
    .main { width: 100%; margin: 0 auto;  overflow: hidden; height: 100%; }
    .body_background {background-color: #55abff;}
    .body_background_gray .msg{font-size: 20px;color:#fff;background-color: #ffae57;height:45px;line-height: 45px;padding:0 45px; text-align: center;}
    /*form_main*/
    .form_main { margin: 0 32px;}
    .form_main h2 { font-size: 30px; font-weight: normal; margin-bottom: 40px; clear: both; color: #fff; text-align: center;}
    .form_main ul.fm_box{border-radius: 6px; background-color: #fff; overflow: hidden;}
    .form_main li {  clear: both; overflow: hidden; border-bottom: 1px solid #e5e5e5; position: relative;}
    .form_main li input{ padding: 17px 0; width: 99%; text-indent: 12px; display: block; background-color: #fff; border: 0; }
    .form_main li .info span{ display: inline-block; vertical-align: middle;}
    .form_main .password_log span{ float: left; color: #dd2824;}
    .form_main .password_log a{ float: right; color: #fff; }
    .form_main .submit_box{ padding: 50px 0; clear: both; }
    .btn_mark{ background-color: #359bff; color: #fff; border-radius: 6px; cursor: pointer; display: inline-block; padding: 8px 10px; text-align: center; text-decoration: none; font-size: 28px;}
    .btn_mark:hover{ background-color: #43a2ff;}
    .btn_mark:active{background-color: #0b86ff;}
    .btn_mark_block{ display: block;}
    input.btn_mark_block{ width: 100%; padding-left: 0; padding-right: 0; text-align: center;}
</style>

<div class="main body_background">
    <h1 class="logo"></h1>
    <div class="form_main">
        <ul class="fm_box">
            <li>
                <input id="tid" name="token" type="tel" value="" placeholder="老师账号"/>
            </li>
            <li>
                <input id="pwd" name="pwd" type="password" value="" placeholder="请输入密码"/>
            </li>
        </ul>
        <div class="submit_box">
            <a id="teacher_login_submit_btn" href="javascript:void(0);" class="btn_mark btn_mark_block">登录</a>
        </div>

    </div>
</div>

</@sigup.page>

