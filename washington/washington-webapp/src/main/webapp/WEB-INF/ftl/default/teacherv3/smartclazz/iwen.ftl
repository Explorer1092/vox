<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page  showNav="hide">
<div class="m-container" style="width: 100%;">
</div>
<style>
    .pc-main{ width: 1000px; margin: 0 auto; background-color: #fff; overflow: hidden;}
    .pc-main .pc-inner{ width: 800px; margin: 0 auto;}
    .pc-main .pc-produce{ padding: 40px 0 0 0; height: 390px;}
    .pc-main .pc-produce .iphone{ background: url("<@app.link href="public/skin/project/iwen/images/iphone.png"/>") no-repeat 0 0; width: 366px; height: 375px; float: left;}
    .pc-main .pc-produce .font{ margin-left: 400px; color: #383a4b; width: 524px;}
    .pc-main .pc-produce .font h1{ font-size: 26px; margin: 30px 0 16px 0; font-weight: normal;}
    .pc-main .pc-produce .font p{ font-size: 16px; line-height: 26px;}
    .pc-main .pc-produce .font li{ padding: 10px 0;}
    .pc-main .pc-produce .font li span{ margin: 0 5px;}
    .pc-main .pc-btn{ padding-bottom: 45px; }
    .pc-main .pc-line{ height: 1px; width: 100%; background-color: #dfdfdf; font-size: 0;}
    .pc-main .pc-btn-icon{ background: url("<@app.link href="public/skin/project/iwen/images/pc-icon.png"/>") no-repeat 1000px 1000px; width: 250px; height: 80px; display: inline-block; margin-right: 20px;}
    .pc-main .pc-btn-icon-ios{ background-position: 0 0;}
    .pc-main .pc-btn-icon-ios:hover{ background-position: 0 -80px;}
    .pc-main .pc-btn-icon-android{ background-position: -274px 0;}
    .pc-main .pc-btn-icon-android:hover{ background-position: -274px -80px;}
    .pc-main .pc-btn-icon-card{ background-position: -548px 0;}
    .pc-main .pc-btn-icon-card:hover{ background-position: -548px -80px;}
    .pc-main .pc-option{ padding: 40px 0 48px 0;}
    .pc-main .pc-option .font{ float: left; font-size: 26px; margin-bottom: 38px;}
    .pc-main .pc-option .font h1{ font-size: 26px; padding-bottom: 30px; font-weight: normal;}
    .pc-main .pc-option ul li{ padding: 11px 0; font-size: 16px;}
    .pc-main .pc-option ul li strong{ width: 34px; height: 34px; background-color: #189cfb; display: inline-block; vertical-align: middle; border-radius: 20px; font:22px/34px "arial"; color: #fff; text-align: center; margin-right: 8px; }
    .pc-main .pc-option .bela{ background: url("<@app.link href="public/skin/project/iwen/images/bala.png"/>") no-repeat 0 0; width: 198px; height: 298px; margin-left: 595px;}
    .pc-main .pc-foot{ border: 1px solid #dfdfdf; padding: 0 32px; overflow: hidden; margin-bottom: 78px; *zoom: 1;}
    .pc-main .pc-foot h2{ font-size: 18px; color: #189cfb; padding: 20px 0; font-weight: normal;}
    .pc-main .pc-foot li{ width: 50%; font-size: 14px; list-style:square; float: left; padding: 12px 0;}
    /*new*/
    .pc-main .pc-sweep{ padding: 30px 0; overflow: hidden; *zoom: 1;}
    .pc-main .pc-sweep dl{ width: 50%; float: left;}
    .pc-main .pc-sweep dl dt{ width: 113px; height: 113px; padding: 4px; border: 1px solid #ccc; float: left;}
    .pc-main .pc-sweep dl .ps-img{ width: 114px; height: 114px; display: inline-block;}
    .pc-main .pc-sweep dl dd{ margin-left: 140px; font-size: 16px;padding-top: 35px; color: #383a4b;}
    .pc-main .pc-sweep dl dd p{ line-height: 22px;}
    .pc-main .pc-sweep dl dd p i.ws-icon{ background: url("<@app.link href="public/skin/project/iwen/images/icon.png"/>") no-repeat 0 0; width: 18px; height: 22px; vertical-align: middle; display: inline-block; margin:0 3px;}
    .pc-main .pc-sweep dl dd p i.ws-icon-2{ background-position: 0 -28px;}
</style>
<@sugar.capsule js=["jquery", "core"] />
<div class="pc-main">

    <div class="pc-produce">
        <div class="iphone"></div>
        <div class="font">
            <h1>产品简介</h1>

            <p>
                爱提问是一起作业为教师提供的用于快速收集学生课堂练习客观题答案的移动端产品，只需拿手机 / 平板电脑轻松扫描，即可获取学生答题情况分析，从而进行有针对性的讲解。
            </p>

            <h1 style="margin-top: 48px">产品特点</h1>
            <ul>
                <li><span>●</span>轻松、快速收集学生答题结果与数据分析图表。</li>
                <li><span>●</span>无需硬件，节省开支。</li>
                <li><span>●</span>iOS、Android两大手机系统全面覆盖。</li>
            </ul>
        </div>
    </div>
    <div class="pc-inner">
        <div class="pc-btn">
            <a class="pc-btn-icon pc-btn-icon-ios" href="https://itunes.apple.com/cn/app/yi-qi-zuo-ye-ai-ti-wen/id910850414?mt=8" target="_blank"></a>
            <a class="pc-btn-icon pc-btn-icon-android" href="//cdn.17zuoye.com/static/project/iwen/iquestion_1107.apk" target="_blank"></a>
            <a style="margin-right: 0" class="pc-btn-icon pc-btn-icon-card" href="//cdn.17zuoye.com/static/project/iwen/student_cards.pdf" target="_blank"></a>
        </div>
    </div>
    <div class="pc-line"></div>
    <!--new--start-->
    <div class="pc-inner">
        <div class="pc-sweep">
            <dl>
                <dt>
                    <span class="ps-img"><img src="<@app.link href="public/skin/project/iwen/images/ios-code.jpg"/>"/></span>
                </dt>
                <dd>
                    <p>
                        用手机扫描二维码<br>
                        快速下载爱提问<i class="ws-icon"></i><span class="w-blue">iOS</span>手机版
                    </p>
                </dd>
            </dl>
            <dl>
                <dt>
                    <span class="ps-img"><img src="<@app.link href="public/skin/project/iwen/images/android-code-1107.jpg"/>"/></span>
                </dt>
                <dd>
                    <p>
                        用手机扫描二维码<br>
                        快速下载爱提问<i class="ws-icon ws-icon-2"></i><span class="w-blue">Android</span>手机版
                    </p>
                </dd>
            </dl>
        </div>
    </div>
    <div class="pc-line"></div>
    <div class="pc-inner">
        <div class="pc-option">
            <div class="font">
                <h1>操作引导</h1>
                <ul>
                    <li><strong>1</strong>根据您的手机操作系统下载客户端并安装</li>
                    <li><strong>2</strong>下载学生卡片并打印，发放给学生</li>
                    <li><strong>3</strong>上课时开启客户端，学生将卡片答案朝上举起，扫描答案</li>
                    <li><strong>4</strong>点击页面下方重置按钮进行下一题答案的收集</li>
                </ul>
            </div>
            <div class="bela"></div>
        </div>
        <div class="pc-foot">
            <h2>| 注意事项</h2>
            <ul>
                <li>该产品仅适用于客观题答案的收集</li>
                <li class="w-ag-right">学生举起卡片时正向面对教师，不要角度太大</li>
                <li>学生举起卡片时可横向、纵向错开，避免互相遮挡</li>
                <li class="w-ag-right">学生举起卡片时手请注意不要接触二维码区域</li>
            </ul>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        //获取班级ID
        var clazzId = $17.getHashQuery("clazzId");
        var forwardUrl = "/teacher/smartclazz/list.vpage";
        if (!$17.isBlank(clazzId)) {
            forwardUrl = "/teacher/smartclazz/clazzdetail.vpage?clazzId=" + clazzId;
        }

        $("a.pc-btn-icon-ios").on("click",function(){
            $17.tongji("爱提问-iOS下载");
        });
        $("a.pc-btn-icon-android").on("click",function(){
            $17.tongji("爱提问-Android下载");
        });
        $("a.pc-btn-icon-card").on("click",function(){
            $17.tongji("爱提问-学生卡片下载");
        });

    });
</script>
</@temp.page>