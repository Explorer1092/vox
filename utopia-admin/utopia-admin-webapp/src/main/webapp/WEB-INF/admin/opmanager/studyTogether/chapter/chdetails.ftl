<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑章节
        <a type="button" id="btn_cancel" href="chindex.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存章节"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="chapterId" name="chapterId" value="${chapterId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 章节ID-->
                        <#if chapterId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节ID</label>
                            <div class="controls">
                                <input type="text" id="chapterId" name="chapterId" class="form-control" value="${chapterId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" class="form-control js-postData" value="${content.skuId!''}" style="width: 336px"/>
                                <span id="skuName"></span>
                            </div>
                        </div>

                        <#-- 章节名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="chapterName" name="chapterName" class="form-control js-postData" value="<#if content??>${content.chapterName!''}</#if>" style="width: 336px;" maxlength="30"/>
                            </div>
                        </div>

                        <#-- 章节描述 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="chapterDesc" name="chapterDesc" class="form-control js-postData" type="text" value="<#if content??>${content.chapterDesc!''}</#if>" style="width: 336px;" maxlength="30"/>
                            </div>
                        </div>

                        <#-- 展示顺序 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">展示顺序 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="seq" name="seq" class="form-control js-postData" type="number" value="<#if content??>${content.seq!''}</#if>" style="width: 336px;" maxlength="30" placeholder="整数填写"/>
                            </div>
                        </div>

                        <#-- 开始时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开课时间 </label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" class="form-control js-postData" value="<#if content??>${content.openDate!''}</#if>" style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});" autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 单周奖励ID-->
                        <div class="control-group" id="singleItemMain">
                            <label class="control-label">单周奖励ID </label>
                            <#if content?? && content.singleRewardIds?? && content.singleRewardIds?size gt 0>
                                <input type="button" value="添加" class="btn btn-primary" id="addRowSingle"> <br/>
                                <#list content.singleRewardIds as cts>
                                    <div class="controls singleItemBox" style="margin-top: 5px;">
                                        奖励<input type="text" name="vitality" value="${cts!''}" id="${"single" + (cts_index + 1)}"  class="input contentCtn" onblur="checkWeeklyId(${cts_index + 1}, '#single')"/>
                                        <input type="button" value="删除" class="btn thisDelete" data-val="${cts_index + 1}">
                                        <span id="${"sname" + (cts_index + 1)}"></span><br/>
                                    </div>
                                </#list>
                            <#else>
                                <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <input type="button" value="添加" class="btn btn-primary" id="addRowSingle">
                                </div>
                            </#if>
                            <div id="newAddSingleItem"></div>
                        </div>

                        <#-- 双周奖励ID-->
                        <div class="control-group" id="doubleItemMain">
                            <label class="control-label">双周奖励ID </label>
                            <#if content?? && content.doubleRewardIds?? && content.doubleRewardIds?size gt 0>
                                <input type="button" value="添加" class="btn btn-primary" id="addRowDouble"> <br/>
                                <#list content.doubleRewardIds as cts>
                                    <div class="controls doubleItemBox" style="margin-top: 5px;">
                                        奖励<input type="text" name="vitality" value="${cts!''}" id="${"double" + (cts_index + 1)}" class="input contentCtn" onblur="checkWeeklyId(${cts_index + 1}, '#double')"/>
                                        <input type="button" value="删除" class="btn thisDelete" data-val="${cts_index + 1}">
                                        <span id="${"dname" + (cts_index + 1)}"></span><br/>
                                    </div>
                                </#list>
                            <#else>
                                <div class="controls doubleItemBox" style="margin-top: 5px;">
                                    <input type="button" value="添加" class="btn btn-primary" id="addRowDouble">
                                </div>
                            </#if>
                            <div id="newAddDoubleItem"></div>
                        </div>

                         <#-- 配置环境 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择配置环境--</option>
                                    <#if levels??>
                                        <#list levels as lels>
                                            <option <#if content?? && content.envLevel??><#if content.envLevel == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == 10>单元测试
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

                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
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

        //sku name 检测
        $("#skuId").blur(function () {
            var skuId = $("#skuId").val();
            if (skuId) {
                $.get("/opmanager/studytogether/common/sku_name.vpage", {skuId: skuId}, function (data) {
                    if (!data.success) {
                        alert(skuId + "对应的SKU不存在");
                        $("#skuId").val('');
                    }
                });
            }
        });

        var one = "'#single'";
        var tow = "'#double'";

        //添加单周奖励
        var singleItemBox = $("#singleItemMain").find(".contentCtn").length;
        $(document).on("click", "#addRowSingle", function () {
            var count = $("#singleItemMain").find(".contentCtn").length;
            singleItemBox = count + 1;
            var newAddList = $("#newAddSingleItem");
            var _html = '<div class="controls singleItemBox" style="margin-top: 5px;">' +
                    '奖励 <input type="text" name="vitality" id="single'+(singleItemBox)+'" value="" onblur="checkWeeklyId('+(singleItemBox)+','+one+')" class="input contentCtn">' +
                    '<input type="button" value="删除" class="btn thisDelete"  data-val="' + (singleItemBox++) + '">' +
                    '<span id="sname'+(singleItemBox - 1)+'"></span>' +
                    '</div>';
            newAddList.append(_html);
        });

        //删除单周奖励
        $(document).on("click", "#singleItemMain .thisDelete", function () {
            var $this = $(this);
            singleItemBox--;
            var count = $("#singleItemMain").find(".contentCtn").length;
            if( count === $this.data("val")) {
                singleIds.splice($this.data("val"), 1);
                $this.closest(".singleItemBox").remove();
            } else {
                alert("从尾行开始删除")
            };
        });

        //添加双周奖励
        var doubleItemBox = $("#doubleItemMain").find(".contentCtn").length;
        $(document).on("click", "#addRowDouble", function () {
            var count = $("#doubleItemMain").find(".contentCtn").length;
            doubleItemBox = count + 1;
            var newAddList = $("#newAddDoubleItem");
            var _html = '<div class="controls doubleItemBox" style="margin-top: 5px;">' +
                    '奖励 <input type="text" name="vitality" id="double'+(doubleItemBox)+'" value="" onblur="checkWeeklyId('+(doubleItemBox)+','+tow+')" class="input contentCtn">' +
                    '<input type="button" value="删除" class="btn thisDelete" data-val="' + (doubleItemBox++) + '">' +
                    '<span id="dname'+(doubleItemBox - 1)+'"></span>' +
                    '</div>';
            newAddList.append(_html);
        });

        //删除双周奖励
        $(document).on("click", "#doubleItemMain .thisDelete", function () {
            var $this = $(this);
            doubleItemBox--;
            var count = $("#doubleItemMain").find(".contentCtn").length;
            if( count === $this.data("val")) {
                doubleIds.splice($this.data("val"), 1);
                $this.closest(".doubleItemBox").remove();
            } else {
                alert("从尾行开始删除")
            };
        });

        //验证表单
        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#skuId').val() == ''){
                msg += "课程ID不能为空！\n";
            }
            if($('#chapterName').val() == ''){
                msg += "请填写章节名称！\n";
            }
            if($('#chapterDesc').val() == ''){
                msg += "请填写章节描述！\n";
            }
            if($('#seq').val() == '' || !$('#seq').val().match(num_reg)) {
                msg += "展示顺序为空或者不是纯数字！\n";
            }
            if($('#envLevel').val() == ''){
                msg += "请选择配置环境！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        //保存提交
        var singleIds = [];
        var doubleIds = [];
        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
                var post = {};
                var singleItemMain = $("#singleItemMain");
                var doubleItemMain = $("#doubleItemMain");

                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });

                singleItemMain.find(".singleItemBox").each(function () {
                    var $this = $(this);
                    if ($this.find(".contentCtn").val()) {
                        singleIds.push($this.find(".contentCtn").val());
                    }
                });
                doubleItemMain.find(".doubleItemBox").each(function () {
                    var $this = $(this);
                    if ($this.find(".contentCtn").val()) {
                        doubleIds.push($this.find(".contentCtn").val());
                    }
                });
                if (singleIds.length > 0) {
                    post.sids = JSON.stringify(singleIds);
                }
                if (doubleIds.length > 0) {
                    post.dids = JSON.stringify(doubleIds);
                }
                $.post('chsave.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'chindex.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        });

        $("#seq").blur(function () {
            var skuId = $("#skuId").val();
            if(skuId === '') {
                alert("请先填写课程ID");
                $("#seq").val('');
                return;
            }
            var seq = $("#seq").val();
            if (!seq || seq <= 0) {
                alert("顺序需要大于0");
                $("#seq").val('');
                return;
            }
            $.post(
                "check_seq.vpage",
                {
                    skuId: skuId,
                    seq: seq
                },
                function (data) {
                    if (!data.success) {
                        alert("对应的课程ID已经存在序号：" + seq + ",请查证后重新填写");
                        $("#seq").val('');
                        return;
                    }
                });
        });

    });
    
    function checkWeeklyId(index, str) {
        var idName = str + index;
        var wkId = $(idName).val();
        var spanName = '';

        if (wkId) {
            $.ajax({
                type: "POST",
                url: "/opmanager/studytogether/common/weeklyreward.vpage",
                data: {
                    weeklyRewardId: wkId
                },
                success: function (data) {
                    if (str === '#single') {
                        spanName = "#sname" + index;
                    } else {
                        spanName = "#dname" + index;
                    }
                    if (data.success) {
                        $(spanName).html(data.name);
                    } else {
                        alert(wkId + "对应的奖励不存在");
                        $(idName).val("");
                        $(spanName).html("");
                    }
                }
            });
        } else {
            $(idName).val("");
        }
    }
</script>
</@layout_default.page>

