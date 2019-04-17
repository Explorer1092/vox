<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='地域字典表维护' page_num=6>
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
            <form id="edit_config_form" class="form-horizontal" enctype="multipart/form-data">
                <fieldset>
                    <input id="dictId" name="dictId" type="hidden"
                           value="<#if regionData??><#if regionData.id??>${regionData.id}</#if></#if>">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">区域编码</label>
                        <div class="controls">
                            <input id="regionCode" <#if regionData??><#if regionData.id??>disabled="true"</#if ></#if>
                                   class="input-large focused" type="text"
                                   value=" <#if regionData??><#if regionData.regionCode??>${regionData.regionCode}</#if></#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">区域名称</label>
                        <div class="controls">
                            <label id="regionName"
                                   class="input-large focused"><#if regionData??><#if regionData.regionName??>${regionData.regionName}</#if></#if></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">小学/中学</label>
                        <div class="controls">
                            <select id="marketStuLevel">
                                <option value="">--</option>
                                <option value="小学" <#if regionData??><#if regionData.marketStuLevel??><#if regionData.marketStuLevel == '小学'>
                                        selected</#if></#if></#if>>小学
                                </option>
                                <option value="中学" <#if regionData??><#if regionData.marketStuLevel??><#if regionData.marketStuLevel == '中学'>
                                        selected</#if></#if></#if>>中学
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">直营/代理</label>
                        <div class="controls">
                            <select id="cityModel">
                                <option value="">--</option>
                                <option value="直营" <#if regionData??><#if regionData.cityModel??><#if regionData.cityModel == '直营'>
                                        selected</#if></#if></#if>>直营
                                </option>
                                <option value="代理" <#if regionData??><#if regionData.cityModel??><#if regionData.cityModel == '代理'>selected</#if></#if></#if>>
                                    代理
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">城市等级</label>
                        <div class="controls">
                            <select id="cityLevel">
                                <option value="">--</option>
                                <option value="A" <#if regionData??><#if regionData.cityLevel??><#if regionData.cityLevel == 'A'>
                                        selected</#if></#if></#if>>A类城市
                                </option>
                                <option value="B" <#if regionData??><#if regionData.cityLevel??><#if regionData.cityLevel == 'B'>
                                        selected</#if></#if></#if>>B类城市
                                </option>
                                <option value="C" <#if regionData??><#if regionData.cityLevel??><#if regionData.cityLevel == 'C'>
                                        selected</#if></#if></#if>>C类城市
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">主城区/非主城区</label>
                        <div class="controls">
                            <select id="springMainCity">
                                <option value="">--</option>
                                <option value="主城区"   <#if regionData??><#if regionData.springMainCity??><#if regionData.springMainCity == '主城区'>
                                        selected</#if></#if></#if>>主城区
                                </option>
                                <option value="非主城区" <#if regionData??><#if regionData.springMainCity??><#if regionData.springMainCity == '非主城区'>
                                        selected</#if></#if></#if>>非主城区
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">高渗/低渗</label>
                        <div class="controls">
                            <select id="settlementType">
                                <option value="">--</option>
                                <option value="低渗" <#if regionData??><#if regionData.citySettlement??><#if regionData.citySettlement == '低渗'>
                                        selected</#if></#if></#if>>低渗
                                </option>
                                <option value="高渗" <#if regionData??><#if regionData.citySettlement??><#if regionData.citySettlement == '高渗'>
                                        selected</#if></#if></#if>>高渗
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">新增认证目标</label>
                        <div class="controls">
                            <input id="addBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.addBudget??>${regionData.addBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">三月月活目标</label>
                        <div class="controls">
                            <input id="marBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.marBudget??>${regionData.marBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">四月月活目标</label>
                        <div class="controls">
                            <input id="aprBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.aprBudget??>${regionData.aprBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">五月月活目标</label>
                        <div class="controls">
                            <input id="mayBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.mayBudget??>${regionData.mayBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">六月月活目标</label>
                        <div class="controls">
                            <input id="junBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.junBudget??>${regionData.junBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">双科认证目标</label>
                        <div class="controls">
                            <input id="doubleSubjectBudget" class="input-large focused" min="0" type="number"
                                   value="<#if regionData??><#if regionData.doubleSubjectBudget??>${regionData.doubleSubjectBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">1~2年级数学认证目标数</label>
                        <div class="controls">
                            <input id="gradeMathAddBudget" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.gradeMathAddBudget??>${regionData.gradeMathAddBudget!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">排除毕业班的学生数</label>
                        <div class="controls">
                            <input id="stuNumExpSix" class="input-large focused" type="number" min="0"
                                   value="<#if regionData??><#if regionData.stuNumExpSix??>${regionData.stuNumExpSix!0}<#else>0</#if><#else>0</#if>">
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="save_config_btn" type="button" class="btn btn-primary">保存</button>
                        &nbsp;&nbsp;
                        <a id="cancel_btn" class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $('#save_config_btn').live('click', function () {
            // 获取数据
            var configInfo = {
                // 基础信息
                dictId: $('#dictId').val(),
                regionCode: $('#regionCode').val(),
                cityModel: $('#cityModel').val(),
                cityLevel: $('#cityLevel').val(),
                springMainCity: $('#springMainCity').val(),
                settlementType: $('#settlementType').val(),
                marketStuLevel: $('#marketStuLevel').val(),
                // 目标信息
                addBudget: $('#addBudget').val(),
                marBudget: $('#marBudget').val(),
                aprBudget: $('#aprBudget').val(),
                mayBudget: $('#mayBudget').val(),
                junBudget: $('#junBudget').val(),
                doubleSubjectBudget: $('#doubleSubjectBudget').val(),
                gradeMathAddBudget: $('#gradeMathAddBudget').val(),
                stuNumExpSix: $('#stuNumExpSix').val()
            };

            // 前端校验
            var msg = validateConfig(configInfo, $('#regionName').html());
            if (msg.length == 0) {
                if (confirm("是否确认信息正确并保存？")) {
                    $.post('addregiondict.vpage', configInfo, function (data) {
                        if (data.success) {
                            alert("保存成功!");
                            window.location.href = 'index.vpage?regionCode=' + configInfo.regionCode;
                        } else {
                            alert(data.info);
                        }
                    });
                }
            } else {
                alert(msg);
            }
        });

        $("#regionCode").live("keypress", function (e) {
            if (e.keyCode == 13) {
                var regionInfo = {
                    regionCode: $("#regionCode").val()
                }
                $.get('findname.vpage', regionInfo, function (data) {
                    // 修改regionName的内容
                    if (data != null) {
                        $("#regionName").html(data.regionName);
                    } else {
                        alert("未找到地区编码所对应的地区");
                        $("#regionName").html("");
                        $("#regionCode").val("");
                    }
                });
            }
        });

        $("#marketStuLevel").live("change", function () {
            if ($("#marketStuLevel").val() == "小学") {
                $("#cityLevel").attr("disabled", false);
                $("#settlementType").attr("disabled", false);
            } else {
                $("#cityLevel").attr("disabled", true);
                $("#settlementType").attr("disabled", true);
            }
        })

        if ($("#marketStuLevel").val() == "小学") {
            $("#cityLevel").attr("disabled", false);
            $("#settlementType").attr("disabled", false);
        } else {
            $("#cityLevel").attr("disabled", true);
            $("#settlementType").attr("disabled", true);
        }
    })

    function validateConfig(configInfo, regionName) {
        var msg = "";
        if (configInfo.regionCode == "") {
            msg += "区域编码不能为空 \r\n";
        }
        if (configInfo.cityModel == "") {
            msg += "直营代理不能为空 \r\n";
        }
        if (configInfo.marketStuLevel == "") {
            msg += "学校级别不能为空 \r\n";
        }
        if (configInfo.springMainCity == "") {
            msg += "主城区/非主城区不能为空 \r\n";
        }
        if (configInfo.regionCode != "" && regionName == "") {
            msg += "无法找到您输入的区域编码对应的区域 \r\n";
        }
        if (configInfo.addBudget < 0) {
            msg += "新增认证目标不能小于0 \r\n";
        }
        if (configInfo.marBudget < 0) {
            msg += "三月月活目标不能小于0 \r\n";
        }
        if (configInfo.aprBudget < 0) {
            msg += "四月月活目标不能小于0 \r\n";
        }
        if (configInfo.mayBudget < 0) {
            msg += "五月月活目标不能小于0 \r\n";
        }
        if (configInfo.junBudget < 0) {
            msg += "六月月活目标不能小于0 \r\n";
        }
        if (configInfo.doubleSubjectBudget < 0) {
            msg += "双科认证目标不能小于0 \r\n";
        }
        if (configInfo.gradeMathAddBudget < 0) {
            msg += "1~2年级数学认证目标数不能小于0 \r\n";
        }
        if (configInfo.stuNumExpSix < 0) {
            msg += "排除毕业班的学生数不能小于0 \r\n";
        }
        if (configInfo.marketStuLevel == "小学") {
            if (configInfo.cityLevel == "") {
                msg += "学校级别为小学时城市级别不能为空 \r\n";
            }
            if (configInfo.citySettlement == "") {
                msg += "学校级别为小学时低渗高渗不能为空 \r\n";
            }
        }
        return msg;
    }

</script>

</@layout_default.page>