<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=12>
<div id="main_container" class="span9">
    <legend>奖品管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" method="post"
                      action="${requestContext.webAppContextPath}/reward/product/productlist.vpage">
                    <fieldset class="form-group">
                        <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                        奖品ID：<input name="productId" id="productId" type="text" style="width:100px;" value="${productId!}"/>
                        奖品名称：<input name="productName" id="productName" type="text" style="width:100px;" value="${productName!}"/>
                        一级分类：<select style="font-size: 12px;width: 100px;" name="productType">
                        <option value="">全部</option>
                        <#if types?? >
                            <#list types as t >
                                <option <#if t.name() == productType>selected="selected"</#if>
                                        value="${t.name()!}">${t.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>
                        二级分类：<select style="font-size: 12px;width: 100px;" name="categoryId">
                        <option value="0">全部</option>
                        <#if categorys?? >
                            <#list categorys as c >
                                <option <#if c.id == categoryId>selected="selected"</#if>
                                        value="${c.id!}">${c.categoryName!}</option>
                            </#list>
                        </#if>
                    </select>
                        标签：<select style="font-size: 12px;width: 150px;" name="tagId">
                        <option value="0">全部</option>
                        <#if tags?? >
                            <#list tags as t >
                                <option <#if t.id == tagId>selected="selected"</#if>
                                        value="${t.id!}">${t.tagName!}</option>
                            </#list>
                        </#if>
                    </select>
                        是否上架：<select style="font-size: 12px;width: 70px;" name="onlined">
                        <option value="">全部</option>
                        <option <#if onlined == 'true'>selected="selected"</#if> value="true">是</option>
                        <option <#if onlined == 'false'>selected="selected"</#if> value="false">否</option>
                    </select>

                        学段：<select style="font-size: 12px;width: 70px;" name="schoolVisible">
                        <option value="">全部</option>
                        <option <#if schoolVisible == '1'>selected="selected"</#if> value="1">小学可见</option>
                        <option <#if schoolVisible == '2'>selected="selected"</#if> value="2">中学可见</option>
                    </select>

                        展示端:
                        <select style="font-size: 12px;width: 70px;" name="displayTerminal">
                            <option value="">全部</option>
                            <option <#if displayTerminal == 'PC'>selected="selected"</#if> value="PC">PC</option>
                            <option <#if displayTerminal == 'Mobile'>selected="selected"</#if> value="Mobile">移动</option>
                        </select>
                    </fieldset>

                    <div class="form-group">
                        <button id="selectTable" type="submit" class="btn btn-primary">查 询</button>
                        <a id="addproduct" href="addproduct.vpage" role="button" class="btn btn-success">添加</a>
                    </div>
                </form>

                <ul class="pager">
                    <#if (productPage.hasPrevious())>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">上一页</a></li>
                    </#if>
                    <#if (productPage.hasNext())>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${productPage.totalPages!} 页 |</li>
                    <li>共 ${productPage.totalElements!} 个商品</li>
                </ul>
                <div id="data_table_journal">
                    <table class="table table-striped table-bordered">
                        <tr>
                            <td>图片</td>
                            <td>ID</td>
                            <td>名称</td>
                        <#--<td>类型</td>-->
                            <td>学生价</td>
                            <td>老师价</td>
                            <td>进货价</td>
                            <td>已售</td>
                            <td>愿望</td>
                            <td>学生排序值</td>
                            <td>老师排序值</td>
                            <td>学生可见</td>
                            <td>老师可见</td>
                            <td>库存</td>
                            <td>上架</td>
                            <td>备注</td>
                            <td>操作</td>
                        </tr>
                        <#if productPage.content?? >
                            <#list productPage.content as product >
                                <tr>
                                    <td><img src="${product.img!}" width="60" style="height: 60px"/></td>
                                    <td>${product.id!}</td>
                                    <td>${product.productName!}</td>
                                <#--<td><#if product.productType == 'JPZX_SHIWU'>实物<#else>体验</#if></td>-->
                                    <td>${product.priceS!}</td>
                                    <td>${product.priceT!}</td>
                                    <td>${product.buyingPrice!}</td>
                                    <td>${product.soldQuantity!}</td>
                                    <td>${product.wishQuantity!}</td>
                                    <td>
                                        <input type="text" value="${product.studentOrderValue!}" style="width: 30px;"
                                               class="JS-setOrderValue" data-type="student"
                                               data-proid="${product.id!}"/>
                                    </td>
                                    <td>
                                        <input type="text" value="${product.teacherOrderValue!}" style="width: 30px;"
                                               class="JS-setOrderValue" data-type="teacher"
                                               data-proid="${product.id!}"/>
                                    </td>
                                    <td><#if product.studentVisible>可见<#else>不可见</#if></td>
                                    <td><#if product.teacherVisible>可见<#else>不可见</#if></td>
                                    <td>${product.inventory!}</td>
                                    <td><#if product.onlined>是<#else>否</#if></td>
                                    <td style="max-width:100px;">${product.remarks!}</td>
                                    <td>
                                        <a name="editProduct" href="editproduct.vpage?productId=${product.id!}"
                                           role="button" class="btn btn-primary" style="font-size: 12px;" target="_blank">编辑</a>
                                        <a name="uploadPhotoButton"
                                           href="productimagelist.vpage?productId=${product.id!}" role="button"
                                           class="btn btn-warning" style="font-size: 12px;" target="_blank">图片</a>
                                        <a name="targetBtn" role="button" class="btn" href="producttarget.vpage?productId=${product.id}&&fromPage=${pageNumber!}">投放</a>
                                        <br/>
                                        <a name="onLined" data-content-id="${product.id!}" role="button"
                                           class="btn btn-success" style="font-size: 12px;">上架</a>
                                        <a name="downLined" data-content-id="${product.id!}" role="button"
                                           class="btn btn-danger" style="font-size: 12px;">下架</a>
                                        <#if product?? && product.productType?? && product.productType == 'JPZX_TIYAN'>
                                            <a name="importDetail"
                                               href="couponimportindex.vpage?productId=${product.id!}" role="button"
                                               class="btn btn-inverse" style="font-size: 12px;" target="_blank">导入</a>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
                <ul class="pager">
                    <#if (productPage.hasPrevious())>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">上一页</a></li>
                    </#if>
                    <#if (productPage.hasNext())>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${productPage.totalPages!} 页</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    $(function () {
        $("a[name='onLined']").on('click', function () {
            var productId = $(this).attr("data-content-id");
            $.post("updownlined.vpage", {productId: productId, onLined: true}, function (data) {
                if (data.success) {
                    alert(data.info);
                } else {
                    alert(data.info);
                }
            });
        });
        $("a[name='downLined']").on('click', function () {
            var productId = $(this).attr("data-content-id");
            $.post("updownlined.vpage", {productId: productId, onLined: false}, function (data) {
                if (data.success) {
                    alert(data.info);
                } else {
                    alert(data.info);
                }
            });
        });

        var recordOrderValue = null;

        $(document).on({
            blur: function () {
                var $this = $(this);

                if (recordOrderValue == $this.val()) {
                    return false;
                }

                $.post("/reward/product/updateordervalue.vpage", {
                    productId: $this.attr("data-proid"),
                    valueType: $this.attr("data-type"),
                    orderValue: $this.val()
                }, function (data) {
                    if (data.success) {
                        //成功
                        recordOrderValue = null;
                    } else {
                        alert(data.info);
                    }
                });
            },
            focus: function () {
                $(this).select();
                recordOrderValue = $(this).val();
            }
        }, ".JS-setOrderValue");

    });
</script>
</@layout_default.page>