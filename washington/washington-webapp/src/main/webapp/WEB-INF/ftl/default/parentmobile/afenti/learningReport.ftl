<!DOCTYPE HTML>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <meta charset="UTF-8">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8">
    <meta name="viewport" content="target-densitydpi=device-dpi,width=640, user-scalable=no">
    <meta name="MobileOptimized" content="320">
    <meta name="Iphone-content" content="320">
    <title>阿分题学习报告</title>
    <@app.css href="public/skin/parentMobile/css/learningReport.css"/>
    <@sugar.capsule js=["jquery", "core"]/>
    <@app.script href="public/skin/mobile/pc/js/fullScreenDpi.js" />
</head>
<body>
    <div class="aFenTi-learnReportBox">
        <div class="lr-banner"></div>
        <div class="lr-main">
            <div class="lr-line"></div>
            <div class="lr-top">
                <div class="problem"><span class="num">${stuExamNum!0}</span>题</div>
                <div class="tip"><span class="name js-userName">${stuName!0}</span>上周自学</div>
            </div>
            <div class="lr-head">全国同级学生对比</div>
            <div class="lr-list">
                <ul>
                    <li>
                        <div class="content">
                            <div class="problem"><span class="num">${classExamAvgNum!0}</span>题</div>
                            <div class="info">同班平均自学</div>
                            <div class="points">超过了<span class="percent"></span>${classExamAvgPer!0}</div>
                        </div>
                    </li>
                    <li>
                        <div class="content">
                            <div class="problem"><span class="num">${schoolExamAvgNum!0}</span>题</div>
                            <div class="info">同校平均自学</div>
                            <div class="points">超过了<span class="percent"></span>${schoolExamAvgPer!0}</div>
                        </div>
                    </li>
                    <li>
                        <div class="content">
                            <div class="problem"><span class="num">${cityExamAvgNum!0}</span>题</div>
                            <div class="info">同市平均自学</div>
                            <div class="points">超过了<span class="percent"></span>${cityExamAvgPer!0}</div>
                        </div>
                    </li>
                    <li>
                        <div class="content">
                            <div class="problem"><span class="num">${nationalExamAvgNum!0}</span>题</div>
                            <div class="info">全国平均自学</div>
                            <div class="points">超过了<span class="percent"></span>${nationalExamAvgPer!0}</div>
                        </div>
                    </li>
                </ul>
            </div>
            <div class="lr-title">当前<span class="sub">${parentNum!0}</span>位家长开通，平均自学<span class="sub">${wordsAvgNum!0}</span>个单词</div>
        </div>
    </div>
    <div class="learnReport-footer">
        <div class="lr-empty"></div>
        <div class="lr-btn">
            <p class="con"><span class="name js-userName">${stuName!0}</span>在阿分题英语的<span class="des">3节免费试用课已结束，无法继续学习，</span>开通全部课程让孩子继续学习。</p>
            <a  href="javascript:void(0);" class="opened_btn">开通学习</a>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            $17.voxLog({
                module : "LearningReport",
                op: "Click-afenti",
                sid : ${sid!0}
            }, "student");
            $(document).on("click",".opened_btn",function(){
                $17.voxLog({
                    module : "LearningReport",
                    op: "ToOpen-afenti",
                    sid : ${sid!0}
                }, "student");

                var buyUrl = "${buyUrl!}";

                if(getAppVersion() != ''){
                    buyUrl = buyUrl + '&app_version=' + getAppVersion()
                }

                location.href = buyUrl;
            });

            //获取App版本
            function getAppVersion(){
                var native_version = "";

                if(window["external"] && window.external["getInitParams"] ){
                    var $params = window.external.getInitParams();

                    if($params){
                        $params = eval("(" + $params + ")");

                        native_version = $params.native_version;
                    }
                }else if(getQueryString("app_version")){
                    native_version = getQueryString("app_version") || "";
                }
                return native_version;
            }

            //Get Query
            function getQueryString(name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            }

            $(".js-userName").text( decodeURI( decodeURI("${stuName!0}") ) );
        });
    </script>
</body>
</html>
<!doctype html>
        <!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>