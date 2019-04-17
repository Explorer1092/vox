<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='寒假学霸提升秘籍'
bodyClass="bgList"
pageJs=["jquery","YQ", "voxLogs"]
pageCssFile={"learnpa" : ["public/skin/mobile/student/app/activity/afenti/learnpa/css/skin"]}
>

<div class="xb-listBox">
    <a href="javascript:void(0)" class="xb-list doClickOpenGame" data-app_key="AfentiMath">
        <img src="<@app.link href="public/skin/mobile/student/app/activity/afenti/learnpa/images/xb-card01.png"/>">
        <div class="txt">
            <h2 class="shadow-blue">数学提升秘籍</h2>
            <p>专题训练，提升能力咫尺之间</p>
        </div>
    </a>
    <a href="javascript:void(0)" class="xb-list doClickOpenGame" data-app_key="AfentiExam">
        <img src="<@app.link href="public/skin/mobile/student/app/activity/afenti/learnpa/images/xb-card02.png"/>">
        <div class="txt">
            <h2 class="shadow-red">英语提升秘籍</h2>
            <p>循序渐进，让你快速平稳进步</p>
        </div>
    </a>
    <a href="javascript:void(0)" class="xb-list doClickOpenGame" data-app_key="AfentiChinese">
        <img src="<@app.link href="public/skin/mobile/student/app/activity/afenti/learnpa/images/xb-card03.png"/>">
        <div class="txt">
            <h2 class="shadow-yellow">语文提升秘籍</h2>
            <p>科学学习，下一个学霸就是你</p>
        </div>
    </a>
</div>

<script type="text/javascript">
    signRunScript = function ($,$17) {
        $(document).on("click", ".doClickOpenGame", function () {
            var $self = $(this);
            var app_key = $self.data('app_key');
            $17.openGameApp({
                appKey: app_key,
                fromFairyland : ($17.getQuery('from') && $17.getQuery('from') == 'fairyland')
            });

            YQ.voxLogs({
                module: "m_2xfmipII",
                op: "o_KJ6ynRi6",
                s0: app_key
            });
        });

        $17.updateNativeTitle("寒假学霸提升秘籍");

        YQ.voxLogs({
            module: "m_2xfmipII",
            op: "o_4q74snoz"
        });
    };
</script>

</@layout.page>