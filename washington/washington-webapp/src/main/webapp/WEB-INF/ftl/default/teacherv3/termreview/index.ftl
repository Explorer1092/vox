<#import "../../nuwa/teachershellv3.ftl" as shell />
<#import "../templates/termreview/typetemplates.ftl" as typeTpls>
<@shell.page show="main">
    <@sugar.capsule js=["ko","datepicker"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework","termreview.finalreview"] />
<div class="w-base" id="J_assignLevelBookPanel" style="position: relative; zoom: 1;  z-index: 10;">
    <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
        <h3>布置作业</h3>
    </div>
    <div id="levelsAndBook" class="w-base-container">
        <div class="t-homework-form">
            <div data-bind="template:{name : 't:LOAD_IMAGE',if:$root.loading()}"></div>
            <dl style="display: none;" data-bind="if:!$root.loading() && $root.clazzList().length > 0,visible:!$root.loading() && $root.clazzList().length > 0">
                <dt>班级</dt>
                <dd style="overflow: hidden; *zoom:1; position: relative;">
                    <div class="w-border-list t-homeworkClass-list">
                        <ul>
                            <!--ko foreach: $root.clazzList()-->
                            <li data-bind="click:$root.levelClick.bind($data,$root),css:{'active' : $root.focusClazzLevel() == $data.clazzLevel },text:$data.clazzLevelName" class="v-level"></li>
                            <!--/ko-->
                            <li class="pull-down">
                                <!--ko if:$root.showClazzList().length > 1-->
                                <p data-bind="click:$root.chooseOrCancelAll,css:{'w-checkbox-current' : $root.isAllChecked()}">
                                    <span class="w-checkbox"></span>
                                    <span class="w-icon-md textWidth">全部</span>
                                </p>
                                <!--/ko-->
                                <!--ko foreach:$root.showClazzList()-->
                                <p data-bind="click : $root.singleClazzAddOrCancel.bind($data,$root,$index()), css:{'w-checkbox-current': $data.checked}" class="marginL26" style="width:100px;">
                                    <span class="w-checkbox"></span>
                                    <span class="w-icon-md" data-bind="attr:{title:$data.clazzName},text:$data.clazzName"></span>
                                </p>
                                <!--/ko-->
                            </li>
                        </ul>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="w-noData-box" style="display: none;" data-bind="if:!$root.loading() && clazzList().length == 0,visible:!$root.loading() && clazzList().length == 0">
            您需要检查班级上一次作业，才可以布置新作业，去<a href="/teacher/new/homework/report/list.vpage?subject=${subject!}" class="w-blue">检查作业</a>
        </div>
        <div id="bookInfo" class="t-homework-form" style="display: none;" data-bind="if:!$root.loading() && $root.bookId() != null,visible:!$root.loading() && $root.bookId() != null">
            <dl style="overflow: visible; z-index: 12;">
                <dt>教材</dt>
                <dd style=" position: relative;">
                    <div class="text">
                        <span data-bind="text:$root.bookName()"></span>
                        <a class="w-blue" href="javascript:void(0);" style="margin-left: 50px;" data-bind="click:changeBook">
                            更换教材<span class="w-icon-public w-icon-switch w-icon-switchBlue" style="margin-right: -5px; margin-left: 10px; *margin: 3px 0 0 10px;"></span>
                        </a>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>

<div class="w-base h-baseTab" id="J_hkTabcontent">
    <div id="termTabs" class="w-base-title" style="height: 110px;border-bottom: 0" data-bind="template:{name:'t:作业形式'}"></div>
    <div id="termTabContent"></div>
</div>

<#---作业统计-->
<div id="ufo" class="h-floatLayer-R assignAndPreview">
    <div class="fl-hd">预计<span data-bind="text:$root.totalMin()">0</span>分钟完成</div>
    <div class="J_UFOInfo fl-mn" data-bind="foreach:{data:$root.tabList(),as:'tab'}">
        <p data-bind="attr:{'type' : tab.key()},singleHomeworkTypeHover:tab.count()">
            <span class="name" data-bind="text:tab.name()">&nbsp;</span>
            <span class="count" data-bind="text:tab.count()">0</span>
            <span class="icon" data-bind="click:$root.deleteTypeData.bind($data,$root)"><i class="h-set-icon-delete h-set-icon-deleteGrey"></i></span>
        </p>
    </div>
    <div class="fl-bot">
        <a href="javascript:void(0)" class="w-btn w-btn-well w-btn-green" data-bind="click:$root.previewOrBackAdjust,text:$root.switchPanel() == 'PREVIEW_PANEL' ? '返回调整' : '预览'">预览</a>
        <a href="javascript:void(0)" data-bind="click:$root.assignClick" class="w-btn w-btn-well w-btn-blue">布置</a>
    </div>
</div>
<div style="display:none" id="J_previewPanel" class="h-homeworkPreview assignAndPreview">
    <div class="w-base">
        <h2 class="h2-title">预览</h2>
        <div data-bind="template:{name : displayMode,foreach : $root.homeworkTypeData,as:'homeworkCt',if:$root.switchPanel() == 'PREVIEW_PANEL'}">

        </div>
    </div>
</div>

<@typeTpls.tabTemplates subject="${subject!}" />
<#include "../templates/homeworkv3/changebook.ftl">
<script id="t:LOAD_IMAGE" type="text/html">
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
    </div>
</script>
<script type="text/javascript">
    var constantObj = {
        subject             : "${subject!}",
        batchclazzs         : ${batchclazzs![]},
        currentDateTime     : "${currentDateTime}",
        endDate             : "${endDate!}",
        endTime             : "${endTime!}",
        tabIconPrefixUrl    : "<@app.link href='public/skin/teacherv3/images/termreview/tab-icon/' />",
        basicIconPrefixUrl  : "<@app.link href='public/skin/teacherv3/images/homework/english-icon/' />",
        imgDomain           : '${imgDomain!''}',
        domain              : '${requestContext.webAppBaseUrl}/',
        env                 : <@ftlmacro.getCurrentProductDevelopment />,
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
    };
    function nextHomeWork(){
        $.prompt.close();
    }
    $(function(){
        LeftMenu.focus("${subject!}_termreview");

        $17.termreview.getTermReview().run();

        var log = $17.getQuery("log") || "leftMenu";
        var jqFrom;
        if(log === "leftMenu"){
            jqFrom = "导航栏_布置作业";
        }else if(log === "popup"){
            jqFrom = "首页_弹窗";
        }else if(log === "ad"){
            jqFrom = "首页_卡片广告页";
        }else if(log === "cardList"){
            jqFrom = "首页卡片"
        }else{
            jqFrom = ""
        }

        $17.voxLog({
            module: "m_8NOEdAtE",
            op : "page_final_review_assign",
            s0 : "${subject!}",
            s1 : jqFrom
        });
    });
</script>
<@sugar.capsule js=["homework2nd","termreview"] />
</@shell.page>