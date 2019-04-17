<#import "../layout.ftl" as uReport>
<@uReport.page title="单元报告" pageJs="unitReport">
    <@sugar.capsule css=['unitReport','jbox'] />

    <div id="loading" style="padding:50px 0; text-align:center">数据加载中...</div>
	<div id="unitReport" style="display:none;">
        <#include "../userpopup.ftl">
        <#include "../subject.ftl">
        <div id="unitReportChip">
            <div class="unitReports-box">
                <div class="container" data-bind="foreach:unitReportList">
                    <div class="list">
                        <h2><a data-bind="click:$root.toDetail($data)" class="btn-view">查看</a><span data-bind="text:$data.unitName"></span></h2>
                    </div>
                </div>
            </div>
            <!-- ko if:unitReportList().length===0 && focusTab() =='english'-->
            <div class="waiting_box"><span class="wb"></span><p>所在班级没有单元报告...</p></div>
            <!-- /ko -->
            <!-- ko if:focusTab() !='english' -->
            <div class="waiting_box"><span class="wb"></span><p>正在开发中,敬请期待...</p></div>
            <!-- /ko -->
        </div>

	</div>
    <div id="isGraduateBox" style="display: none; text-align: center;">暂不支持小学毕业账号</div>
	<#include "../menu.ftl">
</@uReport.page>