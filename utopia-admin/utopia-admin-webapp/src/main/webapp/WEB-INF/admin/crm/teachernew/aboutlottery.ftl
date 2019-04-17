<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">
    <div style="margin-top: 2em">
        <div class="control-group">
            <label class="control-label"  for="_campaignId">选择活动</label>
            <div class="controls">
                <select name="campaignId" id="campaignId" class="select">
                    <option value="51" <#if (campaignId == 51)!false>selected</#if>>17奖学金抽奖</option>
                    <option value="54" <#if (campaignId == 54)!false>selected</#if>>六一点读机抽奖</option>
                    <option value="57" <#if (campaignId == 57)!false>selected</#if>>开学大礼包抽奖</option>
                    <option value="64" <#if (campaignId == 64)!false>selected</#if>>初中英语老师布置作业抽奖</option>
                </select>
            </div>
        </div>
        <legend>老师剩余免费抽奖次数：${(freeCount)!0}</legend>
        <legend>添加抽奖次数</legend>
        <form id="addFreeCount" class="well form-horizontal" style="background-color: #fff;">
            <fieldset>
                <div class="control-group">
                    <label class="control-label"  for="cleanupBindedMobile_mobile">输入次数</label>
                    <div class="controls">
                        <input type="text" name="freeCount" value="" id="freeCount" class="input"/>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <input type="button" id="addFreeCount_Button" value="添加" class="btn btn-primary"/>
                    </div>
                </div>
            </fieldset>
        </form>
        <legend>抽奖记录查询</legend>
        <table class="table table-bordered">
            <tr>
                <th>抽奖时间</th>
                <th>获奖等级</th>
                <th>奖品名称</th>
            </tr>
        <#if histories?has_content>
            <#list histories as his >
                <tr>
                    <td>${his.createDatetime!}</td>
                    <td>${his.awardId!}</td>
                    <td>${his.awardName!}</td>

                </tr>
            </#list>
        <#else ><td >暂无历史信息</td>
        </#if>
        </table>
    </div>
    <script type="text/javascript">
        $(function(){
            $("#addFreeCount_Button").on("click",function(){
                $.ajax({
                    type:"post",
                    url:"/crm/teachernew/addlotteryfreecount.vpage",
                    data:{campaignId:$("#campaignId").val(), teacherId:${(teacherId)!0}, freeCount:$("#freeCount").val()},
                    success:function(data){
                        alert(data.info);
                    }
                });
            });

            $("#campaignId").on("change",function(){
                var campaignId = $(this).val();
                //console.info(campaignId)
                location.href = "/crm/teachernew/aboutlottery.vpage?teacherId=${(teacherId)!0}&campaignId=" + campaignId;
            });
        });
    </script>
</body>
</html>