(function(){
    function voxLog(){
        var roleType = "error_logs";
        var tempObj = {
            userId		: getCookie("uid"),
            page		: currentPage ? currentPage : "system_error",
            refer		: document.referrer,
            op			: "load",
            target		: window.location.href
        };

        var url = '//log.17zuoye.cn/log?_c=vox_logs:' + roleType + '&_l=3&_log=' + encodeURIComponent(JSON.stringify(tempObj)) + '&_t=' + new Date().getTime();
        var scr;
        var imgUrl = document.createElement('img'); imgUrl.style.display = 'none';
        imgUrl.src = url;
        scr = document.getElementsByTagName('body')[0];
        scr.parentNode.appendChild(imgUrl, scr);
        return false;
    }

    function getCookie(name){
        var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

        if(arr=document.cookie.match(reg))

            return unescape(arr[2]);
        else
            return null;
    }

    //执行Logs
    voxLog();
})();

//(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
//        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
//    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
//})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
//
//ga('create', 'UA-38181315-1', 'auto');
//ga('send', 'pageview');