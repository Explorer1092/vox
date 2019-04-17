<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='一起打年兽'
pageJs=["jquery", "voxLogs"]
pageCssFile={"dns" : ["public/skin/mobile/student/app/activity/dns/css/skin"]}

>
<div class="playTheBeast-box">
    <div class="ptb-tips">
        <h1>恭喜你</h1>
        <p>你已成功报名参加<span class="fontYellow">【打年兽小队】</span></p>
    </div>
    <div class="ptb-main">
        <div class="mIcon"></div>
        <div class="ptb-Inner">
            <div class="mTitle">你还成功获得了活动大礼包</div>
            <div class="mTable">
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td>阿分题系列</td>
                        <td rowspan="5"><span class="fontYellow">未开通产品<br>赠送试用期<br><i class="symbol">~~~~~~</i><br>已开通产品<br>鞭炮双倍</span></td>
                    </tr>
                    <tr>
                        <td>走遍美国学英语</td>
                    </tr>
                    <tr>
                        <td>速算脑力王</td>
                    </tr>
                    <tr>
                        <td>百科大挑战</td>
                    </tr>
                    <tr>
                        <td>语文同步练</td>
                    </tr>
                    <tr>
                        <td>活动抽奖机会</td>
                        <td><span class="fontYellow">1次</span></td>
                    </tr>
                </table>
            </div>
            <div class="mInfo fontYellow">活动开始后就可以使用啦</div>
        </div>
    </div>
    <div class="ptb-footer">
        <div class="ptb-arrow">1月16日用自己的知识保卫家园吧</div>
        <#--<a id="returnBtn" href="javascript:void(0);" class="green_btn">好的，我知道啦</a>-->
    </div>
</div>



<script type="text/javascript">
    signRunScript = function ($) {
        $("#returnBtn").on('click',function () {
            setTimeout(function () {
                location.href = '/studentMobile/activity/dns/index.vpage';
            },200);

            YQ.voxLogs({
                module: "m_cKj5BuEp",
                op: "o_Y2duYrb5"
            });
        });

        YQ.voxLogs({
            module: "m_cKj5BuEp",
            op: "o_bP5jBoHv"
        });
    };
</script>

</@layout.page>