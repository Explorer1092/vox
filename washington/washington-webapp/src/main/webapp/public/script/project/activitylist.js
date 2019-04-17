define(['jquery', 'knockout', 'voxSpread'], function($, ko){

    function viewDataJson () {
        var _this = this;

        _this.success = ko.observable('false');
        _this.items = ko.observableArray([]);
        _this.imgDoMain = ko.observable("");

        //130101
        YQ.voxSpread({
            keyId : getQueryString("keyId")
        }, function(result){
            if(result.success){
                _this.success(true);
                _this.items(result.data);
                _this.imgDoMain(result.imgDoMain);
            }
        });

        _this.go_link = function (data) {
            var link = window.location.origin + '/be/london.vpage?aid=' + data.id;
            if (isWinExternal()["openSecondWebview"]) {
                isWinExternal().openSecondWebview( JSON.stringify({
                    url: link
                }) );
            } else {
                location.href = link;
            }
        }
    }

    ko.applyBindings(new viewDataJson());



    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    //是否有X5的存在
    function isWinExternal() {
        var _win = window;
        if (_win['yqexternal']) {
            return _win.yqexternal;
        } else if (_win['external']) {
            return _win.external;
        }else{
            _win.external = {};
            return _win.external
        }
    }

});