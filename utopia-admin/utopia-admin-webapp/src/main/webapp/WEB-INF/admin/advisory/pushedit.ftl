<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-推送管理-新建/编辑' page_num=13 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<style type="text/css">
    ul.fancytree-container {
        width: 280px;
        height: 400px;
        overflow: auto;
        position: relative;
    }
</style>
<div class="span9" id="pushContentBox">
    <table class="table">
        <thead>
        <tr>
            <th></th>
            <th>文章ID</th>
            <th>封面</th>
        </tr>
        </thead>
        <tbody>
            <#assign newsIndex = 0 />
            <#if newsIdAndImgList?? && newsIdAndImgList?size gt 0>
                <#list newsIdAndImgList as news>
                    <#assign newsIndex = news_index +1 />
                <tr <#if (newsIndex) gt 1 > class="atlist" </#if>>
                    <td>第${newsIndex}篇</td>
                    <td>
                        <label>
                            <input id="articleId_${newsIndex}" class="atId" value="${news.newsId!''}" type="text"
                                   placeholder="填写文章ID" style="margin-top: 10px;">
                            <button class="btn btn-default viewCoverBtn">查看封面</button>
                        </label>
                        <span id="span_articleId_${newsIndex}"></span>
                    </td>
                    <td>
                        <#if news.fileName?has_content>
                            <#if newsIndex  == 1>
                                <img class="imgUrl" style="width: 660px; height: 360px;"
                                     data-file_name="${news.fileName!''}" src="${news.imgUrl!''}" alt="封面">
                            <#else>
                                <img class="imgUrl" style="width: 170px; height: 124px;"
                                     data-file_name="${news.fileName!''}" src="${news.imgUrl!''}" alt="封面">
                            </#if>

                        </#if>
                        <input class="fileUpBtn" type="file">
                    </td>
                </tr>
                </#list>
            </#if>

            <#list (newsIndex + 1)..(6) as at>
                <#assign newsIndex = newsIndex + 1 />
            <tr <#if (newsIndex) gt 1 > class="atlist" </#if>>
                <td>第${newsIndex}篇</td>
                <td>
                    <label>
                        <input id="articleId_${newsIndex}" class="atId" type="text" placeholder="填写文章ID"
                               style="margin-top: 10px;">
                        <button class="btn btn-default viewCoverBtn">查看封面</button>
                    </label>
                    <span id="span_articleId_${newsIndex}"></span>
                </td>
                <td>
                    <#if newsIndex  == 1>
                        <img class="imgUrl" style="width: 660px; height: 360px;" src="" alt="封面">
                    <#else>
                        <img class="imgUrl" style="width: 170px; height: 124px;" src="" alt="封面">
                    </#if>
                    <input class="fileUpBtn" type="file">
                </td>
            </tr>
            </#list>

        <tr>
            <td>push提醒</td>
            <td>
                <input type="checkbox" id="isSendPush" <#if isSendPush?has_content && isSendPush>checked="checked"</#if>>发送push提醒
            </td>
            <td></td>
        </tr>

        <tr <#if !(isSendPush?has_content) || !isSendPush>hidden="hidden"</#if> class="push">
            <td>Jpush内容</td>
            <td>
                <textarea id="pushContent" placeholder="请在这里输入要发送的JPush内容" style="width: 300px; height: 100px"><#if pushContent?has_content>${pushContent!''}</#if></textarea>
            </td>
            <td></td>
        </tr>

        <tr <#if !(isSendPush?has_content) || !isSendPush>hidden="hidden"</#if> class="push">
            <td>跳转地址</td>
            <td>
                <textarea id="linkUrl" placeholder="https://www.17zuoye.com/view/mobile/parent/information/handpick?rel=push" readonly="readonly" style="width: 300px; height: 100px" ></textarea>
            </td>
            <td></td>
        </tr>

        <tr <#if !(isSendPush?has_content) || !isSendPush>hidden="hidden"</#if> class="push">
            <td>副标题</td>
            <td>
                <textarea id="subHeading" placeholder="请填写消息列表副标题" style="width: 300px; height: 100px"><#if subHeading?has_content>${subHeading!''}</#if></textarea>
            </td>
            <td></td>
        </tr>

        <tr <#if !(duration?has_content) || !isSendPush>hidden="hidden"</#if> class="push">
            <td>推送时长</td>
            <td>
                <input type="text" id="duration" <#if duration?has_content && duration != 0>value="${duration}" </#if> placeholder="定速推送时长，单位分钟"/>
                <span style="font-size: 5px; color: red">不应小于30分钟</span>
            </td>
            <td></td>
        </tr>

        <tr>
            <td>推送时间</td>
            <td>
                <div class="input-append date form_datetime">
                    <input id="form_datetime" size="16" type="text"
                           <#if startTime?has_content>value="${startTime?string('yyyy-MM-dd HH:mm')}"</#if> readonly>
                    <span class="add-on"><i class="icon-remove"></i></span>
                    <span class="add-on"><i class="icon-th"></i></span>
                    <span style="font-size: 5px; color: red">推送时间请至少设置为10分钟后</span>
                </div>
            </td>
            <td></td>
        </tr>

        <tr>
            <td>推送对象</td>
            <td>
                <div class="controls">
                    <select id="pushType" name="pushType">
                        <option value="1"<#if pushType?? && pushType == 1>selected="selected"</#if>>单个用户</option>
                        <option value="2"<#if pushType?? && pushType == 2>selected="selected"</#if>>全部用户</option>
                        <option value="3"<#if pushType?? && pushType == 3>selected="selected"</#if>>区域投放</option>
                    </select>
                    <input id="availableUserId"
                           style="display:  <#if (pushType?? && pushType == 1) || !pushType??>block <#else >none </#if>"
                           value="${availableUserId!0}" type="text">
                </div>
                <div class="control-group" id="regionDiv" style="display: none;">
                    <input type="text" class="input-small" id="regionNames" readonly="true" value="${regionNames!}"
                           style="cursor: pointer;width: 600px;">
                    <input type="hidden" name="regionIds" id="regionIds" value="${regionIds!}"/>
                    <div id="cardregiontree" class="controls"></div>
                    <div class="control-group">
                        <button id="select_all" type="button" class="btn btn-default" data-dismiss="modal">全选</button>
                        <button id="cancel_all" type="button" class="btn btn-default" data-dismiss="modal">全不选</button>
                        <button id="reversed_select" type="button" class="btn btn-default" data-dismiss="modal">反选
                        </button>
                    </div>
                </div>
            </td>
            <td></td>
        </tr>
        </tbody>
    </table>
    <div>
        <button id="submitBtn" class="btn btn-success">提交</button>
    </div>
