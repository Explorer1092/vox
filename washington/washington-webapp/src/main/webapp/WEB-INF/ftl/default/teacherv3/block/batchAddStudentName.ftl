<script type="text/html" id="T:导入班级名单列表">
<#--给学生账号导入姓名-->
    <div class="w-base" style="margin:-40px -20px -20px; border: none;">
        <div class="w-base-switch w-base-two-switch" id="classMapsList">
            <ul>
                <%for(var i = 0, len = classMaps.length; i < len; i++){%>
                    <%if(i < 8){%>
                    <li style="width: 97px;" data-id="<%=classMaps[i].clazzId%>" title="<%=classMaps[i].clazzName%>" <%if(classMaps[i].clazzId == currentId){%>class="active"<%}%>>
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        <%=classMaps[i].clazzName%>
                    </a>
                    </li>
                    <%}%>
                <%}%>
            </ul>
        </div>
        <div class="t-addstudent-poput" style="margin: 0;">
            <div class="t-student-create">
                <div class="main">
                    <div class="info">
                        <div class="box" style="margin:10px 20px;">
                            输入学生姓名，给每个学生生成一张使用说明，指导学生加入班级做作业
                        </div>
                    </div>
                    <div class="text" style="width: 240px; margin-left: 20px; position: relative; float: left; height:232px;">
                        <label style="position: absolute; left: 15px; top: 10px; color:#999; line-height: 18px;" class="batch_student_text" for="all_batch_student_name">
                            请输入学生姓名：<br/>
                            张兰<br/>
                            李想<br/>
                            王小小<br/>
                            (注意：每次只能导入一个班级)
                        </label>
                        <textarea id="all_batch_student_name" class="w-int" style="width: 91%; height:210px;"></textarea>
                    </div>
                    <div class="t-important-clazz-info">
                        怎么把手中的Excel花名册学生名单导进来？<a href="http://help.17zuoye.com/?p=371" target="_blank" class="w-blue">点我帮你</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div style="clear:both;"></div>
</script>
<script type="text/html" id="T:导入班级名单成功列表">
    <div class='w-ag-center'>导入成功！现在您可以下载有姓名的名单发给学生了。</div>
    <div class='w-ag-center' style='margin:10px;'>需导入其它班级请点击首页“导入班级名单”</div>
    <div class="w-table w-table-border ">
        <table>
            <thead>
            <tr>
                <th style="width: 20%;">序号</th>
                <th style="width: 25%;">姓名</th>
                <th style="width: 25%;">学号</th>
                <th >密码</th>
            </tr>
            </thead>
        </table>
        <div <%if(rows.length > 5){%>style="height: 212px; overflow: hidden; overflow-y: auto; position: relative;"<%}%>>
        <table>
            <tbody>
            <%for(var i = 0; i < rows.length; i++){%>
            <tr>
                <th style="width: 20%;"><%=(i+1)%></th>
                <th style="width: 25%;"><%=rows[i].username%></th>
                <th style="width: 25%;"><%=rows[i].userId%></th>
                <th><%=rows[i].pwd%></th>
            </tr>
            <%}%>
            </tbody>
        </table>
    </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var currentId;
        //选择班级
        $(document).on("click", "#classMapsList li", function(){
            var $this = $(this);

            currentId = $this.data("id")
            $this.addClass("active").siblings().removeClass("active");
            $("#all_batch_student_name").val("");
        });

        //导入班级名单列表
        $(document).on("click", ".data-ImportStudentName", function(){
            var $this = $(this);
            var clazzIdsPublicCurrentAdd = [];
            var _tempClassMaps = [];
            var _tempClazzIds = $this.attr("data-clazzid");
            var creatorType = $this.attr("data-creatorType");

            currentId = $this.data("id");
            $.get("/teacher/xxt/clazzinfo.vpage", {}, function(data){
                if(data.success){
                    if(!$17.isBlank(_tempClazzIds)){
                        if(isNaN(_tempClazzIds)){
                            clazzIdsPublicCurrentAdd = _tempClazzIds.split(",");
                        }else{
                            clazzIdsPublicCurrentAdd.push(_tempClazzIds.toString());
                        }
                    }

                    if(clazzIdsPublicCurrentAdd.length > 0){
                        for(var i = 0, clazzMaps = data.clazzMaps; i < clazzMaps.length; i++){
                            if($.inArray(clazzMaps[i].clazzId.toString(), clazzIdsPublicCurrentAdd) > -1){
                                _tempClassMaps.push(clazzMaps[i]);
                            }
                        }
                    }else{
                        _tempClassMaps = data.clazzMaps;
                    }

                    if(currentId == undefined && _tempClassMaps.length > 0){
                        currentId = _tempClassMaps[0].clazzId;
                    }

                    $.prompt(template("T:导入班级名单列表", {classMaps : _tempClassMaps, currentId : currentId}), {
                        title: "导入学生名单",
                        focus : 1,
                        buttons: { "取消": false,"确定": true },
                        position:{width : 780},
                        loaded : function(){
                            $(document).on("focus", "#all_batch_student_name", function(){
                                $(this).siblings(".batch_student_text").hide();
                            });

                            $(document).on("keyup", "#all_batch_student_name", function(){
                                $(this).siblings(".init").remove();
                            });
                        },
                        submit : function(e, v){
                            if(v){
                                var itemName = $("#all_batch_student_name");
                                var _userNames = $.trim(itemName.val()).split('\n');
                                for(var i = 0; i < _userNames.length; i++){
                                    _userNames[i] = $.trim(_userNames[i]);
                                    if(_userNames[i] == ''){
                                        _userNames.splice(i, 1);
                                    }
                                }

                                if(_userNames.length < 1){
                                    itemName.after("<p class='init' style='line-height: 20px;'>请添加您要上传的学生名单</p>")
                                    return false;
                                }

                                var userNamesStr = _userNames.join(",");

                                $("body").append("<iframe class='vox17zuoyeIframe' style='display:none;' src='/clazz/downloadstudentnames.vpage?clazzId=${clazzId!}&userNames="+userNamesStr+"'/>");
                                $17.voxLog({
                                    module : "downloadStudentAccount",
                                    op : "import"
                                });

                                downloadSuccess("/clazz/downloadstudentnames.vpage?clazzId=${clazzId!}&userNames=" + userNamesStr);
                            }
                        }
                    });
                }else{
                    $17.alert(data.info);
                }
            });
        });


        //下载成功提示
        function downloadSuccess(url){
            setTimeout(function(){
                $.prompt("<div style='font-size: 18px; text-align: center;'>导入成功！请打印刚下载的《一起作业注册指南》发给学生</div>", {
                    title : "导入学生名单",
                    buttons : {"确定" : true},
                    submit : function(e, v){
                        if(v){
                            location.reload();
                        }
                    },
                    close : function() {
                        location.reload();
                    }
                });
            }, 500);
        }
    });
</script>