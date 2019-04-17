<#import "../../module.ftl" as module>
<@module.page
title="亲子活动编辑"
pageJsFile={"siteJs" : "public/script/basic/activity"}
pageJs=["siteJs"]
leftMenu="亲子活动"
>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.config.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.all.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/activity/index.vpage">活动管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}活动</a>
</div>
<h3 class="h3-title">
    ${isNew?string("新增","编辑")}活动
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span> 为必填项</span>
</h3>
<form action="/common/uploadphoto.vpage" method="post" style="display: none;" enctype="multipart/form-data">
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*" />
</form>
<form id="detail-form" action="${isNew?string("addactivity.vpage","editactivity.vpage")}" method="post">
    <input type="hidden" name="gid" value="${(activity.id)!}">
    <div style="float:left;">
        <div class="input-control">
            <label>活动名称：</label>
            <input name="goodsName" data-title="活动名称" class="item" style="width: 300px;" value="${(activity.goodsName)!}" />
        </div>
        <div class="input-control">
            <label>活动简介：</label>
            <textarea name="desc" data-title="活动简介" title="请填写三段简介，用英文逗号分隔，每段不超过10个字" placeholder="请填写三段简介，用英文逗号分隔，每段不超过10个字" style="width: 300px;resize: none" rows="3">${(activity.desc)!}</textarea>
        </div>
        <div class="input-control">
            <label>活动标签：</label>
            <input name="tags" value="${(activity.tags)!?join(',')}" class="item" style="width: 300px;" placeholder="每个不超过6个字，多个之间用英文逗号分隔" />
        </div>
        <div class="input-control">
            <label>体验报告：</label>
            <input name="reportDesc" data-title="体验报告" class="item" style="width: 300px;" value="${(activity.reportDesc)!}" />
        </div>
        <div class="input-control">
            <label>关联机构：</label>
            <#--此处预留了以后机构主使用的case，这次暂时没有测试功能。。。-->
            <#if currentUser.isShopOwner()>
                <select name="sid" class="v-select require" class="sel">
                    <option value="">-请选择关联机构-</option>
                    <#if userShop?? && userShop?size gt 0>
                        <#list userShop as shop>
                            <option value="${(shop.shopId)!}" <#if (activity.shopId)?? && activity.shopId == shop.shopId > selected </#if>>${(shop.shopName)!}</option>
                        </#list>
                    </#if>
                </select>
            <#else>
                <input name="sid" value="${(activity.shopId)!}" class="item" style="width: 300px;" placeholder="运营可以不填"/>
            </#if>
        </div>
        <div class="input-control">
            <label>封面图：</label><span class="upload-tip">请上传封面图</span>
            <#if editable!false>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <input id="banner-img" style="display: none;" name="bannerImg" />
        <div class="image-preview bannerImg clearfix" style="height:150px;">
            <#if (activity.bannerPhoto)??>
                <#list activity.bannerPhoto as imgUrl>
                <#if imgUrl_index == 0>
                    <div class="image">
                        <img src="${imgUrl}" />
                        <#if editable!false>
                            <div class="del-btn">删除</div>
                        </#if>
                    </div>
                </#if>
                </#list>
            </#if>
        </div>
    </div>
    <div style="float:right;">
        <div class="input-control">
            <label><span class="red-mark">*</span>标题：</label>
            <#--<textarea name="title" data-title="标题" class="require" title="请填写活动标题，不超过20字" placeholder="请填写活动标题，不超过20字"-->
                      <#--style="resize: none;height: 70px;s" rows="2" >${(activity.title)!}</textarea>-->
            <input name="title" class="require item" value="${(activity.title)!}" data-title="标题" style="width: 300px;" title="请填写活动标题，不超过20字" placeholder="请填写活动标题，不超过20字" />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>价格：</label>
            <input name="price" class="require item" style="width: 300px;" value="${(activity.price)!}" data-title="价格" placeholder="单位为元 " />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>联系方式：</label>
            <input name="contact" class="require item" style="width: 300px;" value="${(activity.contact)!}" data-title="联系方式" placeholder="请填写联系方式" />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>类型：</label>
            <select name="category" class="v-select require sel">
                <option value="">-请选择活动类型-</option>
                <option <#if (activity.category)?? && activity.category == "科学">selected</#if>>科学</option>
                <option <#if (activity.category)?? && activity.category == "艺术">selected</#if>>艺术</option>
                <option <#if (activity.category)?? && activity.category == "人文">selected</#if>>人文</option>
                <option <#if (activity.category)?? && activity.category == "休闲">selected</#if>>休闲</option>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>活动类型：</label>
            <select name="goodsType" class="v-select require" style="width:300px;">
                <option value="family_activity" <#if (activity.goodsType)?? && activity.goodsType == "family_activity">selected</#if>>亲子活动</option>
                <option value="ustalk" <#if (activity.goodsType)?? && activity.goodsType == "ustalk">selected</#if>>USTalk</option>
            </select>
        </div>
        <div class="input-control">
            <label>支付提示：</label>
            <input name="appointGift" value="${(activity.appointGift)!}" class="item" style="width: 300px;" placeholder="支付成功后提示"   />
        </div>
        <div class="input-control">
            <label>跳转链接：</label>
            <textarea name="redirectUrl" data-title="跳转链接" title="请填写体验报告跳转链接" placeholder="请填写体验报告跳转链接" style="width:300px;resize: none;height: 70px;" rows="2" >${(activity.redirectUrl)!}</textarea>
            <#--<input name="redirectUrl" data-title="跳转链接" class="require" value="${(activity.redirectUrl)!}" />-->
        </div>
        <div class="input-control">
            <label>上课链接：</label>
            <textarea name="successUrl" title="请填写体验报告上课链接" placeholder="请填写体验报告上课链接" style="width:300px;resize: none;height: 70px;" rows="2" >${(activity.successUrl)!}</textarea>
        </div>
        <div class="input-control">
            <label>活动详情：</label><span class="upload-tip">请上传活动详情图</span>
            <#if editable!false>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <input id="detail-img" style="display: none;" name="detailImg" />
        <div class="image-preview detailImg clearfix" style="height:150px;">
            <#if (activity.detail)?? && activity.detail?size gt 0>
                <#list activity.detail as imgUrl>
                 <#if imgUrl_index == 0>
                    <div class="image">
                        <img src="${imgUrl}" />
                        <#if editable!false>
                            <div class="del-btn">删除</div>
                        </#if>
                    </div>
                 </#if>
                </#list>
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label><span class="red-mark">*</span>活动介绍：</label>
        </div>
        <div style="display: inline-block;">
            <script id="activity_area" name="activityDesc" type="text/plain" style="height:260px; width:715px;">${(activity.activityDesc)!}</script>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label><span class="red-mark">*</span>费用说明：</label>
        </div>
        <div style="display: inline-block;">
            <script id="expense_area" name="expenseDesc" type="text/plain" style="height:260px; width:715px;">${(activity.expenseDesc)!}</script>
        </div>
    </div>

    <div class="clearfix" style="clear:both; margin-top: 5px;margin-bottom: 20px;">
        <div class="input-control">
            <label>产品类型：</label><span class="upload-tip">请逐项填写产品类型</span>
            <#if editable!false>
                <a class="blue-btn add-category" href="javascript:void(0)">增加类型</a>
            </#if>
            <input type="hidden" name="productType" value="" id="productType">
        </div>
        <table class="data-table" id="goodsList" style="margin-left: 84px; width: 720px;">
            <thead>
            <tr>
                <th style="text-align: center; width:27%;">产品类型</th>
                <th style="text-align: center; width:27%;">出行时间</th>
                <th style="text-align: center; width:18%;">价格</th>
                <th style="text-align: center; width:18%;">库存</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody id="itemList">
            <#if (activity.items)??>
                <#list activity.items as item>
                <tr>
                    <td style="display: none;"><input title="产品Id" class="data-itemId" value="${(item.itemId)!}"/></td>
                    <td style="text-align: center;"><input title="产品类型" class="data-categoryName" value="${(item.categoryName)!}"/></td>
                    <td style="text-align: center;"><input title="出行时间" class="data-itemName" value="${(item.itemName)!}" /></td>
                    <td style="text-align: center;"><input title="价格" class="data-price" style="width:90px;" value="${(item.price)!}" /></td>
                    <td style="text-align: center;"><input title="库存" class="data-inventory" style="width:90px;" value="${(item.inventory)!}" /></td>
                    <td style="text-align: center;"><a class="op-btn del-line" href="javascript:void(0);">删除</a></td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </div>

    <div class="clearfix" style="clear:both">
        <div class="input-control">
            <label>活动位置：</label>
            <input id="address" name="address" value="${(activity.address)!}" class="item" placeholder="请先于地图上选点，之后可以手动编辑"  style="width:720px;"/>
            <div style="margin-top: 10px; padding-left: 84px; ">
                <div id="innerMap" data-disable="${editable?string("false","true")}" style="width: 100%; height: 400px;"></div>
            </div>
        </div>
        <div class="input-control">
            <label>GPS信息：</label>
            经度: <input class="readonly" readonly id="longitude" name="longitude" value="${(activity.longitude)!}" />
            纬度: <input class="readonly" readonly id="latitude" name="latitude"  value="${(activity.latitude)!}" />
        </div>
    </div>
    <#if editable!false>
    <div class="clearfix submit-box">
        <a id="save-btn" data-type="${isNew?string("add","edit")}" class="submit-btn save-btn" href="javascript:void(0)">保存活动</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/activity/index.vpage">放弃编辑</a>
    </div>
    </#if>

</form>

<script id="T:category" type="text/html">
    <tr>
        <td style="display: none;"><input title="产品Id" class="data-itemId"/></td>
        <td style="text-align: center;"><input title="产品类型" class="data-categoryName"/></td>
        <td style="text-align: center;"><input title="出行时间" class="data-itemName"/></td>
        <td style="text-align: center;"><input title="价格" class="data-price" style="width:90px;"/></td>
        <td style="text-align: center;"><input title="库存" class="data-inventory" style="width:90px;"/></td>
        <td style="text-align: center;"><a class="op-btn del-line" href="javascript:void(0);">删除</a></td>
    </tr>
</script>
</@module.page>