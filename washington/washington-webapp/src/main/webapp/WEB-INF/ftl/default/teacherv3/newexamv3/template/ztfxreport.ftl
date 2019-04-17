<script type="text/html" id="T:Ztfxreport" id="Ztfxreport">
    <div class="rp-box-tea" data-bind="visible:!$root.status()">
        <div class="left" data-bind="foreach:{data : $root.ajaxTitleData,as:'mt'}">
            <div class="left-fix" data-bind="foreach:{data : mt.dataMap.catalog,as:'catalog'}">
                <div class="left-item">
                    <div class="item-h1" data-bind="attr:{'id':'0-'+$index()+'xx'},
                    click:$root.itemClick.bind($root,'0',$index())">
                        <span data-bind="text:catalog.title"></span>
                        <a class="icon_a icon_x" data-bind="visible:catalog.childTitle.length>0,click:$root.itemShowHide.bind($root,'0',$index())"></a>
                    </div>
                    <ul class="item-p" data-bind="foreach:{data : catalog.childTitle,as:'childTitle'}" style="display: none;">
                        <li data-bind="attr:{'id':catalog.moduleId+'-'+$index()+'xx'},text:childTitle,click:$root.itemClick.bind($root,catalog.moduleId,$index())"></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="right">
            <div data-bind="foreach:{data : $root.ajaxData,as:'m'}">
                <#--["moduleId":2,]-->
                <div data-bind="if:m.module_id == 2">
                    <#-- 一级标题 -->
                    <div class="item" data-bind="attr:{'id':'0-'+$index()}">
                        <div data-bind="template:{name: 'titleAnddescAction',data:m}" ></div>
                    </div>
                </div>
                <#--["moduleId":3,]-->
                <div data-bind="if:m.module_id == 3">
                    <#-- 一级标题 -->
                    <div class="item" data-bind="attr:{'id':'0-'+$index()}">
                        <div data-bind="template:{name: 'titleAnddescAction',data:m}" ></div>
                    </div>
                    <!-- 二级标题-->
                    <div data-bind="foreach:{data : m.dataMap.contents,as:'contents'}">
                        <div class="item" data-bind="attr:{'id':m.module_id+'-'+$index()}" >
                            <#--小标题+text-->
                            <div data-bind="template:{name: 'titleAnddescAction_2',data:contents}" ></div>
                            <#--图标+注意-->
                            <#--饼图-->
                            <div data-bind="foreach:{data : contents.echart,as:'echart'}">
                                <div class="table">
                                    <div class="pie-chart">
                                        <div data-bind="attr:{'id':'container'+$index()}" style="width: 540px;height: 288px;"></div>
                                        <div data-bind="text:$root.initMap($index(),echart)"></div>
                                    </div>
                                    <div class="head-title1" data-bind="text:echart.echartTitle"></div>
                                    <div class="notice"  data-bind="text:echart.echartDesc"></div>
                                </div>
                            </div>
                            <#--图表-->
                            <div data-bind="foreach:{data : contents.grid,as:'grid'}">
                                <div data-bind="template:{name: 'tableAction',data:grid}" ></div>
                            </div>
                        </div>
                     </div>
                </div>
                <#--["moduleId":4,]-->
                <div data-bind="if:m.module_id == 4">
                    <#-- 一级标题 -->
                    <div class="item" data-bind="attr:{'id':'0-'+$index()}">
                        <div data-bind="template:{name: 'titleAnddescAction',data:m}" ></div>
                    </div>
                    <!-- 二级标题-->
                    <div data-bind="foreach:{data : m.dataMap.contents,as:'contents'}">
                        <div class="item" data-bind="attr:{'id':m.module_id+'-'+$index()}" >
                            <#--小标题+text-->
                            <div data-bind="template:{name: 'titleAnddescAction_2',data:contents}" ></div>
                            <#--图表-->
                            <div data-bind="foreach:{data : contents.grid,as:'grid'}">
                                <div data-bind="template:{name: 'tableAction',data:grid}" ></div>
                            </div>
                        </div>
                    </div>
                </div>
                <#--["moduleId":5,]-->
                <div data-bind="if:m.module_id == 5">
                    <#--一级标题-->
                    <div class="item" data-bind="attr:{'id':'0-'+$index()}">
                        <div data-bind="template:{name: 'titleAnddescAction',data:m}" ></div>
                    </div>
                    <!-- 二级标题-->
                    <div data-bind="foreach:{data : m.dataMap.contents,as:'contents'}">
                        <div class="item" data-bind="attr:{'id':m.module_id+'-'+$index()}" >
                            <#--小标题+text-->
                            <div data-bind="template:{name: 'titleAnddescAction_2',data:contents}" ></div>
                            <#--图表-->
                            <div data-bind="foreach:{data : contents.grid,as:'grid'}">
                                <div data-bind="template:{name: 'tableAction',data:grid}" ></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <div data-bind="visible:$root.status(),text:$root.info()"
         style="padding: 300px 50px;text-align: center"
    ></div>
