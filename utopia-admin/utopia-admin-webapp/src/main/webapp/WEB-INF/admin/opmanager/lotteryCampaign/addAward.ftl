<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='抽奖活动奖项列表' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    <legend style="font-weight: 700;">
        <ul class="inline">

        <li>
            新增奖项
        </li>
    </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-info" id="save" name="save">保存</button>
                <button class="btn btn-info" id="backspace" name="backspace" onclick="backspace()">返回</button>
            </li>
        </ul>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>奖项名称</dt>
                        <dd>
                            <input type="text" id="name" name="name" />
                            <span style="color: red">(必填)</span>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>奖项图片</dt>
                        <dd>
                            <input type="file" id="imageSquare">
                        </dd>
                    </li>
                </ul>
                <ul class="inline" id="need-not-to-know">
                    <li>
                        <dt>奖项描述</dt>
                        <dd>
                            <textarea id="describeContent" name="describeContent" cols="50" rows="10"></textarea>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>大奖标志</dt>
                        <dd>
                            <input type="checkbox" id="bigAward" name="bigAward"/>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>最小奖项标志</dt>
                        <dd>
                            <input type="checkbox" id="minAward" name="minAward"/>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>中奖率</dt>
                        <dd>
                            <input type="number" id="awardRate" name="awardRate" max=10000 min=0 value="0"/>
                            <span style="color: red">(必填)</span>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>显示排序</dt>
                        <dd>
                            <input type="number" value=0 min=0  id="displayOrder" name="displayOrder"/>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>总共可发放数量</dt>
                        <dd>
                            <input type="number" value="-1" min="-1" id="totalAwardNum" name="totalAwardNum">
                            <span style="color: red">(-1表示不限制)</span>
                        </dd>
                    </li>
                </ul>
            </dl>
        </div>
    </div>

    <div>
        <ul class="inline" style="margin-top: 2em">
            <legend style="font-weight: 700;">奖品列表<span style="color: red">(必填)</span></legend>
            <ul class="inline">
                <li>
                    <button class="btn btn-info" id="addAward" name="addAward">新增奖品</button>
                </li>
            </ul>
            <br>
            <label style="display:none" id="selectRule">0</label>
            <label style="display:none" id="ruleCount">0</label>
            <table id="awardList" class="table table-hover table-striped table-bordered">
                <tbody>
                <tr id="comment_title">
                    <th>奖品名称</th>
                    <th>奖品类型</th>
                    <th>数量</th>
                    <th>奖品图片</th>
                    <th>扩展字段</th>
                </tr>
                </tbody>
            </table>
    </div>
</div>
<div id="editAward-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增奖品</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>奖品名称</dt>
                    <dd>
                        <input type="text" id="awardName" name="awardName" />
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>奖品图片</dt>
                    <dd>
                        <input type="file" id="awardImageSquare">
                    </dd>
                </li>
            </ul>
            <ul class="inline" id="need-not-to-know">
                <li>
                    <dt>奖品类型</dt>
                    <dd>
                        <select id="type" name="type" required="true" onchange="rewardTypeChange(this)">
                            <option value="INTEGRAL">学豆</option>
                            <option value="DEBRIS">碎片</option>
                            <option value="GOODS">实物</option>
                            <option value="DIY">自定义</option>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>奖品数量</dt>
                    <dd>
                        <input type="number" id="num" name="num"/>
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt id="extText">扩展字段</dt>
                    <dd>
                        <input type="text" id="ext" name="ext"/>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="saveAward" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

</div>


