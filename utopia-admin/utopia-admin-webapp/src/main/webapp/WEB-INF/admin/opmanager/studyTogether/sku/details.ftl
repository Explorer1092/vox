<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>

<style>
    input.toCtn {
        width: 60px;
    }

    input.fromCtn {
        width: 60px;
    }
</style>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑SKU
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存SKU"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">

                        <h4>基本信息</h4>
                        <hr style="border:0; background-color: black; height:1px;">

                        <#-- SKU_ID-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SKU_ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" <#if content?? && content.id??>disabled</#if>
                                       class="form-control js-postData" value="${content.id!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- SPU_ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU_ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="spuId" name="spuId" class="form-control js-postData"
                                       value="${content.spuId!''}" style="width: 336px"/>
                                <span id="spuName"></span>
                            </div>
                        </div>

                        <#-- 课程期数 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程期数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="phase" name="phase" class="form-control js-postData"
                                       placeholder="正整数" value="${content.phase!''}" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 开始时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开始时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" placeholder="开始时间"
                                       class="form-control js-postData" value="${content.openDate!''}"
                                       style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});"
                                       autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 结束时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">结束时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="closeDate" name="closeDate" placeholder="结束时间"
                                       class="form-control js-postData" value="${content.closeDate!''}"
                                       style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});"
                                       autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 原价 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">原价 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" step="0.01" id="price" name="price" placeholder="浮点数，保留两位"
                                       class="form-control js-postData" value="${content.price!''}"
                                       style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 优惠价 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">优惠价 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" step="0.01" id="discountPrice" name="discountPrice"
                                       placeholder="浮点数，保留两位" class="form-control js-postData"
                                       value="${content.discountPrice!''}" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 库存 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">库存 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="personLimited" name="personLimited" placeholder="整数"
                                       class="form-control js-postData" value="${content.personLimited!''}"
                                       style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 产品ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">产品ID </label>
                            <div class="controls">
                                <input type="text" id="productId" name="productId" class="form-control js-postData"
                                       value="${content.productId!''}" style="width: 336px;"/>
                                <span id="productName"></span>
                            </div>
                        </div>

                        <#-- 卡片页是否展示 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">卡片页是否展示 <span style="color: red">*</span></label>
                            <input id="cardDisplay" name="cardDisplay"
                                   value="<#if content ?? && content.cardDisplay?? && content.cardDisplay == false>1<#elseif content ?? && content.cardDisplay?? && content.cardDisplay == true>2</#if>"
                                   class="form-control js-postData" style="display: none;">
                            <div class="controls">
                                <input id="card_one" type="radio" value="1"
                                       <#if content ?? && content.cardDisplay?? && content.cardDisplay == false>checked="checked"</#if>>否&nbsp;&nbsp;&nbsp;
                                <input id="card_tow" type="radio" value="2"
                                       <#if content ?? && content.cardDisplay?? && content.cardDisplay == true>checked="checked"</#if>>是
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
                                       <#if content ?? && (!content.isComponentSku?? || content.isComponentSku == false)>checked="checked"
                                       onchange="changeIsComponent()" </#if>>否&nbsp;&nbsp;&nbsp;
                                <input id="isComponentSku_false" name="isComponentSkuRadio" type="radio" value=2
                                       <#if content ?? && content.isComponentSku?? && content.isComponentSku == true>checked="checked"
                                       onchange="changeIsComponent()" </#if>>是
                                </label>
                            </div>
                        </div>
                        <#-- 组件化课程皮肤类型 -->
                        <div class="control-group" id="face-type">
                            <label class="col-sm-2 control-label">皮肤类型</label>
                            <div class="controls">
                                <select id="NewTemplateType" name="newTemplateType" style="width: 350px;"
                                        class="js-postData">
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
                            <div class="controls">
                                <a class="btn btn-primary" id="face-preview">皮肤预览</a>
                            </div>
                        </div>

                        <#-- 电子书获得方式 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">电子书获得方式</label>
                            <div class="controls">
                                <select id="ebookGetWay" name="ebookGetWay" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择获取方式--</option>
                                    <#if ebooks??>
                                        <#list ebooks as lels>
                                            <option <#if content?? && content.ebookGetWay??><#if content.ebookGetWay == lels> selected="selected"</#if></#if> value= ${lels!}>
                                            <#if lels?? && lels == 0>无限制
                                            <#elseif lels?? && lels == 1>分享
                                            <#elseif lels?? && lels == 2>邀请
                                            </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 电子书ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">电子书ID </label>
                            <div class="controls">
                                <input type="text" id="ebookId" name="ebookId" class="form-control js-postData"
                                       value="${content.ebookId!''}" style="width: 336px;"/>
                                <span id="ebookName"></span>
                            </div>
                        </div>

                        <#-- 排行榜奖励说明-->
                        <div class="control-group" id="rank_list_id">
                            <label class="control-label">排行榜奖励说明 <span style="color: red">*</span></label>
                            <#if content?? && content.rankRewards?? && content.rankRewards?size gt 0>
                                <input type="button" value="添加奖励名次及内容" class="btn btn-primary" id="add_content_id">
                                <span style="color: red">奖励名次为整数，第一个数字 <= 第二个，相等即为一个数字</span><br/>
                                <#list content.rankRewards as cts>
                                    <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <span style="color: red">*</span>奖励名次<input type="text" name="vitality" value="${cts.from!''}"
                                placeholder="第一个" class="input fromCtn">&nbsp;-&nbsp;
                                <input type="text" name="vitality" value="${cts.to!''}" placeholder="第二个"
                                       class="input toCtn"> <br>
                                <span style="color: red">*</span>奖品名称<textarea name="vitality"
                                                                               placeholder="奖品可以为多个，不同奖品之间使用英文逗号隔开"
                                                                               class="input itermCtn"
                                                                               style="width: 350px;"><#assign index = 0><#if cts.terms?size gt 0><#list cts.terms as term><#if index != 0>,</#if>${term}<#assign index = index + 1></#list></#if></textarea>
                                <input type="button" value="删除" class="btn thisDelete" data-val="${cts_index + 1}">
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

                        <h4>课程报名激活信息</h4>
                    <hr style="border:0; background-color: black; height:1px;">

                        <#-- 报名开始时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名开始时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="showDate" name="showDate" placeholder="报名开始时间"
                                       class="form-control js-postData" value="${content.showDate!''}"
                                       style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});"
                                       autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 报名结束时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名结束时间 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="sighUpEndDate" name="sighUpEndDate" placeholder="报名结束时间"
                                       class="form-control js-postData" value="${content.sighUpEndDate!''}"
                                       style="width: 336px;" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});"
                                       autocomplete="OFF"/>
                            </div>
                        </div>

                        <#-- 报名方式 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">报名方式 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="joinWay" name="joinWay" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择报名方式--</option>
                                    <#if ways??>
                                        <#list ways as lels>
                                            <option <#if content?? && content.joinWay??><#if content.joinWay == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == 0>免费报名
                                                <#elseif lels?? && lels == 3>拼团报名
                                                <#elseif lels?? && lels == 4>老用户续报优惠报名
                                                <#elseif lels?? && lels == 5>付费报名
                                                <#elseif lels?? && lels == 6>助力报名
                                                <#elseif lels?? && lels == 7>分享购买报名
                                                <#elseif lels?? && lels == 8>分享免费学
                                                <#elseif lels?? && lels == 9>分享8折购买
                                                <#elseif lels?? && lels == 10>5人助力免费学
                                                <#elseif lels?? && lels == 11>新拼团
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 拼团业务ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">业务ID</label>
                            <div class="controls">
                                <input id="grouponId" name="grouponId" class="form-control js-postData" type="text" value="<#if content??>${content.grouponId!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 激活方式 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">激活方式 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="activeType" name="activeType" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择激活方式--</option>
                                    <option value="2"
                                            <#if content?? && content.activeType?? && content.activeType == 2>selected</#if>>
                                        链接
                                    </option>
                                    <option value="3"
                                            <#if content?? && content.activeType?? && content.activeType == 3>selected</#if>>
                                        公众号
                                    </option>
                                    <option value="4"
                                            <#if content?? && content.activeType?? && content.activeType == 4>selected</#if>>
                                        直接激活
                                    </option>
                                </select>
                            </div>
                        </div>

                        <#-- 课程激活微信号类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程激活微信号类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="qrcodeType" name="qrcodeType" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择课程激活微信号类型--</option>
                                    <#if codes??>
                                        <#list codes as lels>
                                            <option <#if content?? && content.qrcodeType??><#if content.qrcodeType == lels> selected="selected"</#if></#if> value= ${lels!}>
                                            <#if lels?? && lels == 1>添加老师微信
                                            <#elseif lels?? && lels == 2>添加群微信
                                            </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 激活页头图 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">激活页头图 </label>
                            <div class="controls">
                                <input type="text" id="activePagePic" name="activePagePic"
                                       class="form-control js-postData input" value="${content.activePagePic!''}"
                                       style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                <a class="btn btn-success preview"
                                   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.activePagePic!''}"</#if>
                                ">预览</a>
                            </div>
                        </div>

                        <#-- SKU类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SKU类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="skuType" name="skuType" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择SKU类型--</option>
                                    <option value="0"
                                            <#if content?? && content.skuType?? && content.skuType == 0>selected</#if>>
                                        训练营
                                    </option>
                                    <option value="1"
                                            <#if content?? && content.skuType?? && content.skuType == 1>selected</#if>>
                                        伪轻课
                                    </option>
                                    <option value="2"
                                            <#if content?? && content.skuType?? && content.skuType == 2>selected</#if>>
                                        其他
                                    </option>
                                    <option value="3"
                                            <#if content?? && content.skuType?? && content.skuType == 3>selected</#if>>
                                        真轻课
                                    </option>
                                </select>
                            </div>
                        </div>

                        <#-- 轻课学习模式 -->
                        <div class="control-group" id="model_id">
                            <label class="col-sm-2 control-label">轻课学习模式 <span style="color: red">*</span></label>
                            <div class="controls">
                                每天最多学习<select id="studyModel" name="studyModel" style="width: 200px;"
                                              class="js-postData">
                                    <option value="0"
                                            <#if content?? && content.studyModel?? && content.studyModel == 0>selected</#if>>
                                        0
                                    </option>
                                    <option value="1"
                                            <#if content?? && content.studyModel?? && content.studyModel == 1>selected</#if>>
                                        1
                                    </option>
                                    <option value="2"
                                            <#if content?? && content.studyModel?? && content.studyModel == 2>selected</#if>>
                                        2
                                    </option>
                                    <option value="3"
                                            <#if content?? && content.studyModel?? && content.studyModel == 3>selected</#if>>
                                        3
                                    </option>
                                    <option value="4"
                                            <#if content?? && content.studyModel?? && content.studyModel == 4>selected</#if>>
                                        4
                                    </option>
                                    <option value="5"
                                            <#if content?? && content.studyModel?? && content.studyModel == 5>selected</#if>>
                                        5
                                    </option>
                                    <option value="6"
                                            <#if content?? && content.studyModel?? && content.studyModel == 6>selected</#if>>
                                        6
                                    </option>
                                    <option value="7"
                                            <#if content?? && content.studyModel?? && content.studyModel == 7>selected</#if>>
                                        7
                                    </option>
                                    <option value="8"
                                            <#if content?? && content.studyModel?? && content.studyModel == 8>selected</#if>>
                                        8
                                    </option>
                                    <option value="9"
                                            <#if content?? && content.studyModel?? && content.studyModel == 9>selected</#if>>
                                        9
                                    </option>
                                    <option value="10"
                                            <#if content?? && content.studyModel?? && content.studyModel == 10>selected</#if>>
                                        10
                                    </option>
                                </select>节课 &nbsp;&nbsp; <span style="color: red">0为每天学习不限</span>
                            </div>
                        </div>

                        <#-- 是否发放连续学习奖励 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否发放连续学习奖励 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="needReward" name="needReward" class="form-control js-postData">
                                    <option value="">--请选择是否发放连续学习奖励--</option>
                                    <option value="true"
                                            <#if content?? && content.needReward?? && content.needReward?string("true","false") == 'true'>selected</#if>>
                                        是
                                    </option>
                                    <option value="false"
                                            <#if content?? && content.needReward?? && content.needReward?string("true","false") == 'false'>selected</#if>>
                                        否
                                    </option>
                                </select>
                            </div>
                        </div>

                        <#-- 配置环境 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择配置环境--</option>
                                    <#if levels??>
                                        <#list levels as lels>
                                            <option <#if content?? && content.envLevel??><#if content.envLevel == lels> selected="selected"</#if></#if> value= ${lels!}>
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
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text"
                                       value="<#if content??>${content.remark!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>


                        <h4>课程报名分享</h4><hr style="border:0; background-color: black; height:1px;">
                    <#-- 分享海报 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享海报</label>
                            <div class="controls">
                                <input type="text" id="pictureUrl" name="pictureUrl" class="form-control js-postData input" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.pictureUrl!''}</#if>" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                    <a class="btn btn-success preview" data-href="<#if content?? && content.shareContentConfig?? && cdn_host??>${cdn_host!''}${content.shareContentConfig.pictureUrl!''}</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 分享链接-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享链接 </label>
                            <div class="controls">
                                <input type="text" id="link" name="link"  class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.link!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>
                        <#-- 分享标题 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享标题 </label>
                            <div class="controls">
                                <input type="text" id="title" name="title" class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.title!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>
                        <#--分享内容-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享内容 </label>
                            <div class="controls">
                                <input type="text" id="content" name="content"  class="form-control js-postData" value="<#if content?? && content.shareContentConfig??>${content.shareContentConfig.content!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>

                    <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text"
                                       value="${createUser!''}" style="width: 336px;" readonly/>
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

        function changeIsComponent() {
            $("#isComponentSku").val($(this).val());
        }


        $("input[name='isComponentSkuRadio']").change(function () {
                var radioVal = $("input[name='isComponentSkuRadio']:checked").val();
                if (radioVal === '1') {
                    $("input[name='isComponentSku']").val(radioVal);
                    $("#face-type").css("display", "none");
                }
                if (radioVal === '2') {
                    $("input[name='isComponentSku']").val(radioVal);
                    $("#face-type").css("display", "block");
                }
            }
        );

        var value = $("#activeType").find("option:selected").val();
        if (value === '3' || value === '4') {
            $("#qrcodeType").val("");
            $("#qrcodeType").attr("disabled", true);
        } else {
            $("#qrcodeType").attr("disabled", false);
        }

        $("#activeType").change(function () {
            var value = $("#activeType").find("option:selected").val();
            if (value === '3' || value === '4') {
                $("#qrcodeType").val("");
                $("#qrcodeType").attr("disabled", true);
            } else {
                $("#qrcodeType").attr("disabled", false);
            }
        });

        var svalue = $("#skuType").find("option:selected").val();
        if (svalue === '3') {
            $("#model_id").show();
        } else {
            $("#model_id").hide();
        }
        $("#skuType").change(function () {
            var value = $("#skuType").find("option:selected").val();
            if (value === '3') {
                $("#model_id").show();
            } else {
                $("#model_id").hide();
            }
        });

        $("#skuId").blur(function () {
            var skuId = $("#skuId").val();
            if (skuId) {
                $.post("checkId.vpage", {skuId: skuId}, function (data) {
                    if (!data.success) {
                        alert(skuId + "对应的SKU已经存在");
                        $("#skuId").val('');
                        return;
                    }
                });
            }
        });

        //商品检测
        $("#productId").blur(function () {
            var productId = $("#productId").val();
            if (productId) {
                $.get("/opmanager/studytogether/common/product_name.vpage", {productId: productId}, function (data) {
                    if (data.success) {
                        $("#productName").html(data.productName);
                    } else {
                        alert(productId + "对应的商品不存在");
                        $("#productId").val("");
                        $("#productName").html("");
                        return;
                    }
                });
            } else {
                $("#productId").val("");
                $("#productName").html("");
                return;
            }
        });
        $("#face-preview").on('click', function () {
            var type = $("#NewTemplateType ").val();
            if (!type) {
                return;
            }
            var url = '';
            switch (type) {
                case 'ChineseFace_1':
                    url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/03/21/20190321134619922108.png';
                    break;
                case 'ChineseFace_2':
                    url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/03/21/20190321134647762271.png';
                    break;
                case 'English_1':
                    url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/03/21/20190321134710158184.png';
                    break;
                case 'Math_1':
                    url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/03/21/20190321134732751617.png';
                    break;
            }
            window.open(url);
        });

        //电子书检测
        $("#ebookId").blur(function () {
            var ebookId = $("#ebookId").val();
            if (ebookId) {
                $.get("/opmanager/studytogether/common/ebook.vpage", {ebookId: ebookId}, function (data) {
                    if (data.success) {
                        $("#ebookName").html(data.ebookName);
                    } else {
                        alert(ebookId + "对应的电子书不存在");
                        $("#ebookId").val("");
                        $("#ebookName").html("");
                        return;
                    }
                });
            } else {
                $("#ebookId").val("");
                $("#ebookName").html("");
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

        //添加排行榜信息
        var rankRewards = [];
        var rankCount = $("#rank_list_id").find(".fromCtn").length;
        $(document).on("click", "#add_content_id", function () {
            var count = $("#rank_list_id").find(".fromCtn").length;
            rankCount = count + 1;
            var newAddList = $("#newAddSingleItem");
            var htmlContent = '<div class="controls singleItemBox" style="margin-top: 5px;">' +
                '<span style="color: red">*</span>奖励名次' + rankCount + '<input type="text" name="vitality" value="" placeholder="第一个" class="input fromCtn">&nbsp;-&nbsp;' +
                '<input type="text" name="vitality" value="" placeholder="第二个" class="input toCtn"><br>' +
                '<span style="color: red">*</span>奖品名称<textarea name="vitality" placeholder="奖品可以为多个，不同奖品之间使用英文逗号隔开" style="width: 350px;" class="input itermCtn"></textarea>' +
                '<input type="button" value="删除" class="btn thisDelete"  data-val="' + (rankCount++) + '">' +
                '</div>';
            newAddList.append(htmlContent);
        });

        //删除排行榜信息
        $(document).on("click", "#rank_list_id .thisDelete", function () {
            var $this = $(this);
            var count = $("#rank_list_id").find(".fromCtn").length;
            if (count === $this.data("val")) {
                rankCount--;
                rankRewards.splice($this.data("val"), 1);
                $this.closest(".singleItemBox").remove();
            } else {
                alert("从尾行开始删除")
            }
        });

        $("#card_one").click(function () {
            $("#cardDisplay").val($("#card_one").val());
            $("#card_tow").attr("checked", false);
        });

        $("#card_tow").click(function () {
            $("#cardDisplay").val($("#card_tow").val());
            $("#card_one").attr("checked", false);
        });

        //check spu's name
        $("#spuId").blur(function () {
            var spuId = $("#spuId").val();
            if (spuId) {
                $.get("/opmanager/studytogether/common/spu_name.vpage", {spuId: spuId}, function (data) {
                    if (data.success) {
                        $("#spuName").html(data.spuName);
                    } else {
                        alert(spuId + "对应的SPU不存在");
                        $("#spuId").val("");
                        $("#spuName").html("");
                        return;
                    }
                });
            } else {
                $("#spuId").val("");
                $("#spuName").html("");
                return;
            }
        });

        //验证表单
        var num_reg = /^[0-9]*$/;
        var double_reg = /^[0-9]+([.]{1}[0-9]{1,2}){0,1}$/;
        var validateForm = function () {
            var msg = "";
            var skuId = $('#skuId').val();
            if (skuId <= 0 || !skuId.match(num_reg)) {
                msg += "SKU_ID必须是正整数！\n";
                $('#skuId').val('');
            }
            if ($('#spuId').val() === '') {
                msg += "SPU_ID为空！\n";
            }
            if ($('#phase').val() === '') {
                msg += "期数为空！\n";
            }
            if ($('#phase').val() <= 0 || !$('#phase').val().match(num_reg)) {
                msg += "期数ID非正整数，重新填写！\n";
            }
            if ($('#openDate').val() == '') {
                msg += "请指定开始时间！\n";
            }
            if ($('#closeDate').val() == '') {
                msg += "请指定结束时间！\n";
            }
            if ($('#price').val() == '' || !$('#price').val().match(double_reg)) {
                msg += "请填写正确的原价！\n";
            }
            if ($('#discountPrice').val() == '' || !$('#discountPrice').val().match(double_reg)) {
                msg += "请填写正确的优惠价！\n";
            }
            if ($('#personLimited').val() == '' || !$('#personLimited').val().match(num_reg)) {
                msg += "请填写正确的库存！\n";
            }
            if ($('#cardDisplay').val() == '') {
                msg += "请选择卡片是否展示！\n";
            }
            if ($('#showDate').val() == '') {
                msg += "请指定报名开始时间！\n";
            }
            if ($('#sighUpEndDate').val() == '') {
                msg += "请指定报名结束时间！\n";
            }
            if ($('#joinWay').val() == '') {
                msg += "请选择报名方式！\n";
            }
            var activeType = $('#activeType').val();
            if (activeType === '') {
                msg += "请选择激活类型！\n";
            }
            if (activeType && activeType != 3 && activeType != 4) {
                if ($('#qrcodeType').val() === '') {
                    msg += "请选择课程激活微信号类型！\n";
                }
            }
            if ($('#skuType').val() === '') {
                msg += "请选择课程类型！\n";
            }
            if ($('#needReward').val() === '') {
                msg += "请选择是否发放学习奖励！\n";
            }
            if ($('#envLevel').val() == '') {
                msg += "请选择配置环境！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        //保存提交
        $(document).on("click", '#save_ad_btn', function () {
            if (validateForm()) {
                var post = {};
                var flag = true;
                var rank_list_id = $("#rank_list_id");
                $(".js-postData").each(function (i, item) {
                    post[item.name] = $(item).val();
                });

                console.log(post);

                rank_list_id.find(".singleItemBox").each(function (index) {
                    var $this = $(this);
                    if ($.inArray(rankRewards[index], rankRewards) > -1) {
                        if (rankRewards[index].from != $this.find(".fromCtn").val() ||
                            rankRewards[index].to != $this.find(".toCtn").val() ||
                            rankRewards[index].terms != $this.find(".itermCtn").val()) {
                            if ($this.find(".fromCtn").val() && $this.find(".toCtn").val() && $this.find(".itermCtn").val()) {
                                rankRewards[index].from = $this.find(".fromCtn").val();
                                rankRewards[index].to = $this.find(".toCtn").val();
                                rankRewards[index].terms = $this.find(".itermCtn").val();
                            }
                        }
                    } else {
                        if ($this.find(".fromCtn").val() && $this.find(".toCtn").val() && $this.find(".itermCtn").val()) {
                            var fromVal = parseInt($this.find(".fromCtn").val());
                            var toVal = parseInt($this.find(".toCtn").val());
                            if (fromVal <= 0 || toVal <= 0 || fromVal > toVal) {
                                alert("填写的数值为0或排行榜名次的第一个数值比第二个数值大");
                                flag = false;
                            } else {
                                rankRewards.push({
                                    from: $this.find(".fromCtn").val(),
                                    to: $this.find(".toCtn").val(),
                                    terms: $this.find(".itermCtn").val()
                                });
                            }
                        } /*else if ($this.find(".fromCtn").val() === '' || $this.find(".itermCtn").val() === '' || $this.find(".toCtn").val() === ''){
                            alert("排行榜奖励说明的名次或名称为空");
                            flag = false;
                        }*/
                    }
                });
                if (!flag) {
                    return;
                }
                /*if (rankRewards.length <= 0) {
                    alert("排行榜奖励说明为空");
                    return;
                }*/
                post.rankRewards = JSON.stringify(rankRewards);
                $.post('save.vpage', post, function (res) {
                    if (res.success) {
                        alert("保存成功");
                        location.href = 'index.vpage';
                    } else {
                        alert("保存失败：" + res.info);
                    }
                });
            }
        });

    });
</script>
</@layout_default.page>

