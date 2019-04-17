<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="2016年教学手札"
pageJs=['jquery', 'YQ', 'voxLogs']
pageCssFile={"css" : ["public/skin/project/2016report/teacher/css/skin"]}
>

<div class="annualReport-box">
    <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_11_01.jpg"/>">
    <div class="anr-column colCenter">
        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/image.png"/>" id="avatarImg" class="colImages" style="border-radius: 100%;">
    </div>
    <div class="anr-info textLeft textTop">
        <div class="anr-title">
            <p>2016年，</p>
            <p><span class="num JS-userName">--</span>老师有 <span class="num JS-studentNum">--</span> 名学生完成作业，</p>
            <p>超过了全国 <span class="num JS-ranking">--</span>% 的老师</p>
            <p>获得一起作业2016年</p>
            <p><span class="num JS-aName">---</span> 荣誉称号。</p>
        </div>
    </div>
    <div class="anr-foot">
        <img src="<@app.link href="public/skin/project/2016report/teacher/images/annualreport/bg_11_02.jpg"/>">
        <a href="http://wx.17zuoye.com/download/17teacherapp?cid=303003" class="receive_btn view_btn share_btn"></a>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function ($, YQ) {
        $('.JS-userName').text(YQ.getQuery('userName') || '--');
        $('.JS-ranking').text(YQ.getQuery('ranking') || '--');
        $('.JS-aName').text(YQ.getQuery('aName') || '--');
        $('.JS-studentNum').text(YQ.getQuery('studentNum') || '--');

        if(YQ.getQuery('avatarUrl')){
            $('#avatarImg').attr('src', YQ.getQuery('avatarUrl'));
        }

        if(typeof YQ.voxLogs == 'function'){
            YQ.voxLogs({
                module: 'annual_interest_report',
                op: 'enter_the_share_page_teacher'
            });
        }
    }
</script>
</@layout.page>