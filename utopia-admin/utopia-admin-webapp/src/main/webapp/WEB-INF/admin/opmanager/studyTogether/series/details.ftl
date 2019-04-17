<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">系列管理/</span>添加|编辑系列
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存系列"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="seriesForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="seriesId" name="seriesId" value="${seriesId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">

                        <#-- 系列ID -->
                        <#if seriesId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID</label>
                            <div class="controls">
                                <input type="text" id="seriesId" name="seriesId" class="form-control" value="${seriesId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 系列名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="<#if content??>${content.name!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 学科名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学科名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="subject" name="subject" class="form-control js-postData">
                                    <option value="">--请选择学科名称--</option>
                                    <option value="CHINESE" <#if content?? && content.subject?? && content.subject == 'CHINESE'>selected</#if>>语文</option>
                                    <option value="ENGLISH" <#if content?? && content.subject?? && content.subject == 'ENGLISH'>selected</#if>>英语</option>
                                    <option value="MATH" <#if content?? && content.subject?? && content.subject == 'MATH'>selected</#if>>数学</option>
                                </select>
                            </div>
                        </div>

                        <#-- 课程结构类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程结构类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="courseType" name="courseType" class="form-control js-postData">
                                    <option id="ct_tp_id" value="">--请选择课程结构类型--</option>
                                    <option id="ch_gw_id" value="1" <#if content?? && content.courseType?? && content.courseType == 1>selected</#if>>语文古文</option>
                                    <option id="en_hb_id" value="2" <#if content?? && content.courseType?? && content.courseType == 2>selected</#if>>英语绘本</option>
                                    <option id="ch_yd_id" value="3" <#if content?? && content.courseType?? && content.courseType == 3>selected</#if>>语文阅读</option>
                                    <#--<option id="ch_hb_id" value="4" <#if content?? && content.courseType?? && content.courseType == 4>selected</#if>>语文绘本</option>-->
                                    <option id="mt_bc_id" value="5" <#if content?? && content.courseType?? && content.courseType == 5>selected</#if>>数学编程</option>
                                    <option id="ch_gs_id" value="6" <#if content?? && content.courseType?? && content.courseType == 6>selected</#if>>语文故事</option>
                                    <option id="co_id" value="7" <#if content?? && content.courseType?? && content.courseType == 7>selected</#if>>组件化课程</option>
                                </select>
                            </div>
                        </div>

                        <#-- 系列类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="seriesType" name="seriesType" class="form-control js-postData">
                                    <option value="">--请选择系列类型--</option>
                                    <option id="gushi_id" value="1" <#if content?? && content.seriesType?? && content.seriesType == 1>selected</#if>>古诗</option>
                                    <option id="huben_id" value="2" <#if content?? && content.seriesType?? && content.seriesType == 2>selected</#if>>英语绘本</option>
                                    <option id="lunyu_id" value="3" <#if content?? && content.seriesType?? && content.seriesType == 3>selected</#if>>论语</option>
                                    <option id="shiji_id" value="4" <#if content?? && content.seriesType?? && content.seriesType == 4>selected</#if>>史记</option>
                                    <option id="chengyu_id" value="5" <#if content?? && content.seriesType?? && content.seriesType == 5>selected</#if>>成语故事</option>
                                    <option id="zhuzi_id" value="6" <#if content?? && content.seriesType?? && content.seriesType == 6>selected</#if>>诸子百家</option>
                                    <option id="chuantong_id" value="7" <#if content?? && content.seriesType?? && content.seriesType == 7>selected</#if>>传统节日</option>
                                    <option id="xifang_id" value="8" <#if content?? && content.seriesType?? && content.seriesType == 8>selected</#if>>西方节日</option>
                                    <option id="yuwenhuiben_id" value="9" <#if content?? && content.seriesType?? && content.seriesType == 9>selected</#if>>语文绘本</option>
                                    <option id="yuwengyuedu_id" value="10" <#if content?? && content.seriesType?? && content.seriesType == 10>selected</#if>>语文阅读</option>
                                    <option id="liuji_id" value="11" <#if content?? && content.seriesType?? && content.seriesType == 11>selected</#if>>三十六计</option>
                                    <option id="shenhua_id" value="12" <#if content?? && content.seriesType?? && content.seriesType == 12>selected</#if>>中国神话</option>
                                    <option id="shuihu_id" value="13" <#if content?? && content.seriesType?? && content.seriesType == 13>selected</#if>>水浒传</option>
                                    <option id="biancheng_id" value="14" <#if content?? && content.seriesType?? && content.seriesType == 14>selected</#if>>数学编程</option>
                                    <option id="quwei_id" value="15" <#if content?? && content.seriesType?? && content.seriesType == 15>selected</#if>>趣味故事</option>
                                    <option id="math_you_id" value="16" <#if content?? && content.seriesType?? && content.seriesType == 16>selected</#if>>数学优等生</option>
                                </select>
                            </div>
                        </div>

                        <#-- 课程分类标签 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程分类标签 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="tag" name="tag" class="form-control js-postData">
                                    <option value="">--请选择课程分类标签--</option>
                                    <option value="1" <#if content?? && content.tag?? && content.tag == 1>selected</#if>>国学素养</option>
                                    <option value="2" <#if content?? && content.tag?? && content.tag == 2>selected</#if>>英语绘本</option>
                                    <option value="3" <#if content?? && content.tag?? && content.tag == 3>selected</#if>>编程数学</option>
                                    <option value="4" <#if content?? && content.tag?? && content.tag == 4>selected</#if>>故事飞船</option>
                                    <option value="5" <#if content?? && content.tag?? && content.tag == 5>selected</#if>>探秘故宫</option>
                                    <option value="6" <#if content?? && content.tag?? && content.tag == 6>selected</#if>>数学思维</option>
                                </select>
                            </div>
                        </div>

                        <#-- 系列难度级别 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列难度级别 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="level" name="level" placeholder="整形数字：1,2,3..." class="form-control js-postData" value="${content.level!''}" style="width: 336px" autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 学习数量单位 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习数量单位 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="symbol" name="symbol" placeholder="纯文本:个,则,首等" class="form-control js-postData" value="${content.symbol!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 系列类型描述 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列类型描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="describe" name="describe" class="form-control js-postData" value="${content.describe!''}" style="width: 336px"/>
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

                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" style="width: 336px" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>

