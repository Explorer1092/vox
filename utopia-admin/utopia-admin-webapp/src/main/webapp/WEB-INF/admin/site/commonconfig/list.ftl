<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='通用配置管理' page_num=4>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <legend style="font-weight: 700;">通用配置管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="config-query" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/site/commonconfig/configlist.vpage" >
                    <ul class="inline">
                        <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                        <li>
                            <button type="button" class="btn btn-info" onclick="loadAddDialog()">新增通用配置</button>
                        </li>
                        <br><br>
                        <li>目录名称&nbsp;
                            <select id="qCategory" name="qCategory" width="25px">
                                <#if categoryMap??>
                                    <#list categoryMap?keys as key>
                                        <option value="${key}" <#if category??><#if category==key>selected</#if></#if>>${categoryMap[key].getNote()!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </li>
                        <li>配置的Key&nbsp;
                            <input type="text" id="qConfigKey" name="qConfigKey" width="25px" value="<#if configKey??>${configKey!}</#if>"/>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">查询</button>
                        </li>
                    </ul>
                </form>
                <#if configPage??>
                <div id="data_table_journal">
                    <table class="table table-bordered table-striped">
                        <tr>
                            <td style="width:40px;">ID</td>
                            <td style="width:70px;">目录名称</td>
                            <td style="width:100px;">配置的Key</td>
                            <td>配置的Value</td>
                            <td style="width:80px;">地区编码</td>
                            <td>描述</td>
                            <#--<td style="width:90px;">创建时间</td>-->
                            <#--<td style="width:90px;">更新时间</td>-->
                            <#--<td style="width:120px;">最后更新人</td>-->
                            <td style="width:40px;">操作</td>
                        </tr>
                        <#if configPage.content??>
                            <#list configPage.content as config >
                                <tr>
                                    <td id="id${config.id!}">${config.id!}</td>
                                    <td>${config.category!}</td>
                                    <td id="cat_${config.id!}" style="display: none;">${config.categoryName!''}</td>
                                    <td id="key_${config.id!}">${config.configKeyName!''}</td>
                                    <td id="value_${config.id!}">${config.configKeyValue!''}</td>
                                    <td id="region_${config.id!}">${config.configRegionCode!''}</td>
                                    <td id="description_${config.id}">${config.description!''}</td>
                                    <#--<td>${config.createDatetime!}</td>-->
                                    <#--<td>${config.updateDatetime!}</td>-->
                                    <#--<td>${config.latestUpdateUserName!''}</td>-->
                                    <td>
                                        <a id="edit_${config.id!}" href="javascript:void(0)" role="button" title="编辑">
                                            <i class="icon-edit"></i>
                                        </a>
                                        <a id="delete_${config.id!}" href="javascript:void(0);" role="button"title="删除">
                                            <i class="icon-trash"></i>
                                        </a>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
                <ul class="pager">
                    <#if configPage.hasPrevious()>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${pageNumber!} 页</a></li>
                    <li class="disabled"><a>共 <#if configPage.totalPages==0>1<#else>${configPage.totalPages!}</#if> 页</a></li>
                    <#if configPage.hasNext()>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
                </#if>
            </div>
        </div>
    </div>
</div>

<div id="add_dialog" class="modal fade hide" style="width:600px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3 class="modal-title">通用配置信息</h3>
            </div>
            <div class="form-horizontal" style="height:420px;">
                <div class="modal-body" style="height: auto; overflow: visible;">
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>目录名称</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <select id="category" style="width: 300px;">
                                <#if categoryMap??>
                                    <#list categoryMap?keys as key>
                                        <option value="${key}" <#if category??><#if category==key>selected</#if></#if>>${categoryMap[key].getNote()!}</option>
                                    </#list>
                                </#if>
                            </select>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>配置的Key</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="config-key" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>配置的Value</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <textarea id="config-value" rows="5" style="resize: none; width: 286px;"></textarea><span style="color: red">*必填</span>
                        </div>
                    </div>
                    <#--// 地区的选择最好还能优化一下-->
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>地区编码</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="number" id="region-code"  disabled="disabled" style="width: 286px;"/>
                            <span style="color: red">*请选择</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>地区选择</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <span id="province" style="width: 100px;"></span>
                            <span id="city" style="width: 100px;"></span>
                            <span id="county" style="width: 100px;"></span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>描述</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <textarea id="description" name="description" cols="40" rows="2" style="resize: none;width: 286px;"></textarea>
                        </div>
                    </div>
                </div>
            </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_modal_submit" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<input type="hidden" id="currentId" value=""/>

