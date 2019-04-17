<script type="text/javascript">
    var uid = getCookie('uid');
    var comments = getElementsClass("js-commentsButton")[0];
    var reports = getElementsClass("js-reportButton")[0];

    comments.onclick = function () {
        window.open('http://system.eduyun.cn/bmp-web/getSpAppDetail_index?appId=KPOPD9gm5NjcRi71EqAmmimwjPNMAHjG&userId=' + uid);
    };
    reports.onclick = function () {
        window.open('http://system.eduyun.cn/bmp-web/sysAppReport/appReport?appId=KPOPD9gm5NjcRi71EqAmmimwjPNMAHjG&userId=' + uid);
    };
    function getCookie(name){
        var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

        if(arr=document.cookie.match(reg))

            return unescape(arr[2]);
        else
            return null;
    }

    function getElementsClass(classnames){
        var classobj= new Array();//定义数组
        var classint=0;//定义数组的下标
        var tags=document.getElementsByTagName("*");//获取HTML的所有标签
        for(var i in tags){//对标签进行遍历
            if(tags[i].nodeType === 1){//判断节点类型
                if(tags[i].getAttribute("class") && tags[i].getAttribute("class").indexOf(classnames) > -1)//判断和需要CLASS名字相同的，并组成一个数组
                {
                    classobj[classint]=tags[i];
                    classint++;
                }
            }
        }
        return classobj;//返回组成的数组
    }

</script>