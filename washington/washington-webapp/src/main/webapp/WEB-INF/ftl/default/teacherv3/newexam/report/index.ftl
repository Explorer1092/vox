<#import "../../../tts/module.ftl" as temp />
<@temp.page level="测试报告">
<@sugar.capsule js=["ko"] css=["new_teacher.message"] />
<div class="tts-title-box">
    <div class="inner">
        <div class="font">
            <p>历史与报告</p>
        </div>
        <div class="tts-clear"></div>
    </div>
</div>
<div class="tts-contain">
    <div class="inner" id="newexamHistory">
        <div style="margin-bottom: 20px;">
            <#--<div style="float: right;">
                <a href="${ProductConfig.getUcenterUrl()!''}/kuailexue.vpage" class="w-btn w-btn-mini" style="width: 155px;">制作答题卡及阅卷入口</a>
            </div>-->

            <div id="clazzLevelSel" class="w-select" style="font-size: 14px;">
                <div class="current" data-bind="click:currentLevelClick">
                    <span class="content" data-bind="text:focusLevel() + '年级'"></span>
                    <span class="w-icon w-icon-arrow"></span>
                </div>
                <ul data-bind="attr:{'data-size':levelList.length},style:{display: showLevelList() ? 'block' : 'none'}">
                    <!--ko foreach:levelList-->
                    <li data-bind="click:$root.levelClick"><a href="javascript:void(0);" data-bind="text:$data + '年级'"></a></li>
                    <!--/ko-->
                </ul>
            </div>
            <div class="w-select" style="font-size: 14px;">
                <div class="current" data-bind="click:currentClazzClick">
                    <span class="content" data-bind="text:focusClazz().className"></span>
                    <span class="w-icon w-icon-arrow"></span>
                </div>
                <ul data-bind="attr:{'data-size':clazzList().length},style:{display: showClazzList() ? 'block' : 'none'}">
                    <!--ko foreach:{data : clazzList,as:'clazz'}-->
                        <li data-bind="click:$root.clazzClick"><a href="javascript:void(0);" data-bind="text:clazz.className"></a></li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <div class="tts-table" id="historyList">
            <!--ko if:showLoadPic()-->
            <div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>
            <!--/ko-->

            <!--ko if:!showLoadPic() -->
            <table>
                <thead>
                <tr>
                    <th>测试名称</th>
                    <th style="width: 190px;">测试时间</th>
                    <th style="width: 150px;">班级</th>
                    <th style="width: 80px;">已考人数</th>
                    <th style="width: 80px;">未考人数</th>
                    <th style="width: 80px;">操作</th>
                </tr>
                </thead>
                <tbody>
                    <!--ko if:historyList().length > 0-->
                        <!--ko foreach:{data:historyList(),as:'report'}-->
                        <tr data-bind="css:{'odd':$index()%2 == 0}">
                            <td data-bind="text:report.newExamName"></td>
                            <td data-bind="text:report.startAt + '至' + report.stopAt"></td>
                            <td data-bind="text:report.clazzName"></td>
                            <td>
                                <!--ko if:report.joinUsers.length > 0-->
                                <a href="javascript:void(0);" data-bind="text:report.joinUsers.length,click:$root.viewStudents.bind($data,report.joinUsers,'已考人数')"></a>
                                <!--/ko-->
                                <!--ko ifnot:report.joinUsers.length > 0-->
                                <span>0</span>
                                <!--/ko-->
                            </td>
                            <td>
                                <!--ko if:report.unJoinStudents && report.unJoinStudents.length > 0-->
                                <a href="javascript:void(0);" data-bind="text:report.unJoinStudents.length,click:$root.viewStudents.bind($data,report.unJoinStudents,'未考人数')"></a>
                                <!--/ko-->
                                <!--ko ifnot:report.unJoinStudents && report.unJoinStudents.length > 0-->
                                <span>0</span>
                                <!--/ko-->
                            </td>
                            <td><a href="javascript:void(0);" data-bind="click:$root.viewReport.bind($data)">详细报告</a></td>
                        </tr>
                        <!--/ko-->
                    <!--/ko-->
                    <!--ko if:historyList().length == 0-->
                    <tr>
                        <td colspan="6" data-bind="text:showMessage()"></td>
                    </tr>
                    <!--/ko-->
                </tbody>
            </table>
            <!--/ko-->
        </div>
        <div class="message_page_list" id="newexamPage">
        </div>
    </div>
</div>
<script id="t:爱发信息的老师" type="text/html">
    <div id="teacher_sendTo_student" class="sms_info_box teacher_new_ms">
        <div class="content_box teacher_ms">
            <div class="select_in" style="width:100%;">
                <div class="select_ne_box">
                    <div class="sms_us_list" style="width: 100%; height: auto;">
                        <div class="sms_us_title w-magB-10 w-magT-10 w-magL-10" style="display: none;">
                            完成作业的同学
                        </div>
                        <ul class="v-students">
                            <% for(var i = 0; i < list.length; i++){ %>
                            <li>
                                <label>
                                    <b>
                                        <%if(list[i].studentImg && list[i].studentImg != ''){%>
                                        <img src="<@app.avatar href='/'/><%= list[i].studentImg %>" width="60" height="60" onerror="this.onerror='';this.src='<@app.avatar href=''/>'">
                                        <%}else{%>
                                        <img src="<@app.avatar href=''/>" width="60" height="60">
                                        <%}%>
                                    </b>
                                    <span><%= list[i].studentName %></span>
                                </label>
                            </li>
                            <% } %>
                        </ul>
                        <div class="w-clear"></div>
                    </div>
                </div>
            </div>
            <div class="select_be" style="width:99%;display:none;">
                <div class="content_ms">
                    <p>
                        <textarea id="sendLetterContent" style="width: 100%;" name="sendLetterContent_parent" class="int_vox" cols="" rows=""></textarea>
                    </p>
                    <p id="wordLimitBox" style=" color:#999; text-align: right;">留言内容：还可以输入50个字</p>
                </div>
            </div>
        </div>
        <div class="mes_show_box_parent"></div>
        <div class="w-clear"></div>
        <div id="show_error_box" class="w-red"></div>
    </div>
