<!doctype html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="MobileOptimized" content="320" />
    <meta name="Iphone-content" content="320" />
    <meta name="apple-mobile-web-app-capable" content="no">
    <meta name='apple-touch-fullscreen' content='no'>
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <meta http-equiv="X-UA-Compatible" content="IE=Edge, chrome=1"/>
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
    <title>一起作业</title>
    <style>
        html,body{margin:0; padding:0; width:100%; height:100%}
        .warp-drag{ position: fixed; background-color: rgba(255, 255, 255, 0.8) ; bottom: 3.7rem; left: 0.1rem; z-index: 10; border-radius: .4rem; font-family: "微软雅黑","Microsoft YaHei",Arial,Helvetica,sans-serif; font-size: 0.8rem; cursor: pointer;}
        .warp-drag a{ display: block; padding: 0.5rem 0.8rem; color: #ff6633; text-decoration: none; max-width: 5rem; min-width: 3rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-align: center;}
    </style>
    <@app.script href="public/script/voxLogs.js" />
    <script type="text/javascript">
        (function(){
            var $currentEnterDate = Date.now();

            YQ.voxLogs({
                database : "parent",
                module: 'm_EXxXlXbF',
                op: 'o_eWHG1BCq',
                s0: location.href
            });

            window.onbeforeunload = window.onunload = function(event) {
                YQ.voxLogs({
                    database : "parent",
                    module: 'm_EXxXlXbF',
                    op: 'o_OgOskxtQ',
                    s0: location.href,
                    s1: Date.now() - $currentEnterDate
                });
            };
        }());
    </script>
</head>
<body>
<#if text?has_content>
    <div class="warp-drag" id="DragMain">
        <a href="${(linkUrl?has_content)?string(linkUrl, 'javascript:;')}">${text!'--'}</a>
    </div>
</#if>
<iframe src="${url!}" style="width: 100%; height: 100%;" frameborder="0" scrolling="auto"></iframe>
</body>
</html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->