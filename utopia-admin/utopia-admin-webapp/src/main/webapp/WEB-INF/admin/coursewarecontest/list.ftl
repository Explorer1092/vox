<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Wmanageeb ' page_num=3>
<style type="text/css">
        @charset "UTF-8";body,html{height:100%;width:100%}
        body,button,input,select,textarea{font-family:TrebuchetMS,Rotobo,"Microsoft YaHei",sans-serif}
        body,dd,dl,dt,form,h1,h2,h3,h4,h5,h6,html,li,ol,p,select,ul{margin:0;padding:0}
        li,ol,ul{list-style-type:none}
        em,i{font-style:normal}
        a{color:#333}
        a{text-decoration:none}
        a img{border:0 none}
        input:-ms-input-placeholder,input::-moz-placeholder,input::-webkit-input-placeholder{color:#999}
        input:focus:-ms-input-placeholder,input:focus::-moz-placeholder,input:focus::-webkit-input-placeholder{color:transparent}
        button,input[type=button],input[type=email],input[type=number],input[type=password],input[type=phone],input[type=search],input[type=submit],input[type=text],textarea{-webkit-appearance:none;-moz-appearance:none;-webkit-border-image:none;border-image:none;-webkit-border-radius:0;border-radius:0;-webkit-box-sizing:border-box;box-sizing:border-box;outline:0}
        button,input[type=button],input[type=reset],input[type=submit]{cursor:pointer;-webkit-appearance:button}
        button::-moz-focus-inner,input::-moz-focus-inner{border:0;padding:0;outline:0}
        table{border-collapse:collapse;border-spacing:0}
        td{vertical-align:top}
        .hidden{display:none!important;visibility:hidden}
        .visuallyhidden{border:0;clip:rect(0 0 0 0);height:1px;margin:-1px;overflow:hidden;padding:0;position:absolute;width:1px}
        .visuallyhidden.focusable:active,.visuallyhidden.focusable:focus{clip:auto;height:auto;margin:0;overflow:visible;position:static;width:auto}
        .invisible{visibility:hidden}
        .clearfix:after,.clearfix:before{content:"";display:table}
        .clearfix:after{clear:both}
        .clearfix{zoom:1}
        @media print{*{background:0 0!important;color:#000!important;text-shadow:none!important;filter:none!important;-ms-filter:none!important}
        a,a:visited{text-decoration:underline}
        a[href]:after{content:" (" attr(href) ")"}
        abbr[title]:after{content:" (" attr(title) ")"}
        .ir a:after,a[href^="#"]:after,a[href^="javascript:"]:after{content:""}
        blockquote,pre{border:1px solid #999;page-break-inside:avoid}
        thead{display:table-header-group}
        img,tr{page-break-inside:avoid}
        img{max-width:100%!important}
        @page{margin:.5cm}
        h2,h3,p{orphans:3;widows:3}
        h2,h3{page-break-after:avoid}}
        html{background-color:#ebebf3}
        .header{width:100%;min-width:1200px;height:111px;background-color:#49559f}
        .header .inline{width:1200px;margin:0 auto}
        .header .title{font-size:34px;color:#fff;padding-top:30px}
        .header .upload_btn{color:#49559f;font-size:14px;float:right;margin:-30px 0 0 0;background-color:#fff;border:1px solid #fff;border-radius:16px;padding:5px 20px}
        .content_nav{background-color:#343f83;min-width:1200px;height:45px}
        .content_nav .nav{width:1200px;margin:0 auto;color:#fff}
        .content_nav .nav .tab{padding:12px 27px;float:left}
        .content_nav .nav .tab.active{background-color:#01c386}
        .main_content .courseware_list{width:1200px;margin:0 auto}
        .main_content .courseware_list .inner{padding-top:14px;width:1200px;margin:0 auto}
        .main_content .courseware_list .inner .item{background-color:#a8b4d3;min-height:150px;margin-top:20px;clear:both}
        .main_content .courseware_list .inner .item .head_img{float:left;margin:17px}
        .main_content .courseware_list .inner .item .content_desc{margin-top:17px;float:left}
        .main_content .courseware_list .inner .item .content_desc .title{font-size:22px}
        .main_content .courseware_list .inner .item .content_desc .state,.main_content .courseware_list .inner .item .content_desc .update_time{font-size:14px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op{margin-top:24px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .state{font-size:18px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .del_btn,.main_content .courseware_list .inner .item .content_desc .state_and_op .edit_btn,.main_content .courseware_list .inner .item .content_desc .state_and_op .state{float:left}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .edit_btn{margin-left:100px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .edit_btn i.edit{background:url(../images/edit_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .del_btn{margin-left:25px}
        .main_content .courseware_list .inner .item .content_desc .state_and_op .del_btn i.del{background:url(../images/del_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
        .main_content .courseware_list .inner .item .content_desc .update_time{clear:both;padding-top:12px}
        .main_content .courseware_list .inner .item .content_count{float:right;margin-top:17px;margin-right:22px;text-align:right;line-height:28px}
        .main_content .courseware_list .inner .item .content_count .stars .star{position:relative;top:6px;vertical-align:middle;float:left;background:url(../images/one_star.png) no-repeat;display:inline-block;width:15px;height:14px;margin-right:5px}
        .main_content .courseware_list .inner .item .content_count .stars .star.half{background:url(../images/half_star.png) no-repeat}
        .main_content .courseware_list .inner .item .content_count .stars .desc{display:inline-block}
        .main_content .courseware_list .inner .item.more{background:0 0;text-align:center;margin-top:45px;color:#7e84b1}
        .main_content_detail .courseware_list{width:1200px;margin:0 auto}
        .main_content_detail .courseware_list .inner{padding-top:20px;width:1200px;margin:0 auto}
        .main_content_detail .courseware_list .inner .item{background:0 0;min-height:180px;margin-top:20px;clear:both}
        .main_content_detail .courseware_list .inner .item .head_img{float:left}
        .main_content_detail .courseware_list .inner .item .content_desc{margin:20px 0 0 32px;float:left}
        .main_content_detail .courseware_list .inner .item .content_desc .title{font-size:30px}
        .main_content_detail .courseware_list .inner .item .content_desc .state,.main_content_detail .courseware_list .inner .item .content_desc .update_time{font-size:18px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op{margin-top:28px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .state{font-size:20px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .del_btn,.main_content_detail .courseware_list .inner .item .content_desc .state_and_op .edit_btn,.main_content_detail .courseware_list .inner .item .content_desc .state_and_op .state{float:left}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .edit_btn{margin-left:100px;font-size:18px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .edit_btn i.edit{background:url(../images/edit_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .del_btn{margin-left:25px;font-size:18px}
        .main_content_detail .courseware_list .inner .item .content_desc .state_and_op .del_btn i.del{background:url(../images/del_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
        .main_content_detail .courseware_list .inner .item .content_desc .update_time{clear:both;padding-top:12px}
        .main_content_detail .courseware_list .inner .item .content_count{float:right;margin-top:17px;margin-right:22px;text-align:left;line-height:28px}
        .main_content_detail .courseware_list .inner .item .content_count .stars .star{position:relative;top:6px;vertical-align:middle;float:left;background:url(../images/one_star.png) no-repeat;display:inline-block;width:15px;height:14px;margin-right:5px}
        .main_content_detail .courseware_list .inner .item .content_count .stars .star.half{background:url(../images/half_star.png) no-repeat}
        .main_content_detail .courseware_list .inner .item .content_count .stars .desc{display:inline-block}
        .course{min-width:1200px}
        .course .per_view_content{width:1200px;background-color:#fff;margin:0 auto;padding:22px;margin-bottom:32px}
        .course .per_view_content .per_view_header{margin-bottom:20px}
        .course .per_view_content .title{font-size:22px}
        .course .per_view_content .tips{float:right;font-size:14px;margin-top:-22px}
        .course .per_view_content .per_view_box{width:100%;height:338px;border:4px dashed #eee}
        .course .per_view_content .per_view_box .view_icon{text-align:center;margin-top:115px;color:#c7cedb}
        .course .teach_design{width:1200px;margin:0 auto;padding:22px;background-color:#f0f0f9}
        .course .teach_design .title{font-size:22px;margin-bottom:20px}
        .course .teach_design .desc{margin:0 auto}
        .course .teach_design .desc textarea{background-color:#dbe0ee;border:none;resize:none;display:block;width:100%;height:143px;font-size:14px}
    </style>
<div id="main_container" class="span9">
    <legend>课件大赛审核</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="/crm/courseware/contest/examine/workslist.vpage">
                <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    年级：
                    <select id="clazzLevel" name="clazzLevel" style="font-size: 12px;width: 100px;">
                        <option value="0" <#if clazzLevel ?? || clazzLevel == 0> selected</#if>>全部</option>
                        <option value="1" <#if clazzLevel ?? && clazzLevel == 1> selected</#if>>一年级</option>
                        <option value="2" <#if clazzLevel ?? && clazzLevel == 2> selected</#if>>二年级</option>
                        <option value="3" <#if clazzLevel ?? && clazzLevel == 3> selected</#if>>三年级</option>
                        <option value="4" <#if clazzLevel ?? && clazzLevel == 4> selected</#if>>四年级</option>
                        <option value="5" <#if clazzLevel ?? && clazzLevel == 5> selected</#if>>五年级</option>
                        <option value="6" <#if clazzLevel ?? && clazzLevel == 6> selected</#if>>六年级</option>
                    </select>
                    册别：
                    <select id="term" name="term" style="font-size: 12px;width: 100px;">
                        <option value="0" <#if term ?? && term == 0> selected</#if>>全部</option>
                        <option value="1" <#if term ?? && term == 1> selected</#if>>上册</option>
                        <option value="2" <#if term ?? && term == 2> selected</#if>>下册</option>
                    </select>
                    状态：
                    <select id="examineStatus" name="examineStatus" style="font-size: 12px;width: 100px;">
                        <#if examineStatusList?has_content>
                            <#list examineStatusList as s>
                                <option value="${s.name()!}" <#if examineStatus ?? && examineStatus == s.name()> selected</#if>>${s.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>

                    课件标题：
                    <input type="text" name="title" style="height: 29px;" placeholder="模糊搜索" <#if title ?? && title != ''> value=${title} </#if> >
                    更新开始时间： <input id="startDate" name="startDate" style="height: 29px;" class="input-medium" type="text" <#if startDate ?? && startDate != ''> value="${startDate}" </#if> readonly />
                    结束时间： <input id="endDate" name="endDate" style="height: 29px;" class="input-medium" type="text" <#if endDate ?? && endDate != ''> value="${endDate}" </#if> readonly />
                    <button class="btn btn-primary">查 询</button>
                </form>

            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>课件标题</td>
                <td>提交人</td>
                <td>更新时间</td>
                <td>状态</td>
                <td>审核人</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.title!}</td>
                        <td>${e.teacher!}</td>
                        <td>${e.date!}</td>
                        <td>${e.examineStatusDesc!}</td>
                        <td>${e.examiner!}</td>
                        <td>
                            <button type="button" name="detail" data-id = "${e.id!}" class="btn btn-primary">详情</button>
                        </td>
                    </tr>
                </#list>
                 <#else ><tr><td colspan="6"><strong>暂无数据</strong></td> </tr>
            </#if>


        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页</li>
    </ul>


  <!-- Modal -->
      <div class="modal fade hide" id="courseware_detail" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"style="display: block;width: 1300px;position: relative;">
          <div class="modal-dialog" role="document">
              <div class="modal-content">
                  <div class="modal-header">
                      <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                      <h4 class="modal-title" id="myModalLabel" style="text-align:center;">课件详情</h4>
                      <input type="hidden" id="detail_id"/>
                  </div>
                  <div class="modal-body">
                     <div class="main_content_detail">
                         <div class="courseware_list">
                             <div class="inner">
                                 <div class="item">
                                     <div class="head_img">
                                         <img  alt="" width="230px" height="160px;" id = "detail_bookImage">
                                     </div>
                                     <div class="content_desc">
                                         <div class="title"><span id="detail_title"></span></div>
                                         <div class="state_and_op">
                                             <div class="state">状态：<span id="detail_statusDesc"  name="detail"></span></div>
                                         </div>
                                         <div class="update_time">更新时间：<span class="time" id="detail_date" name="detail"></span></div>
                                     </div>
                                     <div class="content_count">
                                         <div class="desc">册别：<span id="detail_term"  name="detail"></span></div>
                                         <div class="desc">年级：<span id="detail_clazzlevel" name="detail"></span></div>
                                         <div class="desc">教材：<span id="detail_bookName" name="detail"></span></div>
                                         <div class="desc">单元：<span id="detail_unitName" name="detail"></span></div>
                                     </div>
                                 </div>
                             </div>
                         </div>
                         <div class="course">
                             <div class="per_view_content">
                                 <div class="per_view_header">
                                     <div class="title"><a id="detailFile">教学课件</a></div>
                                 </div>
                                 <div class="per_view_box">
                                     <div class="view_icon">
                                     </div>
                                 </div>
                             </div>
                             <div class="teach_design">
                                 <div class="title">教学设计</div>
                                 <div class="desc"><span id="detail_description"  name="detail"></span></div>
                             </div>
                         </div>
                     </div>
                  </div>
              </div>
          </div>
      </div>


    <script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }
        $(function () {
            $("#startDate,#endDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                }
            });

            $('button[name=detail]').on('click', function () {
                var dataId = $(this).attr("data-id");
                $.ajax({
                    type: "get",
                    url: "/crm/courseware/contest/works/detail.vpage",
                    data: {
                        id: dataId,
                    },
                    success: function (data) {
                        if (data.success) {
                            for(var e in data) {
                                if (e == 'id') {
                                    $("#detail_id").val(data[e]);
                                    continue;
                                }
                                $("#detail_"+ e).text("");
                                if (e == 'term') {
                                    if (data[e] == 1) {
                                       $("#detail_"+ e).text("上册");
                                    }

                                    if (data[e] == 2) {
                                       $("#detail_"+ e).text("下册");
                                    }
                                    continue;
                                }
                                $("#detail_"+ e).text(data[e]);
                            }
                            if (data.image != null && data.image != '') {
                                $("#detail_bookImage").attr("src", data.image);
                            }
                            if (data.coursewareFile != null && data.coursewareFile != '') {
                                $("#detailFile").attr("href", data.coursewareFile);
                            }
                            $('#courseware_detail').modal('show');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
        });
    </script>
</@layout_default.page>