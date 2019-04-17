<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="减负送园丁豆">
<@sugar.capsule js=["ZeroClipboard"] css=[] />
<@app.css href="public/skin/project/inviteparent/skin.css" />
<div class="explain_main_box">
    <div class="title_tip_box"></div>
    <div class="eb_main_box">
        <div class="line_box">
            <ul>
                <li>
                    <h2>为什么能减负：</h2>
                </li>
                <li>
                    <h2>园丁豆怎么送：</h2>
                </li>
                <li>
                    <span>●</span>家长绑定微信就能减负
                </li>
                <li>
                    <span>●</span>任何一个班绑定微信≥<strong>8</strong>人，即可获得<strong>10</strong>园丁豆奖励
                </li>
                <li>
                    <span>●</span>布置了作业，及时微信通知家长
                </li>
                <li>
                    <span>●</span>有几个班级满足条件就奖励几次
                </li>
                <li>
                    <span>●</span>使用【智慧课堂】奖励学生，及时发给家长
                </li>
                <li>
                    <span>●</span>在【班级管理】查看学生绑定数量
                </li>
                <li>
                    <span>●</span>家长可以在微信送您学豆
                </li>
                <li>
                    <span>●</span>每天早上统一发前天的园丁豆，本活动上线时<br/><span>&nbsp;&nbsp;</span>已一次性发放符合条件的园丁豆
                </li>
            </ul>
        </div>
        <div class="info_box">
            <p class="title">家长绑定只要一“点”：</p>
            <p><span class="num">第一步</span>点击右侧按钮复制<a href="javascript:void (0)" id="clip_container"><span id="clip_button">复制链接地址</span></a></p>
            <div class="line_box line_box_gray">
                <textarea id="copy_info_url" readonly="readonly" disabled="disabled" >各位家长，请绑定微信：一起作业家长通。点击链接，打开微信扫码。http://www.17zuoye.com/project/inviteparent/jzt.vpage</textarea>
            </div>
            <p><span class="num">第二步</span>粘贴到家长QQ群、微信群里，发送即可</p>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"), "clip_button", "clip_container");
    });
</script>
</@temp.page>