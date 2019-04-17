<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='抽奖活动管理' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="editCompaign" class="modal fade hide">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑抽奖活动</h3>
    </div>
    <div class="modal-body">
        <input type="hidden" id="id" name="id" <#if lotteryCampaign??>value="${lotteryCampaign.id!''}"</#if> />

        <div class="control-group">
            <label class="control-label">活动名称</label>
            <div class="controls">
                <input type="text" id="campaignName" name="campaignName" <#if lotteryCampaign??> value="${lotteryCampaign.campaignName!''}"</#if>/>
                <span style="color: red">(必填)</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">重复中大奖控制</label>

            <select name="bigAwardRewin" id="bigAwardRewin" style="width:220px;">
                <option value="0">不控制</option>
                <option value="1">不允许同用户</option>
                <option value="2">不允许同校</option>
                <option value="3">不允许同区</option>
            </select>
        </div>

        <div class="control-group">
            <label class="control-label">参与次数</label>
            <div class="controls">
                <input type="text" id="joinCounts" name="joinCounts" <#if lotteryCampaign??> value="${lotteryCampaign.joinCounts!''}"</#if>/>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">参与次数时间范围</label>
            <div class="controls">
                <select name="joinCountsRange" id="joinCountsRange" style="width:220px;">
                    <option value="0">不控制</option>
                    <option value="1">每日</option>
                    <option value="2">每周</option>
                    <option value="3">每月</option>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">活动开始时间</label>
            <div class="controls">
                <input type="text" id="campaignStartTime" name="campaignStartTime" <#if lotteryCampaign??> value="${lotteryCampaign.campaignStartTime!''}"</#if>/>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">活动结束时间</label>
            <div class="controls">
                <input type="text" id="campaignEndTime" name="campaignEndTime" <#if lotteryCampaign??> value="${lotteryCampaign.campaignEndTime!''}"</#if>/>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="save" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

