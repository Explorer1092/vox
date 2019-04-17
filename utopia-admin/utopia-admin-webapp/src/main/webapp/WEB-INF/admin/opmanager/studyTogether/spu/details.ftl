<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑SPU
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存SPU"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="spuId" name="spuId" value="${spuId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">

                        <h4>基本信息</h4><hr style="border:0; background-color: black; height:1px;">

                        <#-- SPU_ID-->
                        <#if spuId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU_ID</label>
                            <div class="controls">
                                <input type="text" id="spuId" name="spuId" class="form-control" value="${spuId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 主题ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="seriesId" placeholder="整型填写" name="seriesId" class="form-control js-postData" value="${content.seriesId!''}" style="width: 336px"/>
                                <span id="seriesName"></span>
                            </div>
                        </div>

                        <#-- SPU名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="${content.name!''}" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- SPU副标题 故宫用 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU副标题(故宫用) </label>
                            <div class="controls">
                                <input type="text" id="subTitle" name="subTitle" class="form-control js-postData" value="${content.subtitle!''}" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- SPU类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" class="form-control js-postData">
                                    <option value="0" <#if content?? && content.type?? && content.type == 0>selected</#if>>普通</option>
                                    <option value="1" <#if content?? && content.type?? && content.type == 1>selected</#if>>线下推广</option>
                                </select>
                            </div>
                        </div>

                        <#-- 适合年级 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">适合年级 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="min" name="min" placeholder="最小年级，正整数" class="form-control js-postData" value="${min!''}" style="width: 155px;"/>--
                                <input type="number" id="max" name="max" placeholder="最大年级，正整数" class="form-control js-postData" value="${max!''}" style="width: 155px;"/>
                                <span style="color: red">最低年级 <= 最高年级，相等表示只有一个年级，范围[1,6]</span>
                            </div>
                        </div>


                        <#-- 学习天数 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="days" name="days" placeholder="整数填写" class="form-control js-postData" value="${content.days!''}" style="width: 167px;"/>天
                            </div>
                        </div>

                        <#-- 课程图标 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程卡片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="icon" name="icon" class="form-control js-postData input" value="${content.icon!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                    <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.icon!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 课程描述 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="desc" name="desc" class="form-control js-postData" placeholder="含拼音时,括号内展示在右面：课（kè）程（chéng）,暂无字数限制" type="text" value="<#if content??>${content.desc!''}</#if>" style="width: 450px;"/>
                            </div>
                        </div>

                        <#-- 课程头图 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程头图(故宫用) </label>
                            <div class="controls">
                                <input type="text" id="headIcon" name="headIcon" class="form-control js-postData input" value="${content.headIcon!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                    <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.headIcon!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <#-- 课程大图 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程大图 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="courseBigPic" name="courseBigPic" class="form-control js-postData input" value="${content.courseBigPic!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                    <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.courseBigPic!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <#-- 课程小图 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程小图 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="courseLittlePic" name="courseLittlePic" class="form-control js-postData input" value="${content.courseLittlePic!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                    <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.courseLittlePic!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 配置环境 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 180px;" class="js-postData">
                                    <option value="">--请选择配置环境--</option>
                                    <#if levels??>
                                        <#list levels as lels>
                                            <option <#if content?? && content.envLevel??><#if content.envLevel == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == 10>单元测试环境
                                                <#elseif lels?? && lels == 20>开发环境
                                                <#elseif lels?? && lels == 30>测试环境
                                                <#elseif lels?? && lels == 40>预发布环境
                                                <#elseif lels?? && lels == 50>生产环境
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 是否套课 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否套课 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="isLessonSet" name="isLessonSet" class="form-control js-postData">
                                    <option value="false" <#if content?? && content.isLessonSet?? && content.isLessonSet?string("true","false") == 'false'>selected</#if>>否</option>
                                    <option value="true" <#if content?? && content.isLessonSet?? && content.isLessonSet?string("true","false") == 'true'>selected</#if>>是</option>
                                </select>
                            </div>
                        </div>
                        <h4>其他规则设置</h4><hr style="border:0; background-color: black; height:1px;">

                        <#-- 课程能力细分描述 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程能力细分描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="tags" name="tags" class="form-control js-postData" placeholder="不同能力之间使用英文逗号隔开,示例: 听,说" type="text"
                                    value="<#assign index = 0><#if content?? && content.tags?? && content.tags?size gt 0><#list content.tags as tag><#if index != 0>,</#if>${tag}<#assign index = index + 1></#list></#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 是否有周复习课 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否有周复习课 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="hasReview" name="hasReview" class="form-control js-postData">
                                    <option value="false" <#if content?? && content.hasReview?? && content.hasReview?string("true","false") == 'false'>selected</#if>>否</option>
                                    <option value="true" <#if content?? && content.hasReview?? && content.hasReview?string("true","false") == 'true'>selected</#if>>是</option>
                                </select>
                            </div>
                        </div>

                        <#-- 知识点数量 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">知识点数量 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="knowledgeCount" name="knowledgeCount" class="form-control js-postData" value="${content.knowledgeCount!''}" style="width: 167px;"/>个
                            </div>
                        </div>

                        <#-- 体验课ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">体验课ID </label>
                            <div class="controls">
                                <input type="text" id="experienceLessonId" name="experienceLessonId" class="form-control js-postData" value="${content.experienceLessonId!''}" style="width: 167px;"/>
                            </div>
                        </div>

                        <#-- 动态匹配设置 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">动态匹配设置 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="dynamicAdapt" name="dynamicAdapt" class="form-control js-postData">
                                    <option value="true" <#if content?? && content.dynamicAdapt?? && content.dynamicAdapt?string("true","false") == 'true'>selected</#if>>是</option>
                                    <option value="false" <#if content?? && content.dynamicAdapt?? && content.dynamicAdapt?string("true","false") == 'false'>selected</#if>>否</option>
                                </select>
                            </div>
                        </div>

                        <#-- SPU奖励顺序 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU奖励顺序(故宫用) </label>
                            <div class="controls">
                                <input type="number" id="seq" name="seq" class="form-control js-postData" value="${content.seq!''}" style="width: 167px;"/>
                                <span style="color: red">初期仅用于故宫神兽顺序配置，1个季节内，节气顺序为偶数时配置</span>
                            </div>
                        </div>

                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" style="width: 336px;" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {

        $("#seriesId").blur(function () {
            var seriesId = $("#seriesId").val();
            if (seriesId) {
                $.get("/opmanager/studytogether/common/series_name.vpage", {seriesId: seriesId}, function (data) {
                    if (data.success) {
                        $("#seriesName").html(data.seriesName);
                    } else {
                        alert(seriesId + "对应的系列不存在");
                        $("#seriesId").val("");
                        $("#seriesName").html("");
                        return;
                    }
                });
            } else {
                $("#seriesId").val("");
                $("#seriesName").html("");
                return;
            }
        });

        $(".upload_file").change(function () {
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                var acceptSuffix = new String($this.attr("data-suffix")).split("#");
                if (acceptSuffix.indexOf(suffix) === -1) {
                    alert("仅支持以下文件格式" + acceptSuffix);
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                $.ajax({
                    url: '/opmanager/studytogether/common/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value", data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href", data.fileUrl);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });

        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#seriesId').val() == ''){
                msg += "系列ID为空！\n";
            }
            if($('#name').val() == ''){
                msg += "SPU名称为空！\n";
            }
            if($('#type').val() == '') {
                msg += "请选择SPU类型！\n";
            }
            var min = $('#min').val();
            var max = $('#max').val();
            if(min === '' || max === '') {
                msg += "请填写适合年级！\n";
            }
            if(min <=0 || min > 6 ||max > 6 || max <=0) {
                msg += "年级区间必须是[1-6]！\n";
            }
            var dayNum =$('#days').val();
            if(dayNum === '' || dayNum <= 0 || !dayNum.match(num_reg)){
                msg += "请填写正确的学习天数！\n";
            }
            if($('#icon').val() == '') {
                msg += "课程图标为空！\n";
            }
            if($('#desc').val() == '') {
                msg += "课程描述为空！\n";
            }
            if($('#envLevel').val() == ''){
                msg += "请选择配置环境！\n";
            }
            if($('#tags').val() == ''){
                msg += "课程能力描述为空！\n";
            }
            if($('#courseBigPic').val() == ''){
                msg += "课程大图为空！\n";
            }
            if($('#courseLittlePic').val() == ''){
                msg += "课程小图为空！\n";
            }
            if($('#hasReview').val() == ''){
                msg += "周复习未选择！\n";
            }
            if($('#knowledgeCount').val() == ''){
                msg += "请填写知识点数量！\n";
            }
            if($('#dynamicAdapt').val() == ''){
                msg += "动态匹配设置未选择！\n";
            }
            if($('#seq').val() != '' && ($('#seq').val() <0 || !$('#seq').val().match(num_reg))) {
                msg += "SPU奖励顺序必须为非负整数，重新填写！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
                var post = {};
                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });
                $.post('save.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'index.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        });

    });
</script>
</@layout_default.page>

