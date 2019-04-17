<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='地域字典表维护' page_num=6>
<style>
    /*.theadTr label{text-align:center}*/
    /*.tbodyTr td{text-align:center;padding:0 15px 0 0;}*/
    .theadTr td,.tbodyTr td label{width:150px;text-align:center;}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑地域字典表配置</h2>
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
            <div id="edit_config_form" class="form-horizontal" enctype="multipart/form-data">
                <fieldset>
                    <input id="dictId" name="dictId" type="hidden"
                           value="<#if schoolData??><#if schoolData.id??>${schoolData.id}</#if></#if>">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">学校编码</label>
                        <div class="controls">
                            <input id="schoolId"  <#if schoolData??><#if schoolData.id??>disabled="true"</#if></#if>
                                   class="input-large focused" type="number"

                                   value="<#if schoolData??><#if schoolData.schoolId??>${schoolData.schoolId}</#if></#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">学校名称</label>
                        <div class="controls">
                            <label id="schoolName"
                                   class="input-large focused"><#if schoolData??><#if schoolData.schoolName??>${schoolData.schoolName}</#if></#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">阶段</label>
                        <div class="controls">
                            <label id="schoolLevel"
                                   class="input-large focused"><#if schoolData??><#if schoolData.schoolLevel??>${schoolData.schoolLevel}</#if></#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">区域编码</label>
                        <div class="controls">
                            <label id="regionCode"
                                   class="input-large focused"><#if schoolData??><#if schoolData.regionCode??>${schoolData.regionCode}</#if></#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">等级</label>
                        <div class="controls">
                            <select id="schoolPopularity">
                                <option value="">
                                    请选择
                                </option>
                                <#if schoolPopularity?has_content>
                                    <#list schoolPopularity as type>
                                        <option value="${type.level!''}" <#if schoolData??><#if (schoolData.schoolPopularity!'') == type.level >selected</#if></#if>>
                                        ${type.level!'-'}
                                        </option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">难度</label>
                        <div class="controls">
                            <select id="schoolDifficulty">
                                <option value="">
                                    请选择
                                </option>
                                <#if schoolDifficulty?has_content>
                                    <#list schoolDifficulty as type>
                                        <option value="${type.level!''}" <#if schoolData??><#if (schoolData.schoolDifficulty!'') == type.level >selected</#if></#if>>
                                        ${type.describe!'-'}
                                        </option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">是否结算</label>
                        <div class="controls">
                            <select id="calPerformance">
                                <option value="0">请选择</option>
                                <option value="1" <#if schoolData??><#if schoolData.calPerformance!false >selected</#if></#if>>
                                    是
                                </option>
                                <option value="2" <#if schoolData??><#if !schoolData.calPerformance!false >selected</#if></#if>>
                                    否
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">部门</label>
                        <div class="controls">
                            <select id="department">
                                <option value="0">请选择</option>
                                <#if department??>
                                    <#list department as d>
                                        <option value="${d.id!0}" <#if schoolData??><#if (schoolData.groupId!0)== d.id>selected</#if></#if>>${d.groupName!''}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">渗透情况</label>
                        <div class="controls">
                            <select id="permeability">
                                <option value="">
                                    请选择
                                </option>
                                <#if permeability?has_content>
                                    <#list permeability as type>
                                        <option value="${type.desc!""}"  <#if schoolData??><#if (schoolData.agentSchoolPermeabilityType!'') == type.desc >selected</#if></#if>>
                                            ${type.desc}
                                        </option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>

                    <#assign level = schoolData?? && schoolData.schoolLevel?? && (schoolData.schoolLevel == '小学' || schoolData.schoolLevel == '学前')>



                    <div class="form-actions">
                        <button id="save_config_btn" type="button" class="btn btn-primary">保存</button>
                        &nbsp;&nbsp;
                        <a id="cancel_btn" class="btn" href="schoolDictDetail.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        createSchoolBudget();
    });
    var schoolLevel ="<#if schoolData??><#if schoolData.schoolLevel??>${schoolData.schoolLevel}</#if></#if>";
    var oldSchoolList = [];
    var first = true;
    function createSchoolBudget() {
        var schoolList = [];
        for (var i = 0; i < $(".month").length; i++) {
            schoolList.push({
                "month": $(".month").eq(i).html(),
                "permeability": $(".permeability").eq(i).val(),
                <#if level>
                    "sglSubjIncBudget": $(".sglSubjIncBudget").eq(i).val(),
                    "sglSubjLtBfBudget": $(".sglSubjLtBfBudget").eq(i).val(),
                    "sglSubjStBfBudget": $(".sglSubjStBfBudget").eq(i).val()
                <#else>
                    "engBudget": $(".engBudget").eq(i).val(),
                    "mathAnshIncBudget": $(".mathAnshIncBudget").eq(i).val(),
                    "mathAnshBfBudget": $(".mathAnshBfBudget").eq(i).val()
                </#if>
            })
        }
        if (first) {
            first = false;
            oldSchoolList = schoolList.concat(0)
        }
        return schoolList;
    }

    $(function () {
        $('#save_config_btn').live('click', function () {
            // 获取数据
            var configInfo = {
                // 基础信息
                dictId: $('#dictId').val(),
                schoolId: $('#schoolId').val(),
                calPerformance:$("#calPerformance").val(),
                schoolPopularity:$("#schoolPopularity").val(),
                schoolDifficulty: $("#schoolDifficulty").val(),
                department: $("#department").val(),
                permeability:$('#permeability').val()
            };

            // 前端校验
            var msg = validateConfig(configInfo);
            if (msg.length == 0) {
                if (confirm("是否确认信息正确并保存？")) {
                    $.post('saveSchoolDictInfo.vpage', configInfo, function (data) {
                        if (data.success) {
                            alert("保存成功!");
                            window.location.href = 'schoolDictDetail.vpage?schoolId=' + configInfo.schoolId;
                        } else {
                            alert(data.info);
                        }
                    });
                }
            } else {
                alert(msg);
            }
        });


        $("#schoolId").live("keypress", function (e) {
            if (e.keyCode == 13) {
                var schoolInfo = {
                    schoolId: $("#schoolId").val()
                };
                $.get('getSchoolDictionaryData.vpage', schoolInfo, function (data) {
                    if (data.success) {
                        var schoolI = data.schoolInfo;
                        var dep = data.department;

                        if (schoolI != undefined) {
                            $("#schoolLevel").html(schoolI.schoolLevel);
                            $("#schoolName").html(schoolI.schoolName);
                            $("#regionCode").html(schoolI.regionCode);
                            $("#department").html("");
                            $("#department").html(departmentList(dep));
                            $("#department").val(schoolI.groupId);
                            $("#department").trigger('change');

                        }
                    } else {
                        alert(data.info);
                        $("#schoolId").val("");
                        $("#schoolName").html("");
                        $("#schoolLevel").html("");
                        $("#regionCode").html("");
                        $("#department").html("");
                        $("#department").html(departmentList(dep));
                        $("#department").val("0");
                        $("#department").trigger('change');
                    }
                });
            }
        });
    });

    function departmentList(dep) {
        var depHtml = "<option value='0'>请选择</option>";
        if (dep) {
            for (var i = 0; i < dep.length; i++) {
                depHtml += "<option value='" + dep[i].id + "'>" + (dep[i].groupName) + "</option>"
            }
        }
        return depHtml;
    }

    function validateConfig(configInfo) {
        var msg = "";
        if (configInfo.schoolId == "") {
            msg += "学校编码不能为空 \r\n";
        }
        if(configInfo.calPerformance==0){
            msg += "是否参与业绩计算必选 \r\n";
        }
        if (configInfo.department == 0) {
            msg += "学校所属部门必选 \r\n";
        }
        return msg;

    }
</script>

</@layout_default.page>