</div>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">抽奖活动管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="lottery-query" class="form-horizontal" method="post" action="${requestContext.webAppContextPath}/opmanager/lottery/compaign/list.vpage" >
                    <ul class="inline">
                        <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                        <li>
                            <input id="addCompaign" name="addCompaign" class="btn btn-info" value="新建抽奖活动"/>
                        </li>
                    </ul>
                </form>
                <div id="data_table_journal">
                    <table class="table table-bordered table-striped">
                        <tr>
                            <td width="60px">活动ID</td>
                            <td width="150px">活动名称</td>
                            <td width="130px">活动开始时间</td>
                            <td width="130px">活动结束时间</td>
                            <td width="150px">重复中大奖类型</td>
                            <td width="150px">参与次数</td>
                            <td width="80px">参与范围</td>
                            <td width="200px">操作</td>
                        </tr>
                        <#if lotteryCampaigns??>
                            <#list lotteryCampaigns as lotteryCampaign >
                                <tr>
                                    <td>${lotteryCampaign.id!}</td>
                                    <td>${lotteryCampaign.campaignName!}</td>
                                    <td>${lotteryCampaign.campaignStartTime!}</td>
                                    <td>${lotteryCampaign.campaignEndTime!}</td>
                                    <td>
                                        <#if lotteryCampaign.bigAwardRewin?exists && lotteryCampaign.bigAwardRewin == 0>不控制</#if>
                                        <#if lotteryCampaign.bigAwardRewin?exists && lotteryCampaign.bigAwardRewin == 1>不允许同用户</#if>
                                        <#if lotteryCampaign.bigAwardRewin?exists && lotteryCampaign.bigAwardRewin == 2>不允许同校</#if>
                                        <#if lotteryCampaign.bigAwardRewin?exists && lotteryCampaign.bigAwardRewin == 3>不允许同区</#if>
                                    </td>
                                    <td>${lotteryCampaign.joinCounts!''}</td>
                                    <td>
                                        <#if lotteryCampaign.joinCountsRange?exists && lotteryCampaign.joinCountsRange == 0>不控制</#if>
                                        <#if lotteryCampaign.joinCountsRange?exists && lotteryCampaign.joinCountsRange == 1>每日</#if>
                                        <#if lotteryCampaign.joinCountsRange?exists && lotteryCampaign.joinCountsRange == 2>每周</#if>
                                        <#if lotteryCampaign.joinCountsRange?exists && lotteryCampaign.joinCountsRange == 3>每月</#if>
                                    </td>
                                    <td>
                                        <button id="editCampaign" name="editCampaign" class="btn btn-info" >编辑</button>
                                        <#if lotteryCampaign.onlined?? && lotteryCampaign.onlined>
                                            <button class="btn btn-danger" id="offline" name="offline" onclick="updown(${lotteryCampaign.id!}, false)">下线</button>
                                        <#else>
                                            <button class="btn btn-success" id="online" name="online" onclick="updown(${lotteryCampaign.id!}, true)">上线</button>
                                        </#if>
                                        <a href="awardList.vpage?lotteryCampaignId=${lotteryCampaign.id}" class="btn btn-info" role="button">编辑奖项</a>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("#campaignStartTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#campaignEndTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

    });

    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#lottery-query").submit();
    }

    $("button[name='editCampaign']").on('click', function (e){
        tds = $(e.target.parentNode.parentNode).find("td");

        $("#id").val(tds[0].innerText);
        $("#campaignName").val(tds[1].innerText);
        $("#campaignStartTime").val(tds[2].innerText);
        $("#campaignEndTime").val(tds[3].innerText);

        var tempBigAwardRewin = tds[4].innerText;
        $('#bigAwardRewin option:contains(' + tempBigAwardRewin + ')').each(function(){
            if ($(this).text() == tempBigAwardRewin) {
                $(this).attr('selected', true);
            }
        });

        $("#joinCounts").val(tds[5].innerText);

        var tempjoinCountsRange = tds[6].innerText;
        $('#joinCountsRange option:contains(' + tempjoinCountsRange + ')').each(function(){
            if ($(this).text() == tempjoinCountsRange) {
                $(this).attr('selected', true);
            }
        });
        $("#editCompaign").modal("show");
    });


    $("#addCompaign").on("click", function () {
        $("#editCompaign").modal("show");

    });

    $("#save").on("click", function () {
        if (confirm("是否确认保存活动？")) {
            var postUrl = "upsert.vpage";

            var id = $("#id").val();
            var onlined = $("#onlined").val();

            var campaignName = $("#campaignName").val();
            var bigAwardRewin = $("#bigAwardRewin").val();
            var joinCounts = $("#joinCounts").val();
            var joinCountsRange = $("#joinCountsRange").val();
            var campaignStartTime = $("#campaignStartTime").val();
            var campaignEndTime = $("#campaignEndTime").val();
            var data = {
                id: id,
                onlined: onlined,
                campaignName: campaignName,
                bigAwardRewin: bigAwardRewin,
                joinCounts: joinCounts,
                joinCountsRange: joinCountsRange,
                campaignStartTime: campaignStartTime,
                campaignEndTime: campaignEndTime
            }

            if (campaignName === '') {
                alert("活动名称不可为空");
                return false
            }

            $.ajax({
                type: "post",
                url: postUrl,
                data: JSON.stringify(data),
                dataType: 'JSON',
                contentType: 'application/json',
                async: false,
                success: function (data) {
                    $("#record_success").val(data.success);
                    if (data.success) {
                        $("#editCompaign").modal("hide");
                        window.location.href = 'list.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        }
    });

    function updown(id, onlned) {
        if (confirm("是否确认修改上下架状态？")) {
            $.post("updownlined.vpage", {id: id, onLined: onlned}, function (data) {
                if (data.success) {
                    // alert(data.info);
                    window.location.href = 'list.vpage';
                } else {
                    alert(data.info);
                }
            });
        }
    }
</script>


</@layout_default.page>