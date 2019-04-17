<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>

<style>
    input.toCtn {width: 60px;}
    input.fromCtn {width: 60px;}
</style>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        SKU详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <h4>基本信息</h4><hr style="border:0; background-color: black; height:1px;">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SKU_ID</label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU_ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="spuId" name="spuId" class="form-control js-postData" value="${content.spuId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程期数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="phase" name="phase" class="form-control js-postData" placeholder="整数" value="${content.phase!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开始时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" class="form-control js-postData" value="${content.openDate!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">结束时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="closeDate" name="closeDate" class="form-control js-postData" value="${content.closeDate!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">原价 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" step="0.01" id="price" name="price" class="form-control js-postData" value="${content.price!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">优惠价 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" step="0.01" id="discountPrice" name="discountPrice" class="form-control js-postData" value="${content.discountPrice!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">库存 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="personLimited" name="personLimited" class="form-control js-postData" value="${content.personLimited!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">产品ID </label>
                            <div class="controls">
                                <input type="text" id="productId" name="productId" class="form-control js-postData" value="${content.productId!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">卡片页是否展示 <span style="color: red">*</span></label>
                            <input id="cardDisplay" name="cardDisplay" value="<#if content ?? && content.cardDisplay?? && content.cardDisplay == false>1<#else>2</#if>" class="form-control js-postData" style="display: none;">
                            <div class="controls">
                                <input id="card_one" type="radio" value="1" <#if content ?? && content.cardDisplay?? && content.cardDisplay == false>checked="checked"</#if> disabled>否&nbsp;&nbsp;&nbsp;
                                <input id="card_tow" type="radio" value="2" <#if content ?? && content.cardDisplay?? && content.cardDisplay == true>checked="checked"</#if> disabled>是
                                </label>
                            </div>
                        </div>
                        <#-- 是否是组件化课程 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否是组件化课程 <span style="color: red">*</span></label>
                            <input id="isComponentSku" name="isComponentSku"
                                   value="<#if content ?? && (!content.isComponentSku?? || content.isComponentSku == false)>1<#elseif content ?? && content.isComponentSku?? && content.isComponentSku == true>2</#if>"
                                   class="form-control js-postData" style="display: none;">
                            <div class="controls">
                                <input id="isComponentSku_true" name="isComponentSkuRadio" type="radio" value=1
                                       <#if content ?? && (!content.isComponentSku?? || content.isComponentSku == false)>checked="checked" onchange="changeIsComponent()" </#if> disabled>否&nbsp;&nbsp;&nbsp;
                                <input id="isComponentSku_false" name="isComponentSkuRadio" type="radio" value=2
                                       <#if content ?? && content.isComponentSku?? && content.isComponentSku == true>checked="checked" onchange="changeIsComponent()" </#if> disabled>是
                                </label>
                            </div>
                        </div>
                        <#-- 组件化课程皮肤类型 -->
                        <div class="control-group" id="face-type">
                            <label class="col-sm-2 control-label">皮肤类型</label>
                            <div class="controls">
                                <select id="NewTemplateType" name="newTemplateType" style="width: 350px;"
                                        class="js-postData" disabled>
                                    <option value="">--请选择获取方式--</option>
                                    <#if StudyCourseMapFaceTypeList??>
                                        <#list StudyCourseMapFaceTypeList as type>
                                            <option <#if content?? && content.templateType??><#if content.templateType == type> selected="selected"</#if></#if> value= ${type!}>
                                            <#if type?? && type == 'ChineseFace_1'>语文皮肤一(语文古文)
                                            <#elseif type?? && type == 'ChineseFace_2'>语文皮肤二(语文阅读)
                                            <#elseif type?? && type == 'English_1'>英语皮肤一(英语绘本)
                                            <#elseif type?? && type == 'Math_1'>数学皮肤一()
                                            </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">电子书获得方式</label>
                            <div class="controls">
                                <input type="text" id="ebookGetWay" name="ebookGetWay" class="form-control js-postData"
                                       value="<#if content?? && content.ebookGetWay?? && content.ebookGetWay == 0>无限制<#elseif content?? && content.ebookGetWay?? && content.ebookGetWay == 1>分享<#elseif content?? && content.ebookGetWay?? && content.ebookGetWay == 2>邀请</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">电子书ID </label>
                            <div class="controls">
                                <input type="text" id="ebookId" name="ebookId" class="form-control js-postData" value="${content.ebookId!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group" id="rank_list_id">
                            <label class="control-label">排行榜奖励说明 <span style="color: red">*</span></label>
                            <#if content?? && content.rankRewards?? && content.rankRewards?size gt 0>
                                <#list content.rankRewards as cts>
                                    <div class="controls singleItemBox" style="margin-top: 5px;">
                                        <span style="color: red">*</span>奖励名次<input type="text" name="vitality" value="${cts.from!''}" placeholder="第一个" class="input fromCtn" disabled>&nbsp;-&nbsp;
                                        <input type="text" name="vitality" value="${cts.to!''}" placeholder="第二个" class="input toCtn" disabled> <br>
                                        <span style="color: red">*</span>奖品名称<input type="text" name="vitality"
                                                                                    value="<#assign index = 0><#if cts.terms?size gt 0><#list cts.terms as term><#if index != 0>,</#if>${term}<#assign index = index + 1></#list></#if>" class="input termsCtn" disabled/>
                                    </div>
                                </#list>
                            <#else>
                                <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <input type="button" value="添加奖励名次及内容" class="btn btn-primary" id="add_content_id">
                                    <span style="color: red">奖励名次为整数，第一个 <= 第二个，相等即为一个数字</span>
                                </div>
                            </#if>
                            <div id="newAddSingleItem"></div>
                        </div>

                        <h4>课程报名激活信息</h4><hr style="border:0; background-color: black; height:1px;">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名开始时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="showDate" name="showDate" class="form-control js-postData" value="${content.showDate!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名结束时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="sighUpEndDate" name="sighUpEndDate" class="form-control js-postData" value="${content.sighUpEndDate!''}" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名方式 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookGetWay" name="ebookGetWay" class="form-control js-postData" value="<#if content?? && content.joinWay?? && content.joinWay == 0>免费报名<#elseif content?? && content.joinWay?? && content.joinWay == 3>拼团报名<#elseif content?? && content.joinWay?? && content.joinWay == 4>老用户续报优惠报名<#elseif content?? && content.joinWay?? && content.joinWay == 5>付费报名<#elseif content?? && content.joinWay?? && content.joinWay == 6>助力购买报名<#elseif content?? && content.joinWay?? && content.joinWay == 7>分享购买报名<#elseif content?? && content.joinWay?? && content.joinWay == 8>分享免费学<#elseif content?? && content.joinWay?? && content.joinWay == 9>分享8折购买<#elseif content?? && content.joinWay?? && content.joinWay == 10>5人助力免费学</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">激活方式 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="activeType" name="activeType" style="width: 350px;" class="js-postData" disabled>
                                    <option value="2" <#if content?? && content.activeType?? && content.activeType == 2>selected</#if>>链接</option>
                                    <option value="3" <#if content?? && content.activeType?? && content.activeType == 3>selected</#if>>公众号</option>
                                    <option value="4" <#if content?? && content.activeType?? && content.activeType == 4>selected</#if>>无需激活</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程激活微信号类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookGetWay" name="ebookGetWay" class="form-control js-postData"
                                       value="<#if content?? && content.qrcodeType?? && content.qrcodeType == 1>添加老师微信<#elseif content?? && content.qrcodeType?? && content.qrcodeType == 2>添加群微信<#else></#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">激活页头图 </label>
                            <div class="controls">
                                <input type="text" id="activePagePic" name="activePagePic" class="form-control js-postData input" value="${content.activePagePic!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.activePagePic!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SKU类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="skuType" name="skuType" style="width: 350px;" class="js-postData" disabled>
                                    <option value="0" <#if content?? && content.skuType?? && content.skuType == 0>selected</#if>>训练营</option>
                                    <option value="1" <#if content?? && content.skuType?? && content.skuType == 1>selected</#if>>伪轻课</option>
                                    <option value="2" <#if content?? && content.skuType?? && content.skuType == 2>selected</#if>>其他</option>
                                    <option value="3" <#if content?? && content.skuType?? && content.skuType == 3>selected</#if>>真轻课</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">轻课学习模式 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="skuType" name="skuType" style="width: 350px;" class="js-postData">
                                    <div class="controls">
                                        每天最多学习<select id="studyModel" name="studyModel" style="width: 200px;" class="js-postData">
                                                <option value="0" <#if content?? && content.studyModel?? && content.studyModel == 0>selected</#if>>0</option>
                                                <option value="1" <#if content?? && content.studyModel?? && content.studyModel == 1>selected</#if>>1</option>
                                                <option value="2" <#if content?? && content.studyModel?? && content.studyModel == 2>selected</#if>>2</option>
                                                <option value="3" <#if content?? && content.studyModel?? && content.studyModel == 3>selected</#if>>3</option>
                                                <option value="4" <#if content?? && content.studyModel?? && content.studyModel == 4>selected</#if>>4</option>
                                                <option value="5" <#if content?? && content.studyModel?? && content.studyModel == 5>selected</#if>>5</option>
                                                <option value="6" <#if content?? && content.studyModel?? && content.studyModel == 6>selected</#if>>6</option>
                                                <option value="7" <#if content?? && content.studyModel?? && content.studyModel == 7>selected</#if>>7</option>
                                                <option value="8" <#if content?? && content.studyModel?? && content.studyModel == 8>selected</#if>>8</option>
                                                <option value="9" <#if content?? && content.studyModel?? && content.studyModel == 9>selected</#if>>9</option>
                                                <option value="10" <#if content?? && content.studyModel?? && content.studyModel == 10>selected</#if>>10</option>
                                               </select>节课
                                    </div>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <label class="col-sm-2 control-label">是否发放连续学习奖励 <span style="color: red">*</span></label>
                                <select id="needReward" name="needReward" class="form-control js-postData">
                                    <option value="true" <#if content?? && content.needReward?? && content.needReward?string("true","false") == 'true'>selected</#if>>是</option>
                                    <option value="false" <#if content?? && content.needReward?? && content.needReward?string("true","false") == 'false'>selected</#if>>否</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 350px;" class="js-postData" disabled>
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
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;" readonly/>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享海报 </label>
                            <div class="controls">
                                <input type="text" id="activePagePic" name="activePagePic" class="form-control js-postData input" value="${content.activePagePic!''}" style="width: 336px" disabled/>
                                    <a class="btn btn-success preview" data-href="<#if content?? && content.shareContentConfig?? && cdn_host??>${cdn_host!''}${content.shareContentConfig.pictureUrl!''}</#if>">预览</a>
                            </div>
                        </div>


                    <#-- 分享链接-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享链接 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="link" name="link"  class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.link!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                    <#-- 分享标题 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享标题 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="title" name="title" class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.title!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                    <#--分享内容-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享内容 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="content" name="content"  class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.content!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px;" readonly/>
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
        var isComponentSku = false;
        <#if content ?? && content.isComponentSku??>
        isComponentSku =${content.isComponentSku?c};
        </#if>
        if (!isComponentSku) {
            $("#face-type").css("display", "none");
        }
        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });
    });
</script>
</@layout_default.page>

