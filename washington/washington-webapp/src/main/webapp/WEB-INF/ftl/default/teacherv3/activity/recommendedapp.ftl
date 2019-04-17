<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="hide">
    <@sugar.capsule js=["ZeroClipboard"] css=[] />
    <@app.css href="public/skin/project/recommendedapp/css/skin.css" />
    <div class="tea-stuapp">
        <div class="ts-main">
            <a href="/" class="return-home" style="left: -80px; width: 130px; height: 50px; top: 0; text-indent: -150px;">返回首页</a>
            <a href="/" class="return-home">返回首页</a>
            <div class="main-left">
                <p class="tips">推荐老师：<span class="ft-red">通过校讯通、飞信、QQ家长群、微信群</span>等，告知家长使用说明</p>
                <div class="copy-text">
                    <textarea readonly="readonly" id="copy_info_url">家长你好！我在“一起作业”布置了作业，请家长帮孩子注册，保证孩子按时完成作业。可以用手机直接下载app并注册，在手机上完成作业。有电脑账号的在手机上输入账号密码即可登录。注册时请填写我的号码：${(currentUser.id)!}。
下载地址：www.17zyw.cn/ZRnAb2
（<#if (currentTeacherDetail.subject == "ENGLISH")!false>英语</#if><#if (currentTeacherDetail.subject == "MATH")!false>数学</#if><#if (currentTeacherDetail.subject == "CHINESE")!false>语文</#if>老师：${(currentUser.profile.realname)!}）</textarea>
                </div>
                <a class="copy-btn btn"  href="javascript:void(0);" id="clip_container" style="position: relative; "><span id="clip_button">复制上面内容</span></a>
            </div>
            <div class="main-right">
                <h4>老师可以用手机扫描二维码<br/>下载体验一下学生端</h4>
                <img src="<@app.link href="public/skin/project/recommendedapp/images/app-code.jpg"/>">
                <p>手机作业，告别电脑限制<br/>英语课文，手机随身听<br/>录音识别，更准更清晰</p>
                <a href="http://wx.17zuoye.com/download/17studentapp?cid=102006" target="_blank" class="c-load btn">点击下载</a>
            </div>
            <div style="clear: both;"></div>
        </div>
        <div style="clear: both;"></div>
    </div>
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"), "clip_button", "clip_container", function(){
            $17.voxLog({
                app : "shares",
                module : "teacherSharesRegCourse",
                op : "pc-activityPage-copyLink"
            });
        });
    });
</script>
</@temp.page>