<#import "../../module.ftl" as module>
<@module.page
title="商品"
pageJsFile={"siteJs" : "public/script/basic/groupongoods", "commonJs" : "public/script/common/common"}
pageJs=["siteJs"]
leftMenu="商品管理"
>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.config.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/ueditor.all.min.js" type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
<style>
    .input-control>input,.input-control>textarea{background:#fff;width:715px;height:30px;line-height:28px;-webkit-box-sizing:border-box;-moz-box-sizing:border-box;box-sizing:border-box;border:1px solid #d3d8df;-webkit-border-radius:2px;-moz-border-radius:2px;border-radius:2px;padding:0 12px}
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/groupon/category/list.vpage">管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}商品</a>
</div>
<h3 class="h3-title">
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span>为必填项</span>
</h3>
<form action="/common/uploadphoto.vpage" method="post" style="display: none;">
    <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*"/>
    <input id="id" value="${(goods.id)!}" name="gid"/>
</form>

<form id="detail-form" action="/groupon/goods/addgoods.vpage" method="post">
    <#if isNew?? &&!isNew><input value="${(goods.id)!}" name="id" style="display:none;"></#if>
    <div style="float:left;">
        <div class="input-control" style="width: 800px;">
            <label><span class="red-mark">*</span>短标题：</label>
            <input name="shortTitle" data-title="商品ID" class="require" value="${(goods.shortTitle)!}"/>
        </div>
        <div class="input-control">
            <label>长标题：</label>
            <textarea name="title" data-title="商品描述"  style="resize: none;height: auto;" rows="3">${(goods.title)!}</textarea>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>商品标签：</label>
            <input name="goodsTag" data-title="商品描述" placeholder="商品标签,以逗号分隔" value="${(goods.goodsTag)!}"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>推荐详情：</label>
            <div style="clear: both; display: inline-block;">
                <script id="content_area" name="recommend" type="text/plain" style="width:715px;">${(goods.recommend)!}</script>
            </div>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>特色标签：</label>
            <div class="both">
                <label><input type="checkbox" name="specialTag" value="hot" <#if (goods.specialTag)??><#if goods.specialTag?contains('hot')>checked="checked"</#if></#if>>热卖</label>
                <label><input type="checkbox" name="specialTag" value="new" <#if (goods.specialTag)??><#if goods.specialTag?contains('new')>checked="checked"</#if></#if>>新品</label>
                <label><input type="checkbox" name="specialTag" value="postFree" <#if (goods.specialTag)??><#if goods.specialTag?contains('postFree')>checked="checked"</#if></#if>>包邮</label>
                <label><input type="checkbox" name="specialTag"  value="promotions" <#if (goods.specialTag)??><#if goods.specialTag?contains('promotions')>checked="checked"</#if></#if>>促销</label>
            </div>
        </div>
        <div class="input-control">
            <label>原价：</label>
            <input name="originalPrice" data-title="商品原价" value="${(goods.originalPrice)!}"/>
        </div>
        <div class="input-control">
            <label>现价：</label>
            <input name="price" data-title="商品现价"  value="${(goods.price)!}"/>
        </div>
        <div class="input-control">
            <label>销量：</label>
            <input name="saleCount" data-title="商品销量"  value="${(goods.saleCount)!}"/>
        </div>

        <div class="input-control">
            <label>商品来源：</label>
            <select name="goodsSource" class="v-select" style="width:715px;">
                <#if goodsSourceTypeList?size gt 0>
                    <#list goodsSourceTypeList as goodsSource>
                        <option value="${goodsSource.code}"
                                <#if (goods.goodsSource)?? && goods.goodsSource == goodsSource.code>selected</#if>>${goodsSource.getName()}</option>
                    </#list>
                </#if>
            </select>
        </div>
        <div class="input-control">
            <label>数据来源：</label>
            <select name="dataSource" class="v-select" style="width:715px;">
                <#if dataSourceTypeList?size gt 0>
                    <#list dataSourceTypeList as dataSource>
                        <#if isNew?? && isNew>
                            <option value="${dataSource.code}"
                                    <#if  'manual_edit'== dataSource.code>selected</#if>>${dataSource.getName()}</option>
                        <#else>
                            <option value="${dataSource.code}"
                                    <#if (goods.dataSource)?? && goods.dataSource == dataSource.code>selected</#if>>${dataSource.getName()}</option>
                        </#if>
                    </#list>
                </#if>
            </select>
        </div>

        <div class="input-control">
            <label>排序值：</label>
            <input name="orderIndex" id="orderIndex" data-title="排序值,大值靠前"
                   value="${(goods.orderIndex)!}"/>
        </div>
        <div class="input-control clearfix">
            <label>所属分类：</label>
            <select name="categoryCode" class="v-select" style="width:715px;">
                <#if categoryList?size gt 0>
                    <#list categoryList as category>
                        <option value="${category.categoryCode}"
                                <#if (goods.categoryCode)?? && goods.categoryCode == category.categoryCode>selected</#if>>${category.categoryName}</option>
                    </#list>
                </#if>
            </select>
        </div>

        <div class="input-control clearfix">
            <label>是否卖光：</label>
            <#if isNew?? && isNew>
                <label class="checkbox"><input class="require" type="radio" value="1" id="oos" name="oos">是</label>
                <label class="checkbox"> <input class="require" type="radio" value="0" id="oos" name="oos"
                                                checked>否</label>
            <#else>
                <label class="checkbox"><input class="require" type="radio" value="1" id="oos" name="oos"
                                               <#if (goods.oos)??><#if goods.oos>checked</#if><#else>checked</#if>>是</label>
                <label class="checkbox"> <input class="require" type="radio" value="0" id="oos" name="oos"
                                                <#if (goods.oos)?? && !goods.oos>checked</#if>>否</label>
            </#if>
        </div>
        <div class="input-control clearfix">
            <label>状态：</label>
            <select name="status" class="v-select" style="width:715px;">
                <#if goodStatusTypeList?size gt 0>
                    <#list goodStatusTypeList as type>
                        <#if isNew?? && isNew>
                            <option value="${type.code}" <#if 'ONLINE' == type.code>selected</#if>>${type.desc}</option>
                        <#else>
                            <option value="${type.code}"
                                    <#if (goods.status)?? && goods.status == type.code>selected</#if>>${type.desc}</option>
                        </#if>
                    </#list>
                </#if>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>发布时间：</label>
            <input  id="deployTime" name="deployTime" data-title="发布时间" class="require" value="${(goods.deployTime)!}"/>
        </div>

        <div class="input-control">
            <label><span class="red-mark">*</span>原始URL：</label>
            <input type="text" id="originUrl" name="originUrl" data-title="原始URL" class="require"  value="${(goods.originUrl)!}" style="width:640px;"/>
            <a class="blue-btn" id="grab-btn" href="javascript:void(0)">抓取</a>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>URL：</label>
            <input name="url" data-title="url" class="require" value="${(goods.url)!}"/>
        </div>
        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label><span class="red-mark">*</span>图片详情：</label><span class="upload-tip" id="tipSpan">请上传详情图片</span>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </div>
            <div class="image-preview clearfix">
                <input id="detail-img" name="image" value="${(goods.image)!}" tipId="tipSpan" class="require" data-title="请上传详情图片" style="display: none;"/>
                <#if (goods.image)??>
                    <div class="image">
                        <img src="${goods.image}"/>
                        <div class="del-btn">删除</div>
                    </div>
                </#if>
            </div>
        </div>

        <div class=" submit-box">
            <a id="save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/groupon/goods/list.vpage">取消</a>
        </div>
    </div>

</form>
</@module.page>