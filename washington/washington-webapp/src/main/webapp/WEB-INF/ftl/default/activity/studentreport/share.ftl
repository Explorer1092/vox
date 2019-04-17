<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="2016年成长记录"
pageJs=['jquery', 'YQ', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/2016report/student/css/skin"]}
>

<div class="growthRecord-box">
    <img src="<@app.link href="public/skin/project/2016report/student/images/growthrecord/bg_12.jpg"/>">
    <div class="grd-info topInfo">
        <div class="gImage"><img src="" id="avatarImg"/></div>
        <p>本学期，<span class="num JS-userName">---</span> 同学的平均正确率</p>
        <p>超过全国<span class="num JS-ranking">---</span>％的同年级同学，</p>
        <p>获得一起作业2016年<span class="num JS-aName">---</span>称号</p>
    </div>
    <div class="grd-content topContent JS-aName">---</div>
    <div class="grd-footer">
        <a href="http://wx.17zuoye.com/download/17studentapp?cid=103001" id="studentDown" class="receive_btn">查看你的2016成长记录</a>
        <a href="http://wx.17zuoye.com/download/17parentapp?cid=203010" id="parentDown" style="display: none;" class="receive_btn">查看你的2016成长记录</a>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function ($, YQ) {
        $('.JS-userName').text(YQ.getQuery('userName'));
        $('.JS-ranking').text(YQ.getQuery('ranking'));
        $('.JS-aName').text(YQ.getQuery('aName'));
        $('#avatarImg').attr('src', YQ.getQuery('avatarUrl'));

        if(YQ.getQuery('userType') == 2){
            $("#studentDown").hide();
            $("#parentDown").show();
        }

        if(typeof YQ.voxLogs == 'function'){
            YQ.voxLogs({
                module: 'annual_interest_report',
                op: 'enter_the_share_page_student'
            });
        }
    }
</script>
</@layout.page>