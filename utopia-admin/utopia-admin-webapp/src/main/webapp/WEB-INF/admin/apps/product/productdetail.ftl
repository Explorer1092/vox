<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加新商品" page_num=10>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加新商品&nbsp;&nbsp;
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                    <input id="productId" name="id" value="${id!}" type="hidden" />
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">名称<span style="font-size: 12px;color: red;">*必填</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control input_txt" value="<#if product?? && product?size gt 0>${product.name!}</#if>" />
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">描述</label>
                            <div class="controls">
                                <textarea id="desc" name="desc" class="intro_small"  placeholder="请填写描述"><#if product?? && product?size gt 0>${product.desc!}</#if></textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">产品类型</label>
                            <div class="controls">
                                <select id="productType" name="productType" class="option">
                                    <#list productTypes as s>
                                        <option  value="${s.name()!}" <#if s.name()==(product.productType)!>selected</#if>>${s.name()!}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">销售方式</label>
                            <div class="controls">
                                <select name="salesType">
                                    <#list saleTypes as s>
                                        <option value="${s.name()!}" <#if s.name()==(product.salesType)!>selected</#if>>${s.name()!}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>

                        <div id="itemDiv" class="control-group">
                            <label class="col-sm-2 control-label">关联子商品</label>
                            <input type="hidden" name="refIds" id="refIds" />
                            <div id="childItems" class="controls">
                                <table class="table table-striped table-condensed table-bordered" style="width: 700px;">
                                    <thead>
                                        <th width="150px;">产品类型</th>
                                        <th width="300px;">子产品</th>
                                        <th>操作</th>
                                    </thead>
                                    <tbody id="itemTable">
                                        <#if itemList?? && itemList?size gt 0>
                                            <#list itemList as item>
                                            <tr class="item-tr">
                                                <td>
                                                    <select name="item-product-type" data-id="${(item.id)!}" data-ref="sub_${item_index}" class="option product-type-option itemProductType">
                                                        <#list productTypes as s>
                                                            <option value="${s.name()!}" <#if s.name()==(item.productType)!>selected</#if>>${s.name()!}</option>
                                                        </#list>
                                                    </select>
                                                </td>
                                                <td>
                                                    <select name="item-obj" id="sub_${item_index}" class="option item-option">
                                                    </select>
                                                </td>
                                                <td><a href="javascript:void(0);" class="del-item">删除</a></td>
                                            </tr>
                                            </#list>
                                        </#if>
                                    </tbody>
                                </table>
                                <a class="btn btn-warning" id="productAddBtn">
                                    <i class="icon-plus icon-white"></i>添加子产品
                                </a>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">价格</label>
                            <div class="controls">
                                <input type="text" id="price" name="price" class="form-control input_txt" value="<#if product?? && product?size gt 0>${product.price!}</#if>" />
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">原价</label>
                            <div class="controls">
                                <input type="text" id="originalPrice" name="originalPrice" class="form-control input_txt" value="<#if product?? && product?size gt 0>${product.originalPrice!}</#if>" />
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分类名称</label>
                            <div class="controls">
                                <input type="text" id="category" name="category" class="form-control input_txt" value="<#if product?? && product?size gt 0>${product.category!}</#if>" />
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">扩展属性</label>
                            <div class="controls">
                                <textarea id="attributes" name="attributes"><#if product?? && product?size gt 0>${product.attributes!}</#if></textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">状态</label>
                            <div class="controls">
                                <select id="status" name="status" class="form-control">
                                    <option value="ONLINE" <#if (product.status)?? && product.status == 'ONLINE'>selected</#if>>ONLINE</option>
                                    <option value="OFFLINE" <#if (product.status)?? && product.status == 'OFFLINE'>selected</#if>>OFFLINE</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否实物商品</label>
                            <div class="controls">
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" name="actualGoods"  style="position: relative;top:-3px;" <#if product??><#if product.actualGoods>checked="checked"</#if><#else>checked="checked"</#if> /> 是</label>
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" <#if (!product.actualGoods)!false>checked="checked"</#if> value="false" name="actualGoods" style="position: relative;top:-3px;" /> 否</label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="T:NEW_PRODUCT">
    <tr class="item-tr">
        <td>
            <select name="item-product-type" data-ref="sub_<%=itemIndex%>" class="option product-type-option itemProductType">
                <#list productTypes as s>
                    <option value="${s.name()!}">${s.name()!}</option>
                </#list>
            </select>
        </td>
        <td>
            <select name="item-obj" id="sub_<%=itemIndex%>" class="option item-option">
            </select>
        </td>
        <td><a href="javascript:void(0);" class="del-item">删除</a></td>
    </tr>
</script>
<script type="text/html" id="T:SUB_PRODUCT">
    <%if(itemList && itemList.length > 0){%>
    <%for(var t = 0,tLen = itemList.length; t < tLen; t++){%>
        <option value="<%=itemList[t].id%>" <%if(focusId != null && itemList[t].id == focusId){%>selected<%}%>><%=itemList[t].name%></option>
    <%}%>
    <%}%>
</script>
<script type="text/javascript">
    var productLine = <#if itemList?? && itemList?size gt 0>${itemList?size}<#else >0</#if>;
    $(function () {
        $(document).on('change','.itemProductType', function() {
            var $this = $(this),productType = $this.val(),subId = ($this.attr("data-id") || ""),$subProduct = $('#' + $this.attr("data-ref"));

            $.post('loaditems.vpage',{productType:productType},function(data){
                if(data.success){
                    if(data.itemList && data.itemList.length > 0){
                        $subProduct.html(template("T:SUB_PRODUCT",{itemList : data.itemList,focusId : subId}));
                    }else{
                        $subProduct.html("");
                    }
                }
            });

        });

        $(".itemProductType").each(function(index,value){
            $(this).change();
        });


        $("#productAddBtn").on("click",function(){
            productLine++;
            var htmlStr = template("T:NEW_PRODUCT",{itemIndex : productLine});
            $(htmlStr).appendTo("#itemTable");
            $('select[data-ref="sub_'  + productLine + '"]').change();
        });

        $(document).on('click', ".del-item", function () {
            $(this).parent().parent().remove();

        });

        $('#save_info').on('click', function () {
            // 做数据校验
            var name = $("#name").val();
            if (name == '') {
                alert("请输入名称");
                return false;
            }
            var price = $("#price").val();
            if (price == '') {
                alert("请输入价格");
                return false;
            }
            var desc = $("#desc").val();
            if (desc == '') {
                alert("请输入描述");
                return false;
            }

            var arr = [];
            $("select[id^='sub_']").each(function(index,value){
                var subVal = $(this).val();
                typeof subVal === "string" && subVal && (arr.push($(this).val()));
            });
            if(arr.length == 0){
                alert("请选择至少一个子产品");
                return false;
            }
            var str = arr.join(",");
            // 保存商品信息
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'save.vpage',
                success: function (data) {
                    if (data.success) {
                        $.post('saveitemrefs.vpage',{productId:data.id,itemIds:str},function(res){
                            if(res.success){
                                alert("保存成功");
                                window.location.href = "productdetail.vpage?id=" + data.id;
                            }else{
                                alert('保存子商品信息失败:' + res.info);
                            }
                        });
                    } else {
                        alert("保存失败:" + data.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });


        });

    });
</script>
</@layout_default.page>