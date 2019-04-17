<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='标签管理' page_num=6>
<style>
    .table>tbody>tr>td{
        text-align:center;
    }
</style>
<div class="box span12">
    <div class="box-header well" data-original-title="">
        <h2><i class="icon-th"></i> 运营标签详情</h2>

        <div class="box-icon">
            <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
            <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
        </div>

    </div>

    <div class="box-content ">
        <div style="margin-bottom:15px;clear:both;overflow:hidden">
            <form id="regionSearch" action="/sysconfig/schooldic/schoolDictDetail.vpage" method="get" style="margin:15px 0;clear：both ;overflow: hidden">
                <ul class="inline" style="float:left;text-decoration: none;width:100%;display: block;line-height:30px;height: 30px;clear:both;overflow:hidden;margin:0">
                    <li style="width:10%;display: inline-block;">
                        <label style="text-align:left">学校所在地区</label>
                    </li>
                    <li style="width:20%;display: inline-block;">
                        <label for="regionCode" style="text-align:left">所在省：
                            <select id="provinces" name="provinces" class="multiple district_select" next_level="citys" style="width:50%">
                                <option value="0" selected>请选择</option>
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
                                <option value="0" selected>请选择</option>
                            </select>
                        </label>
                    </li>
                    <li style="width:20%;display: inline-block;">
                        <label for="countys" style="text-align:left">
                            所在区：
                            <select id="countys" data-init='false' name="countys" class="multiple district_select" style="width:50%">
                                <option value="0" selected>请选择</option>
                            </select>
                        </label>
                    </li>
                    <li style="width:10%;display: inline-block;"><input type="button" id="search" style="margin-top:-10px" value="查询"/></li>
                    <a href="javascript:;" class="btn btn-primary" id="most-delete" style="margin-bottom: 12px">批量删除</a>
                </ul>
            </form>
        </div>
        <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>

                <tr>
                    <th class="unSorting" style="width: 30px;text-align: center"><input type="checkbox" class="all-select">全选</th>
                    <th class="sorting" style="width: 80px;text-align: center">学校/老师 ID</th>
                    <th class="sorting" style="width: 80px;text-align: center">标签名称</th>
                    <th class="sorting" style="width: 90px;text-align: center">学校/老师 名称</th>
                    <th class="sorting" style="width: 60px;text-align: center">阶段</th>
                    <th class="sorting" style="width: 100px;text-align: center">城市</th>
                    <th class="sorting" style="width: 60px;text-align: center">地区</th>
                    <th class="sorting" style="width: 75px;text-align: center">操作</th>
                </tr>
                </thead>

                <tbody role="alert" aria-live="polite" aria-relevant="all">
                </tbody>
            </table>
        </div>
    </div>
    <#--删除确认弹窗-->
    <div id="deleteTag_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <h3 id="delete-info"></h3>
                </div>
                <div class="modal-footer">
                    <div>
                        <button id="deleteDepSubmitBtn" type="button" class="btn btn-large btn-default" >确认</button>
                        <button type="button" class="btn btn-large btn-primary" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {
        function getUrlParam (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return decodeURIComponent(r[2]); return null;
        };
        var tagId = getUrlParam("tagId");
//        点击查询
        $(document).on("click","#search",function () {
            var provinceCode = $("#provinces").val(),
                    cityCode = $("#citys").val(),
                    countyCode = $("#countys").val();
            if(provinceCode === "0"){
                provinceCode = "";
                cityCode = "";
                countyCode = "";
            };
            var dataObj = {
                tagId : tagId,
                provinceCode : provinceCode,
                cityCode : cityCode,
                countyCode : countyCode,
            };
            $.get("/sysconfig/tag/tag_target_list.vpage",dataObj,function (res) {
                if(res.success){
                    var dataTableList = [];
                    if(res.dataList){
                        tag_name = res.dataList && res.dataList.length > 0 ? res.dataList[0].tagName : "";
                    };
                    for(var i=0;i < res.dataList.length;i++){
                        var item = res.dataList[i];
                        var operator = '<a href="javascript:;" id="singleDelete" data-id="'+ item.id +'">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;';
                        var _input = '<input type="checkbox" class="tag-apply-item" data-id ="'+ item.id+'">'
                        if(item.tagType === "SCHOOL"){
                            var schoolOrTeacherName = item.schoolName,
                                    schoolOrTeacherId = item.schoolId;
                        }else if(item.tagType === "TEACHER"){
                            var schoolOrTeacherName = item.teacherName,
                                    schoolOrTeacherId = item.teacherId;
                        };
                        if(item.schoolLevel === "JUNIOR"){
                            item.schoolLevel = "小学"
                        }else if(item.schoolLevel === "MIDDLE"){
                            item.schoolLevel = "中学"
                        }else if(item.schoolLevel === "HIGH"){
                            item.schoolLevel = "高中"
                        }else if(item.schoolLevel === "INFANT"){
                            item.schoolLevel = "学前"
                        }
                        var arr = [_input,schoolOrTeacherId, item.tagName , schoolOrTeacherName, item.schoolLevel, item.cityName,item.countyName,operator];
                        dataTableList.push(arr);
                    }
                    var reloadDataTable = function () {
                        var table = $('#DataTables_Table_0').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据
                    };
                    setTimeout(reloadDataTable(),0);
                }else{
                    layer.alert(res.info)
                }
            });
        })
//获取默认列表数据
        var tag_name = "";
        $.get("/sysconfig/tag/tag_target_list.vpage",{tagId:tagId},function (res) {
            if(res.success){
                var dataTableList = [];
                tag_name = res.dataList && res.dataList.length > 0 ? res.dataList[0].tagName : "";
                for(var i=0;i < res.dataList.length;i++){
                    var item = res.dataList[i];
                    var operator = '<a href="javascript:;" id="singleDelete" data-id="'+ item.id +'">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;';
                    var _input = '<input type="checkbox" class="tag-apply-item" data-id ="'+ item.id+'">'
                    if(item.tagType === "SCHOOL"){
                        var schoolOrTeacherName = item.schoolName,
                            schoolOrTeacherId = item.schoolId;
                    }else if(item.tagType === "TEACHER"){
                        var schoolOrTeacherName = item.teacherName,
                            schoolOrTeacherId = item.teacherId;
                    };
                    if(item.schoolLevel === "JUNIOR"){
                        item.schoolLevel = "小学"
                    }else if(item.schoolLevel === "MIDDLE"){
                        item.schoolLevel = "中学"
                    }else if(item.schoolLevel === "HIGH"){
                        item.schoolLevel = "高中"
                    }else if(item.schoolLevel === "INFANT"){
                        item.schoolLevel = "学前"
                    }
                    var arr = [_input,schoolOrTeacherId, item.tagName, schoolOrTeacherName, item.schoolLevel, item.cityName,item.countyName,operator];
                    dataTableList.push(arr);
                }
                var reloadDataTable = function () {
                    var table = $('#DataTables_Table_0').dataTable();
                    table.fnClearTable();
                    table.fnAddData(dataTableList); //添加添加新数据
                };
                setTimeout(reloadDataTable(),0);
            }else{
                layer.alert(res.info)
            }
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
                next_level = $("#" + next_level);
                clearNextLevel($this);
                if(regionCode !== "0"){
                    $.ajax({
                        type: "post",
                        url: "/sysconfig/schooldic/regionlist.vpage",
                        data: {
                            regionCode: regionCode
                        },
                        success: function (data) {
                            var regionList = data.regionList;
                            for (var i in regionList) {
                                html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                            }
                            next_level.html(html);
                            next_level.trigger('change');
                        }
                    });
                }else {
                    $("#citys").html("<option value=\"0\" selected>请选择</option>");
                    $("#countys").html("<option value=\"0\" selected>请选择</option>");
                }
            }
        });
