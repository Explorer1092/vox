<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='抽奖活动奖项列表' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">

    <div>
        <ul class="inline" style="margin-top: 2em">
            <legend style="font-weight: 700;">奖项列表</legend>
            <ul class="inline">
                <li>
                    <button class="btn btn-info" id="backspace" name="backspace">返回</button>
                </li>
                <li>
                    <button class="btn btn-info" id="addLotteryCampaignAward" name="addLotteryCampaignAward">新增奖项</button>
                </li>
            </ul>
            <br>
            <label style="display:none" id="selectRule">0</label>
            <label style="display:none" id="ruleCount">0</label>
            <table id="customer_service_record" class="table table-hover table-striped table-bordered">
                <tbody>
                <tr id="comment_title">
                    <th>奖项名称</th>
                    <th>图片</th>
                    <th>描述</th>
                    <th>大奖标志</th>
                    <th>最小奖项标志</th>
                    <th>中奖率</th>
                    <th>显示排序</th>
                    <th>总共可发放数量</th>
                    <th>已发放数量</th>
                    <th>奖品信息列表</th>
                    <th>操作</th>
                </tr>
                </tbody>
                <#assign awardTypeMap={"INTEGRAL":"学豆","DEBRIS":"碎片","GOODS":"奖品","DIY":"自定义"}>
                <#if lotteryCampaignAwards??>
                <#list lotteryCampaignAwards as lotteryCampaignAward >
                    <tr>
                        <td>${lotteryCampaignAward.name!}</td>
                        <td>
                            <#if lotteryCampaignAward.imgUrl??>
                                <img src="${lotteryCampaignAward.imgUrl!}" style="height:40px;width:40px"/>
                            </#if>
                        </td>
                        <td>${lotteryCampaignAward.describeContent!""}</td>
                        <td>
                            <#if lotteryCampaignAward.bigAward!>
                                是
                            <#else >
                                否
                            </#if >
                        </td>
                        <td>
                            <#if lotteryCampaignAward.minAward!>
                                是
                            <#else >
                                否
                            </#if >
                        </td>
                        <td>${lotteryCampaignAward.awardRate!""}</td>
                        <td>${lotteryCampaignAward.displayOrder!""}</td>
                        <td>${lotteryCampaignAward.totalAwardNum!""}</td>
                        <td>${lotteryCampaignAward.alreadyIssuedNum!""}</td>
                        <td>
                            <#list lotteryCampaignAward.awardList as awardList>
                                ${awardList.name}
                                | ${awardTypeMap["${awardList.type}"]}
                                | 数量:${awardList.num}
                                <#if awardList.imgUrl?? && awardList.imgUrl != "">,图片：<img src="${awardList.imgUrl!""}" style="height:40px;width:40px"/></#if>
                                <#if awardList.ext?? && awardList.ext != "">
                                    <#if awardList.type=="GOODS">
                                        ,奖品ID：${awardList.ext}
                                    <#else>
                                        ,扩展字段：${awardList.ext}
                                    </#if>
                                </#if> <br>
                            </#list>
                        </td>
                        <td>
                            <#if lotteryCampaignAward.disabled>
                                <input class="btn btn-primary" value="启用" type="button"
                                       onclick="updateAwardStatus('${lotteryCampaignAward.id!}','启用')"/>
                            <#else>
                                <input class="btn btn-danger" value="禁用" type="button"
                                       onclick="updateAwardStatus('${lotteryCampaignAward.id!}','禁用')"/>
                            </#if>
                            <input class="btn btn-primary" type="button" value="编辑"
                                   onclick="window.location.href='editAward.vpage?awardId=${lotteryCampaignAward.id!''}'"/>
                        </td>
                    </tr>
                </#list>
                </#if>
            </table>
    </div>
</div>
<div id="editLotteryCampaignAward-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增奖项</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>奖项名称</dt>
                    <dd>
                        <input type="text" id="name" name="name" />
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>奖项图片</dt>
                    <dd>
                        <input type="text" id="imgUrl" name="imgUrl"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline" id="need-not-to-know">
                <li>
                    <dt>奖项描述</dt>
                    <dd>
                        <textarea id="describeContent" name="describeContent" cols="50" rows="10"></textarea>
                        <span style="color: red">(必填)</span>
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
                        <input type="number" id="awardRate" name="awardRate" max=10000 min=0 />
                        <span style="color: red">(必填)</span>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>显示排序</dt>
                    <dd>
                        <input type="number" id="displayOrder" name="displayOrder"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>总共可发放数量</dt>
                    <dd>
                        <input type="number" id="totalAwardNum" name="totalAwardNum"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>奖品信息列表</dt>
                    <dd>
                        <textarea readonly=true id="describeContent" name="awardList" ></textarea>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学豆奖品</dt>
                    <dd>
                        <input type="checkbox" id="integralAward" name="integralQward"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>碎片奖品</dt>
                    <dd>
                        <input type="checkbox" id="fragmentAward" name="integralQward"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>实物奖品</dt>
                    <dd>
                        <input type="checkbox" id="goodsAward" name="integralQward"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>自定义奖品</dt>
                    <dd>
                        <input type="checkbox" id="customAward" name="integralQward"/>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="save" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

</div>


<script type="text/javascript">

    function GetQueryString(name) {
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null)return  unescape(r[2]); return null;
    }


    $(function () {

        $("#backspace").on("click", function () {
            window.location.href = 'list.vpage';
        });

        $("#addLotteryCampaignAward").on("click", function () {
            window.location.href = 'addAward.vpage?lotteryCampaignId=' + GetQueryString("lotteryCampaignId");
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
                var alreadyIssuedNum = $("#alreadyIssuedNum").val();
                var awardList = $("#awardList").val();

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
                    alreadyIssuedNum:alreadyIssuedNum,
                    awardList: awardList
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
                            window.location.href = 'awardList.vpage?lotteryCampaignId=' + GetQueryString("lotteryCampaignId");
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });
    });

    function updateAwardStatus(id, action) {
        if (confirm("是否确认" + action + "？")) {
            var postUrl = "delAward.vpage";
            $.ajax({
                type: "post",
                url: postUrl,
                data:{id: id},
                success: function (data) {
                    $("#record_success").val(data.success);
                    if (data.success) {
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