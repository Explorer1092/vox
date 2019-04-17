/*-----------品牌相关-----------*/
define(["jquery"], function ($) {

    /*********       地图相关          *********/
    if (typeof AMap != "undefined") {
        //首先判断对象是否存在
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var map = new AMap.Map('innerMap', {
            resizeEnable: true,
            zoom: 12
        });

        var marker = '';
        if(longitude != '' && latitude != ''){
            map.setCenter([longitude,latitude]);
            marker = new AMap.Marker({map: map, position: [longitude,latitude], animation: 'AMAP_ANIMATION_DROP'});
        }

        map.on("click", function (e) {
            //预览页面禁止重新标记
            var disable = $('#innerMap').data('disable') || false;
            if (!disable) {
            } else {
                return false;
            }

            $('#longitude').val(e.lnglat.getLng());
            $('#latitude').val(e.lnglat.getLat());
            $('#baiduGps').attr("checked", true);

            if(marker != ''){
                marker.setMap(null);//清空已有的标记
            }
            marker = new AMap.Marker({map: map, position: e.lnglat, animation: 'AMAP_ANIMATION_DROP'});
        });
    }
    /*********       地图结束          *********/
});