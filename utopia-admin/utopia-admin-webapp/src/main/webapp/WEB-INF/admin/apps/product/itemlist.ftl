<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=10>
    <#import "../../mizar/pager.ftl" as pager />
<div id="main_container" class="span9">
    <legend>
        <strong>子商品管理</strong>
        <a id="add_product_btn" href="itemdetail.vpage" type="button" class="btn btn-info" style="float: right">
            <i class="icon-plus icon-white"></i>添加
        </a>
    </legend>
    <form id="activity-query" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/appmanager/product/itemlist.vpage" >
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="itemlist.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            名称：<input type="text" id="name" name="name" value="<#if name??>${name}</#if>" placeholder="输入名称">
                        </li>
                        <li>
                            类别：<select id="productType" name="productType">
                            <option value="">全部</option>
                            <#list productTypes as c>
                                <option value="${c.name()!}" <#if productType?? && c.name() == productType>selected</#if> >${c.name()!}</option>
                            </#list>
                        </select>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查  询
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th width="300">名称</th>
                        <th>类型</th>
                        <th>销售方式</th>
                        <th>有效期</th>
                        <th>价格</th>
                        <th>激活方式</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if itemPage?? && itemPage.content?? >
                            <#list itemPage.content as p >
                            <tr>
                                <td><pre>${p.id!}</pre></td>
                                <td><pre>${p.name!}</pre></td>
                                <td><pre>${p.productType!}</pre></td>
                                <td><pre>${p.salesType!}</pre></td>
                                <td><pre>${p.period!}</pre></td>
                                <td><pre>${p.originalPrice!}</pre></td>
                                <td><pre>${p.activeType!}</pre></td>
                                <td>
                                    <a type="button" class="btn btn-info" href="itemdetail.vpage?id=${p.id!''}">
                                        <i class="icon-edit icon-white"></i>编辑
                                    </a>
                                    <a type="button" class="btn btn-info delete-btn" href="javascript:void(0)" data-id="${p.id!''}">
                                        <i class="icon-remove icon-white"></i>删除
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<script type="text/javascript">
    $(function(){
        $(document).on('click','.delete-btn',function(){
            if(!confirm("确定删除商品吗？")){
                return;
            }
            var $this=$(this);
            $.post('deleteproductitem.vpage',{id:$this.attr("data-id")},function(res){
                if(res.success){
                    alert("操作成功");
                    window.location.reload();
                }else{
                    alert(res.info);
                }
            });
        });
    });
</script>
</@layout_default.page>