<script type="text/javascript">
    $(function () {

        var value1 = $("#subject").find("option:selected").val();
        if (value1 === 'CHINESE') {
            $("#en_hb_id").hide();
            $("#mt_bc_id").hide();
            $("#ch_gw_id").show();
            $("#ch_yd_id").show();
            $("#ch_hb_id").show();
            $("#ch_gs_id").show();
            $("#co_id").show();
        } else if (value1 === 'ENGLISH'){
            $("#en_hb_id").show();
            $("#mt_bc_id").hide();
            $("#ch_gw_id").hide();
            $("#ch_yd_id").hide();
            $("#ch_hb_id").hide();
            $("#ch_gs_id").hide();
            $("#co_id").show();
        } else if (value1 === 'MATH') {
            $("#mt_bc_id").show();
            $("#en_hb_id").hide();
            $("#ch_gw_id").hide();
            $("#ch_yd_id").hide();
            $("#ch_hb_id").hide();
            $("#ch_gs_id").hide();
            $("#co_id").show();
        }

        var value2 = $("#courseType").find("option:selected").val();
        if (value2 === '1') {
            $("#gushi_id").show();
            $("#lunyu_id").show();
            $("#shiji_id").show();
            $("#zhuzi_id").show();
            $("#chengyu_id").show();
            $("#chuantong_id").show();
            $("#liuji_id").show();
            $("#shenhua_id").show();
            $("#shuihu_id").hide();
            $("#huben_id").hide();
            $("#xifang_id").hide();
            $("#yuwenhuiben_id").hide();
            $("#yuwengyuedu_id").hide();
            $("#biancheng_id").hide();
            $("#quwei_id").hide();
            $("#math_you_id").hide();
        } else if (value2 === '2'){
            $("#gushi_id").hide();
            $("#lunyu_id").hide();
            $("#shiji_id").hide();
            $("#zhuzi_id").hide();
            $("#chengyu_id").hide();
            $("#chuantong_id").hide();
            $("#liuji_id").hide();
            $("#shenhua_id").hide();
            $("#shuihu_id").hide();
            $("#huben_id").show();
            $("#xifang_id").show();
            $("#yuwenhuiben_id").hide();
            $("#yuwengyuedu_id").hide();
            $("#biancheng_id").hide();
            $("#quwei_id").hide();
            $("#math_you_id").hide();
        } else if (value2 === '3' || value2 == '4'){
            $("#gushi_id").hide();
            $("#lunyu_id").hide();
            $("#shiji_id").hide();
            $("#zhuzi_id").hide();
            $("#chengyu_id").hide();
            $("#chuantong_id").hide();
            $("#liuji_id").hide();
            $("#shenhua_id").hide();
            $("#huben_id").hide();
            $("#xifang_id").hide();
            $("#quwei_id").hide();
            $("#shuihu_id").show();
            $("#yuwenhuiben_id").show();
            $("#yuwengyuedu_id").show();
            $("#biancheng_id").hide();
            $("#math_you_id").hide();
        } else if (value2 === '5'){
            $("#gushi_id").hide();
            $("#lunyu_id").hide();
            $("#shiji_id").hide();
            $("#zhuzi_id").hide();
            $("#chengyu_id").hide();
            $("#chuantong_id").hide();
            $("#liuji_id").hide();
            $("#shenhua_id").hide();
            $("#shuihu_id").hide();
            $("#huben_id").hide();
            $("#xifang_id").hide();
            $("#yuwenhuiben_id").hide();
            $("#yuwengyuedu_id").hide();
            $("#quwei_id").hide();
            $("#biancheng_id").show();
            $("#math_you_id").hide();
        } else if (value2 === '6'){
            $("#gushi_id").hide();
            $("#lunyu_id").hide();
            $("#shiji_id").hide();
            $("#zhuzi_id").hide();
            $("#chengyu_id").hide();
            $("#chuantong_id").hide();
            $("#liuji_id").hide();
            $("#shenhua_id").hide();
            $("#shuihu_id").hide();
            $("#huben_id").hide();
            $("#xifang_id").hide();
            $("#yuwenhuiben_id").hide();
            $("#yuwengyuedu_id").hide();
            $("#biancheng_id").hide();
            $("#quwei_id").show();
            $("#math_you_id").hide();
        } else if(value2 === '7'){
            if(value1 === 'CHINESE'){
                $("#gushi_id").show();
                $("#lunyu_id").show();
                $("#shiji_id").show();
                $("#zhuzi_id").show();
                $("#chengyu_id").show();
                $("#chuantong_id").show();
                $("#liuji_id").show();
                $("#shenhua_id").show();
                $("#shuihu_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#yuwenhuiben_id").show();
                $("#yuwengyuedu_id").show();
                $("#biancheng_id").hide();
                $("#quwei_id").hide();
                $("#math_you_id").hide();
            }
            if(value1 === 'ENGLISH'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#shuihu_id").hide();
                $("#huben_id").show();
                $("#xifang_id").show();
                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#biancheng_id").hide();
                $("#quwei_id").hide();
                $("#math_you_id").hide();
            }
            if(value1 === 'MATH'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#shuihu_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#quwei_id").hide();
                $("#biancheng_id").show();
                $("#math_you_id").show();
            }
        }

        $("#subject").change(function () {
            var value = $("#subject").find("option:selected").val();
            if (value === 'CHINESE') {
                $("#en_hb_id").hide();
                $("#mt_bc_id").hide();
                $("#ch_gw_id").show();
                $("#ch_yd_id").show();
                $("#ch_hb_id").show();
                $("#ch_gs_id").show();
                $("#co_id").show();
                $("#courseType").val('');
            } else if (value === 'ENGLISH'){
                $("#en_hb_id").show();
                $("#mt_bc_id").hide();
                $("#ch_gw_id").hide();
                $("#ch_yd_id").hide();
                $("#ch_hb_id").hide();
                $("#ch_gs_id").hide();
                $("#co_id").show();
                $("#courseType").val('');
            } else if (value === 'MATH') {
                $("#mt_bc_id").show();
                $("#en_hb_id").hide();
                $("#ch_gw_id").hide();
                $("#ch_yd_id").hide();
                $("#ch_hb_id").hide();
                $("#ch_gs_id").hide();
                $("#co_id").show();
                $("#courseType").val('');
            }
        });

        $("#courseType").change(function () {
            var value = $("#courseType").find("option:selected").val();
            var value1 = $("#subject").find("option:selected").val();
            if (value === '1') {
                $("#gushi_id").show();
                $("#lunyu_id").show();
                $("#shiji_id").show();
                $("#zhuzi_id").show();
                $("#chengyu_id").show();
                $("#chuantong_id").show();
                $("#liuji_id").show();
                $("#shenhua_id").show();

                $("#shuihu_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#biancheng_id").hide();
                $("#quwei_id").hide();
                $("#math_you_id").hide();
                $("#seriesType").val('');
            } else if (value === '2'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#shuihu_id").hide();

                $("#huben_id").show();
                $("#xifang_id").show();

                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#biancheng_id").hide();
                $("#quwei_id").hide();
                $("#math_you_id").hide();
                $("#seriesType").val('');
            } else if (value === '3'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#quwei_id").hide();
                $("#shuihu_id").show();
                $("#yuwenhuiben_id").show();
                $("#yuwengyuedu_id").show();
                $("#biancheng_id").hide();
                $("#math_you_id").hide();
                $("#seriesType").val('');
            } else if (value === '5'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#shuihu_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#quwei_id").hide();
                $("#biancheng_id").show();
                $("#math_you_id").hide();
                $("#seriesType").val('');
            } else if (value === '6'){
                $("#gushi_id").hide();
                $("#lunyu_id").hide();
                $("#shiji_id").hide();
                $("#zhuzi_id").hide();
                $("#chengyu_id").hide();
                $("#chuantong_id").hide();
                $("#liuji_id").hide();
                $("#shenhua_id").hide();
                $("#shuihu_id").hide();
                $("#huben_id").hide();
                $("#xifang_id").hide();
                $("#yuwenhuiben_id").hide();
                $("#yuwengyuedu_id").hide();
                $("#biancheng_id").hide();
                $("#quwei_id").show();
                $("#math_you_id").hide();
                $("#seriesType").val('');
            }else if(value === '7'){
                if(value1 === 'CHINESE'){
                    $("#gushi_id").show();
                    $("#lunyu_id").show();
                    $("#shiji_id").show();
                    $("#zhuzi_id").show();
                    $("#chengyu_id").show();
                    $("#chuantong_id").show();
                    $("#liuji_id").show();
                    $("#shenhua_id").show();
                    $("#shuihu_id").hide();
                    $("#huben_id").hide();
                    $("#xifang_id").hide();
                    $("#yuwenhuiben_id").show();
                    $("#yuwengyuedu_id").show();
                    $("#biancheng_id").hide();
                    $("#quwei_id").hide();
                    $("#math_you_id").hide();
                }
                if(value1 === 'ENGLISH'){
                    $("#gushi_id").hide();
                    $("#lunyu_id").hide();
                    $("#shiji_id").hide();
                    $("#zhuzi_id").hide();
                    $("#chengyu_id").hide();
                    $("#chuantong_id").hide();
                    $("#liuji_id").hide();
                    $("#shenhua_id").hide();
                    $("#shuihu_id").hide();
                    $("#huben_id").show();
                    $("#xifang_id").show();
                    $("#yuwenhuiben_id").hide();
                    $("#yuwengyuedu_id").hide();
                    $("#biancheng_id").hide();
                    $("#quwei_id").hide();
                    $("#math_you_id").hide();
                }
                if(value1 === 'MATH'){
                    $("#gushi_id").hide();
                    $("#lunyu_id").hide();
                    $("#shiji_id").hide();
                    $("#zhuzi_id").hide();
                    $("#chengyu_id").hide();
                    $("#chuantong_id").hide();
                    $("#liuji_id").hide();
                    $("#shenhua_id").hide();
                    $("#shuihu_id").hide();
                    $("#huben_id").hide();
                    $("#xifang_id").hide();
                    $("#yuwenhuiben_id").hide();
                    $("#yuwengyuedu_id").hide();
                    $("#quwei_id").hide();
                    $("#biancheng_id").show();
                    $("#math_you_id").show();
                }
            }
        });

        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#name').val() === ''){
                msg += "系列名称为空！\n";
            }
            if($('#subject').val() === ''){
                msg += "学科名称为空！\n";
            }
            if($('#courseType').val() === ''){
                msg += "课程结构类型为空！\n";
            }
            if($('#seriesType').val() === ''){
                msg += "系列类型为空！\n";
            }
            var level = $('#level').val();
            if(level === ''){
                msg += "系列难度级别为空！\n";
            }
            if(level <= 0 || !level.match(num_reg)){
                msg += "系列难度级别必须是正整数！\n";
            }
            if($('#symbol').val() === ''){
                msg += "学习数量单位为空！\n";
            }
            if($('#describe').val() === ''){
                msg += "系列类型描述为空！\n";
            }
            if($('#envLevel').val() === ''){
                msg += "配置环境为空！\n";
            }
            if($('#tag').val() === ''){
                msg += "课程分类标签未选择！\n";
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

