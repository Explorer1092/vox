//获取位置回调
var postData = {
    longitude: 0,
    latitude: 0
};

var vox = vox || {};
vox.task = vox.task || {};
vox.task.setLocation = function (res) {
    var resJson = JSON.parse(res);
    var errorCode = parseInt(resJson.errorCode || 0);

    if (errorCode > 0) {
        onError({
            code: errorCode
        })
    } else {
        onSuccess({
            coords: {
                longitude: resJson.longitude,
                latitude: resJson.latitude,
                address: resJson.address
            }
        });
    }
};

function getLocation() {
    var options = {
        enableHighAccuracy: true,
        maximumAge: 1000
    };

    if (window['external'] && ('getLocation' in window.external)) {
        window.external.getLocation();
    } else {
        if (navigator['geolocation'] && navigator.geolocation['getCurrentPosition']) {
            navigator.geolocation.getCurrentPosition(onSuccess, onError, options);
        } else {
            //alert("位置反解析失败,重新定位");
            loadData();
        }
    }
}

//成功时
function onSuccess(position, callback) {
    postData.latitude = position.coords.latitude;
    postData.longitude = position.coords.longitude;

    loadData();
}

//失败时
function onError(error) {
    switch (error.code) {
        case 1:
            footBarInfo("位置服务被拒绝,重新定位");
            break;
        case 2:
            footBarInfo("暂时获取不到位置信息,重新定位");
            break;
        case 3:
            footBarInfo("获取信息超时,重新定位");
            break;
        case 4:
            footBarInfo("未知错误,重新定位");
            break;
    }
}

function footBarInfo(text){
    loadData();
    //$("#PPGDetailList").html( template("T:定位提示", {info: text}) );
}

function loadData(){
    var PPGDetailList = $("#PPGDetailList");
    var listArray = [
        {
            'key': 'english',
            'title': '外语',
            'firstCategory': '少儿外语'
        },
        {
            'key': 'art',
            'title': '兴趣',
            'firstCategory': '兴趣才艺'
        },
        {
            'key': 'play',
            'title': '玩乐',
            'firstCategory': '游学玩乐'
        }
    ];

    $.ajax({
        url: "/mizar/loadppgdata.vpage",
        type: "POST",
        data: {
            longitude: postData.longitude,
            latitude: postData.latitude
        },
        success: function (data) {
            if (data.success && data.dataMap) {
                //处理图片
                $.each(data.dataMap, function(index, item){
                    for(var i = 0; i < item.length; i++){
                        item[i].brandLog = pressImage(item[i].brandLog);
                        item[i].distance = parseFloat(item[i].distance);
                    }
                });

                PPGDetailList.html(template("T:PPGDetailList", {
                    dataMap: data.dataMap,
                    listArray: listArray
                }));
            }
        },
        error: function(){
            PPGDetailList.html( template("T:定位提示", {info: "数据加载失败..."}) );
        }
    });

    function pressImage(link, w){
        var defW = 200;
        if(w){
            defW = w;
        }

        if(link && link != "" && link.indexOf('oss-image.17zuoye.com') > -1 ){
            return link + '@' + defW + 'w_1o_75q';
        }else{
            return link;
        }
    }
}

define(['jquery', 'template', 'voxLogs'], function ($) {
    var moduleName = "m_KoeVwt6X";

    //商品详情
    $(document).on("click", ".JS-GoToPage", function () {
        var $this = $(this), aKey = $this.attr("data-key");

        YQ.voxLogs({
            database: 'parent',
            module: moduleName,
            op: "o_EbEXQWWE",
            s0: aKey,
            s1: $this.attr("data-url")
        });

        location.href = $this.attr("data-url");
    });

    YQ.voxLogs({
        database: 'parent',
        module: moduleName,
        op: "o_FbpdjIk6"
    });

    //品牌馆页
    if (typeof(initMode) == "string" && initMode == "PPGDetail") {
        getLocation();

        $(document).on("click", ".JS-moreDetail", function(){
            var $self = $(this);

            $self.siblings(".JS-item").show();
            $self.hide();

            var aKey = $self.attr("data-key");

            YQ.voxLogs({
                database: 'parent',
                module: moduleName,
                op: "o_l3EJfHZR",
                s0: aKey
            });
        });

        $(document).on("click", ".JS-GetTargeting", function(){
            $("#PPGDetailList").html( template("T:定位提示", {info: "数据加载中...", type: 'loading'}) );

            getLocation();
        });
    }
});