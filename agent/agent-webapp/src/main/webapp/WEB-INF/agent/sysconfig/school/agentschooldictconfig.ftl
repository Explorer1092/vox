<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='学校字典表设置' page_num=6>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在删除，请等待……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>出错啦！ ${error!}</strong>
        </div>
    </#if>
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 学校字典表设置</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

        </div>

        <div class="box-content ">

            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right" style="margin-bottom:15px">
                    <#if schoolOperate!false>
                        <a id="add_dict" class="btn btn-success" href="updateSchoolDictInfo.vpage">
                            <i class="icon-plus icon-white"></i>
                            添加
                        </a>
                        <a href="importSchoolDictPage.vpage" class="btn btn-primary">批量导入字典表</a>
                    </#if>
                        <a href="/sysconfig/schooldic/exportSchoolDictInfo.vpage" target="_blank" class="btn btn-primary">导出字典表</a>
                    <#if schoolOperate!false>
                        <a href="dispose_apply_school.vpage" class="btn btn-success">
                            <i class="icon-wrench icon-white"></i>
                            处理申请学校
                        </a>
                    </#if>
                </div>
            </#if>
            <div style="margin-bottom:15px;clear:both;overflow:hidden">
                <form id="regionSearch" action="/sysconfig/schooldic/schoolDictDetail.vpage" method="get" style="margin:15px 0;clear：both ;overflow: hidden">
                    <ul class="inline" style="float:left;text-decoration: none;width:100%;display: block;line-height:30px;height: 30px;clear:both;overflow:hidden;margin:0">
                        <li style="width:10%;display: inline-block;">
                            <label style="text-align:left">学校所在地区</label>
                        </li>
                        <li style="width:20%;display: inline-block;">
                            <label for="regionCode" style="text-align:left">所在省：
                                <select id="provinces" name="provinces" class="multiple district_select" next_level="citys" style="width:50%">
                                    <#if provinces??>
                                        <#list provinces as p>
                                            <option value="${p.key}">${p.value}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li style="width:20%;display: inline-block;">
                            <label for="citys" style="text-align:left">
                                所在市：
                                <select id="citys" data-init='false' name="citys" class="multiple district_select" next_level="countys" style="width:50%">
                                </select>
                            </label>
                        </li>
                        <li style="width:20%;display: inline-block;">
                            <label for="countys" style="text-align:left">
                                所在区：
                                <select id="countys" data-init='false' name="countys" class="multiple district_select" style="width:50%">
                                </select>
                            </label>
                        </li>
                        <li style="width:20%;display: inline-block;"><input type="button" id="search" style="margin-top:-10px" value="查询"/></li>
                    </ul>
                </form>
            </div>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>

                    <tr>
                        <th class="sorting" style="width: 80px;">城市</th>
                        <th class="sorting" style="width: 90px;">地区</th>
                        <th class="sorting" style="width: 60px;">学校ID</th>
                        <th class="sorting" style="width: 100px;">学校名称</th>
                        <th class="sorting" style="width: 60px;">阶段</th>
                        <th class="sorting" style="width: 75px;">等级</th>
                        <th class="sorting" style="width: 75px;">难度</th>
                        <th class="sorting" style="width: 75px;">渗透情况</th>
                        <th class="sorting" style="width: 75px;">是否结算</th>
                        <th class="sorting" style="width: 145px;">操作</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if schoolList??>
                            <#list schoolList as s>
                            <tr class="odd">
                                <td class="center">${s.regionCode!'--'}</td>
                                <td class="center">${s.regionName!'--'}</td>
                                <td class="center">${s.schoolId!}</td>
                                <td class="center">${s.schoolName!'学校判假或失效'}</td>
                                <td class="center">${s.schoolLevel!'--'}</td>
                                <td class="center">${s.schoolPopularity!'--'}</td>
                                <td class="center">${s.schoolDifficulty!'--'}</td>
                                <td class="center">${s.agentSchoolPermeabilityType!'--'}</td>
                                <td class="center"><#if s.calPerformance?has_content && s.calPerformance!false >是<#else >否</#if></td>
                                <td class="center ">
                                    <#if schoolOperate!false>
                                        <a id="edit_${s.id}" class="btn btn-info"
                                           href="updateSchoolDictInfo.vpage?dictId=${s.id}">
                                            <i class="icon-edit icon-white"></i>
                                            编 辑
                                        </a>
                                        <a id="delete_${s.id}" class="btn btn-danger" href="javascript:void(0);">
                                            <i class="icon-trash icon-white"></i>
                                            删 除
                                        </a>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("a[id^='delete_").live('click', function () {
            if (confirm("是否确认删除该条字典表数据？")) {
                $("#loadingDiv").show();
                $.post('removeSchoolDictInfo.vpage', {
                    dictId: $(this).attr("id").substring("delete_".length)
                }, function (data) {
                    if (data.success) {
                        alert("删除成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                        $("#loadingDiv").hide();
                    }
                });
            }
        });
    });

    $("#search").on("click", function () {
        $("#regionSearch").submit();
    });

    function clearNextLevel(obj) {
        if (obj.attr("next_level")) {
            clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
        }
    }

    $(".district_select").on("change", function () {
        var html = null;
        var $this = $(this);
        var next_level = $this.attr("next_level");
        var regionCode = $this.val() === null?"":$this.val();
        if (next_level) {
            var codeType = next_level;
            next_level = $("#" + next_level);
            clearNextLevel($this);
            $.ajax({
                type: "post",
                url: "regionlist.vpage",
                data: {
                    regionCode: regionCode
                },
                success: function (data) {
                    if ("countys" == codeType) {
                        html = '<option value="-1">全部</option>';
                    } else {
                        html = '';
                    }
                    var regionList = data.regionList;
                    for (var i in regionList) {
                        html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                    }
                    next_level.html(html);
                    <#if conditionMap?has_content>
                        if (codeType == 'citys' && !next_level.data('init')) {
                            next_level.val(${conditionMap.citys!'-1'});
                            next_level.data('init', true);
                        } else if (codeType == 'countys' && !next_level.data('init')) {
                            next_level.val(${conditionMap.countys!'-1'});
                            next_level.data('init', true);
                        }
                    </#if>
                    next_level.trigger('change');
                }
            });
        }
    });
    $(function () {
        <#if conditionMap?has_content>
            $("#provinces").val('${conditionMap.provinces!"-1"}');
            $("#provinces").trigger('change');
        </#if>
    })
</script>
</@layout_default.page>
