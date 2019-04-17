<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>

<@layout_default.page page_title="多账号用户合并(${groupId!''})" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.min.js"></script>
<div class="span9">
    <@headsearch.headSearch/>
    <div>
        <fieldset>
            <legend>多账号学生详情</legend>
        </fieldset>
        <#if groupInfo??&&groupInfo?has_content>
            <legend style="border:0;border-top: 1px solid #A21616;">
                组ID(${groupInfo.id!} / <#if groupInfo.groupType=='WALKING_GROUP'>教学<#else>行政</#if>)
                <button class="btn btn-success" onclick="mergeGroup(${groupId!''})">合并组</button>
                <button class="btn btn-danger" onclick="mergeStudent(${groupId!''})">批量合并多账号用户</button>
            </legend>
        </#if >

        <#if results??&&results?has_content>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>学生姓名</th>
                <th>一起作业</th>
                <th>扫描信息</th>
                <th>数学作业</th>
            </tr>
            <#list results?keys as key>
            <tr>

                <td>
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <td> ${key}</td>
                        </tr>
                    </table>
                </td>

                <td>
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <th>17ID</th>
                            <th>创建时间</th>
                            <th>最近登录时间</th>
                            <th>操作</th>
                        </tr>
                         <#list results['${key}'] as studentInfo>
                             <tr>
                                 <td> ${studentInfo['id']?default("")}</td>
                                 <td> ${studentInfo['createTime']?default("")}</td>
                                 <td> ${studentInfo['loginTime']?default("")}</td>
                                 <td> <input class="v-selectStudent" data-name="${key!''}"data-studentId="${studentInfo.id!''}" name="student_${key_index}" type="radio" value=""/> 保留</td>
                             </tr>
                         </#list>
                    </table>
                </td>
                <td>
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <th>KlxID</th>
                            <th>填涂号</th>
                            <th>扫描数量</th>
                            <th>最后一次扫描时间</th>
                            <th>操作</th>
                        </tr>
                         <#list results['${key}'] as studentInfo>
                             <tr>
                                 <td> ${studentInfo['klxId']?default("")}</td>
                                 <td> ${studentInfo['scanNumber']?default("")}</td>
                                 <td> ${studentInfo['scanCount']?default("")}</td>
                                 <td> ${studentInfo['lastTime']?default("")}</td>
                                 <td>
                                     <input class="v-selectStudent" data-name="${key!''}" data-klxId = "${studentInfo.klxId!''}"  name="klxStudent_${key_index}" type="radio" value=""/> 保留
                                 </td>
                             </tr>
                         </#list>
                    </table>
                </td>
                <td>
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <th>作业总量</th>
                            <th>最近作业时间</th>
                        </tr>
                         <#list results['${key}'] as studentInfo>
                             <tr>
                                 <td> ${studentInfo['mathCount']?default("")}</td>
                                 <td> ${studentInfo['mathLastTime']?default("")}</td>
                             </tr>
                         </#list>
                    </table>
                </td>
            </tr>
            </#list>
        </table>
        <#else>
            <span>没有需要合并的学生</span>
        </#if >
    </div>
    <div id="modal-mergeGroup_dialog" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-body dl-horizontal" id="scid">
                    <dl>
                        <dt style="width: 170px;">当前组ID:</dt>
                        <dd id="targetGroupId">${groupId!}</dd>
                    </dl>
                    <dl>
                        <dt>被合并组的ID:</dt>
                        <dd><input id="merge_group_id" placeholder="被合并的组将要被删除" type="text"/></dd>
                    </dl>
                </div>
                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="modal-btn-mergeGroup" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "../specialschool.ftl">
<script>

    function mergeGroup(groupId) {
        $("#modal-mergeGroup_dialog").modal("show");
    }

    $("#modal-btn-mergeGroup").on("click", function () {
        var oid = $("#merge_group_id").val();
        if (!oid) {
            alert("请输入组ID");
            return;
        }
        $.post("mergegroup.vpage", {
            oid: oid,
            nid: ${groupId!},
        }, function (data) {
            if (data.success) {
                alert("合并成功");
                window.location.reload();
            } else {
                alert(data.info);
            }
        });
    });

    //验证是否未定义或null或空字符串
    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    var selectedObj = [];

    $(document).on("click", ".v-selectStudent", function () {
        var $this = $(this);
        var $studentId = $this.attr("data-studentId");
        var $klxId = $this.attr("data-klxId");
        var $name = $this.attr("data-name");
        if ($this.prop("checked")) {
            var flag = false;
            for (var obj in selectedObj) {
                var object = selectedObj[obj];
                if (object.name == $name) {
                    var students = object.students;
                    selectedObj.splice(obj, 1);
                    if ($klxId) {
                        students.klxId = $klxId
                    }
                    if ($studentId) {
                        students.id = $studentId
                    }
                    selectedObj.push({name:$name, students:students});
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                selectedObj.push({name:$name, students:{id:$studentId, klxId:$klxId}});
            }
            console.log(selectedObj);
        }
    })

    function mergeStudent(groupId) {
        if (!confirm("是否合并多账号用户")) {
            return false;
        }

        if (selectedObj.length == 0) {
            alert("没有选择用户");
            return false;
        }


        var students = [];

        for (var obj in selectedObj) {
            var object = selectedObj[obj];
            students.push(object.students)
        }

        for (var i in students) {
            var student = students[i]
            if (isBlank(student.id) || isBlank(student.klxId)) {
                alert("选择的数据有误，请核实")
                return false;
            }
        }

        $.ajax({
            type: "post",
            url: "/crm/shensz/mergestudent.vpage",
            data: {
                groupId: groupId,
                students: JSON.stringify(students)
            },
            success: function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            }
        });
    }
</script>
</@layout_default.page>