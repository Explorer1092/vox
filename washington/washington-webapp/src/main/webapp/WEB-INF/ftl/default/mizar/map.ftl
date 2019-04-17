<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title=(shop.fullName)!'机构位置'
pageJs=["voxLogs"]
>
<link rel="stylesheet" href="https://cache.amap.com/lbs/static/main1119.css"/>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>
<script type="text/javascript" src="https://cache.amap.com/lbs/static/addToolbar.js"></script>

<style type="text/css">
    #weather { height: 180px;  background-color: #fff;  padding-left: 10px;  padding-right: 10px;  position: absolute;  bottom: 20px; font-size: 12px;  right: 10px; border-radius: 3px;  line-height: 20px;border: 1px solid #ccc; }
    .weather{  width: 60px;  padding-left: 8px;   display: inline-block;}
    .amap-info-close{ display: none;}
</style>

<div id="container"></div>
<div id="tip" style="line-height: 24px"></div>

<script type="text/javascript">
    var map = new AMap.Map('container', {
        resizeEnable: true,
        center: [${(shop.longitude)!0}, ${(shop.latitude)!0}],
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
        str.push('<div>地址：${(shop.address)!}</div>');
        str.push('<div>电话：<#if (shop.contactPhone)?has_content><#list shop.contactPhone as p><a href="tel:${p!}">${p!}</a></#list></#if></div>');
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
</script>
</@layout.page>