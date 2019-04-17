<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-推送管理' page_num=13>
<div class="span9">
    <fieldset>
        <legend>推送管理</legend>
        <div style="padding-bottom: 10px">
            <a class="btn btn-primary" href="${requestContext.webAppContextPath}/advisory/pushedit.vpage" id="contentCreateBtn">新建推送</a>
        </div>
    </fieldset>

    <fieldset>
        <div id="pushManageBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<script type="text/html" id="pushManageBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>序号</th>
            <th>推送ID</th>
            <th>创建时间</th>
            <th>推送时间</th>
            <th>阅读数</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
            <%for(var i = 0; i < content.length;i++){%>
                <tr>
                    <td><%=i+1%></td>
                    <td><%=content[i].pushRecordId%></td>
                    <td><%=content[i].createTime%></td>
                    <td><%=content[i].pushTime%></td>
                    <td><%=content[i].count%></td>
                    <td data-pid="<%=content[i].pushRecordId%>">
                        <a class="btn btn-info" href="pushedit.vpage?pushRecordId=<%=content[i].pushRecordId%>">编辑</a>
                        <%if(content[i].isOnline){%>
                            <button class="btn btn-primary offlinePushRecordBtn">取消推送</button>
                        <%}else{%>
                            <button class="btn btn-success onlinePushRecordBtn">推送</button>
                        <%}%>

                    </td>
                </tr>
            <%}%>
        <%}else{%>
            <tr>
                <td colspan="6">暂无数据</td>
            </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">
    function getPushManageList(page) {
        $.post('getPushManageList.vpage', {currentPage: page}, function (data) {
            if (data.success) {
                $('#pushManageBox').html(template("pushManageBox_tem", {
                    content : data.pushRecordList
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        getPushManageList(index);
                    }
                });
            }
        });
    }
    $(function () {
        //初始化
        getPushManageList(1);

        //上线
        $(document).on('click','.onlinePushRecordBtn',function(){
            var $this = $(this);
            var pid = $this.closest('td').data('pid');
            if(confirm("确定推送？")){
                $.post("onlinepushrecord.vpage",{pushRecordId: pid},function(data){
                    if(data.success){
                        getPushManageList(1);
                    }else{
                        alert(data.info);
                    }

                });
            }
        });

        //下线
        $(document).on('click','.offlinePushRecordBtn',function(){
            var $this = $(this);
            var pid = $this.closest('td').data('pid');
            if(confirm("确定取消推送？")){
                $.post("offlinepushrecord.vpage",{pushRecordId: pid},function(data){
                    if(data.success){
                        getPushManageList(1);
                    }else{
                        alert(data.info);
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>