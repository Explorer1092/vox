<#import "../../module.ftl" as module>
<@module.page
title="专题管理"
pageJsFile={"siteJs" : "public/script/basic/topic"}
pageJs=["siteJs"]
leftMenu="专题管理"
>
<div class="bread-nav">
    <a class="parent-dir" href="/groupon/topic/index.vpage">专题列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">专题编辑</a>
</div>
<h3 class="h3-title">
    专题编辑
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span> 为必填项</span>
</h3>
<form action="/common/uploadphoto.vpage" method="post" style="display: none;">
    <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*"/>
    <input id="topic-id" value="${(topic.id)!}" name="tid" />
</form>


<form id="detail-form" action="savetopic.vpage" method="post">
    <div style="float:left;">
        <div class="input-control">
            <label>专题ID：</label>
            <input name="topicId" value="<#if topic??>${(topic.id)!}</#if>" style="outline: none;" class="readonly" placeholder="(保存后自动生成)"/>
        </div>
        <div class="input-control">
            <label>专题类型：</label>
            <label>
                <input type="radio" name="type" id="type" value="to_detail" <#if (topic.type)??><#if topic.type='to_detail'>checked</#if><#else>checked</#if> >商品专题
            </label>
            <label>   <input type="radio" name="type" id="type" value="outer_url" <#if (topic.type)??&&topic.type='outer_url'>checked</#if>>链接专题 </label>

        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>专题位置：</label>
            <select name="position" id="position" class="v-select require" style="width:300px;">
                <option value="BANNER" <#if topic.position?? && topic.position == 'BANNER'> selected</#if>>顶部头图</option>
                <option value="LEFT" <#if topic.position?? && topic.position == 'LEFT'> selected</#if>>左侧图</option>
                <option value="RIGHT_TOP" <#if topic.position?? && topic.position == 'RIGHT_TOP'> selected</#if>>右上图</option>
                <option value="RIGHT_BOTTOM" <#if topic.position?? && topic.position == 'RIGHT_BOTTOM'> selected</#if>>右下图</option>
            </select>
        </div>
        <#if (topic.type)??>
            <#if topic.type='to_detail'>
            <div class="input-control" id="urlDiv" >
                <label>跳转URL：</label>
                <input  id="url" name="url"  data-title="跳转url" readonly value="/groupon/special.vpage<#if topic.id??>?stId=${topic.id}</#if>"/>
            </div>
            <#else>
            <div class="input-control" id="urlDiv" >
                <label>跳转URL：</label>
                <input  id="url" name="url" class="require" data-title="跳转url" placeholder="http(s)://开头" value="${(topic.url)!''}" />
            </div>
            </#if>
        <#else>
            <div class="input-control" id="urlDiv" style="display: none">
                <label>跳转URL：</label>
                <input id="url"  name="url" id="url" readonly data-title="跳转url" placeholder="http(s)://开头"/>
            </div>
        </#if>

        <div class="input-control">
            <label><span class="red-mark">*</span>封面图：</label><span id="spanCoverImg" class="upload-tip">请上传封面图，不超过500kb</span>
            <#if editable!false>
            <a class="blue-btn upload-image" refId="cover-img"  href="javascript:void(0)"  >上传照片</a>
            </#if>
        </div>
        <input id="cover-img" style="display: none;" name="coverImg" value="${(topic.coverImg)!}" tipId ="spanCoverImg"/>
        <div class="image-preview coverImg clearfix">
            <#if topic?? && topic.coverImg?has_content>
                <div class="image">
                    <img src="${(topic.coverImg)!}" />
                    <#if editable!false>
                    <div class="del-btn">删除</div>
                    </#if>
                </div>
            </#if>
        </div>
    </div>
    <div style="float:right;">
        <div class="input-control">
            <label><span class="red-mark">*</span>专题名称：</label>
            <input name="name" class="require item" style="width: 300px;" <#if (topic.name)??>value="${(topic.name)!}"</#if> data-title="专题名称" placeholder="请填写专题名称 " />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>优先级：</label>
            <select name="orderIndex" class="v-select require" style="width:300px;">
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
            <label><span class="red-mark">*</span>开始时间：</label>
            <input id="startTime" name="startTime" class="item" style="width: 300px;" <#if (topic.startTime)??> value="${(topic.startTime)?string('yyyy-MM-dd hh:mm:ss')}"</#if> class="require" data-title="开始时间"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>结束时间：</label>
            <input id="endTime" name="endTime" class="item" style="width: 300px;" <#if (topic.endTime)??>value="${(topic.endTime)?string('yyyy-MM-dd hh:mm:ss')}"</#if> class="require" data-title="结束时间"/>
        </div>
        <div id="special_detailImg_div" <#if topic.type??&& topic.type=='outer_url'>style="display: none;"</#if>>
        <div class="input-control">
            <label>专题头图：</label><span id="spanDetailImg" class="upload-tip">请上传专题头图，不超过500kb</span>
            <#if editable!false>
            <a class="blue-btn upload-image" refId="detail-img" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <input id="detail-img" style="display: none;" name="detailImg"  value="${(topic.detailImg)!}"  tipId ="spanDetailImg" />
        <div class="image-preview detailImg clearfix">
            <#if topic?? && topic.detailImg?has_content>
            <div class="image">
                <img src="${(topic.detailImg)!}" />
                <#if editable!false>
                <div class="del-btn">删除</div>
                </#if>
            </div>
            </#if>
        </div>
        </div>
    </div>

        <div id="special_goods_div"  style="clear:both;<#if topic.type??&& topic.type=='outer_url'>display: none;</#if>">
        <div class="input-control" id="goods-input">
            <label><span class="red-mark">*</span>专题商品：</label><span class="upload-tip">请填写专题商品，多个以英文逗号分隔</span>
            <a class="blue-btn groupon-btn" href="javascript:void(0)">填写专题商品</a>
        </div>
        <div class="input-control">
            <label>商品列表：</label>
        </div>
        <div class="op-wrapper clearfix">
            <table class="data-table" style="width:715px;border:none;" id="goodsList">
                <#if goodsList?? && goodsList?size gt 0>
                <#list goodsList as goods>
                    <tr id="row_${goods.id!}">
                        <td style="width:140px;">${goods.id!}</td>
                        <td>${goods.title!}</td>
                        <td style="width:70px;">
                            <input id="input_${goods.id!}" name="grouponGoodsIdList" type="hidden" value="${goods.id!''}">
                            <a data-gid="${goods.id!}" class="op-btn del-goods" href="javascript:void(0)" style="float: none; display: inline-block;">删除</a>
                        </td>
                    </tr>
                </#list>
                </#if>
            </table>
        </div>
    </div>

    <div class="clearfix submit-box" style="clear:both;">
         <a id="save-btn" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/groupon/topic/index.vpage">返回</a>
    </div>
</form>
</@module.page>