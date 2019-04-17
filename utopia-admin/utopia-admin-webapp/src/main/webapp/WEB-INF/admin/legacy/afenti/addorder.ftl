<#import "../../layout_default.ftl" as layout_default>

<@layout_default.page page_title="应用订单" page_num=3>
<div class="span9">

    <form method="post" action="main.vpage?" class="form-horizontal" id="searchOrder">
        <legend>新增订单</legend>
        <ul class="inline">
            <li>
                <label for="accountInput">
                    用户ID：
                    <input id="accountInput"  name="userId" type="text" value="${(user.id)!''}" />
                </label>
            </li>
            <li>
                <label for="productTypeInput">
                    商品类别：<select id="productType" name="productType">
                    <option value="">全部</option>
                    <#list productTypes as c>
                        <option value="${c.name()!}"
                                <#if productType?? && c.name() == productType>selected</#if>>${c.name()!}</option>
                    </#list>
                </select>
                </label>
            </li>

            <li>
                <label for="productNameInput">
                    产品名称：
                    <input id="productNameInput"  name="productName" type="text" value="${productName!''}" />
                </label>
            </li>

            <li>
                <a href="#myModal" role="button" class="btn btn-primary" id="addOrderBtn" data-toggle="modal">新增订单</a>
            </li>
        </ul>
    </form>

    <#if user??>
        <p>
            <span class="label label-important">用户ID: ${user.id}</span>
            <span class="label label-info">用户名: ${(user.profile.realname)!''}</span>
        </p>
    </#if>

    <#if message??>
        <div class="alert alert-error">
            系统消息：${message}
        </div>
    </#if>

    <#if availableProducts?has_content>
        <h3>${(user.profile.realname)!''} 的订单</h3>
        <table class="table table-hover table-striped  table-bordered">
            <tr>
                <th>产品名称</th>
                <th>产品ID</th>
                <th>原价</th>
                <th>网上支付价格</th>
                <th>产品类型</th>
                <th nowrap="1">操作</th>
            </tr>
            <#list availableProducts as one>
                <tr>
                    <td>${one.productName!''}</td>
                    <td>${one.productId!''}</td>
                    <td>${one.totalPriceOriginal!''}元</td>
                    <td>${one.totalPriceGeneric!''}元</td>
                    <td>${one.productServiceType!''}</td>
                    <td nowrap="1">
                        <a product-id='${one.productId}' class='order-product' href='javascript:void(0);'>生成订单</a>
                    </td>
                </tr>
            </#list>
        </table>
    </#if>

    <div id="myModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h5>温馨提示</h5>
        </div>
        <div class="modal-body">
            <p>请输入有效的用户ID。</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
        </div>
    </div>
    <script>

        $(function(){
            $('.order-product').click(function(){
                var productId = $(this).attr('product-id');
                var userId = ${user.id!''};
                var data = {productId: productId,userId: userId};
                $.post('postorder.vpage',data , function(data){
                    alert(data.message);
                    window.location.href = 'addorder.vpage?userId=' + ${user.id};
                });
                return false;
            });

            $("#addOrderBtn").on('click', function(){
                var postUserId = $('#accountInput').val();
                var productType = $('#productType').val();
                var productName = $('#productNameInput').val();
                if( !isNaN(parseInt(postUserId, 10)) && postUserId != ""){
                    postUserId = "addorder.vpage?userId=" + postUserId+"&productType="+productType+"&productName="+productName;
                    location.href = postUserId;
                    return false;
                }
            });
        });
    </script>
</div>
</@layout_default.page>