<script type="text/javascript">
    $(function () {
        $('[id^="edit_"]').on('click', function(){
            loadEditDialog($(this).attr("id").substring("edit_".length));
        });

        $('[id^="delete_"]').on("click", function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            var category = $('#cat_'+id).html().trim();
            $.post('deleteconfig.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $("#qCategory").val(category);
                    window.location.reload();
                }
            });
        });

        $("#btn_modal_submit").on("click",function(){
            var category = $("#category").val();
            var configKey = $("#config-key").val();
            var configValue = $("#config-value").val();
            var regionCode = $("#region-code").val();
            var description = $("#description").val();
            var id = $("#currentId").val();
            if(!validateInput(category, configKey, configValue, regionCode)) {
                return false;
            }
            $.post('saveconfig.vpage',{
                id:id,
                category:category,
                configKey:configKey,
                configValue:configValue,
                regionCode:regionCode,
                description:description
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $("#qCategory").val(category);
                    window.location.reload();
                }
            });
        });
    });

    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#config-query").submit();
    }

    function loadAddDialog(){
        $('#currentId').val('');
        $('#category').val('');
        $('#config-key').val('');
        $('#config-value').val('');
        $('#region-code').val('0');
        $('#description').val('');
        $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
            $("#province").html( template("T:Province", { list : data, code:0}) );
        });
        $("#city").attr("style","display:none");
        $("#county").attr("style","display:none");
        $('#add_dialog').modal('show');
    }

    function initRegion(type, pcode, ccode, acode) {
        if(type == 0 || type == 1) {
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
                $("#province").html( template("T:Province", { list : data, code: pcode}) );
            });
            $("#city").attr("style","display:none");
            $("#county").attr("style","display:none");
        } else if(type == 2) {
            $("#city").attr("style","");
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
                $("#province").html( template("T:Province", { list : data, code: pcode}) );
            });
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-"+pcode+".vpage", function(data) {
                $("#city").html( template("T:City", { list : data, code: ccode}) );
            });
            $("#county").attr("style","display:none");
        } else if(type == 3) {
            $("#city").attr("style","");
            $("#county").attr("style","");
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
                $("#province").html( template("T:Province", { list : data, code: pcode}) );
            });
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-"+pcode+".vpage", function(data) {
                $("#city").html( template("T:City", { list : data, code: ccode}) );
            });
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-"+ccode+".vpage", function(data) {
                $("#county").html( template("T:County", { list : data, code: acode}) );
            });
        }
    }

    function loadEditDialog(id){
        var region = $('#region_'+id).html().trim();
        $('#currentId').val(id);
        $('#category').val($('#cat_'+id).html().trim());
        $('#config-key').val($('#key_'+id).html().trim());
        $('#config-value').val($('#value_'+id).text().trim());
        $('#description').val($('#description_'+id).html().trim());
        $('#region-code').val(region);
        $('#add_dialog').modal('show');
        // FIXME 下策，待我再研究研究
        $.getJSON("${requestContext.webAppContextPath}/legacy/map/region-"+region+".vpage", function(data) {
            initRegion(data.type, data.pcode, data.ccode, data.acode);
        });
        <#--var arr = new Array(0);-->
        <#--$.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {-->
            <#--$("#province").html( template("T:Province", { list : data}) );-->
        <#--});-->
        <#--$('#city').html( template("T:City", {list: arr}));-->
        <#--$('#county').html( template("T:County", {list: arr}));-->

    }

    function validateInput(category, config_key, config_value, region_code) {
        var pattern_str = /^([a-zA-Z_])([a-zA-Z0-9_.]){0,29}$/;
        var pattern_region = /^[0]|([1-9][0-9]{5})$/;
        if(category.trim() == ''){
            alert("请填写目录名称!");
            return false;
        }
        if(!pattern_str.test(category.trim())){
            alert("目录名称只能由字母、数字以及点(.)下划线(_)组成，最长三十个字符！");
            return false;
        }
        if(config_key.trim() == ''){
            alert("请填写配置的Key！");
            return false;
        }
        if(!pattern_str.test(config_key.trim())){
            alert("配置的Key只能由字母、数字以及点(.)下划线(_)组成，最长三十个字符！");
            return false;
        }
        if(config_value.trim() == ''){
            alert("请填写配置的Value！");
            return false;
        }
        if(region_code.trim() == '' || !pattern_region.test(region_code.trim())){
            alert("请填写正确的地区编码！");
            return false;
        }
        return true;
    }
    function selectProvince(province){
        if(province == 0){
            $("#city").attr("style","display:none");
            $("#county").attr("style","display:none");
        } else {
            $("#city").attr("style","");
            $("#county").attr("style","display:none");
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-" + province +".vpage", function(data) {
                $("#city").html( template("T:City", { list : data, code : province}) );
            });
        }
        $("#region-code").val(province);
    }

    function selectCity(city){
        if(city == 0){
            $("#county").attr("style","display:none");
            city = $("#prov-selector option:selected").val();
        } else {
            $("#county").attr("style","");
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-" + city +".vpage", function(data) {
                $("#county").html( template("T:County", { list : data, code : city}) );
            });
        }
        $("#region-code").val(city);
    }

    function selectCounty(county){
        if(county == 0){
            county = $("#city-selector option:selected").val();
        }
        $("#region-code").val(county);
    }

</script>
<script type="text/html" id="T:Province">
    <select id="prov-selector" onchange="selectProvince(this[selectedIndex].value)" style="width: 97px;">
        <option value="0" selected>全国</option>
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <%if (code == list[i].id){%>
        <option value="<%=list[i].id%>" selected><%=list[i].text%></option>
        <%} else {%>
        <option value="<%=list[i].id%>"><%=list[i].text%></option>
        <%}%>
        <%}%>
    </select>
</script>
<script type="text/html" id="T:City">
    <select id="city-selector" onchange="selectCity(this[selectedIndex].value)" style="width: 97px;">
        <option value="0" selected>全省</option>
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <%if (code == list[i].id){%>
        <option value="<%=list[i].id%>" selected><%=list[i].text%></option>
        <%} else {%>
        <option value="<%=list[i].id%>"><%=list[i].text%></option>
        <%}%>
        <%}%>
    </select>
</script>
<script type="text/html" id="T:County">
    <select id="county-selector" onchange="selectCounty(this[selectedIndex].value)" style="width: 97px;">
        <option value="0" selected>全市</option>
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <%if (code == list[i].id){%>
        <option value="<%=list[i].id%>" selected><%=list[i].text%></option>
        <%} else {%>
        <option value="<%=list[i].id%>"><%=list[i].text%></option>
        <%}%>
        <%}%>
    </select>
</script>
</@layout_default.page>