<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="关联走课学生" page_num=3>
<div id="main_container" class="span9" style="width: 100%">
    <#if error?? && error?has_content>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
    </#if>
    <#if school??>
        <legend>
            <strong>${school.cname!''}(${school.id!''})</strong>
        </legend>
    </#if>
    <div>
        <h4>注意事项：</h4>
        <h5>1.此功能只针对初高中使用</h5>
        <h5>2.不允许跨校、跨年级执行此操作，切记！！！</h5>
        <h5>3.处理完成会自动下载Excel到本地，Excel中会有成功列表和失败列表</h5>
        <h4>换班Excel文件模板：</h4>
        <hr>
        <table class="table table-bordered">
            <thead>
            <tr style="background-color: #d2cfcf;">
                <th>年級</th>
                <th>当前班级（非必填）</th>
                <th>校内学号（非必填）</th>
                <th>学生姓名</th>
                <th>关联的新班级</th>
                <th>新班级老师ID</th>
                <th>成功/失败（操作后回填）</th>
                <th>备注（操作后回填）</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>格式要求：纯数字<br/>
                    1：一年级<br/>
                    2：二年级<br/>
                    ...<br/>
                    9：九年级<br/>
                    11：高一<br/>
                    12：高二<br/>
                    13：高三<br/>
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td>如：<br/>
                    3班<br/>
                    4班
                </td>
                <td></td>
                <td>
                    1.操作成功<br/>
                    2.操作失败
                </td>
                <td>
                    操作成功<br/>
                    学生ID<br/>
                    操作失败<br/>
                    本条记录失败原因<br/>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="alert alert-error" style="display: none;" id="alertDiv">
        <button type="button" class="close" data-dismiss="alert">×</button>
        <strong id="infoMsg"></strong>
    </div>
    <#if school??>
    <div>
        <form action="linkclasses.vpage" id="uploadForm" method="post" enctype="multipart/form-data">
            <label for="">请选择需要上传的Excel 文件：</label>
            <input type="file" id="upLoadFileInput" name="excelFile" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"/>
            <a class="btn btn-danger" id="uploadBtn" href="javascript:;"> 开始处理</a>
            <input type="hidden" value="${schoolId!}" name="schoolId" id="schoolId">
            <input type="hidden" value="" name="excelName" id="upLoadFileName">
            <span class="js-upLoadMsg"></span>
        </form>
    </div>
    </#if>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 150%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在处理，请稍候……</p>
</div>
    <#include "../specialschool.ftl">
<script>
    (function () {
        var checkExcelFile = function (file) {
            if(!file){
                alert("请选择Excel文件");
                return false;
            }
            var fileName = file.name;
            if(fileName.indexOf('.xls') != -1 || fileName.indexOf('.xlsx') != -1) {
                return true;
            }else{
                alert("请选择Excel文件");
                return false;
            }
        };
        $(document).on("click", "#uploadBtn", function () {
            var file = document.getElementById('upLoadFileInput').files[0];
            if (checkExcelFile(file)) {
                if (file) {
                    $(".js-upLoadMsg").html('');
                    $("#upLoadFileName").val(file.name.split(".")[0]);
                    if (!checkSpecialSchool()) {
                        return false;
                    }
                    $("#uploadForm").submit();
                } else {
                    $(".js-upLoadMsg").html('请选择文件提交');
                }
            }
        });
    })()
</script>
</@layout_default.page>