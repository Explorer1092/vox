<#import "../layout.ftl" as uClassDetail>
<@uClassDetail.page title="作业报告" pageJs="uDetail">
    <@sugar.capsule css=['unitReport'] />
    <script type="text/javascript">
    var result=${json_encode(urd)}
    </script>
    <div id="loading" style="padding:50px 0; text-align:center">数据加载中...</div>
    <div id="reportDetail" class="unitReports-box" style="display:none;">
        <div id="reportTitle" class="header" data-bind="text:title"></div>
        <div id="important">
            <div class="title">
                <i class="icon icon-1"></i>
                单元重点
            </div>
            <!-- ko if:showImportants().length==0 -->
            <div style="padding:50px 0; text-align:center;font-size:30px;">本单元没有重点知识</div>
            <!-- /ko -->
            <!-- ko if:showImportants().length>0 -->
            <div class="container">
                <table>
                    <thead>
                        <tr class="odd-1">
                            <td>序号</td>
                            <td>重点知识</td>
                            <td>易考例题</td>
                        </tr>
                    </thead>
                    <tbody data-bind="foreach:showImportants">
                        <tr>
                            <td class="order" data-bind="text:$root.order($index())"></td>
                            <td data-bind="text:pointName"></td>
                            <!-- ko if:$data.eid !='' -->
                            <td class="view" data-bind="click:$root.showCurUnitQuestion($data,$root.order($index()))"><a style="color:inherit" href="javascript:void (0);">查看</a></td>
                            <!-- /ko -->
                            <!-- ko if:$data.eid =='' -->
                            <td class="view">
                                <a style="color:#eee">查看</a>
                            </td>
                            <!-- /ko -->
                        </tr>
                    </tbody>
                </table>
                <div class="turn-list" data-bind="visible:hasMoreImportant">
                    <a href="javascript:void (0)" data-bind="click:showMoreImportants">
                        查看更多
                        <span class="triangle down"></span>
                    </a>
                </div>
            </div>
            <!-- /ko -->
        </div>
        
        <div id="noWrong" style="display:none;">
            <div class="title">
                <i class="icon icon-2"></i>
                本班情况
            </div>
            <div class="container">
                <div class="msg">高频错题</div>
                <div style="padding:50px 0; text-align:center;font-size:30px;">本单元没有高频错题</div>
            </div>
        </div>  
        <div id="wrong">
            <div class="title">
                <i class="icon icon-2"></i>
                本班情况
            </div>
            <div class="container">
                <div class="msg">高频错题</div>
                <div id="wrongQuestions"></div>
                <div id="moreWrong" class="turn-list">
                    <a href="javascript:void (0);">
                        查看更多
                        <span class="triangle down"></span>
                    </a>
                </div>
            </div>
        </div>  
        <div id="prepare" data-bind="visible:showPrepares().length>0">
            <div class="title">
                <i class="icon icon-3"></i>
                预习安排
            </div>
            <div class="container">
                <table>
                    <thead>
                    <tr class="odd-1">
                        <td>序号</td>
                        <td>重点知识</td>
                        <td>易考例题</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach:showPrepares">
                    <tr>
                        <td class="order" data-bind="text:$root.order($index())"></td>
                        <td data-bind="text:pointName"></td>
                        <!-- ko if:$data.eid !='' -->
                        <td class="view" data-bind="click:$root.showNextUnitQuestion($data,$root.order($index()))"><a style="color:inherit" href="javascript:void (0);">查看</a></td>
                        <!-- /ko -->
                        <!-- ko if:$data.eid =='' -->
                        <td class="view">
                            <a style="color:#eee">查看</a>
                        </td>
                        <!-- /ko -->
                    </tr>
                    </tbody>
                </table>
                <div class="turn-list" data-bind="{visible:hasMorePrepare,click:showMorePrepares}">
                    <a href="javascript:void (0)">
                        查看更多
                        <span class="triangle down"></span>
                    </a>
                </div>
            </div>      
        </div>
    </div>
    <#include "../menu.ftl">
</@uClassDetail.page>