<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>${pageOrigin!'一起作业'}</title>
    <#--<link href="https://cdn.bootcss.com/bootstrap/4.0.0-alpha.3/css/bootstrap.css" rel="stylesheet">-->
</head>
<body>
<script src="https://qiyukf.com/script/f10a2349a4bead156114e00f9084177c.js"
        charset="utf-8"></script>
<script type="text/javascript">
    window.onload = function () {
        var ua = window.navigator.userAgent.toLowerCase();
        ysf.on({
            'onload': function () {
                ysf.config({
                    uid: "${uid!}",
                    data: '${data!}',
                    groupid:${destId!},
                    robotShuntSwitch:${robustOption!0},
                    qtype:${qtype!0},
                    unconfirm:1,
                    robotId:${robotId!0},
                    success: function(){ // 成功回调
                        if (ua.indexOf('ios') > -1) {
                            window.location.replace(ysf.url());
                        }
                    },
                    error: function () { //错误回调
                        window.location.replace(ysf.url());
                    }
                });

//            var system = {
//                win: false,
//                mac: false,
//                xll: false
//            };
//
//            //检测是否PC
//            var p = navigator.platform;
//            system.win = p.indexOf("Win") == 0;
//            system.mac = p.indexOf("Mac") == 0;
//            system.x11 = (p == "X11") || (p.indexOf("Linux") == 0);
                //跳转语句，如果是手机访问就自动走ysf.url()方法
//            if (system.win || system.mac || system.xll) {
//                ysf.open();
//            } else {
//                setTimeout("window.location.replace(ysf.url())", 1500);
//            }
                if (ua.indexOf('ios') == -1) {
                    window.location.replace(ysf.url());
                }
            }
        });
    };
</script>
</body>
</html>
