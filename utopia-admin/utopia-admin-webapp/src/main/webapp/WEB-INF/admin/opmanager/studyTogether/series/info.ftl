<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">系列管理/</span>系列详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存系列"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="seriesForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="seriesId" name="seriesId" value="${seriesId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID</label>
                            <div class="controls">
                                <input type="text" id="seriesId" name="seriesId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="<#if content??>${content.name!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学科名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="subject" name="subject" disabled>
                                    <option value="CHINESE" <#if content?? && content.subject?? && content.subject == 'CHINESE'>selected</#if>>语文</option>
                                    <option value="ENGLISH" <#if content?? && content.subject?? && content.subject == 'ENGLISH'>selected</#if>>英语</option>
                                    <option value="MATH" <#if content?? && content.subject?? && content.subject == 'MATH'>selected</#if>>数学</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程结构类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="courseType" name="courseType" disabled>
                                    <option id="ch_gw_id" value="1" <#if content?? && content.courseType?? && content.courseType == 1>selected</#if>>语文古文</option>
                                    <option id="en_hb_id" value="2" <#if content?? && content.courseType?? && content.courseType == 2>selected</#if>>英语绘本</option>
                                    <option id="ch_yd_id" value="3" <#if content?? && content.courseType?? && content.courseType == 3>selected</#if>>语文阅读</option>
                                    <option id="mt_bc_id" value="5" <#if content?? && content.courseType?? && content.courseType == 5>selected</#if>>数学编程</option>
                                    <option id="ch_gs_id" value="6" <#if content?? && content.courseType?? && content.courseType == 6>selected</#if>>语文故事</option>
                                    <option id="co_id" value="7" <#if content?? && content.courseType?? && content.courseType == 7>selected</#if>>组件化课程</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="seriesType" name="seriesType" disabled>
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
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程分类标签 </label>
                            <div class="controls">
                                <select id="tag" name="tag" class="form-control js-postData" disabled>
                                    <option value="1" <#if content?? && content.tag?? && content.tag == 1>selected</#if>>国学素养</option>
                                    <option value="2" <#if content?? && content.tag?? && content.tag == 2>selected</#if>>英语绘本</option>
                                    <option value="3" <#if content?? && content.tag?? && content.tag == 3>selected</#if>>编程数学</option>
                                    <option value="4" <#if content?? && content.tag?? && content.tag == 4>selected</#if>>故事飞船</option>
                                    <option value="5" <#if content?? && content.tag?? && content.tag == 5>selected</#if>>探秘故宫</option>
                                    <option value="6" <#if content?? && content.tag?? && content.tag == 6>selected</#if>>数学思维</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列难度级别 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="level" name="level" class="form-control js-postData" value="${content.level!''}" disabled style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习数量单位 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="symbol" name="symbol" class="form-control js-postData" style="width:336px;" value="${content.symbol!''}" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列类型描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="describe" name="describe" class="form-control js-postData" value="${content.describe!''}" disabled style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 180px;" class="js-postData" disabled>
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
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px" disabled/>
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
</@layout_default.page>

