<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加修改事项' page_num=6>
<style>
    .packet_item .control-group {display: inline-block;}
    .packet_item .controls {margin-left: 0;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>修改事项</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content school_manage">
            <form class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">类型</label>
                    <div class="controls">
                        <select id="typeId" class="js-postData form-control js-needed" data-einfo="请选择类型" name="typeId" style="width: 280px;">
                            <option value="">请选择</option>
                            <#if dataList?has_content && dataList?size gt 0>
                                <#list dataList as list>
                                    <option value="${list.id!''}" <#if (list.id!0) == (selfHelp.typeId!0)> selected</#if>>${list.typeName!''}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">事项名称</label>
                    <div class="controls">
                        <input name="title" id="title" maxlength="15" value="${selfHelp.title!''}" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写事项名称">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">联系人姓名</label>
                    <div class="controls">
                        <input id="contact" name="contact" maxlength="10" value="${selfHelp.contact!''}" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写联系人姓名">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">邮箱</label>
                    <div class="controls">
                        <input id="email" name="email" maxlength="30" value="${selfHelp.email!''}" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写邮箱">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">微信群</label>
                    <div class="controls">
                        <input id="wechatGroup" name="wechatGroup" maxlength="20" value="${selfHelp.wechatGroup!''}" class="js-postData input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">说明</label>
                    <div class="controls">
                        <textarea name="comment" id="comment" maxlength="200" class="js-postData input-xlarge focused" cols="30" rows="5">${selfHelp.comment?replace('<br>','\n')}</textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">相关资料</label>
                    <div class="controls">
                        <p><input type="button" class="btn btn-primary addPacket" value="添加资料"></p>
                        <div class="packetList">

                        </div>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                    <button type="button" class="btn btn-primary submitBtn" data-info="saveData.vpage">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>

<#--添加资料包模板-->
<script id="packetAdd" type="text/html">
    <#list packets as p>
    <%var packetList = []%>
    <div class="packet_item" style="width: 100%;">
        <div class="control-group">
            <div class="controls">
                <select class="first">
                    <option value="">请选择</option>
                    <%for(var i = 0; i< res.length; i++){%>
                    <%var data = res[i]%>
                        <%if('${p.datumType!}' == data.packetTypeName){%>
                        <% packetList = data.packetList%>
                        <option value="<%=data.packetTypeId%>" selected><%=data.packetTypeName%></option>
                        <%}else{%>
                        <option value="<%=data.packetTypeId%>"><%=data.packetTypeName%></option>
                        <%}%>
                    <%}%>
                </select>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <select class="second">
                    <option value="">请选择</option>
                    <%for(var i = 0; i< packetList.length; i++){%>
                    <%var data = packetList[i]%>
                    <option value="<%=data.packetId%>" <%if("${p.contentId!0}" == data.packetId){%>selected<%}%> ><%=data.packetTitle%></option>
                    <%}%>
                </select>
            </div>
        </div>
        <a class="btn btn-warning packetDelBtn">删除</a>
    </div>
    </#list>
</script>

<#--添加资料包模板2-->
<script id="packetAdd2" type="text/html">
    <div class="packet_item" style="width: 100%;">
        <div class="control-group">
            <div class="controls">
                <select class="first">
                    <option value="">请选择</option>
                    <%for(var i = 0; i< res.length; i++){%>
                    <%var data = res[i]%>
                    <option value="<%=data.packetTypeId%>"><%=data.packetTypeName%></option>
                    <%}%>
                </select>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <select class="second">
                    <option value="">请选择</option>
                </select>
            </div>
        </div>
        <a class="btn btn-warning packetDelBtn">删除</a>
    </div>
</script>
<script id="packetListAdd2" type="text/html">
    <option value="">请选择</option>
    <%for(var i = 0; i< res.length; i++){%>
    <%var data = res[i]%>
    <option value="<%=data.packetId%>"><%=data.packetTitle%></option>
    <%}%>
</script>
<script type="text/javascript">
    $(function(){
        //获取资料包
        var packet = [];
        $.get('getAllPacketType.vpage',{},function (res) {
            if(res.success){
                packet = res.data;
                $('.packetList').append(template('packetAdd',{res:packet || ''}));
            }
        });


        // 保存
        var postData = {};
        $(document).on('click','.submitBtn',function () {
            var postFlag = true;
            var info = $(this).data('info');
            if(info==0){
                window.history.back();
            }else{
                $.each($(".js-postData"),function(i,item){
                    if($(item).hasClass('js-needed')){
                        if(!($(item).val())){
                            layer.alert($(item).data("einfo"));
                            postFlag = false;
                            return false;
                        }
                    }
                    if(item.name=='comment'){
                        postData[item.name] = $(item).val().replace(/\n/g,'<br>');
                    }else {
                        postData[item.name] = $(item).val();
                    }
                });
                var packet = [];
                for (var i=0;i<$(".second").length;i++){
                    if($(".second").eq(i).val()){
                        packet.push($(".second").eq(i).val())
                    }
                }
                postData['packetIds'] = packet.join(',');
                postData['id'] = getQuery("id");
                if(postFlag){
                    $.get('saveData.vpage',postData,function (res) {
                        if(res.success){
                            layer.alert('修改成功！');
                            window.history.back();
                        }else{
                            layer.alert(res.info);
                        }
                    })
                }
            }
        })


        //添加资料包
        $(document).on('click','.addPacket',function () {
            $('.packetList').append(template('packetAdd2',{res:packet || ''}));
        });

        // 删除资料包
        $(document).on('click','.packetDelBtn',function () {
            $(this).parent().remove();
        });

        $(document).on('change','.first',function () {
            var _this = $(this);
            var list = [];
            packet.forEach(function (item) {
                if(item.packetTypeId == _this.val()){
                    list = item.packetList;
                }
            });
            _this.parent().parent().next().find('.second').html(template('packetListAdd2',{res:list || ''}));
        });

    });
</script>
</@layout_default.page>
