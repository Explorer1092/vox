<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg"
title='走遍美国'
pageJs=["jquery", "voxLogs"]
pageCssFile={"beanreward" : ["public/skin/mobile/student/app/activity/travelamerica/css/skin"]}

>

<div class="footer">
    <div class="inner">
        <a href="javascript:void(0);" class="btn doClickOpenGame">马上去挑战</a>
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

            var url = window.location.origin + '/app/redirect/thirdApp.vpage?appKey=UsaAdventure&platform=STUDENT_APP&productType=APPS';
            var appKey = 'UsaAdventure';
            var browser = "crossWalk";
            var orientation = 'landscape';
            if (getExternal()["openFairylandPage"]) {
                getExternal().openFairylandPage(JSON.stringify({
                    url: url,
                    name: "fairyland_app:" + (appKey),
                    useNewCore: browser,
                    orientation: orientation,
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            } else {
                YQ.voxLogs({
                    module: 'fairyland_app',
                    op: 'error-free-trial',
                    s0: 'null'
                });
            }
            YQ.voxLogs({
                module: "m_KLgOuIqy",
                op: "o_TuoVjl3j"
            });
        });
        YQ.voxLogs({
            module: "m_KLgOuIqy",
            op: "o_6uQNtMl8"
        });
    };
</script>

</@layout.page>