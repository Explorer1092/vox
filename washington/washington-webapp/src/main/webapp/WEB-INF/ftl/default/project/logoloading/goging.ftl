<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="百科大作战"
pageJs=['Jquery']
>

<div style="width:100%;height:100%;background:#000;opacity: 0.5">
    <img style="position:absolute;top: 0; left: 0; bottom: 0; right: 0; "src="<@app.link href="public/skin/project/logoloading/app/images/logo_loading.png"/>">
</div>

<script type="text/javascript">

    var $userType = ${(currentUser.userType)!0};
    var $userId = ${(currentUser.id)!0};
    var _sid = 0;
    signRunScript = function(){
        function _getCookie(name){
            var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
            if(arr=document.cookie.match(reg))
                return unescape(arr[2]);
            else
                return null;
        }

        //Get Query
        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }

        var _SID = _getCookie("sid") || getQueryString("sid") || 0;

        console.info(_SID);
        _sid = _SID;
    }
</script>
</@layout.page>