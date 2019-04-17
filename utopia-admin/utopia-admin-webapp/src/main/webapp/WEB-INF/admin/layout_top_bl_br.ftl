<#macro page page_title layout_left_iframe_src layout_main_iframe_src>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>${page_title!'Utopia Admin'}</title>
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery.form.js"></script>
    <style>
        body { margin:  0; padding:  0; overflow: hidden; }
        iframe { margin: 0; padding: 0; border: none; display: block; }
        iframe#iframe_left { float: left; }
        iframe#iframe_main { float: left; }
    </style>
</head>

<body>
<div class="navbar" style="margin-bottom: 0">
    <div class="navbar-inner">
        <div class="container">
            <#nested />
        </div>
    </div>
</div>

<iframe id="iframe_left" name="iframe_left" src="${layout_left_iframe_src!'about:blank'}"></iframe>
<iframe id="iframe_main" name="iframe_main" src="${layout_main_iframe_src!'about:blank'}"></iframe>

<script>
    (function($){
        $.extend({
            longPolling : function(options){
                var _options = options;
                options.complete = function(){
                    $.longPolling(_options);
                };
                $.ajax(options);
            }
        });
    }(jQuery));

    function resizeIframes() {
        $("#iframe_left").height($(window).height() - $("#iframe_left").position().top );
        $("#iframe_main").height($("#iframe_left").height());
        $("#iframe_main").width($(window).width() - $("#iframe_left").width() - 1);
    }
    $(function(){
        resizeIframes();
    });

    $(window).resize(resizeIframes);
</script>
</body>
</html>
</#macro>
