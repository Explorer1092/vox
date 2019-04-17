<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='抽奖活动奖项列表' page_num=9>
    <link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
          xmlns="http://www.w3.org/1999/html">
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
    <div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
        <legend style="font-weight: 700;">
            <ul class="inline">
                <li>
                    编辑奖项
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
                                <input type="text" id="name" name="name" value="${award.name}"/>
                                <span style="color: red">(必填)</span>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>奖项图片</dt>
                            <dd>
                                <#if award.imgUrl??>
                                    <img id="previewImgUrl" src="${award.imgUrl}" width="100px" height="100px"/>
                                </#if>
                                <input type="file" id="imageSquare" onchange="showPreview(this.id,'previewImgUrl')"">
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline" id="need-not-to-know">
                        <li>
                            <dt>奖项描述</dt>
                            <dd>
                                <textarea id="describeContent" name="describeContent" cols="50"
                                          rows="10">${award.describeContent}</textarea>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>大奖标志</dt>
                            <dd>
                                <input type="checkbox" id="bigAward" name="bigAward"
                                       <#if award.bigAward>checked="checked" </#if> />
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>最小奖项标志</dt>
                            <dd>
                                <input type="checkbox" id="minAward" name="minAward"
                                       <#if award.minAward>checked="checked" </#if>/>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>中奖率</dt>
                            <dd>
                                <input type="number" id="awardRate" name="awardRate" max=10000 min=0
                                       value="${award.awardRate}"/>
                                <span style="color: red">(必填)</span>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>显示排序</dt>
                            <dd>
                                <input type="number" min=0 id="displayOrder" name="displayOrder"
                                       value="${award.displayOrder}"/>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>总共可发放数量</dt>
                            <dd>
                                <input type="number" min="-1" id="totalAwardNum" name="totalAwardNum"
                                       value="${award.totalAwardNum}">
                                <span style="color: red">(-1表示不限制)</span>
                            </dd>
                        </li>
                    </ul>
                </dl>
            </div>
        </div>
    </div>

    <script type="text/javascript">

        var lotteryCampaignId = "${award.lotteryCampaignId}";
        var awardId = "${award.id}";

        function backspace() {
            window.location = 'awardList.vpage?lotteryCampaignId=' + lotteryCampaignId;
        }

        function showPreview(fileId, imgId) {
            var file = document.getElementById(fileId);
            var ua = navigator.userAgent.toLowerCase();
            var url = '';
            if (/msie/.test(ua)) {
                url = file.value;
            } else {
                url = window.URL.createObjectURL(file.files[0]);
            }
            document.getElementById(imgId).src = url;
        }

        function GetQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        }

        $(function () {
            $("#save").on("click", function () {
                var postUrl = "editAward.vpage";

                var name = $("#name").val();
                var imgUrl = $("#imgUrl").val();
                var describeContent = $("#describeContent").val();
                var bigAward = $('#bigAward').is(':checked');
                var minAward = $('#minAward').is(':checked');
                var awardRate = $("#awardRate").val();
                var displayOrder = $("#displayOrder").val();
                var totalAwardNum = $("#totalAwardNum").val();

                var data = {
                    id: awardId,
                    lotteryCampaignId: lotteryCampaignId,
                    name: name,
                    imgUrl: imgUrl,
                    describeContent: describeContent,
                    bigAward: bigAward,
                    minAward: minAward,
                    awardRate: awardRate,
                    displayOrder: displayOrder,
                    totalAwardNum: totalAwardNum
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

                $.ajax({
                    type: "post",
                    url: postUrl,
                    data: JSON.stringify(data),
                    dataType: 'JSON',
                    contentType: 'application/json',
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            var formData = new FormData();
                            var imageInput = $('#imageSquare')[0];
                            if (imageInput.files.length > 0) {
                                formData.append('file', imageInput.files[0]);
                                formData.append('id', awardId);
                                $.ajax({
                                    url: "uploadLotteryCampaignAwardImg.vpage",
                                    type: "POST",
                                    processData: false,
                                    contentType: false,
                                    data: formData,
                                    async: false
                                })
                            }

                            window.location.href = 'awardList.vpage?lotteryCampaignId=' + lotteryCampaignId;
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
        });
    </script>
</@layout_default.page>