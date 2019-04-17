<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="去学习"
bodyClass="body-gray"
pageJs=["learning"]
pageJsFile={"learning" : "public/script/parentMobile/learning"}
pageCssFile={"learning" : ["public/skin/parentMobile/learning/css/skin"]}
>
<div class="lc-header" style="display: none;" data-bind="visible: $root.SID()&&getStudentList().length > 0 ">
    <ul class="innerBox"  data-bind="foreach: getStudentList">
        <li data-bind="css: { active: $root.SID() == id}">
            <a href="javascript:;" data-bind="attr:{ 'data-openurl': '/parentMobile/learning/index.vpage?sid='+ id,'data-studentName':name,'data-studentId':id}, css: { active: $root.SID() == id}" data-linktype="switchStudent">
                <!-- ko if : img -->
                <span class="pic">
                    <img src="" data-bind="attr: {src: img}">
                </span>
                <!-- /ko -->
                <!-- ko ifnot : img -->
                <span class="pic">
                    <img src="<@app.link href="public/skin/parentMobile/learning/images/tool03.png"/>">
                </span>
                <!-- /ko -->
                <span class="name" data-bind="text: name">--</span>
            </a>
        </li>
    </ul>
</div>

<#if isGraduate!false>
    <div class="no-account"></div>
    <div class="null-text">暂不支持小学毕业账号</div>
<#else>
    <div class="lc-banner" id="headerBannerCrm" style="position: relative;">
        <ul class="slides" data-bind="foreach: headerBanner()">
            <li>
                <a href="javascript:;" data-bind="attr:{'data-openurl': $root.goLink() + '?aid=' + id}" data-linktype="headerBanner">
                    <!-- ko if : img -->
                    <img data-bind="attr:{src : $root.imgDoMain() + 'gridfs/' + img}"/>
                    <!-- /ko -->
                </a>
            </li>
        </ul>
    </div>

    <div class="lc-banner" id="headerBannerDefault" style="display: none;">
        <img src="<@app.link href="public/skin/parentMobile/learning/images/banner.png"/>" width="100%"/>
    </div>

    <div class="lc-section" id="learnTool">
        <div class="title">同步学习工具</div>
        <ul class="learnTool slides">
            <#list toolList as tl>
                <li class="js-clickOpenApp" data-name="${tl.selfStudyType!}" style="cursor: pointer;" data-index="${(tl.toolOpId)!0}">
                    <#if (tl.iconUrl)?has_content>
                        <img src="<@app.link href="${tl.iconUrl!}"/>">
                    <#else>
                        <@app.avatar href=''/>
                    </#if>
                    <p class="text">${tl.toolName!'--'}</p>
                    <#if (tl.toolOpId)!?has_content>
                    <!--ko if: toolRedPointItems().indexOf('${(tl.toolOpId)!0}') == -1 -->
                        <span class="new-icon"></span>
                    <!--/ko-->
                    </#if>
                    <#if (tl.toolOpText)!?has_content>
                        <div class="i-absInfo">${tl.toolOpText!}</div>
                    </#if>
                </li>
            </#list>
            <li style="cursor: default;">
                <img src="<@app.link href="public/skin/parentMobile/learning/images/tool04.png"/>">
                <p class="text">敬请期待</p>
            </li>
        </ul>
        <div class="clearfix"></div>
    </div>
        <#if productList??>
        <div class="lc-section" style="width: 100%; overflow: hidden;" data-bind="visible: $root.SID()!='362850585'">
            <div class="title">趣味学习应用<a href="javascript:;" data-bind="attr: {'data-openurl': '/parentMobile/ucenter/shoppinginfolist.vpage?sid=' + SID()}" class="more-link" data-linktype="learnAppsMore">查看更多></a></div>
            <div id="learnApps">
                <ul class="learnApps slides" style="width: 10000px;">
                    <#list productList as pdl>
                        <#if pdl_index lt 4>
                            <li>
                                <a href="javascript:;" data-bind="attr: {'data-openurl': '/parentMobile/ucenter/shoppinginfo.vpage?productType=${pdl.appKey!}&sid=' + SID()}" data-appkey="${(pdl.appKey)!}" data-linktype="learnApps">
                                    <div class="picBox">
                                        <img src="<@app.avatar href='${pdl.backgroundImage!}'/>">
                                        <p class="intro">${pdl.operationMessage!'---'}</p>
                                    </div>
                                    <p class="text" style="text-align: center;">${pdl.productName!'---'}</p>
                                </a>
                            </li>
                        </#if>
                    </#list>
                </ul>
            </div>
        </div>
        </#if>

    <!-- ko if : isSuccess() -->
    <div class="lc-section" style="display: none;" data-bind="visible: isSuccess()">
        <div class="title">自学动态</div>
        <ul class="learnDynamic" data-bind="foreach: dynamicContent()">
            <li>
                <div class="left">
                    <!-- ko if : avatar -->
                    <img src="" data-bind="attr: {src: '<@app.avatar href='/'/>' + avatar()}">
                    <!-- /ko -->

                    <!-- ko ifnot : avatar -->
                    <img src="<@app.link href="public/skin/parentMobile/learning/images/pic-parent.png"/>">
                    <!-- /ko -->
                </div>
                <div class="right">
                    <div class="name" data-bind="text: relevantUserName">-</div>
                    <div class="article" data-bind="html: param.content">本周王大锤，李木子等12位同学 订正了作业，并在类题练习中正确率超过了80%</div>
                    <div class="look-link" data-bind="visible: link_url()"><a href="javascript:void(0)" data-bind="attr: {'data-openurl': link_url()}" data-linktype="qukankan">去看看></a></div>
                    <!--仅图片-->
                    <div class="pic" data-bind="if: journalType == 'LEARNING_CYCLE_T2', visible: journalType == 'LEARNING_CYCLE_T2'">
                        <img src="" class="pic-img" data-bind="attr:{src: '<@app.avatar href='/'/>' + img_url}">
                    </div>
                    <!--图文并列-->
                    <div class="pic pic-text" data-bind="if: journalType == 'LEARNING_CYCLE_T3', visible: journalType == 'LEARNING_CYCLE_T3'">
                        <img src="" class="pic-img" data-bind="attr:{src: '<@app.avatar href='/'/>' + img_url}">
                        <span class="text" data-bind="text: img_content"></span>
                    </div>
                    <!--发布时间-->
                    <div class="time">
                        <span data-bind="text: date">--</span>
                        <a href="javascript:void(0)" class="zan-icon" data-bind="click: $parent.getLike"></a>
                    </div>
                    <!--点赞名单-->
                    <div class="zan-name" data-bind="if: show_name(), visible: show_name();">
                        <span data-bind="text: show_name()"></span>
                    </div>
                </div>
            </li>
        </ul>
    </div>
    <!-- /ko -->

    <div data-bind="visible: dialogContent()" style="display: none;">
        <div class="dialog-alert">
            <div class="layer-confirm">
            <#--<div class="close js-dialogClose" style="cursor: pointer;"><span></span></div>-->
                <div class="text" data-bind="text: dialogContent">----</div>
                <div class="foot">
                    <a href="javascript:;" class="js-dialogClose" data-type="close">取消</a>
                    <a href="javascript:;" class="green js-dialogClose" data-type="submit" data-bind="text: dialogBtn">确定</a>
                </div>
            </div>
        </div>
    </div>
</#if>

</@layout.page>