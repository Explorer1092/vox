<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='CRM' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<style>
    .uploading {
        background:#020516;
        width:100%;
        height:100%;
        opacity:0.4;
        filter:alpha(opacity=40);
        position:fixed;
        left:0;
        top:0; z-index:1000;
        display:none;
    }
    .loading{
        width:38px;
        height:38px;
        background:url(/public/img/loading.gif) no-repeat;
        position:fixed;
        left:50%;
        top:50%;
        margin-left:-16px;
        margin-top:-16px;
        z-index:4000;
        display:none;
    }
    .field-title {
        font-weight: bold;
    }

</style>

<div id="main_container" class="span9">
    <legend>
        <#if  edit?? && edit == 1>
            <#if activityObj?? && activityObj.id?has_content>编辑<#else >新增</#if>活动
        <#else >
            活动详情
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <input type="hidden" id="template_id" name="template_id" value="<#if activityObj??>${activityObj.id!''}</#if>">
                <fieldset>
                    <legend class="field-title"></legend>
                    <fieldset>
                        <#assign pcodes="",ccodes="",acodes="",schoolIds="" />
                        <#if activityObj?? && activityObj.regions?has_content>
                            <#list activityObj.regions as regionObj>
                                <#if regionObj.regionLevel == "province">
                                    <#assign pcodes = regionObj.regionIds?join(",")/>
                                </#if>
                                <#if regionObj.regionLevel == "city">
                                    <#assign ccodes = regionObj.regionIds?join(",")/>
                                </#if>
                                <#if regionObj.regionLevel == "country">
                                    <#assign acodes = regionObj.regionIds?join(",")/>
                                </#if>
                                <#if regionObj.regionLevel == "school">
                                    <#assign schoolIds = regionObj.regionIds?join(",")/>
                                </#if>
                            </#list>
                        </#if>
                        <div class="control-group">
                            <label class="control-label" for="productName">活动省code：</label>
                            <div class="controls">
                                <input type="text" value="${pcodes!}" class="input activitRegion" data-region-level="province">
                                <span style="color: grey;margin-left: 10px;">活动在省范围的，只填写省code,逗号分隔</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">活动市code：</label>
                            <div class="controls">
                                <input type="text" value="${ccodes!}" class="input activitRegion" data-region-level="city">
                                <span style="color: grey;margin-left: 10px;">活动在市范围的，只填写市code,逗号分隔</span>
                            </div>
                        </div>
                        <div class="control-group activitRegion">
                            <label class="control-label" for="productName">活动区code：</label>
                            <div class="controls">
                                <input type="text" value="${acodes!}" class="input activitRegion" data-region-level="country">
                                <span style="color: grey;margin-left: 10px;">活动在区范围的，只填写区code,逗号分隔</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">学校编码：</label>
                            <div class="controls">
                                <input type="text" value="${schoolIds!}" class="input activitRegion" data-region-level="school">
                                <span style="color: grey;margin-left: 10px;">活动在学校范围的，只填写学校编码,逗号分隔</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>活动名称：</label>
                            <div class="controls">
                                <input id="activityName" type="text" value="<#if activityObj??>${activityObj.name!''}</#if>"  class="input">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>开始时间：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj?? && activityObj.startDate?has_content>${activityObj.startDate?string("YYYY-MM-dd")}</#if>" name="startTime" id="startTime" class="input"><span style="color: grey;margin-left: 10px;">格式：yyyy-MM-dd</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>结束时间：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj?? && activityObj.startDate?has_content>${activityObj.endDate?string("YYYY-MM-dd")}</#if>" name="endTime" id="endTime" class="input"><span style="color: grey;margin-left: 10px;">格式：yyyy-MM-dd </span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>活动年级：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj?? && activityObj.classLevel?has_content>${activityObj.classLevel?join(',')}</#if>" name="activityLevel" id="activityLevel" class="input"><span style="color: grey;margin-left: 10px;">如1,2 &nbsp;【1,2】英语逗号隔开</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>活动主题：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj?? && activityObj.label?has_content>${activityObj.label?join('、')}</#if>" name="activityLevel" id="activityLabel" class="input"><span style="color: grey;margin-left: 10px;">如1、2 &nbsp;【黄河、自然、童趣】中文半角顿号隔开</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传活动封面：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj??>${activityObj.coverImgUrl!''}</#if>" name="coverImgUrl" id="coverImgUrl" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix="jpg#png">
                                <a class="btn btn-success preview" data-href="<#if activityObj?? && cdn_host??>${cdn_host!''}${activityObj.coverImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>上传背景图：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj??>${activityObj.backgroundImgUrl!''}</#if>" name="backgroundImgUrl" id="backgroundImgUrl" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix = "jpg#png">
                                <a class="btn btn-success preview" data-href="<#if activityObj?? && cdn_host??>${cdn_host!''}${activityObj.backgroundImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>背景头部图：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj??>${activityObj.backgroundTopImgUrl!''}</#if>" name="backgroundTopImgUrl" id="backgroundTopImgUrl" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix = "jpg#png">
                                <a class="btn btn-success preview" data-href="<#if activityObj?? && cdn_host??>${cdn_host!''}${activityObj.backgroundTopImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName"><span style="color: red;font-size: 20px;">*</span>关卡头部图：</label>
                            <div class="controls">
                                <input type="text" value="<#if activityObj??>${activityObj.missionTopImgUrl!''}</#if>" name="missionTopImgUrl" id="missionTopImgUrl" class="input" disabled="disabled">
                                <input class="upload_file" type="file" data-suffix = "jpg#png">
                                <a class="btn btn-success preview" data-href="<#if activityObj?? && cdn_host??>${cdn_host!''}${activityObj.missionTopImgUrl!''}</#if>">预览</a>
                            </div>
                        </div>
                        <#if edit?? && edit == 1>
                        <a class="btn btn-primary" id="addContentRow" href="javascript:void(0);">新增古诗关卡</a>
                        </#if>
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th><span style="color: red;font-size: 20px;">*</span>关卡数</th>
                                <th><span style="color: red;font-size: 20px;">*</span>古诗名称</th>
                                <th><span style="color: red;font-size: 20px;">*</span>古诗ID</th>
                                <th><span style="color: red;font-size: 20px;">*</span>关卡图</th>
                                <th><span style="color: red;font-size: 20px;">*</span>关卡背景图</th>
                                <th><span style="color: red;font-size: 20px;">*</span>关卡数图</th>
                                <th style="width: 60px;"><span style="color: red;font-size: 20px;">*</span>管理</th>
                            </tr>
                            </thead>
                            <tbody id="newTemplateContent">
                                <#if poetryMissionList?? && poetryMissionList?size gt 0>
                                    <#list poetryMissionList as mission>
                                    <tr class="mission-item">
                                        <td>
                                            <div class="input-group">
                                                <input type="text" style="width:30px;" class="form-control" name="missionIndex"  value="${mission_index + 1}" dir='rtl'/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" name="missionName" style="width: 70px;" value="${mission.missionName!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <input type="text" class="form-control" name="missionId" style="width: 70px;" value="${mission.missionId!''}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="controls" style="margin-left: 0px;">
                                                <input type="text" name="coverImgUrl" value="${mission.coverImgUrl!''}" class="input" disabled="disabled">
                                                <input class="upload_file" type="file" data-suffix = "jpg#png">
                                                <a class="btn btn-success preview" data-href="<#if cdn_host??>${cdn_host!''}${mission.coverImgUrl!''}</#if>">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="controls" style="margin-left: 0px;">
                                                <input type="text" value="${mission.backgroundImgUrl!''}" name="backgroundImgUrl" class="input" disabled="disabled">
                                                <input class="upload_file" type="file" data-suffix="jpg#png">
                                                <a class="btn btn-success preview" data-href="<#if cdn_host??>${cdn_host!''}${mission.backgroundImgUrl!''}</#if>">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="controls" style="margin-left: 0px;">
                                                <input type="text" value="${mission.signImgUrl!''}" name="signImgUrl" class="input" disabled="disabled">
                                                <input class="upload_file" type="file" data-suffix = "jpg#png">
                                                <a class="btn btn-success preview" data-href="<#if cdn_host??>${cdn_host!''}${mission.signImgUrl!''}</#if>">预览</a>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="input-group">
                                                <a class="btn btn-warning delete_mission">删除</a>
                                            </div>
                                        </td>
                                    </tr>
                                    </#list>
                                </#if>
                            </tbody>
                        </table>
                    </fieldset>
                </fieldset>
                <div class="control-group" >
                    <span id="save_error_message" style="color: red"></span>
                </div>
                <#if edit?? && edit == 1>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="save_info_button" value="提交" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </#if>
            </form>
        </div>
    </div>
</div>
<div class="uploading" id="uploading"></div>
<div class="loading" id="loading"></div>

<script type="text/html" id="T:MISSION-ITEM">
    <tr class="mission-item">
        <td>
            <div class="input-group">
                <input type="text" style="width:30px;" class="form-control" name="missionIndex"  value="" dir='rtl'/>
            </div>
        </td>
        <td>
            <div class="input-group">
                <input type="text" class="form-control" name="missionName" style="width: 70px;" value=""/>
            </div>
        </td>
        <td>
            <div class="input-group">
                <input type="text" class="form-control" name="missionId" style="width: 70px;" value=""/>
            </div>
        </td>
        <td>
            <div class="controls" style="margin-left: 0px;">
                <input type="text" name="coverImgUrl" value="" class="input" disabled="disabled">
                <input class="upload_file" type="file" data-suffix = "jpg#png">
                <a class="btn btn-success preview" data-href="">预览</a>
            </div>
        </td>
        <td>
            <div class="controls" style="margin-left: 0px;">
                <input type="text" value="" name="backgroundImgUrl" class="input" disabled="disabled">
                <input class="upload_file" type="file" data-suffix="jpg#png">
                <a class="btn btn-success preview" data-href="">预览</a>
            </div>
        </td>
        <td>
            <div class="controls" style="margin-left: 0px;">
                <input type="text" value="" name="signImgUrl" class="input" disabled="disabled">
                <input class="upload_file" type="file" data-suffix = "jpg#png">
                <a class="btn btn-success preview" data-href="">预览</a>
            </div>
        </td>
        <td>
            <div class="input-group">
                <a class="btn btn-warning delete_mission">删除</a>
            </div>
        </td>
    </tr>
</script>
<script type="text/javascript">
    $(function () {
        function openUploadModel() {
            $("#uploading").show();
            $("#loading").show();
        }

        function closeUploadModel() {
            $("#uploading").hide();
            $("#loading").hide();
        }
        //上传单个图片或者音频
        $(".upload_file").change(function () {
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                var acceptSuffix = $this.attr("data-suffix").split("#");
                if(acceptSuffix.indexOf(suffix) === -1){
                    alert("仅支持以下文件格式" + acceptSuffix);
                    return;
                }
                //限制200K
                if ($this[0].files[0].size > 204800) {
                    alert("您选择的文件大小超过200K");
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                openUploadModel();
                $.ajax({
                    url: 'upload_signal_file_to_oss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        closeUploadModel();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value",data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href",data.fileUrl);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //文件预览
        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });

        //添加一行正文
        $(document).on("click", "#addContentRow", function () {
            $("#newTemplateContent").append(template("T:MISSION-ITEM",{}));
            return false;
        });

        //删除一行正文
        $(document).on("click", "a.delete_mission", function () {
            var $this = $(this);
            $this.closest(".mission-item").remove();
        });

        //\\s表示 空格,回车,换行等空白符
        var replaceIllegalChar = function(value){
            if(typeof value !== "string"){
                return value;
            }
            return value.replace(/\s+/g, "");
        };

        //保存古诗模版
        $("#save_info_button").on("click", function () {

            //古诗名称
            var activityName = $("#activityName").val().trim();
            if(!activityName){
                alert("名称不能为空");
                return false;
            }
            if (activityName.length > 50) {
                alert("模板名称不能超过50个字");
                return;
            }

            var regions = [];
            $("input.activitRegion").each(function(){
                var $this = $(this);
                var regionIds = $this.val().split(",");
                if(regionIds.length > 0){
                    regions.push({
                        regionLevel : $this.attr("data-region-level"),
                        regionIds : regionIds
                    });
                }
            });
            //标签
            var labelList = $("#activityLabel").val().trim().split("、");

            //关卡
            var missions = [];
            $("tr.mission-item").each(function(){
                var $inputTextList = $(this).find("input[type='text']");
                var inputObj = {};
                $inputTextList.each(function(){
                    inputObj[$(this).attr("name")] = $(this).val();
                });
                missions.push(inputObj);
            });
            //升序排列
            missions.sort(function(v1,v2){
                var missionIndex1 = +v1["missionIndex"];
                missionIndex1 = missionIndex1 > 0 ? missionIndex1 : 999;
                var missionIndex2 = +v2["missionIndex"];
                missionIndex2 = missionIndex2 > 0 ? missionIndex2 : 999;
                return missionIndex1 - missionIndex2;
            });

            var classLevel = $("#activityLevel").val().trim().split(",");
            

            //古诗基本信息
            var template_info = {
                id: $("#template_id").val(),
                name: activityName,
                coverImgUrl: replaceIllegalChar($("#coverImgUrl").val().trim()),  //封面图
                backgroundImgUrl : replaceIllegalChar($("#backgroundImgUrl").val().trim()),  //背景图
                backgroundTopImgUrl : replaceIllegalChar($("#backgroundTopImgUrl").val().trim()), //背景头部图
                missionTopImgUrl : replaceIllegalChar($("#missionTopImgUrl").val().trim()), //关卡头部图url
                regions : regions,
                label : labelList,
                missions : missions,
                startDate : $("#startTime").val(),
                endDate : $("#endTime").val(),
                classLevel : classLevel
            };
            //保存
            $.post("/opmanager/poetry/save/ancient_poetry_activity.vpage", {
                activity_info: JSON.stringify(template_info)
            }).done(function (data) {
                if (data.success) {
                    alert("保存成功，活动ID为：" + data.id);
                    window.close();
                } else {
                    alert(data.info);
                    $("#save_error_message").html(data.info);
                }
            }).fail(function(){
                alert("网络出错，请重试");
            });
        });
    });
</script>
</@layout_default.page>