//        全选
        $(".all-select").click(function () {
            var allSelect = $(this).attr("checked");
            $(".tag-apply-item").each(function (index, element) {
                if (allSelect) {
                    $(element).attr("checked", allSelect);
                    $(element).parent("span").addClass("checked");
                } else {
                    $(element).attr("checked", false);
                    $(element).parent("span").removeClass("checked");
                }
            });
        });

//        点击批量删除
        $(document).on("click","#most-delete",function () {
            $("#deleteTag_dialog").modal("show");
            $("#delete-info").html('确认要批量删除所选学校或老师的"'+tag_name+'""标签么');
            $("#deleteDepSubmitBtn").unbind("click");//清除之前绑定的事件
            $("#deleteDepSubmitBtn").on("click",function () {
                $("#deleteTag_dialog").modal("hide");
                var mostDeleteArray = [];
                $(".tag-apply-item").each(function (index,element) {
                    if($(element).attr("checked")){
                        mostDeleteArray.push($(element).attr("data-id"));
                    }
                });
                var ids = mostDeleteArray.join(",");
                deleteAjax(ids);
            })

        });
//        单个删除
        $(document).on("click","#singleDelete",function () {
            var id = $(this).attr("data-id");
            deleteAjax(id);
        });
//        删除请求接口
        function deleteAjax(ids){
            $.get("/sysconfig/tag/delete_tag_target.vpage",{ids:ids,tagId:tagId},function (res) {
                if (res.success) {
                    alert("删除成功");
                    window.location.reload();
                }else{
                    alert(res.info);
                }
            });
        }
    })
</script>
</@layout_default.page>