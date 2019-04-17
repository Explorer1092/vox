<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="减负送园丁豆">
<@sugar.capsule js=["ZeroClipboard"] css=[] />
<@app.css href="public/skin/project/inviteparent/skin.css" />
<div class="explain_main_box">
    <div class="eb_main_box">
        <#if currentUser?? && currentUser.userType == 1>
            <div class="info_box">
                <p class="title">家长绑定只要一“点”：</p>
                <p><span class="num">第一步</span>点击右侧按钮复制</p>
                <div class="line_box line_box_gray">
                    <textarea id="copy_info_url" readonly="readonly" disabled="disabled" style="height: 150px;">
                        <#if (currentTeacherDetail.subject == "ENGLISH")!false>英语</#if><#if (currentTeacherDetail.subject == "MATH")!false>数学</#if>${(currentUser.profile.realname)!}老师邀请你关注一起作业微信公众号：一起作业家长通。免费接收老师通知、作业消息，还可以为班级赠送用于奖励学生的学豆，请点击此链接，打开微信扫描二维码。www.17zuoye.com/project/inviteparent/xxtjzt.vpage
                    </textarea>
                </div>
                <p><span class="num">第二步</span>粘贴到家长QQ群、微信群里，发送即可</p>
            </div>
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"), "clip_button", "clip_container");
    });
</script>
</@temp.page>