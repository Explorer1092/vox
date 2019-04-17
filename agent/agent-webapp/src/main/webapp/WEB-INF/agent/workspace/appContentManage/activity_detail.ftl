<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加活动' page_num=1>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>-->
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑活动</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${error!}</strong>
            </div>
        </#if>
        <div class="box-content">


            <fieldset>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动名称:</label>
                    <div class="controls">
                        <input id="iActivityName" name="activityName" class="input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">起始有效期:</label>
                    <div class="controls">
                        <input id="activityStartDate" name="startDate" class="input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">截止有效期:</label>
                    <div class="controls">
                        <input id="activityEndDate" name="endDate" class="input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动范围:</label>
                    <div class="controls">
                        <#list activityScope  as data>
                            <input type="checkbox" id="a-s-${data.level!0}"
                                   class="activity-scope" value="${data.level!0}"/>${data.description!""}&nbsp;&nbsp;
                        </#list>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动入口:</label>
                    <div class="controls">
                        <#list activityEntrance as data>
                            <input type="checkbox" id="a-e-${data.id!0}" class="activity-entrance"
                                   value="${data.id!0}"/>${data.entranceName!""}&nbsp;&nbsp;
                        </#list>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">活动城市:</label>
                    <div class="controls">
                        <textarea id="cityName" readonly
                                  style="cursor: pointer; width: 500px; height: 80px; resize: none;"
                                  placeholder="全国"></textarea>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="focusedInput">内容:</label>
                    <script id="content_area" type="text/plain"></script>
                </div>
            </fieldset>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="form-actions">
                    <button id="save_content" type="button" class="btn btn-primary">保存</button>
                    <a class="btn" href="/workspace/appupdate/marketing_activity_manage.vpage"> 取消 </a>
                </div>
            </#if>
            <input type="hidden" id="cityCodes">
        </div>
    </div>
</div>

<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择区域</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <div id="regiontree" class="controls" style="width: 280px;height: 400px"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="region_select_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </form>
        </div>
    </div>
</div>


<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>

<script type="text/javascript">
    $(function () {
        var options = {
            filterMode: true, //true时根据 htmlTags 过滤HTML代码，false时允许输入任何代码。
            items: [
                'source', '|', 'undo', 'redo', '|', 'preview', 'template', 'cut', 'copy', 'paste',
                'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
                'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
                'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
                'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image',
                'table', 'hr', 'emoticons', 'pagebreak',
                'anchor', 'link', 'unlink', '|'
            ],
            uploadJson: 'edituploadimage.vpage',
            fileManagerJson: 'edituploadimage.vpage',
            allowFileManager: true
        };

        var ue = UE.getEditor('content_area', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 999,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });

        $("#activityStartDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });
        $("#activityEndDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $('#regiontree').fancytree({
            source: {
                url: "/workspace/appupdate/select_city.vpage<#if id??>?id=${id!0}</#if>",
                cache: false
            },
            checkbox: true,
            selectMode: 2
        });

        $('#cityName').on('click', function () {
            <#--var $regiontree = $("#regiontree");-->
            <#--$regiontree.fancytree('destroy');-->

            <#--$regiontree.fancytree({-->
                <#--source: {-->
                    <#--url: "/workspace/appupdate/select_city.vpage<#if id??>?id=${id!0}</#if>",-->
                    <#--cache: false-->
                <#--},-->
                <#--checkbox: true,-->
                <#--selectMode: 2-->
            <#--});-->
            <#--var tree = $("#regiontree").fancytree("getTree");-->
            <#--var cities = $('#cityCodes').val().split(",");-->
            <#--tree.visit(function(node) {-->
                <#--for (var j = 0; j < cities.length; ++i) {-->
                    <#--if (node.key == cities[i]) {-->
                        <#--node.setSelected(true);-->
                    <#--}-->
                 <#--}-->
            <#--});-->

            $('#region_select_dialog').modal('show');
        });

        $('#region_select_btn').on('click', function () {
            var regionTree = $("#regiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if (regionNodes == null || regionNodes == "undefined") {
                $('#cityCodes').val('');
                $('#cityName').val('');
                return;
            }

            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function (node) {
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });

            $('#cityCodes').val(selectRegionIdList.join(','));
            $('#cityName').val(selectRegionNameList.join(','));

            $('#region_select_dialog').modal('hide');

        });

        var id = "${id!''}";
        $.post("edit_activity.vpage", {id: id}, function (res) {
            if (res.success) {
                $("#iActivityName").val(res.activityName);
                $("#activityStartDate").val(res.startDate);
                $("#activityEndDate").val(res.endDate);
                var scopeIds = res.scopeIds;
                for (var i = 0; i < scopeIds.length; i++) {
                    var scope = $("#a-s-" + scopeIds[i]);
                    scope.parent("span").addClass("checked");
                    scope.attr("checked", true);
                }

                var entranceIds = res.entranceIds;
                for (var i = 0; i < entranceIds.length; i++) {
                    var entrance = $("#a-e-" + entranceIds[i]);
                    entrance.parent("span").addClass("checked");
                    entrance.attr("checked", true);
                }
                $('#cityCodes').val(res.selectCityCode);
                $('#cityName').val(res.selectCityName);

                ue.ready(function () {
                    ue.setContent(res.content.replace(/\n/g, '<br />'));
                });
                if (res.disabled) {
                    $(".form-actions").hide();
                }
            } else {
                alert(res.info);
            }
        });

        $("#save_content").on("click", function () {
            var activityName = $("#iActivityName").val();
            if (blankString(activityName)) {
                alert("请填写活动名称！");
                return;
            }

            var startDate = $("#activityStartDate").val();
            var endDate = $("#activityEndDate").val();
            if (blankString(startDate)) {
                alert("活动起始时间为空！");
                return;
            }
            if (blankString(endDate)) {
                alert("活动结束时间为空！");
                return;
            }
            if (startDate > endDate) {
                alert("活动的开始时间不能大于结束时间！");
                return;
            }

            var activityScope = "";
            $(".activity-scope").each(function () {
                if ($("#" + this.id).attr("checked")) {
                    activityScope += this.value + ","
                }
            });
            if (blankStringOrZero(activityScope)) {
                alert("请选择活动的范围");
                return;
            }

            var activityEntrance = "";
            $(".activity-entrance").each(function () {
                if ($("#" + this.id).attr("checked")) {
                    activityEntrance += this.value + ","
                }
            });
            if (blankStringOrZero(activityEntrance)) {
                alert("请选择活动的入口");
                return;
            }


            var content = ue.getContent();
            if (blankString(content)) {
                alert("内容不能为空");
                return false;
            }
            var cityCodes = $("#cityCodes").val();
            var data = {
                id: id,
                activityName: activityName,
                startDate: startDate,
                endDate: endDate,
                content: content,
                cityCodes: cityCodes,
                scopeIds: activityScope,
                entranceIds: activityEntrance
            };

            $.post("save_activity_presentation.vpage", data, function (res) {
                if (res.success) {
                    location.href = "marketing_activity_manage.vpage";
                } else {
                    alert(res.info);
                }
            })
        });
    });


</script>

</@layout_default.page>