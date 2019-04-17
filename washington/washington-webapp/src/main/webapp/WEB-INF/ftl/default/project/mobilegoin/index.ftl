<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起教育科技_让学习成为美好体验"
pageJs=["jquery","voxLogs"]
pageCssFile={"index" : ["public/skin/project/mobilegoin/css/index"]}
keywords="17作业，作业，一起作业下载，一起作业学生，学生APP，学生端下载，在线教育平台"
description="一起作业是一款免费学习工具，是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。"
>
<div class="official-web">
    <div class="logo">让学习成为美好体验</div>
    <div class="entrance">
        <a href="javascript:;" class="box clearfix none">
            <#--<div class="head"><img src="<@app.link href="public/skin/project/mobilegoin/images/icon-01.png"/>"></div>-->
            <div class="name">学生端</div>
            <div class="text">贴合教材+跟读打分</div>
            <#--<em class="icon"></em>-->
        </a>
        <div  class="box app-box clearfix">
            <a class="lf student_box" href="javascript:;">
                <div class="img head"><img src="<@app.link href="public/skin/project/mobilegoin/images/icon-01.png"/>"></div>
                <span>小学生</span>
            </a>
            <a class="rt" href="https://www.17zuoye.com/view/mobile/common/download?app_type=17juniorstudent">
                <div class="img head"><img src="<@app.link href="public/skin/project/mobilegoin/images/student.png"/>"></div>
                <span>中学生</span>
            </a>
        </div>
        <a href="javascript:;" class="box clearfix  none">
            <#--<div class="head"><img src="<@app.link href="public/skin/project/mobilegoin/images/icon-03-new.png"/>"></div>-->
            <div class="name">教师端</div>
            <div class="text">海量题库+自动批改</div>
            <#--<em class="icon"></em>-->
        </a>
        <div  class="box app-box clearfix">
            <a class="lf teacher_box" href="javascript:;">
                <div class="img head"><img src="<@app.link href="public/skin/project/mobilegoin/images/icon-03-new.png"/>"></div>
                <span>小学老师</span>
            </a>
            <a class="rt" href="https://www.17zuoye.com/view/mobile/common/download?app_type=17juniorteacher">
                <div class="img head"><img src="<@app.link href="public/skin/project/mobilegoin/images/teacher.png"/>"></div>
                <span>中学老师</span>
            </a>
        </div>
        <a href="javascript:;" class="box clearfix jzt_box">
            <div class="head"><img src="<@app.link href="public/skin/project/mobilegoin/images/icon-parents.png"/>"></div>
            <div class="name">一起学(原家长通)</div>
            <div class="text">教育资讯+自学工具</div>
            <em class="icon"></em>
        </a>
    </div>
    <div class="pc-enter" style="display:none;"><a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=back">访问电脑版</a></div>
</div>
<script type="text/javascript">
    signRunScript = function () {
        function getQuery(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        }

        var student_cid = getQuery('scid'),
            teacher_cid = getQuery('tcid'),
            jzt_cid = getQuery('pcid');

        if (!student_cid) {
            if(getQuery('referer') === 'wechat'){
                student_cid = '102024';
            } else {
                $('.pc-enter').show();
                student_cid = '102003';
            }
        }

        if (!teacher_cid) {
            if(getQuery('referer') === 'wechat'){
                teacher_cid = '302013';
            } else {
                teacher_cid = '302003';
            }
        }

        if (!jzt_cid) {
            if(getQuery('referer') === 'wechat'){
                jzt_cid = '202024';
            } else {
                jzt_cid = '202003';
            }
        }

        $(".student_box").attr('href',"https://www.17zuoye.com/view/mobile/common/download?app_type=17student&cid="+student_cid);
        $(".jzt_box").attr('href',"https://www.17zuoye.com/view/mobile/common/download?app_type=17parent&cid="+jzt_cid);
        $(".teacher_box").attr('href',"https://www.17zuoye.com/view/mobile/common/download?app_type=17teacher&cid="+teacher_cid);

    };

</script>
</@layout.page>