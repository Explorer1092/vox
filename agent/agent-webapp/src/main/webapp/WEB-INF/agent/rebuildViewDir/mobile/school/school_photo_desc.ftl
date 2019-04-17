<#import "../../../mobile/layout_new_no_group.ftl" as layout>
<@layout.page title="照片说明">
<#--<div class="mobileCRM-V2-header">-->
    <#--<div class="inner">-->
        <#--<div class="box">-->
            <#--<a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>-->
            <#--<div class="headerText">照片说明</div>-->
        <#--</div>-->
    <#--</div>-->
<#--</div>-->
<div style="line-height: 24px;margin: 0 10px 0 10px">
    <div class="pdHeader">一、正门照片上传要求</div>
    <p>&nbsp;&nbsp;（1）必须为新建学校的正门的照片，除特殊情况外，必须含有学校名称信息。</p>
    <p>&nbsp;&nbsp;（2）必须为未经压缩的原图，且含有位置信息。</p>
    <div class="pdHeader">二、手机拍摄含有位置信息的原图拍摄方法：</div>
    <p> A: iphone手机操作步骤：</p>
    <p>&nbsp;&nbsp;（1）开启手机GPS，方法如下：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;进入设置—>隐私—>定位服务（打开）</p>
    <p>&nbsp;&nbsp;（2）设置相机拍照允许访问位置，方法如下：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;进入隐私—>定位服务—>相机—>选择使用期间</p>
    <div class="pdivs">
        <img src="/public/css/images/iphoneprivate.png" alt="" width="80%" class="photoImageDiv">
        <img src="/public/css/images/iphonelocation.png" alt="" width="80%" class="photoImageDiv">
        <img src="/public/css/images/iphonecamlocation.png" alt="" width="80%" class="photoImageDiv">
    </div>
    <p> B: android系统手机操作步骤：</p>
    <p>&nbsp;&nbsp;（1）开启手机GPS定位服务，方法如下：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;打开GPS，一般在设置—>隐私和安全—>定位服务（打开）</p>
    <p>&nbsp;&nbsp;（2）设置相机拍照时允许访问位置，方法如下：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;打开相机—>设置—>地理位置（开启）</p>
    <div class="pdivs">
        <img src="/public/css/images/androidlocation.png" alt="" width="80%" class="photoImageDiv">
        <img src="/public/css/images/androidlocationser.png" alt="" width="80%" class="photoImageDiv">
        <img src="/public/css/images/androidplace.png" alt="" width="80%" class="photoImageDiv">
    </div>

    <div class="pdHeader">三、从手机相册中上传含有位置信息的原图：</div>
    <p>&nbsp;&nbsp;（1）若手机中的照片为按照上述方法自己拍摄的存储在相册中的照片，直接上传即可</p>
    <p>&nbsp;&nbsp;（2）老师通过微信传学校正门图片：需要保证第一，老师拍摄的图片是含有位置信息的，第二，老师通过微信传的是原图而非被压缩后的缩略图片。<br>
        方法如下：</p>
    <p>1、选择要传的图片；<br>
        2、点击预览按钮；<br>
        3、在预览页面左下角原图打钩；<br>
        4、发送</p>

    <div class="pdivs">
        <img src="/public/css/images/choosePhoto.png" alt="" width="42%" class="photoImageDiv" style="float: left;margin-left: 5%;">
        <img src="/public/css/images/sendphoto.png" alt="" width="42%" class="photoImageDiv" style="float: left;margin-left: 5%;">
    </div>
</div>

</@layout.page>