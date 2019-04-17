<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">系列管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>系列ID&nbsp;
                        <input type="text" id="seriesId" name="seriesId" value="${seriesId!''}"/>
                    </label>
                </li>
                <li>
                    <label>系列名称&nbsp;
                        <input type="text" id="name" name="name" value="${name!''}" />
                    </label>
                </li>
                <li>
                    <label>系列难度级别&nbsp;
                        <input type="text" id="level" name="level" value="${level!''}" />
                    </label>
                </li>
                <li>
                    <label>学科名称&nbsp;
                        <select id="subject" name="subject">
                            <option value="ALL">全部</option>
                            <option value="CHINESE" <#if subject?? && subject == 'CHINESE'>selected</#if>>语文</option>
                            <option value="ENGLISH" <#if subject?? && subject == 'ENGLISH'>selected</#if>>英语</option>
                            <option value="MATH" <#if subject?? && subject == 'MATH'>selected</#if>>数学</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>系列类型&nbsp;
                        <select id="seriesType" name="seriesType">
                            <option value="-1">全部</option>
                            <option value="1" <#if seriesType?? && seriesType == 1>selected</#if>>古诗</option>
                            <option value="2" <#if seriesType?? && seriesType == 2>selected</#if>>英语绘本</option>
                            <option value="3" <#if seriesType?? && seriesType == 3>selected</#if>>论语</option>
                            <option value="4" <#if seriesType?? && seriesType == 4>selected</#if>>史记</option>
                            <option value="5" <#if seriesType?? && seriesType == 5>selected</#if>>成语故事</option>
                            <option value="6" <#if seriesType?? && seriesType == 6>selected</#if>>诸子百家</option>
                            <option value="7" <#if seriesType?? && seriesType == 7>selected</#if>>传统节日</option>
                            <option value="8" <#if seriesType?? && seriesType == 8>selected</#if>>西方节日</option>
                            <option value="9" <#if seriesType?? && seriesType == 9>selected</#if>>语文绘本</option>
                            <option value="10" <#if seriesType?? && seriesType == 10>selected</#if>>语文阅读</option>
                            <option value="11" <#if seriesType?? && seriesType == 11>selected</#if>>三十六计</option>
                            <option value="12" <#if seriesType?? && seriesType == 12>selected</#if>>中国神话</option>
                            <option value="13" <#if seriesType?? && seriesType == 13>selected</#if>>水浒传</option>
                            <option value="14" <#if seriesType?? && seriesType == 14>selected</#if>>数学编程</option>
                            <option value="15" <#if seriesType?? && seriesType == 15>selected</#if>>趣味故事</option>
                            <option value="16" <#if seriesType?? && seriesType == 16>selected</#if>>数学优等生</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>创建人&nbsp;
                        <input type="text" id="createUser" name="createUser" value="${createUser!''}"/>
                    </label>
                </li>
                <li><button type="button" class="btn btn-primary" id="searchBtn">查询</button></li>
            </ul>
        </div>
    </form>

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增系列</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>系列ID</th>
                        <th>系列名称</th>
                        <th>学科名称</th>
                        <th>课程结构类型</th>
                        <th>系列类型</th>
                        <th>系列难度级别</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as series>
                            <tr>
                                <td>${series.id!''}</td>
                                <td>${series.name!''}</td>
                                <td>
                                    <#if series?? && series.subject?? && series.subject == 'CHINESE'>语文
                                    <#elseif series?? && series.subject?? && series.subject == 'ENGLISH'>英语
                                    <#elseif series?? && series.subject?? && series.subject == 'MATH'>数学
                                    </#if>
                                </td>
                                <td>
                                    <#if series?? && series.courseType?? && series.courseType == 1>语文古文
                                    <#elseif series?? && series.courseType?? && series.courseType == 2>英语绘本
                                    <#elseif series?? && series.courseType?? && series.courseType == 3>语文阅读
                                    <#elseif series?? && series.courseType?? && series.courseType == 5>数学编程
                                    <#elseif series?? && series.courseType?? && series.courseType == 6>语文故事
                                    <#elseif series?? && series.courseType?? && series.courseType == 7>组件化课程
                                    </#if>
                                </td>
                                <td>
                                    <#if series?? && series.seriesType?? && series.seriesType == 1>古诗
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 2>英语绘本
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 3>论语
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 4>史记
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 5>成语故事
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 6>诸子百家
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 7>传统节日
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 8>西方节日
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 9>语文绘本
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 10>语文阅读
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 11>三十六计
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 12>中国神话
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 13>水浒传
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 14>数学编程
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 15>趣味故事
                                    <#elseif series?? && series.seriesType?? && series.seriesType == 16>数学优等生
                                    </#if>
                                </td>
                                <td>${series.level!''}</td>
                                <td>
                                    <#if series.envLevel?? && series.envLevel == 10>单元测试
                                    <#elseif series.envLevel?? && series.envLevel == 20>开发环境
                                    <#elseif series.envLevel?? && series.envLevel == 30>测试环境
                                    <#elseif series.envLevel?? && series.envLevel == 40>预发布环境
                                    <#elseif series.envLevel?? && series.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${series.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${series.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${series.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${series.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="9" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list"></ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {

        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        $(document).on('click',".js-couponOption",function () {
            var $this = $(this),
                    type = $this.data('type'),
                    cid = $this.data('cid'),
                    mapLink = {
                        'add' : '',
                        'edit': '?seriesId=' + cid,
                        'info': '?seriesId=' + cid,
                        'logs': '?seriesId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'info.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'logs.vpage' + mapLink[type];
            } else if (type === "edit") {
                url = 'details.vpage' + mapLink[type];
            } else {
                url = 'details.vpage';
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>