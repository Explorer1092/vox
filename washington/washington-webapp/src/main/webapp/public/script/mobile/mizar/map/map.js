/**
 * @author xinqiang.wang
 * @description "地图相关"
 * 相关参数：
 *          longitude：经度 必填
 *          latitude：维度 必填
 *          address：地址
 *          tel：电话号码 （数组）
 *          title: 标题
 * @createDate 2016/10/31
 */

define(['jquery'], function ($) {
    var map = new AMap.Map('container', {
        resizeEnable: true,
        center: [mapDetail.longitude, mapDetail.latitude],
        zoom: 12
    });

    AMap.service('AMap.Geocoder', function () {
        var geocoder = new AMap.Geocoder();

        geocoder.getAddress(map.getCenter(), function (status, result) {
            if (status === 'complete' && result.info === 'OK') {
                geocoder_CallBack(result);
            }
        });
    });

    function geocoder_CallBack(data) {
        var address = data.regeocode.formattedAddress; //返回地址描述
        var str = [];
        str.push('<div>地址：'+(mapDetail.address || address)+'</div>');
        if(mapDetail.tel.length > 0){
            var telHtml = '';
            for(var i = 0; i < mapDetail.tel.length; i++){
                telHtml+='<a href="tel:'+mapDetail.tel[i]+'">'+mapDetail.tel[i]+'</a> ';
            }
            str.push('<div>电话：'+telHtml+'</div>');
        }

        var marker = new AMap.Marker({map: map, position: map.getCenter()});
        var infoWin = new AMap.InfoWindow({
            content: str.join(''),
            offset: new AMap.Pixel(0, -20)
        });
        infoWin.open(map, marker.getPosition());
        marker.on('click', function () {
            infoWin.open(map, marker.getPosition());
        });
    }

    // set title
    if(mapDetail.title != ''){
        window.title = mapDetail.title;
    }
});