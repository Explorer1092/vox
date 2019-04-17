<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="绑定微信">
<@app.css href="public/skin/project/inviteparent/skin.css" />
<div class="explain_main_box">
    <div class="title_box">
        <i class="icon_phone"></i>
        <p>
            请各位家长绑定微信<br>
            一起作业家长通
        </p>
    </div>
    <div class="con_box">
        <p class="cb_title">如何绑定：</p>
        <div class="tip_box">
                 <span style="width: 215px;">
                    <i class="number">1</i>打开微信扫描二维码
                </span>
                <span style="width: 312px;">
                    <i class="number">2</i>点击一起作业家长通【最新作业】
                </span>
                <span>
                    <i class="number">3</i>根据提示输入孩子账号和密码
                </span>
        </div>
        <div class="cb_con">
            <div class="slide">
                <i class="weixin">
                    <img src="<@app.link href="/public/skin/project/inviteparent/weixin-xxt.jpg"/>" width="186"/>
                </i>
                <p>微信扫码，关注：一起作业家长通</p>
            </div>
            <div class="list">
                <p class="cb_title" style="padding: 0 0 0 40px;">绑定微信好处：</p>
                <ul>
                    <li>
                        <i class="number">1</i>接收老师通知
                    </li>
                    <li>
                        <i class="number">2</i>第一时间了解老师布置了作业
                    </li>
                    <li>
                        <i class="number">3</i>查看孩子作业成绩及对错
                    </li>
                    <li>
                        <i class="number">4</i>了解孩子课堂表现
                    </li>
                </ul>
                <p class="font">如有疑问，请联系客服 <@ftlmacro.hotline/></p>
            </div>
        </div>
    </div>
</div>
</@temp.page>