</script>
<script type="text/javascript">
    var newexamHistory;
    $(function () {
        var clazzLevelMap = <#if clazzLevelMap?has_content>${clazzLevelMap!{}}<#else>{}</#if>;
        var env = <@ftlmacro.getCurrentProductDevelopment />;
        function NewExamHistory(options){
            var self = this;
            options = options || {};
            self.subject = options.subject || null;
            self.clazzLevelRef = options.clazzLevelMap || {};
            self.focusLevel = ko.observable(null);
            self.levelList = [];
            self.showLevelList = ko.observable(false);
            self.focusClazz = ko.observable({});
            self.clazzList = ko.observableArray([]);
            self.showClazzList = ko.observable(false);
            self.historyList = ko.observableArray([]);
            self.showLoadPic = ko.observable(true);
            self.showMessage = ko.observable("");
            self.currentLevelClick = function(){
                self.showLevelList(!self.showLevelList());
            };
            self.levelClick = function(){
                self.setClazz(this);
                self.showLevelList(false);
            };
            self.currentClazzClick = function(){
                self.showClazzList(!self.showClazzList());
            };
            self.clazzClick = function(){
                self.focusClazz(this);
                self.showClazzList(false);
                self.loadHistroyList(1);
            };
            self.setClazz = function(level){
                var _clazzList = self.clazzLevelRef[level];
                self.clazzList(_clazzList);
                self.focusClazz(_clazzList[0]);
                self.focusLevel(level);
                self.loadHistroyList(1);
            };
            self.loadHistroyList = function(pageNo){
                self.showLoadPic(true);
                self.historyList([]);
                try{
                    var paramObj = {
                        clazzId : self.focusClazz().id,
                        subject : self.subject,
                        currentPage : pageNo - 1
                    };
                    $.post("/teacher/newexam/report/list.vpage",paramObj,function(data){
                        if(data.success){
                            var _pageAble = data.pageable || {};
                            var _result = _pageAble.content || [];
                            if(_result.length > 0){
                                self.historyList(_result);
                                setTimeout(function(){
                                    $("#newexamPage").page({
                                        total         : _pageAble.totalPages,
                                        current       : pageNo,
                                        showTotalPage : false,
                                        jumpCallBack  : function (index) {
                                            self.loadHistroyList(index);
                                        }
                                    });
                                },200);
                            }else{
                                self.showMessage("您所在的区域本学期没有测试");
                                $("#newexamPage").hide();
                            }
                            $17.voxLog({
                                module : "m_yJO2o3u3",
                                op     : "o_KSgA5MRn",
                                s0     : self.subject,
                                s1     : _result.length > 0
                            });
                        }else{
                            self.showMessage(data.info || "后台请求失败，请稍后重试");
                            data.errorCode !== "200" && $17.voxLog({
                                module : 'API_REQUEST_ERROR',
                                op     : 'API_STATE_ERROR',
                                s0     : '/teacher/newexam/report/list.vpage',
                                s1     :  JSON.stringify(data),
                                s2     :  JSON.stringify(paramObj),
                                s3     : env
                            });
                        }
                        self.showLoadPic(false);
                    });
                }catch(e){
                    self.showMessage("网络错误，请刷新重试。错误信息:" + e.message);
                    self.showLoadPic(false);
                }
            };
            self.viewStudents = function(userIds,title){
                var that = this;
                if(!$.isArray(userIds) || userIds.length == 0){return false;}

                $.post("/teacher/newexam/report/user.vpage",{userIds : userIds.toString()},function(data){
                    if(data.success){
                        $.prompt(template("t:爱发信息的老师",{list : data.users || []}),{
                            title   : title || "查看",
                            focus   : 1,
                            buttons : { "知道了": false },
                            submit  : function(e,v,f,m){}
                        });
                    }
                });

                $17.voxLog({
                    module : "m_yJO2o3u3",
                    op     : title === "已考人数" ? "o_rifYmshj" : (title === "未考人数" ? "o_o3MTqZGm" : ""),
                    s0     : that.newExamId,
                    s1     : userIds.length,
                    s2     : title
                });
            };
            self.viewReport = function(){
                var report = this;
                $17.voxLog({
                    module : "m_yJO2o3u3",
                    op     : "o_0rdG8qOn",
                    s0     : self.subject,
                    s1     : report.newExamId
                });
                if(typeof report.banView === "boolean" && report.banView){
                    $17.alert(report.banReason || '暂不允许查看详情');
                    return false;
                }
                setTimeout(function(){
                    location.href = '/teacher/newexam/report/detail.vpage?' + $.param({
                                clazzId : self.focusClazz().id,
                                newExamId : report.newExamId
                            });
                },200);
            };
            self.init = function(){
                var levelArr = [];
                for(var level in self.clazzLevelRef){
                    if(self.clazzLevelRef.hasOwnProperty(level)){
                        levelArr.push(level);
                    }
                }
                if(levelArr.length > 0){
                    self.levelList = levelArr;
                    self.setClazz(levelArr[0]);
                }
            };
            self.init();
            newexamHistory = self;
        }
        ko.applyBindings(new NewExamHistory({clazzLevelMap:clazzLevelMap,subject : $17.getQuery("subject")}),document.getElementById("newexamHistory"));
    });
</script>
</@temp.page>