<#macro send_message>
<div style='display:none'>
    <script type="text/html" id="t:给家长留言">
        <div id="teacher_sendTo_student" class="sms_info_box" style="width:550px;">
            <div class="content_box teacher_ms">

                <div class="select_in" style="width:280px;">
                    <p>
                        请选择班级：
                        <select id="sendLetterToStudent_parent" class="int_vox">
                            <option value="-1">--请选择--</option>
                            <%for(var i in clazzList){%>
                            <option value="<%=clazzList[i].id%>"><%=clazzList[i].className%></option>
                            <%}%>
                        </select>
                    </p>
                    <div class="sms_us_list">
                        <ul id="sendLetterStudents1_parent"></ul>
                        <div class="w-clear"></div>
                    </div>
                    <p id="select_all_students" style="display:none;">
                        <label><input name="students_parent" type="checkbox" value="" />全选</label>
                    </p>
                </div>
                <div class="select_be"  style="width:250px;float:right;">
                    <p>已选择家长：</p>
                    <div class="sector_box">
                        <p id="sector1_parent"></p>
                    </div>
                    <div class="content_ms">
                        <p>
                            <textarea id="sendLetterContent_parent" name="sendLetterContent_parent" class="int_vox" cols="" rows=""></textarea>
                        </p>
                        <p style=" color:#999;">留言内容：<span id="sendLetterContentLength_parent">0</span>/140</p>
                    </div>
                </div>
            </div>
            <div class="mes_show_box_parent"></div>
            <div class="w-clear"></div>
            <div class="show_error_box_parent"></div>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            /*选择留言学生*/
            $('#sendLetterStudents1_parent li').live("click", function(){
                var _this    = $(this);
                var id       = _this.data('receiver_id');
                var realName = _this.data('receiver_name');
                var sector1_parent = $('#sector1_parent');
                var span = $('<span class="receiver_parent" id='+id+'>'+realName+'，</span>');
                if(_this.find('b').hasClass('selected')){
                    _this.find('b').removeClass('selected');
                    sector1_parent.find('#'+id+'').remove();
                    $("input:checkbox[name='students_parent']").attr("checked", false);
                }else{
                    _this.find('b').addClass('selected');
                    sector1_parent.append(span);
                    if ( $('#sendLetterStudents1_parent li b.selected').length == $('#sendLetterStudents1_parent li').length ) {
                        $("input:checkbox[name='students_parent']").attr("checked", true);
                    }
                }
            });

            /*选择班级*/
            $('#sendLetterToStudent_parent').live('change', function(){
                var clazzId = $(this).val();
                if(clazzId == -1) return false;
                $("#sector1_parent").html("");
                $('#select_all_students').show();
                $("input:checkbox[name='students_parent']").attr("checked", false);
                $("#sendLetterStudents1_parent").html('<div class="text_center" style="padding: 20px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>').load("/teacher/message/parent/clazzstudents.vpage?clazzId="+clazzId);
            });

            /*全选*/
            $("input:checkbox[name='students_parent']").live('click',function(){
                var _cthis = $(this);
                $('#sendLetterStudents1_parent li').each(function(){
                    var _this = $(this);
                    if ( _cthis.attr("checked") ) {
                        if( !_this.find('b').hasClass('selected') ){
                            _this.trigger('click');
                        }
                    } else {
                        if( _this.find('b').hasClass('selected') ){
                            _this.trigger('click');
                        }
                    }
                })
            });

            /*留言字数*/
            $('#sendLetterContent_parent').live("keyup", function(){
                $("#sendLetterContentLength_parent").text($(this).val().length);
            });
        });
    </script>
</div>
</#macro>
