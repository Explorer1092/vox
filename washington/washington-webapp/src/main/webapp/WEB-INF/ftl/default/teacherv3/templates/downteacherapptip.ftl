<script id="t:downTeacherApp" type="text/html">
    <div style="position: relative; width: 583px; height: 472px; border: none; background: url(<@app.link href='public/skin/teacherv3/images/hk-popup.png?1.0.1'/>) no-repeat;font-family:'微软雅黑';font-size:18px;color:#514f57;font-weight: 700;line-height: 38px;">
        <a href="javascript:void(0);" id="downTeacherAppClose" style="position: absolute; width:48px;height:48px;right:0; top: 0; border-radius: 50%;"></a>
        <div style="position:absolute;left:34px;top:66px;">
            <%=msg%>。布置使用老师app，布置、检查作业更方便哦~<br />
            还有家校沟通，与班群家长直接在线沟通，轻松好用。
        </div>
    </div>
</script>
<script type="text/javascript">
    (function(){
        function teacherAppPopup(msg,module,op,cb,subject){
            $.prompt("<div class='w-ag-center'>" + template("t:downTeacherApp",{msg : msg}) + "</div>", {
                buttons: {},
                prefix : "hk-teacherApp",
                classes : {
                    fade: 'jqifade',
                    close: 'w-hide'
                },
                position: {width: 624},
                loaded : function(){
                    if(module && op){
                        $17.voxLog({
                            module: module,
                            op : op,
                            s0 : subject
                        });
                    }
                    $("#downTeacherAppClose").on("click",function(){
                        cb && cb();
                    });
                }
            });
        }
        $17.homeworkv3 = $17.homeworkv3 || {};
        $17.extend($17.homeworkv3, {
            teacherAppPopup : teacherAppPopup
        });
    }());
</script>