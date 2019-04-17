<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='期末总复习'
pageJs=["jquery", "voxLogs"]
pageCssFile={"beanreward" : ["public/skin/mobile/student/app/activity/css/skin"]}

>

<div class="fr-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/images/fr-bg01.png"/>"></div>
<div class="fr-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/images/fr-bg02.png"/>"></div>
<div class="fr-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/images/fr-bg03.png"/>"></div>
<div class="fr-bg">
    <img src="<@app.link href="/public/skin/mobile/student/app/activity/images/fr-bg04-v1.png"/>">
    <div class="fr-btnBox" style="bottom: 2.5rem;">
        <a href="javascript:void(0)" class="btn doClickOpenGame" data-url="${AfentiMathUrl!}" data-app_key="AfentiMath">开始数学复习</a>
        <a href="javascript:void(0)" class="btn doClickOpenGame" data-url="${AfentiChineseUrl!}" data-app_key="AfentiChinese">开始语文复习</a>
        <a href="javascript:void(0)" class="btn doClickOpenGame" data-url="${AfentiExamUrl!}" data-app_key="AfentiExam">开始英语复习</a>
    </div>
    <div style=" font-size: .6rem; text-align: center; position: absolute; bottom: 0.4rem; width: 100%;  color: #fff; line-height: 160%;">
        本页面的自学产品非老师布置的学校作业，<br/>
        请自愿开通使用。是否开通，不影响校内作业。
    </div>
</div>

<script type="text/javascript">
    signRunScript = function () {
        function getExternal() {
            var _WIN = window;
            if (_WIN['yqexternal']) {
                return _WIN.yqexternal;
            } else if (_WIN['external']) {
                return _WIN.external;
            } else {
                return _WIN.external = function () {
                };
            }
        }

        $(document).on("click", ".doClickOpenGame", function () {
            var $self = $(this);
            var url = window.location.origin + $self.data('url');
            var appKey = $self.data('app_key');
            var browser = "${useNewCore!}";
            var orientation = '${orientation!}';
            if (getExternal()["openFairylandPage"]) {
                getExternal().openFairylandPage(JSON.stringify({
                    url: url,
                    name: "fairyland_app:" + (appKey || "link"),
                    useNewCore: browser || "system",
                    orientation: orientation || "sensor",
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            } else {
                YQ.voxLogs({
                    module: 'fairyland_app',
                    op: 'error-hd',
                    s0: 'null'
                });
            }
            YQ.voxLogs({
                module: "m_RrBgwGwy",
                op: "o_Tn5maTPB",
                s0: appKey
            });
        });
        YQ.voxLogs({
            module: "m_RrBgwGwy",
            op: "o_MsExfFOo"
        });
    };
</script>

</@layout.page>