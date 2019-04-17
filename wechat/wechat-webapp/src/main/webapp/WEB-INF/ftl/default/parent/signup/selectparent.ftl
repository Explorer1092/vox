<#import "../layout.ftl" as login>
<@login.page title='登录' pageJs="login">
<style>
    html, body{background-color: #55abff;}
    .bindAccount-title { color: #fff; font-size: 0.875rem; line-height: 1.125rem; font-weight: 700; }
    .bindAccount-switch { padding: 1.25rem 0 1rem; text-align: center; }
    .bindAccount-switch > a,.bindAccount-switch > div { position: relative; display: inline-block; vertical-align: top; margin: 0 0.5rem; width: 24%; text-align: center; color: #fff; font-size: 1rem; line-height: 2.125rem; border-radius: 0.25rem; background-color: #a2cffa; cursor: pointer; }
    .bindAccount-switch > a.active,.bindAccount-switch > div.active { background-color: #2a91f0; }
    .bindAccount-switch .ico { position: absolute; top: -0.3125rem; right: -0.3125rem; width: 0.75rem; height: 0.75rem; background: url(/public/images/parent/bindAccount-ico.png) no-repeat; background-size: 100% 100%; display: none; }
    .bindAccount-switch > a.active .ico,.bindAccount-switch > div.active .ico { display: block; }
    .bindAccount-select { display: block; width: 100%; height: 2.125rem; border: 0 none; border-radius: 0.25rem; color: #7b8188; font-size: 0.6875rem; }
</style>
<#--
爸爸(1, "爸爸"),
妈妈(2, "妈妈"),
爷爷(3, "爷爷"),
奶奶(4, "奶奶"),
姥爷(5, "姥爷"),
姥姥(6, "姥姥"),
其它(7,"其它");
-->

<div class="main body_background">
    <h1 class="logo"></h1>
    <div class="form_main">
        <div class="bindAccount-title">您是${name!''}的：</div>
        <div class="bindAccount-switch" id="roleListBox">
            <a href="javascript:void (0);" data-value="1"><em class="ico"></em>爸爸</a>
            <a href="javascript:void(0);" data-value="2"><em class="ico"></em>妈妈</a>
            <a href="javascript:void(0);" data-value="-1"><em class="ico"></em>其他</a>
        </div>
        <div id="otherRoleListBox" class="bindAccount-content" style="display:none;">
            <select name="" id="" class="bindAccount-select">
                <option value="3">爷爷</option>
                <option value="4">奶奶</option>
                <option value="5">姥爷</option>
                <option value="6">姥姥</option>
                <option value="7">其他监护人</option>
            </select>
        </div>

        <div class="submit_box">
            <input type="hidden" id="source" value="${source!}"/>
            <input type="hidden" id="studentId" value="${studentId!}"/>
            <a id="roleSubmitBtn" href="javascript:void(0);" class="btn_mark btn_mark_block">确定</a>
        </div>
    </div>
</div>
</@login.page>