<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='${shopName!"品牌馆"}'
pageJs=["remarkRemark"]
pageJsFile={"remarkRemark" : "public/script/mobile/mizar/remarkRemark"}
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>

    <div class="markEstimate-box">
        <div class="mke-top">
            <div class="mke-info">总体</div>
            <div class="starBg">
                <!-- ko foreach : {data : starList, as : '_list'} -->
                <a href="javascript:void(0);" data-bind="css:{'cliBg' : _list.checked}, click: $root.starBtn.bind($data,$index()+1)" ></a>
                <!--/ko-->
            </div>
        </div>
        <div class="mke-txt">
            <textarea data-bind="value: remarkContent, attr: {'placeholder': textareaPlaceholder}" placeholder="" maxlength="300"></textarea>
        </div>
    </div>
    <div class="markEstimate-main">
        <ul data-bind="visible: uploadImgList" style="display: none;">
            <!-- ko foreach : {data : uploadImgList, as : '_list'} -->
            <li>
                <div class="mke-image">
                    <img src="" data-bind="attr: {src: _list}">
                    <div class="mke-close" data-bind="click: $root.deleteImgBtn.bind($data,$index())"></div>
                </div>
            </li>
            <!--/ko-->
        </ul>
        <div class="mke-content" style="cursor: pointer;">
            <div class="mke-upload">
                <div class="uploadBg"></div>
            </div>
            <div class="mke-uploadInfo">上传照片</div>
            <#--模拟file上传-->
            <input class="fileUpBtn" data-bind="event:{change: uploadImg}" type="file" accept="image/gif, image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
        </div>
    </div>
    <div class="mke-column" data-bind="visible: fromShopDetail != ''" style="display: none;">
        报名费用 <input data-bind="value: coursePrice" type="text" class="txt" maxlength="8" placeholder="请填写课程价格">
    </div>
    <div class="footer footerHei noFixed">
        <div class="inner">
            <a href="javascript:void(0);" data-bind="click: submitBtn" class="w-orderedBtn w-btn-green w-btnWidPer">发布评论</a>
        </div>
    </div>

</@layout.page>