</script>
<#-- 一级 title desc-->
<script type="text/html" id="titleAnddescAction">
    <div class="title"   data-bind="text:$parent.dataMap.title"></div>
    <div class="content" data-bind="foreach:{data :$parent.dataMap.desc,as:'desc'}">
        <p data-bind="text:desc"></p>
    </div>
</script>
<#-- 二级 title desc-->
<script type="text/html" id="titleAnddescAction_2">
    <div class="title-small"   data-bind="text:$parent.title"></div>
    <div class="content" data-bind="foreach:{data :$parent.text,as:'text'}">
        <p data-bind="text:text"></p>
    </div>
</script>
<#-- 表格 -->
<script type="text/html" id="tableAction">
    <#--gridType == 1-->
    <div  data-bind="if:$parent.gridType == 1">
        <div class="table table1">
            <div class="head-title1" data-bind="text:$parent.gridTitle"></div>
            <table class="tab-content" border="0" cellspacing="0" cellpadding="0"
                   data-bind="foreach:{data : $parent.gridData,as:'gridData'}">

                    <tr class="tab-th" data-bind="if:gridData.type == 'head'">
                        <td style="background-color: #FFFFFF"></td>
                        <td data-bind="text:gridData.col_one"></td>
                        <td data-bind="text:gridData.col_two"></td>
                    </tr>
                    <tr  data-bind="if:gridData.type == 'row',
                    attr:{'style':'background-color:'+gridData.bgcolor}" >
                        <td data-bind="attr:{'style':'background-color:'+gridData.tip_color}"></td>
                        <td data-bind="text:gridData.col_one"></td>
                        <td data-bind="visible:gridData.col_two.length>0,foreach:{data : gridData.col_two,as:'col_two'}">
                            <span data-bind="text:col_two"></span>
                        </td>
                    </tr>
            </table>
        </div>
        <div class="notice" data-bind="foreach:{data:$parent.gridDesc,as:'gridDesc'}">
            <p data-bind="text:gridDesc"></p>
        </div>
    </div>
    <#--gridType == 2-->
    <div  data-bind="if:$parent.gridType == 2">
        <div class="table table2">
            <div class="head-title1" data-bind="text:$parent.gridTitle"></div>
            <table class="tab-content" border="0" cellspacing="0" cellpadding="0"
                   data-bind="foreach:{data : $parent.gridData,as:'gridData'}">
                <tr class="tab-th" data-bind="if:gridData.type == 'head'">
                    <td data-bind="text:gridData.col_one"></td>
                    <td data-bind="text:gridData.col_two"></td>
                </tr>
                <tr data-bind="if:gridData.type == 'row'">
                    <td class="ico-td">
                        <img data-bind="visible:gridData.process_rank == 'A'" src="/public/skin/newexamv3/images/teareport/img_jbtc.png">
                        <img data-bind="visible:gridData.process_rank == 'B'" src="/public/skin/newexamv3/images/teareport/img_xfts.png">
                        <img data-bind="visible:gridData.process_rank == 'C'" src="/public/skin/newexamv3/images/teareport/img_jbhm.png">
                        <span data-bind="text:gridData.col_one">进步缓慢</span>
                    </td>
                    <td data-bind="visible:gridData.col_two.length>0,foreach:{data : gridData.col_two,as:'col_two'}">
                        <span data-bind="text:col_two"></span>&nbsp;
                    </td>
                </tr>
            </table>
        </div>
        <div class="notice" data-bind="foreach:{data:$parent.gridDesc,as:'gridDesc'}">
            <p data-bind="text:gridDesc"></p>
        </div>
    </div>
    <#--gridType == 3-->
    <div  data-bind="if:$parent.gridType == 3">
        <div class="table table3">
            <div class="head-title1" data-bind="text:$parent.gridTitle"></div>
            <table class="tab-content" border="0" cellspacing="0" cellpadding="0"
                   data-bind="foreach:{data : $parent.gridData,as:'gridData'}" >
                <tr class="tab-th" data-bind="attr:{'style': $root.bgaddOReven($index(),gridData)},
                foreach:{data : gridData.skill_com,as:'skill_com'},
                visible:gridData.type == 'head'">
                    <td data-bind="visible:$index()==0,text:gridData.task"></td>
                    <td data-bind="text:skill_com"></td>
                </tr>
                <tr data-bind=" attr:{'style': $root.bgaddOReven($index(),gridData)},
                foreach:{data : gridData.score,as:'score'},
                visible:gridData.type == 'row'
                    ">
                    <td data-bind="visible:$index()==0,text:gridData.task" ></td>
                    <td>
                        <span data-bind="text:$root.NA(score),attr:{'style':'background-color:'+gridData.score_bg_color[$index()]}"></span>
                    </td>
                </tr>
            </table>
        </div>
        <div class="notice" data-bind="foreach:{data:$parent.gridDesc,as:'gridDesc'}">
            <p data-bind="text:gridDesc"></p>
        </div>
    </div>
    <#--gridType == 4-->
    <div  data-bind="if:$parent.gridType == 4">
        <div class="table table4">
            <div class="head-title1" data-bind="text:$parent.gridTitle"></div>
            <table class="tab-content" border="0" cellspacing="0" cellpadding="0"
                   data-bind="foreach:{data : $parent.gridData,as:'gridData'}">
                <tr data-bind="foreach:{data : gridData.skill_com,as:'skill_com'},
                visible:gridData.type == 'head',
                attr:{'style':'background-color:'+gridData.bgcolor}" >
                    <td data-bind="attr:{'style':'background-color:'+gridData.tip_color},visible:$index()==0"></td>
                    <td data-bind="visible:$index()==0,text:gridData.student_name" ></td>
                    <td data-bind="text:skill_com" ></td>
                </tr>
                <tr data-bind="foreach:{data : gridData.score,as:'score'},
                visible:gridData.type == 'row',

                attr:{'style':'background-color:'+gridData.bgcolor}" >
                    <td data-bind="attr:{'style':'background-color:'+gridData.tip_color},visible:$index()==0"></td>
                    <td data-bind="visible:$index()==0,text:gridData.student_name" ></td>
                    <td data-bind="text:score" ></td>
                </tr>
            </table>
            <div data-bind="foreach:{data : $parent.gridData,as:'gridData'}">
                <ul class="item-icon" data-bind="foreach:{data : gridData.value,as:'value'},visible:gridData.type == 'legend'">
                    <li>
                        <i data-bind="attr:{'style':'background-color:'+value.color}"></i>
                        <span data-bind="text:value.rank"></span>
                    </li>
                </ul>
            </div>
        </div>
        <div class="notice" data-bind="foreach:{data:$parent.gridDesc,as:'gridDesc'}">
            <p data-bind="text:gridDesc"></p>
        </div>
    </div>
    <#--gridType == 5-->
    <div  data-bind="if:$parent.gridType == 5">
        <div class="table table5">
            <div class="head-title1" data-bind="text:$parent.gridTitle"></div>
            <table class="tab-content"border="0" cellspacing="0" cellpadding="0"
                   data-bind="foreach:{data : $parent.gridData,as:'gridData'}">
                <tr class="tab-th" data-bind="if:gridData.type == 'head',,attr:{'style':'background-color:'+gridData.bgcolor}" >
                    <td data-bind="text:gridData.question_num" style="min-width: 50px"></td>
                    <td data-bind="text:gridData.right_ratio"></td>
                    <td data-bind="text:gridData.task_name"></td>
                    <td data-bind="text:gridData.error_num"></td>
                    <td data-bind="text:gridData.answer_time_avg"></td>
                </tr>
                <tr data-bind="if:gridData.type == 'row',
                attr:{'style':'background-color:'+gridData.bgcolor+';color:'+gridData.fontcolor}"">
                    <td data-bind="text:gridData.question_num"></td>
                    <td data-bind="text:gridData.right_ratio"></td>
                    <td data-bind="text:gridData.task_name"></td>
                    <td data-bind="text:gridData.error_num"></td>
                    <td data-bind="text:gridData.answer_time_avg"></td>
                </tr>
            </table>
        </div>
        <div class="notice" data-bind="foreach:{data:$parent.gridDesc,as:'gridDesc'}">
            <p data-bind="text:gridDesc"></p>
        </div>
    </div>
    <#---->
    <div data-bind="text:$root.initDomEvent()"></div>
    <a class="teach-gotop" href="javascript:void(0);"></a>
</script>