<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='商家位置'
pageJs=["map"]
pageJsFile={"map" : "public/script/mobile/mizar/map/map"}
>
<link rel="stylesheet" href="https://cache.amap.com/lbs/static/main1119.css"/>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>

<style type="text/css">
    #weather { height: 180px;  background-color: #fff;  padding-left: 10px;  padding-right: 10px;  position: absolute;  bottom: 20px; font-size: 12px;  right: 10px; border-radius: 3px;  line-height: 20px;border: 1px solid #ccc; }
    .weather{  width: 60px;  padding-left: 8px;   display: inline-block;}
    .amap-info-close{ display: none;}
</style>

<div id="container"></div>
<div id="tip" style="line-height: 24px"></div>

<script type="text/javascript">
    var mapDetail = {
        longitude: ${longitude!},
        latitude: ${latitude!},
        address: '${address!}',
        title: "${title!}",
        tel: ${(json_encode(tels))![]} //电话为数组
    };
</script>
</@layout.page>