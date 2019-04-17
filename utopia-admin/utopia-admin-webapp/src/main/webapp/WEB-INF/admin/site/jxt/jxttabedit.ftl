<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='tab编辑' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .uploadBox {
        height: 100px;
    }

    .uploadBox .addBox {
        cursor: pointer;
        width: 170px;
        height: 124px;
        border: 1px solid #ccc;
        text-align: center;
        color: #ccc;
        float: left;
        margin-right: 20px;
    }

    .uploadBox .addBox .addIcon {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox .addBox img {
        width: 170px;
        height: 124px;
    }
</style>

<div class="span9">
    <fieldset>
        <legend>tab编辑</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">id：</label>
                        <div class="controls">
                            <label for="name">
                                <input type="text" <#if (extTab.id)??>value="${extTab.id!''}"</#if> name="id" id="id"
                                       maxlength="30" placeholder="id" disabled style="width: 60%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">主标题：</label>
                        <div class="controls">
                            <label for="name">
                                <input type="text" <#if (extTab.name)??> value="${extTab.name!''}"</#if> name="name"
                                       id="name" maxlength="500" placeholder="name" class="input" style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">副标题：</label>
                        <div class="controls">
                            <label for="desc">
                                <input type="text" <#if (extTab.desc)??> value="${extTab.desc!''}"</#if> name="desc"
                                       id="desc" maxlength="500" placeholder="desc" class="input" style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">tab类型：</label>
                        <div class="controls">
                            <label for="desc">
                                <input type="text" <#if (extTab.tabType)??> value="${extTab.tabType!''}"</#if>
                                       name="tabType" id="tabType" maxlength="500" placeholder="tabType" class="input"
                                       style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">icon：</label>
                        <div class="controls">
                            <p style="font-size: 12px" class="text-error">建议尺寸(100*100)</p>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <div class="uploadBox">
                                    <div class="addBox"><input type="file" id="uploadPhotoButton"></div>
                                    <div class="addBox">
                                        <img id="imgUlr" <#if url??>src="${url!''}"
                                             data-file_name="${extTab.img!''}"</#if>>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">跳转地址：</label>
                        <div class="controls">
                            <label for="link">
                                <input type="text" <#if (extTab.link)??> value="${extTab.link!''}"</#if> name="link"
                                       id="link" maxlength="500" placeholder="link" class="input" style="width: 50%">&nbsp;&nbsp;<input
                                    id="linkType" name="linkType" type="checkbox"
                                    <#if (extTab.linkType)?? && extTab.linkType== 1>checked="checked"
                                    value="${extTab.linkType!0}" <#else > value="0" </#if>>绝对地址
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">消息显示类型：</label>
                        <div class="controls">
                            <select id="showMessageCount" name="showMessageCount">
                                <option value="0"
                                        <#if extTab?? && extTab.showMessageCount?? && extTab.showMessageCount == false>selected="selected"</#if>>
                                    显示为红点
                                </option>
                                <option value="1"
                                        <#if extTab?? && extTab.showMessageCount?? && extTab.showMessageCount == true>selected="selected"</#if>>
                                    显示为数字
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">灰度一级名称：</label>
                        <div class="controls">
                            <label for="mainFunctionName">
                                <input type="text" <#if (extTab.mainFunctionName)??>
                                       value="${extTab.mainFunctionName!''}"</#if> name="mainFunctionName"
                                       id="mainFunctionName" maxlength="500" placeholder="mainFunctionName"
                                       class="input" style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">灰度二级名称：</label>
                        <div class="controls">
                            <label for="subFunctionName">
                                <input type="text" <#if (extTab.subFunctionName)??>
                                       value="${extTab.subFunctionName!''}"</#if> name="subFunctionName"
                                       id="subFunctionName" maxlength="500" placeholder="subFunctionName" class="input"
                                       style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">优先级：</label>
                        <div class="controls">
                            <label for="rank">
                                <input type="text" <#if (extTab.rank)??> value="${extTab.rank!''}"</#if> name="rank"
                                       id="rank" maxlength="500" placeholder="rank" class="input" style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">生效时间：</label>

                        <div class="input-append date form_datetime">
                            <input id="startDate" size="16" type="text" <#if extTab?? &&  extTab.startDate?has_content>
                                   value="${extTab.startDate?string('yyyy-MM-dd HH:mm')}"</#if> name="startDate"
                                   readonly>
                            <span class="add-on"><i class="icon-remove"></i></span>
                            <span class="add-on"><i class="icon-th"></i></span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">失效时间：</label>

                        <div class="input-append date form_datetime">
                            <input id="endDate" size="16" type="text" <#if  extTab?? && extTab.endDate?has_content>
                                   value="${extTab.endDate?string('yyyy-MM-dd HH:mm')}"</#if> name="endDate" readonly>
                            <span class="add-on"><i class="icon-remove"></i></span>
                            <span class="add-on"><i class="icon-th"></i></span>
                        </div>
                    </div>


                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保  存" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {
        $("#linkType").on("click", function () {
            if (this.checked == true) {
                this.value = 1;
            } else {
                this.value = 0;
            }
        });
    });


    $(".form_datetime").datetimepicker({
        autoclose: true,
        startDate: "${.now?string('yyyy-MM-dd HH:mm')}",
        minuteStep: 5,
        format: 'yyyy-mm-dd hh:ii'
    });

    $("#uploadPhotoButton").change(function () {
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
                        $("#imgUlr").attr('src', data.url).attr('data-file_name', data.fileName).closest('div.addBox').show();
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });

        }
    });

    //保存
    $('#saveBtn').on('click', function () {
        var id = $('#id').val();
        var name = $('#name').val();
        var desc = $('#desc').val();
        var link = $("#link").val();
        var linkType = $("#linkType").val();
        var img = $("#imgUlr").data('file_name');
        var mainFunctionName = $("#mainFunctionName").val();
        var rank = $("#rank").val();
        var startDate = $("#startDate").val();
        var endDate = $("#endDate").val();
        var subFunctionName = $("#subFunctionName").val();
        var tabType = $("#tabType").val();
        var showMessageCount = $("#showMessageCount option:selected").val();

        if (startDate.trim() == '' || endDate.trim() == '') {
                alert("生效时间和失效时间不能为空");
                return false;
        }

        var postData = {
            id: id,
            name: name,
            desc: desc,
            link: link,
            linkType: linkType,
            img: img,
            mainFunctionName: mainFunctionName,
            //online: online,
            rank: rank,
            startDate: startDate,
            endDate: endDate,
            subFunctionName: subFunctionName,
            tabType: tabType,
            showMessageCount: showMessageCount
        };

        $.post('jxtexttabsave.vpage', postData, function (data) {
            if (data.success) {
                location.href = 'getjxtexttablist.vpage';
            } else {
                alert(data.info);
            }
        });
    });

</script>
</@layout_default.page>