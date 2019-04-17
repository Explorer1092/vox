<#import "../module.ftl" as module>
<@module.page
title="查看门店"
pageJsFile={"siteJs" : "public/script/bookstore/view"}
pageJs=["echarts", "siteJs"]
leftMenu="门店列表"
>

    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>

<style>
    .input-control > label {
        width: 109px;
    }

    .input-control > .item {
        height: 40px;
        background:transparent;
        border:none;
        line-height: 30px;

    }
    .input-control .sel{
        border:none;
    }
    .h120 {
        height: 120px;
    !important;
    }
    .btn{
        display: inline-block;
        margin-left:30%;
        margin-top:100px;
        width:50px;
        height:20px;
        text-align: center;
        border:1px solid #ccc;
        border-radius:5px;
        padding:5px;
    }
    .qcode{
        cursor:pointer;

        margin-top: 30px;
        width: 200px;
        height: 200px;
        line-height: 220px;
        margin-left: 60px;
        display: inline-block;
    }
    .downCode{
        cursor:pointer;
        width: 100px;
        text-align: center;
        padding: 10px;
        border:1px solid #ccc;
        border-radius:10px;
        display: inline-block;
        margin-left:40px;
        margin-top:10px;
        vertical-align: middle;
    }
    .downCode:active
    {
        background-color:red;
    }


    .smallImage{
        position: relative;
        margin-left: 40px;
        margin-top: 20px;
    }
    .erwei{
        position: absolute;
        top: 180px;
        left: 108px;
    }
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/bookstore/manager/list.vpage">门店列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">查看</a>
</div>

<h3 class="h3-title">
    门店信息
</h3>

<form id="add-form" action="detail.vpage" method="post">
    <#if bookStoreBean?? >
            <div style="margin-top: 10px;">
        <div class="input-control">
            <label><span class="red-mark">*</span>门店名称：</label>
            <span class="require item">${bookStoreBean.bookStoreName!}</span>
            </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>联系人：</label>
            <span class="require item">${bookStoreBean.contactName!}</span>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>手机号：</label>
            <span class="require item">${bookStoreBean.mobile!}</span>
        </div>

        <div class="input-control">
            <label><span class="red-mark">*</span>门店地址：</label>
            <div class="container">
                <span class="sel" style="width: 223px;margin-left: 10px;">${(bookStoreBean.storeAddressMap.provinceName)!''}</span>
                <span class="sel" style="width: 223px;">${(bookStoreBean.storeAddressMap.cityName)!''}</span>
            </div>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>详细地址：</label>
            <span class="require item" style="height:90px;">${(bookStoreBean.storeAddressMap.detailAddress)!''}</span>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>店铺面积：</label>
            <div style="line-height: 30px;">
                <#if bookStoreBean.storeSizeType == 1>
                <span style="display:inline-block;margin-left: 10px;">A.200平米以下</span>
                <#elseif bookStoreBean.storeSizeType == 2>
                <span style="display:inline-block;margin-left: 10px;">B.200-800平米以下</span>
                <#elseif bookStoreBean.storeSizeType == 3>
                <span style="display:inline-block;margin-left: 10px;">C.800-2000平米</span>
                <#elseif bookStoreBean.storeSizeType == 4>
                <span style="display:inline-block;margin-left: 10px;">D.2000平米以上</span>
                </#if>
            </div>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>周边学校：</label>
            <span data-title="详细地址" style="height:50px;" class="require item">${bookStoreBean.surroundingSchool!}</span>
        </div>
        <div class="input-control">
            <p style="margin:30px 25px;">说明：为了方便结算，请认真填写以下信息</p>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>身份证号：</label>
            <span id="identityCardNumber" name="identityCardNumber" class="item" style="height:50px;">${(bookStoreBean.identityCardNumber)!''}</span>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>银行卡号：</label>
            <span id="bankCardNumber" name="bankCardNumber"  class="item" style="height:50px;">${(bookStoreBean.bankCardNumber)!''}</span>
        </div>
        <div class="input-control">
             <label><span class="red-mark"></span>开户行：</label>
             <span id="depositBank" name="depositBank" class="item" style="height:50px;">${(bookStoreBean.depositBank)!''}</span>
        </div>

        <div class="input-control">
            <h3 class="h3-title">近7天数据（订单数）</h3>
            <div style="width: 100%; text-align: center; margin: 0 auto;">
                <#if (bookStoreBean.recentDaysOrderNum)?? && bookStoreBean.recentDaysOrderNum != '{}'>
                    <div id="chartData" style="display: none"> ${bookStoreBean.recentDaysOrderNum} </div>
                    <div id="chart" class="chartData" style="width: 100%;height:200px"></div>
                <#else>
                    <div style="line-height: 100px;">暂无数据</div>
                </#if>
            </div>
        </div>

        <div class="input-control">
            <h3 class="h3-title">海报</h3>
            <div style="margin-left:40px;">1、海报中的二维码是当前门店专用，从二维码扫码购买的订单才可计入到商户端系统中，请妥善保存。</div>
            <div class="textpc" style="margin-left:40px;">2、点击按钮可下载海报到电脑</div>
            <div class="textmobile" style="margin-left:40px;">2、请到电脑端操作下载高清海报</div>
            <div class="smallImage">
                <img src="/public/skin/images/index4.jpg" width="320"/>
                <div id="erwei" class="erwei">
                </div>
            </div>



            <div class="show container" id="down">
            <a id="downBtn" class="downCode" href="javascript:void(0);">下载高清海报</a>
            </div>
        </div>
        <div class="input-control">
            <h3 class="h3-title">手持身份证照片</h3>
            <div style="margin-left:40px;">1.请上传本人手持身份证照片</div>
            <div  style="margin-left:40px;">2.照片要求可清晰看到身份证号及身份证正面本人照片</div>
            <div  style="margin-left:40px;">3.照片大小不超过5M</div>

            <div class="smallImage">
                <#if (bookStoreBean.identityPic)?? && bookStoreBean.identityPic?length gt 1>
                    <img src="${(bookStoreBean.identityPic) + '?x-oss-process=image/resize,w_550/quality,q_80'}" style="width: 550px; height: auto" />
                <#else>

                 <div style="width: 200px;height: 200px;border: 1px solid #cccccc; text-align: center; line-height: 200px">
                     <p>无图片！</p>
                 </div>
                </#if>
            </div>

        </div>

    </div>
    </#if>
    <a class="btn" id="reBtn" href="/bookstore/manager/list.vpage" onclick="goBack">返回</a>
</form>

</@module.page>

