<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="用户管理" page_num=16>
<div id="main_container" class="span9">
    <legend>
        <strong>${account.name!'公众号'}--用户管理</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form class="form-inline" action="usermanagement.vpage" method="get">
                    <div class="form-group" style="display:none">
                        <label for="exampleInputName2">id:</label>
                        <input type="text" class="form-control query-input date-input" name="accountId" value="${accountId!0}">
                    </div>
                    <div class="form-group">
                        <label for="exampleInputEmail2">用户ID:</label>
                        <input name="userId" type="text" class="query-input" value="${userId!''}" required>
                    </div>
                    <div class="form-group">
                        <button class="btn" type="submit" id="query-btn">查询</button>
                    </div>
                    <#if isTopAuditor>
                        <div class="form-group">
                            <button class="btn" type="button" id="query-btn" data-toggle="modal" data-target="#import-users-dialog">导入关注用户</button>
                        </div>
                    </#if>
                </form>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="120px">序号</th>
                        <th>家长ID</th>
                        <th>区域</th>
                        <th>关注日期</th>
                        <th>主动关注</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if userRef??>
                            <td>${userRef.id}</td>
                            <td>${userRef.userId}</td>
                            <td>${userRef.region!''}</td>
                            <td>${userRef.updateDatetime}</td>
                            <td>
                            <#switch userRef.status>
                                <#case 'AutoFollow'>
                                    导入关注
                                    <#break/>
                                <#case 'Follow'>
                                    主动关注
                                    <#break/>
                                <#case 'UnFollow'>
                                    取消关注
                                    <#break/>
                            </#switch>
                            </td>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- 用户导入的窗口 -->
    <div id="import-users-dialog" class="modal fade hide" aria-hidden="true" style="display:none">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">导入关注用户</h3>
            </div>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <div class="form-horizontal">
                    <form id="config-admins-frm" action="save.vpage" method="post">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">用户列表:</label>
                            <div class="controls">
                                <textarea id="import-user-list" rows="10" placeholder="一行输入一条数据"></textarea>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="import-follow-btn" type="button" class="btn btn-primary">导入</button>
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

    .form-group{
        display: inline-block;
    }

    .query-input{
        width:100px;
    }

    ul.content-list > li{
        float:left;
    }

    img.source-img{
        width:66px;
        height:36px;
    }

    .article-part{
        display:inline-block;
    }

</style>
<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#activity-query").submit();
    }
    $(function(){

        $("#revoke-btn").on('click',function(){
            var $this=$(this),
                data={
                    bundleId  : $this.attr("data-buddleId"),
                    accountId : ${accountId!0}
                };
            $.post("articleoffline.vpage",data,function(res){
                if(res.success){location.reload();}
            });
        });

        $(".date-input").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#publish-btn").click(function(){
            var bundleId = $(this).data("buddleid");
            $.post("publisharticle.vpage",{"bundleId":bundleId},function(data){
                if(data.success){
                    alert("发布成功!");
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        $("button#import-follow-btn").click(function() {
            if($("#import-user-list").val().trim() == ''){
                alert('内容不能为空！');
                return;
            }

            $.post("importfollowaccountusers.vpage",
                    {
                        "accountId":${accountId!'0'},
                        "userIds": $("#import-user-list").val()
                    },
                    function (data) {
                        if (data.success) {
                            alert("导入完成，不存在的id列表："+data.notExistIdList + ",不能重复关注的id列表："+data.existUserList);
                            $("#import-users-dialog").modal('hide');
                        } else {
                            alert(data.info);
                        }
                    });
        });

    });
</script>
</@layout_default.page>