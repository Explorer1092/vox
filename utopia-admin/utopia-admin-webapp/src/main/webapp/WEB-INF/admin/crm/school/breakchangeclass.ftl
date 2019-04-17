<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="打散换班" page_num=3>
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
        <h5>1.需要区分行政班内部打乱、或是教学重新分班两种不同类型</h5>
        <h5>2.一次导入只能处理一种类型打散换班</h5>
        <h5>3.上传的excel文件需要为 .xls或 .xlsx格式</h5>
        <h5>4.处理完成后会下载excel到电脑，excel中有成功/失败备注</h5>
        <h5>5.excel模板如右图<#if (school.level!0) == 2>，<a id="downloadExcel" href="javascript:;" style="color:#34a8fb;">点击下载模板</a></#if></h5>
        <h4>换班Excel文件模板：</h4>
        <hr>
        <#if (school.level!0) == 1>
            <table class="table table-bordered">
                <thead>
                <tr style="background-color: #d2cfcf;">
                    <th>年級</th>
                    <th>换班前班级（非必填）</th>
                    <th>换班前学号（非必填）</th>
                    <th>换班后学号（非必填）</th>
                    <th>学生姓名</th>
                    <th>换班后班级</th>
                    <th>换班后老师ID</th>
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
                        51：小班<br/>
                        52：中班<br/>
                        53：大班<br/>
                        54：学前班<br/>
                    </td>
                    <td></td>
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
        <#else>
        <table class="table table-bordered" style="width: 600px">
            <thead>
            <tr style="background-color: #d2cfcf;">
                <th>年級</th>
                <th>学生姓名</th>
                <th>换班后新班级</th>
                <th>类型</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>7年级</td>
                <td>王思思</td>
                <td>4班</td>
                <td>行政班</td>
            </tr>
            <tr>
                <td>高一</td>
                <td>王晓晓</td>
                <td>物理B班</td>
                <td>教学班</td>
            </tr>
            </tbody>
        </table>
        </#if>
    </div>
    <div class="alert alert-error" style="display: none;" id="alertDiv">
        <button type="button" class="close" data-dismiss="alert">×</button>
        <strong id="infoMsg"></strong>
    </div>
    <#if school??>
        <div>
            <#if (school.level!0) == 1>
            <form action="/crm/school/changeclass.vpage" id="uploadForm" method="post" enctype="multipart/form-data" style="display: none;">
                <input type="file" class="v-fileupload" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel" name="excelFile" id="upLoadFileInput" />
                <input type="text" name="excelName" id="uploadFileName" />
                <input type="text" value="${schoolId!}" name="schoolId" />
            </form>
            <#else>
            <form action="/crm/school/executechangeclass.vpage" id="uploadForm" method="post" enctype="multipart/form-data" style="display: none;">
                <input type="file" class="v-fileupload" accept=".xls, .xlsx" name="changeStudentData" id="upLoadFileInput" />
                <input type="text" value="${schoolId!}" name="schoolId" />
            </form>
            </#if>

            <a class="btn" style="display: inline-block; min-width: 250px; margin-right: 10px;" id="uploadInputBtn">请选择需要上传的excel</a>
            <a class="btn btn-danger" id="uploadBtn" href="javascript:;"> 开始处理</a>
            <span class="js-upLoadMsg" style="color: #f00;"></span>
        </div>
    </#if>
</div>
<#include "../specialschool.ftl" />

<script>
    (function () {
        var lockUpload = false; // 防止上传时再次请求
        var schoolId = "${schoolId!}";
        var isPrimarySchool = false;
        <#if (school.level!0) == 1>
            isPrimarySchool = true;
        </#if>

        // excel 格式校验
        var checkExcelFile = function (file) {
            if (!file) {
                $(".js-upLoadMsg").html('您还没有选择excel文档');
                lockUpload = false;
                return false;
            }

            var fileName = file.name;
            if (fileName.indexOf('.xls') === -1 && fileName.indexOf('.xlsx') === -1) {
                $(".js-upLoadMsg").html('仅支持上传excel文档');
                lockUpload = false;
                return false;
            }

            return true;
        };

        // 下载excel模板
        $(document).on("click", "#downloadExcel", function () {
            var templateId = 'xhjx'; // 中学模板
//            if (isPrimarySchool) {
//                var templateId = ''; // 小学模板
//            }
            var downloadIframe = "<iframe style='display:none;' src='/crm/school/gettemplate.vpage?template=" + templateId + "&schoolId=" + schoolId + "' />";
            $("body").append(downloadIframe);
        });

        // 点击选择excel
        $(document).on('click', '#uploadInputBtn', function () {
            $('#upLoadFileInput').trigger('click');
        });

        // 监听input被选中
        $(document).on('change', '#upLoadFileInput', function () {
            var file = document.getElementById('upLoadFileInput').files[0];
            if (!file) { // 未选择
                $('#uploadInputBtn').text('请选择需要上传的excel');
                return ;
            }
            $('#uploadInputBtn').text(file.name);
            $('#uploadFileName').val(file.name);
            $(".js-upLoadMsg").html('');
        });

        // 点击处理excel
        $(document).on("click", "#uploadBtn", function () {
            if (lockUpload) return ;
            lockUpload = true;

            var file = document.getElementById('upLoadFileInput').files[0];
            if (!checkExcelFile(file)) return ;

            var formData = new FormData();
            formData.append('changeStudentData', file);
            formData.append('schoolId', schoolId);
            checkUploadData(formData);
        });

        // 第一步：check上传的excel
        var checkUploadData = function (formData) {
            if (isPrimarySchool) { // 小学直接走表单提交
                $("#uploadForm").submit();
            } else { // 中学先ajax check再用表单提交
                $.ajax({
                    url: '/crm/school/checkchangeclassdata.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    async: true,
                    timeout: 20 * 1000,
                    success: function (res) {
                        if (res.success) {
                            // 第二步：上传excel
                            $("#uploadForm").submit();
                            window.alert('处理成功，请查看Excel获取详细结果');
                        } else {
                            $(".js-upLoadMsg").html(res.info);
                        }
                    },
                    complete: function () {
                        lockUpload = false; // 解锁
                    }
                });
            }
        }
    })();
</script>
</@layout_default.page>