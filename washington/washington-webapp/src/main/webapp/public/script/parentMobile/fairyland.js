define(['template', 'jqPopup'], function(template){
    // 填充模版
    var SID = PM.sid ? PM.sid : 0;

    //header banner
    YQ.voxSpread({
        boxId : $("#headerBannerCrm"),
        keyId : 220601,
        userId : SID
    });

    //点击切换TAG
    $(document).on("click", ".do_clickTypeName",function(){
        var $this = $(this);

        readerTemplate( $this.attr("data-tag") );
    });

    var recordAjaxData = {};

    //渲染模板
    function readerTemplate(tag){
        var $boxId = $("#fairylandList");
        var pageSID = "&sid=" + SID;
        var isWeChat = function(){
            return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
        };

        if(isWeChat()){
            pageSID = "";
        }

        if(recordAjaxData[tag] && recordAjaxData[tag].success){
            $boxId.html( template("T:趣味学习LIST", {
                data : recordAjaxData[tag],
                tag : tag,
                SID : pageSID,
                isApp : isShowEnterApp()
            }) );
            return false;
        }

        $.get("/parentMobile/ucenter/shoppinginfo/productlist.vpage?tag=" + tag + "&sid=" + SID + "&app_version=" + (getQueryString("app_version") || ""), {}, function(data){
            if(data.success && data.products.length > 0){
                recordAjaxData[tag] = data;
                $boxId.html( template("T:趣味学习LIST", {
                    data : data,
                    tag : tag,
                    SID : pageSID,
                    isApp : isShowEnterApp()
                }) );
            }else{
                //请求失败
                $boxId.html( template("T:productSizeNull", {}) );
            }
        });
    }

    $(document).on("click", ".JS-gotoGame", function(){
        var $self = $(this);
        var $dataValue = $self.attr("data-value");
        var $gameItem = [];

        if($dataValue != ""){
            $gameItem = $self.attr("data-value").split(',');
        }

        if (window['external'] && window.external["openFairylandPage"]) {
            window.external.openFairylandPage(JSON.stringify({
                name: "fairyland_app:" + ($gameItem[0] || "link"),
                url: hasUrlHttp($gameItem[1]),
                useNewCore: $gameItem[2] || "system",
                orientation: $gameItem[3] || "sensor",
                initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
            }));
        }else{
            console.info('null pageQueueNew')
        }
    });

    //是否带http
    function hasUrlHttp(url){
        if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
            return url;
        }

        return location.protocol + '//' + location.host + url;
    }


    //初始化渲染模板
    var currentActiveTag = $(".do_clickTypeName.active");
    if (currentActiveTag.length) {
        readerTemplate(currentActiveTag.attr("data-tag"));
    }

    function isShowEnterApp(){
        /*var ua = navigator.userAgent.toLowerCase();
        if(ua.match(/MicroMessenger/i) == "micromessenger") {
            return false;
        } else {
            return true;
        }*/

        //window.external.openFairylandPage = function(){};

        if (window['external'] && window.external["openFairylandPage"]) {
            return true;
        }
    }

    //Get Query
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    YQ.voxLogs({database: "parent", module: 'm_84NR1ObF', op: 'intereststudy_pageload'});
});