define([], function () {
    var $17 = {};
    function extend(child, parent) {
        var key;
        for (key in parent) {
            if (parent.hasOwnProperty(key)) {
                child[key] = parent[key];
            }
        }
    }

    //验证是否未定义或null或空字符串
    function isBlank(str) {
        return str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    //判读是否为阿里云图片路径
    function ossImg(imgUrl,width) {
        width = width || 360;
        if(imgUrl && imgUrl != "" && imgUrl.indexOf('oss-image.17zuoye.com') > -1 ){
            return imgUrl + '@' + width + 'w_1o';
        }else{
            return imgUrl;
        }
    }

    function promptAlert(){
        switch(arguments.length){
            case 1:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}});
                break;
            case 2:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}, submit: arguments[1], close: arguments[1]});
                break;
            case 3:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}, submit: arguments[1], close: arguments[2]});
                break;
        }
    }

    extend($17, {
        isBlank: isBlank,
        ossImg: ossImg,
        alert: promptAlert
    });

    return $17;
});