</div>

<script type="text/javascript">
    $(function () {

        loadRegion();

        //jpush消息模块
        $("#isSendPush").on("click", function () {
            if ($("#isSendPush").is(":checked")) {
                $(".push").show();
            } else {
                $(".push").hide();
            }
        });

        //查看封面
        $('.viewCoverBtn').on('click', function () {
            var $this = $(this);
            var atId = $this.siblings('input.atId').val();
            if (atId == '') {
                alert("文章ID不能为空");
                return false;
            }

            $.post("newsimg.vpage", {newsId: atId}, function (data) {
                console.info(data);
                if (data.success) {
                    if (data.imgUrl != '') {
                        $this.closest('td').siblings('td').find('.imgUrl').show().attr('src', data.imgUrl).attr('data-file_name', data.fileName);
                    } else {
                        alert("暂无封面");
                    }
                } else {
                    alert(data.info);
                }
            });
        });

        $(".fileUpBtn").change(function () {
            var $this = $(this);
            if ($this.val() != '') {
                var formData = new FormData();
                formData.append('imgFile', $this[0].files[0]);
                $.ajax({
                    url: 'edituploadimage.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            $this.siblings('img.imgUrl').show().attr('src', data.url).attr('data-file_name', data.fileName);
                            alert("上传成功");
                        } else {
                            alert("上传失败");
                        }
                    }
                });

            }
        });

        $(".form_datetime").datetimepicker({
            autoclose: true,
            startDate: "${.now?string('yyyy-MM-dd HH:mm')}",
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii'
        });

        $(".atId").blur(function () {
            var checkStatusNewsId = [];
            var newsId = $(this).attr("value");
            var indexId = $(this).attr('id');
            checkStatusNewsId.push(newsId);
            $.post('jxtnewscurrentstatus.vpage', {newsIds: checkStatusNewsId.toString()}, function (data) {
                if (data.success) {
                    $.each(data.result, function (index, item) {
                        var spanTitle = "span_" + indexId;
                        if (item.publishTime != null) {
                            $('#' + spanTitle).css("color", "red").html("当前状态：【定时发布】" + item.publishTime);
                        }
                        else {
                            if (item.online) {
                                $('#' + spanTitle).html("当前状态：已上线");
                            } else {
                                $('#' + spanTitle).css("color", "red").html("当前状态：未上线");
                            }
                        }
                    });
                }
            });
        });

        //保存
        $("#submitBtn").on('click', function () {
            var articleId_1 = $("#articleId_1"), atMap = {};

            $("#pushContentBox tbody tr.atlist").each(function () {
                var that = $(this);
                var atId = that.find('.atId').val();
                atMap[atId] = that.find('.imgUrl').data('file_name');
            });

            console.info(JSON.stringify(atMap));

            var pushRecordId = '${pushRecordId!''}';
            var firstNewsId = articleId_1.val();
            var firstNewsImg = articleId_1.closest('td').siblings('td').find('.imgUrl').data('file_name') || '';
            var availableUserId = $("#availableUserId").val();
            var totalNews = JSON.stringify(atMap);
            var regionIds = $("#regionIds").val();
            var pushType = $("#pushType option:selected").val();

            var isSendPush = $("#isSendPush").is(":checked")
            var pushContent = "";
            var subHeading = "";
            var duration = $("#duration").val();
            if (isSendPush) {
                pushContent = $("#pushContent").val();
                subHeading = $("#subHeading").val();
                if ($.trim(pushContent) == '' || $.trim(subHeading) == '') {
                    alert("请填写完整的push信息");
                    return false;
                }

                if (duration < 30) {
                    alert("推送时长不能小于30分钟");
                    return false;
                }
            }

            var startTime = $("#form_datetime").val();

            //判断推送内容
            if (firstNewsId == '') {
                alert("第1篇文章ID不能为空");
                return false;
            }

            if (firstNewsImg == '') {
                alert("第1篇文章封面不能为空,请点击“查看封面”");
                return false;
            }

            if (startTime == '') {
                alert("推送时间不能为空");
                return false;
            }
            var checkNewsId = [];
            $(".atId").each(function () {
                var newsId = $(this).attr("value");
                checkNewsId.push(newsId);
            });
            //判断推送方式
            if (pushType == 1 && availableUserId == '') {
                alert("推送对象ID不能为空");
                return false;
            }

            if (checkNewsId.toString() != '') {
                var flag = false;
                $.ajax({
                    url: 'jxtnewscurrentstatus.vpage',
                    async: false,
                    type: 'post',
                    data: {newsIds: checkNewsId.toString()},
                    success: function (data) {
                        if (data.success) {
                            $.each(data.result, function (index, item) {
                                if (data != '' || data != null) {
                                    if (!item.online) {
                                        flag = true;
                                    }
                                }
                            });
                        }
                    }
                });
                if (flag) {
                    if (!confirm("当前有文章未上线，是否提交？")) {
                        return false;
                    }
                }

            }

            if (pushType == 3 && regionIds == '') {
                alert("推送区域不能为空");
                return false;
            }

            var postData = {
                pushRecordId: pushRecordId,
                firstNewsId: firstNewsId,
                firstNewsImg: firstNewsImg,
                totalNews: totalNews,
                availableUserId: availableUserId,
                isSendPush: isSendPush,
                pushContent: pushContent,
                subHeading: subHeading,
                duration: duration,
                startTime: startTime,
                regionIds: regionIds,
                pushType: pushType
            };

            $.post('savepushrecord.vpage', postData, function (data) {
                console.info(data);
                if (data.success) {
                    location.href = 'pushmanage.vpage';
                } else {
                    alert(data.info);
                }

            });

        });

        $("#pushType").on("change", function () {
            var pushType = $("#pushType option:selected").val();
            if (pushType == 2) {
                $("#availableUserId").hide();
                $("#regionDiv").hide();
                $("#availableUserId").val(0);
            } else if (pushType == 3) {
                $("#availableUserId").hide();
                loadRegion();
            } else {
                $("#regionDiv").hide();
                $("#availableUserId").show();
            }

        });

        //获取某个具体推送或者资讯的区域列表
        function loadRegion() {
            var pushType = $("#pushType option:selected").val();
            var regionDiv = $("#regionDiv");
            var $regiontree = $("#cardregiontree");
            try {
                $regiontree.fancytree('destroy');
            } catch (e) {

            }
            if (pushType == 3) {

                //选择区域
                $regiontree.fancytree({
                    source: {
                        url: "load_region.vpage?type=jxt_news_push&typeId=" + '${pushRecordId!''}',
                        cache: false
                    },
                    checkbox: true,
                    selectMode: 2,
                    select: function () {
                        updateRegion();
                    }

                });
                regionDiv.show();
            } else {
                regionDiv.hide();
            }
        }

        //点击区域选中时。更新选中的区域Id和名称
        function updateRegion() {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if (regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }
            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function (node) {
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });
            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));
        }

        //全选
        $("#select_all").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    if (rootNode.key == currentNode.key) {
                        currentNode.setSelected(true);
                    }
                });
            });
        });
        //全不选
        $("#cancel_all").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    currentNode.setSelected(false);
                });
            });
        });
        //反选
        $("#reversed_select").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    if (rootNode.key == currentNode.key) {
                        if (currentNode.selected) {
                            currentNode.setSelected(false);
                        } else {
                            currentNode.setSelected(true);
                        }
                    }
                });
            });
        });
    });
</script>
</@layout_default.page>