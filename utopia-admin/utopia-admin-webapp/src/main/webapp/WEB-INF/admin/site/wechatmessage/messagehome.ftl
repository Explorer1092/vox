<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>微信推送消息验证</legend>
        </fieldset>
        <ul class="inline">
            <li>
                字段名称： <input id="wecharType" name="wecharType" class="input-medium" type="text" placeholder="填写字段名称">
            </li>
            <li>
                <button id="wecharBut" type="button" class="btn btn-primary">查 询</button>
            </li>
        </ul>
    </div>

    <div id="data_table_wx">

    </div>

    <script type="text/html" id="data_table_wx_t">
        <% if (data.length > 0) { %>
            <div style="margin-bottom: 10px">
                <input id="hiddenType" type="hidden" value=""/>
                <button id="updateBut" type="button" class="btn btn-primary btn-warning">更 新</button>
                <button id="deleteBut" type="button" class="btn btn-primary btn-warning">删 除</button>
            </div>
        <% } %>

        <table class="table table-striped table-bordered so_checkboxs">
            <tr>
                <td>ID</td>
                <td>OPENID</td>
                <td>MESSAGE</td>
                <td>CREATETIME</td>
            </tr>
            <% if (data.length > 0) { %>
                <% for (var i = 0 ;i < data.length ; i++) { %>
                    <tr>
                        <td style="width: 50px;"><%=data[i].id%></td>
                        <td><%=data[i].openId%></td>
                        <td><%=data[i].message%></td>
                        <td style="width: 90px;"><%=data[i].createTime%></td>
                    </tr>
                <%}%>
            <% }else{ %>
                <tr>
                    <td>暂无数据</td>
                </tr>
            <% } %>
        </table>
    </script>
</div>

<script type="text/javascript">
    $(function(){
        $('#wecharBut').on('click',function(){
            var wecharType = $('#wecharType');
            if(wecharType.val() == ''){
                wecharType.focus();
                alert('字段名称');
                return false;
            }
            $.post('${requestContext.webAppContextPath}/site/wechatmessage/messagelist.vpage',{type : wecharType.val()},function(data){
                if(data.success){
                    var html = template("data_table_wx_t",{
                        data : data.notices
                    });
                    $("#data_table_wx").html(html);
                    $('#hiddenType').val(wecharType.val());
                    wecharType.val('');
                }
            });
        });

        //updateBut
        $(document).on('click','#updateBut',function(){
            $.post('${requestContext.webAppContextPath}/site/wechatmessage/update.vpage',{type : $('#hiddenType').val()},function(data){
                if(data.success){
                    alert('更新成功');
                    location.reload();
                }
            });
        });

        //deleteBut
        $(document).on('click','#deleteBut',function(){
            $.post('${requestContext.webAppContextPath}/site/wechatmessage/delete.vpage',{type : $('#hiddenType').val()},function(data){
                if(data.success){
                    alert('删除成功');
                    location.reload();
                }
            });
        });

    });
</script>

</@layout_default.page>