<script type="text/javascript">

    function backspace() {
        window.location = 'awardList.vpage?lotteryCampaignId=' + GetQueryString("lotteryCampaignId");
    }

    function rewardTypeChange(e) {
        if (e.value == 'GOODS') {
            $("#extText").html("奖品ID")
        } else {
            $("#extText").html("扩展字段")
        }
    }

    function GetQueryString(name) {
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null)return  unescape(r[2]); return null;
    }


    $(function () {
        var awardList = [];

        $("#addAward").on("click", function () {
            $("#awardName").val("");
            $("#ext").val("");
            $("#num").val("");
            $('#type').val("");
             $('#awardImageSquare').val("");
            $("#editAward-dialog").modal("show");
        });


        $("#saveAward").on("click", function () {
            if (confirm("是否确认新增？")) {

                var awardImageSquare = "";
                var name = $("#awardName").val();
                var ext = $("#ext").val();
                var num = $("#num").val();
                var type = $('#type').val();

                if (name == undefined || name.trim() == '') {
                    alert("请输入奖品名称");
                    return false;
                }
                if (num == undefined || num.trim() == '') {
                    alert("请输入奖品数量");
                    return false;
                }
                if (type == undefined || type.trim() == '') {
                    alert("请输入奖品类型");
                    return false;
                }

                var imageInput = $('#awardImageSquare')[0];
                if (imageInput.files.length > 0) {
                    var formData = new FormData();
                    formData.append('file', imageInput.files[0]);
                    $.ajax({
                        url: "uploadAwardImg.vpage",
                        type: "POST",
                        processData: false,
                        contentType: false,
                        data: formData,
                        async:false,
                        success: function (data) {
                            if (data.success) {
                                awardImageSquare = data.filename;
                            } else {
                                alert(data.info);
                            }
                        }
                    })
                }
                var rowTem = '<tr>'
                        + '<td>' + name + ' </td>'
                        + '<td>' + type  + '</td>'
                        + '<td>' + num + '</td>'
                        + '<td> <img src="' + awardImageSquare + ' " width="60" style="height: 60px"/> </td>'
                        + '<td>' + ext + '</td>'
                        + '</tr>';
                $("#awardList tbody:last").append(rowTem);
                $("#editAward-dialog").modal("hide");

                awardList.push({name: name, type: type, num: num, imgUrl: awardImageSquare, ext: ext})
            }
        });

        $("#save").on("click", function () {
            if (confirm("是否确认新增？")) {
                var postUrl = "addLotteryCampaignAwead.vpage";

                var lotteryCampaignId = GetQueryString("lotteryCampaignId");

                var name = $("#name").val();
                var imgUrl = $("#imgUrl").val();
                var describeContent = $("#describeContent").val();
                var bigAward = $('#bigAward').is(':checked');
                var minAward = $('#minAward').is(':checked');
                var awardRate = $("#awardRate").val();
                var displayOrder = $("#displayOrder").val();
                var totalAwardNum = $("#totalAwardNum").val();

                var data = {
                    lotteryCampaignId: lotteryCampaignId,
                    name: name,
                    imgUrl: imgUrl,
                    describeContent: describeContent,
                    bigAward: bigAward,
                    minAward: minAward,
                    awardRate: awardRate,
                    displayOrder: displayOrder,
                    totalAwardNum: totalAwardNum,
                    awardList: awardList
                }

                if (data.lotteryCampaignId == undefined || data.lotteryCampaignId.trim() == '') {
                    alert("活动id不存在");
                    window.location.href = 'list.vpage?';
                    return false;
                }
                if (data.name == undefined || data.name.trim() == '') {
                    alert("请输入奖项名称");
                    return false;
                }

                if (data.awardRate == undefined || data.awardRate.trim() == '') {
                    alert("请输入中奖率");
                    return false;
                }
                if (data.displayOrder == undefined || data.displayOrder.trim() == '') {
                    alert("请输入显示排序");
                    return false;
                }
                if (data.totalAwardNum == undefined || data.totalAwardNum.trim() == '') {
                    alert("请输入总共可发放数量");
                    return false;
                }
                if (data.awardList == undefined || data.awardList.length <= 0) {
                    alert("请先添加奖品");
                    return false;
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
                            // 放在这个地方，防止新建的时候，活动记录没生成就去更新
                            var formData = new FormData();
                            var imageInput = $('#imageSquare')[0];
                            if (imageInput.files.length > 0) {
                                formData.append('file', imageInput.files[0]);
                                formData.append('id', data.id);
                                $.ajax({
                                    url: "uploadLotteryCampaignAwardImg.vpage",
                                    type: "POST",
                                    processData: false,
                                    contentType: false,
                                    data: formData,
                                    async:false
                                })
                            }

                            $("#editCompaign").modal("hide");
                            window.location.href = 'awardList.vpage?lotteryCampaignId=' + GetQueryString("lotteryCampaignId");
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });
    });

    function delAward(id) {
        if (confirm("是否确认删除？")) {
            var postUrl = "delAward.vpage";
            $.ajax({
                type: "post",
                url: postUrl,
                data:{id: id},
                success: function (data) {
                    $("#record_success").val(data.success);
                    if (data.success) {
                        $("#editCompaign").modal("hide");
                        window.location.href = 'awardList.vpage?lotteryCampaignId=' + GetQueryString("lotteryCampaignId");
                    } else {
                        alert(data.info);
                    }
                }
            });
        }
    }
</script>
</@layout_default.page>