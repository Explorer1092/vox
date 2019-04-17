<#import "../../module.ftl" as module>
<@module.page
title="专题管理"
pageJsFile={"siteJs" : "public/script/basic/topic"}
pageJs=["siteJs"]
leftMenu="专题管理"
>
<h3 class="h3-title">
    专题详情
</h3>
    <div style="float:left;">
        <div class="input-control">
            <label>专题ID：</label>
            <input name="topicId" value="<#if topic??>${(topic.id)!}</#if>" class="disabled item" style="width: 300px;" disabled="disabled" placeholder="(保存后自动生成)"/>
        </div>
        <div class="input-control">
            <label>专题类型：</label>
            <label>
                <input type="radio" name="type" disabled="disabled" value="to_detail" <#if (topic.type)??><#if topic.type='to_detail'>checked</#if><#else>checked</#if> >商品专题
                <input type="radio" name="type" disabled="disabled" value="outer_url" <#if (topic.type)??&&topic.type='outer_url'>checked</#if>>链接专题
            </label>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>专题位置：</label>
            <select name="position" class="v-select"  disabled="disabled" style="width:300px;">
                <option value="">-请选择专题位置-</option>
                <option value="BANNER" <#if topic?? && topic.position == 'BANNER'> selected</#if>>顶部头图</option>
                <option value="LEFT" <#if topic?? && topic.position == 'LEFT'> selected</#if>>左侧图</option>
                <option value="RIGHT_TOP" <#if topic?? && topic.position == 'RIGHT_TOP'> selected</#if>>右上图</option>
                <option value="RIGHT_BOTTOM" <#if topic?? && topic.position == 'RIGHT_BOTTOM'> selected</#if>>右下图</option>
            </select>
        </div>

        <div class="input-control">
            <label>跳转URL：</label>
            <input name="url" value="${(topic.url)!}" class="disabled item" style="width: 300px;" disabled="disabled"/>
        </div>
        <div class="input-control">
            <label>封面图：</label><span class="upload-tip">请上传封面图，不超过500kb</span>
            <#if editable!false>
            <a class="blue-btn upload-image" disabled="disabled" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <input id="cover-img" style="display: none;" name="coverImg" disabled="disabled" />
        <div class="image-preview coverImg clearfix">
            <#if topic?? && topic.coverImg?has_content>
                <div class="image">
                    <img src="${(topic.coverImg)!}" />
                </div>
            </#if>
        </div>
    </div>
    <div style="float:right;">
        <div class="input-control">
            <label><span class="red-mark">*</span>专题名称：</label>
            <input name="name" class="disabled item" style="width:300px;" disabled="disabled" value="${(topic.name)!}" data-title="专题名称" placeholder="请填写专题名称 " />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>优先级：</label>
            <select name="orderIndex" class="v-select "  disabled="disabled"  style="width:300px;">
                <option value="">-请选择优先级-</option>
                <option value="0" <#if (topic.orderIndex)?? && (topic.orderIndex) == 0> selected</#if>>默认</option>
                <option value="1" <#if (topic.orderIndex)?? && (topic.orderIndex) == 1> selected</#if>>Lv1</option>
                <option value="2" <#if (topic.orderIndex)?? && (topic.orderIndex) == 2> selected</#if>>Lv2</option>
                <option value="3" <#if (topic.orderIndex)?? && (topic.orderIndex) == 3> selected</#if>>Lv3</option>
                <option value="4" <#if (topic.orderIndex)?? && (topic.orderIndex) == 4> selected</#if>>Lv4</option>
                <option value="5" <#if (topic.orderIndex)?? && (topic.orderIndex) == 5> selected</#if>>Lv5</option>
                <option value="6" <#if (topic.orderIndex)?? && (topic.orderIndex) == 6> selected</#if>>Lv6</option>
                <option value="7" <#if (topic.orderIndex)?? && (topic.orderIndex) == 7> selected</#if>>Lv7</option>
                <option value="8" <#if (topic.orderIndex)?? && (topic.orderIndex) == 8> selected</#if>>Lv8</option>
                <option value="9" <#if (topic.orderIndex)?? && (topic.orderIndex) == 9> selected</#if>>Lv9</option>
            </select>
        </div>

        <div class="input-control">
            <label>开始时间：</label>
            <input id="startTime" name="startTime" value="${(topic.startTime)!}" class="disabled item" style="width: 300px;"  disabled="disabled" data-title="开始时间"/>
        </div>
        <div class="input-control">
            <label>结束时间：</label>
            <input id="endTime" name="endTime" value="${(topic.endTime)!}" class="disabled item" style="width: 300px;" disabled="disabled" data-title="结束时间"/>
        </div>
        <div id="special_detailImg_div" <#if topic.type??&& topic.type=='outer_url'>style="display: none;"</#if>>
        <div class="input-control">
            <label>专题头图：</label><span class="upload-tip">请上传专题头图，不超过500kb</span>
        </div>
        <input id="detail-img" style="display: none;" name="detailImg" />
        <div class="image-preview detailImg clearfix">
            <#if topic?? && topic.detailImg?has_content>
            <div class="image">
                <img src="${(topic.detailImg)!}" />
            </div>
            </#if>
        </div>
        </div>
    </div>
        <div id="special_goods_div"  style="clear:both;<#if topic.type??&& topic.type=='outer_url'>display: none;</#if>">
        <div class="input-control" id="goods-input">
            <label><span class="red-mark">*</span>专题商品：</label>
            <#if goodsList?? && goodsList?size gt 0>
                <#list goodsList as g>
                    <input id="input_${g.id!}" name="grouponGoodsIdList" type="hidden" value="${g.id!''}" readonly="readonly">
                </#list>
            </#if>
            <#--<textarea name="grouponGoodsIdList" data-title="专题商品" class="require"-->
                      <#--style="resize: none" rows="3" placeholder="填写商品ID,逗号分隔">${(topic.grouponGoodsIdList)!?join(",")}</textarea>-->
        </div>
        <div class="input-control">
            <label>商品列表：</label>
        </div>
        <div class="op-wrapper clearfix">
            <table class="data-table" style="width:715px;" id="goodsList">
                <#if goodsList?? && goodsList?size gt 0>
                <#list goodsList as goods>
                    <tr id="row_${goods.id!}">
                        <td style="width:140px;">${goods.id!}</td>
                        <td>${goods.title!}</td>
                    </tr>
                </#list>
                </#if>
            </table>
        </div>
    </div>
    <div class="clearfix submit-box" style="clear:both;">
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/groupon/topic/index.vpage">返回</a>
    </div>
</@module.page>