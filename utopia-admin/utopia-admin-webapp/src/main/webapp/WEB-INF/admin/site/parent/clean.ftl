<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>

<!--/span-->
<div class="span9">
    <ul class="inline">
        <li>
            <button id="btnDup" disabled="disabled" class="btn" onclick="disableref('duplicated')">消除重复关联数据</button>
            <button id="btnKey" disabled="disabled" class="btn" onclick="disableref('keyparent')">消除重复关键家长数据</button>
            <button id="btnGender"  disabled="disabled" class="btn" onclick="disableref('gender')">消除身份性别冲突数据</button>
        </li>
        </ul>
<#--//start-->
    <table class="table table-bordered">
        <tr>
            <th>USER_ID</th>
            <th>USER_ID</th>
            <th>身份</th>
            <th>关键家长</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>原因</th>
            <th>编号</th>
            <th>选择</th>
        </tr>
        <tbody id="tbody"></tbody>
    </table>
<#--end//-->
</div>
<!--/span-->
<script type="text/javascript">
    $(function(){
        $.post('/site/parent/loadduplicated.vpage',{},function(data){
            console.log(data);

            if(!data.success){
                alert(data.info);
                return;
            }

            var html = '';

            if(data.type=='duplicated'){
                //处理重复数据
                html = duplicated(data);
                $('#btnDup').removeAttr('disabled');
            }else if(data.type == 'keyparent'){
                //处理重复关键家长
                html = duplicated(data);
                $('#btnKey').removeAttr('disabled');
            }else if(data.type == 'gender'){
                //处理身份性别冲突
                html = duplicated(data);
                $('#btnGender').removeAttr('disabled');
            }else{
                alert('未知类型数据');
            }

            $('#tbody').html(html);
        });
    });

    //重复关联数据
    function duplicated(data){
        var html = '';
        $.each(data.rows,function(k,v){
                var count = 0;
                var parentFirst = true;

                var h_p='';
                $.each(v,function(dd,vv){
                    if(!parentFirst){
                        h_p+='<tr>';
                    }else{
                        parentFirst = false;
                    }

                    var childFirst = true;

                    var countChild = 0;
                    var h_c = '';
                    $.each(vv,function(i){
                        if(!childFirst){
                            h_c +='<tr>';
                        }else{
                            childFirst = false;
                        }

                        h_c += '<td>'+vv[i]["CALL_NAME"]+'</td>';
                        h_c += '<td>'+(vv[i]['KEY_PARENT'] == '0'?'否':'是') +'</td>';
                        h_c += '<td>'+(new Date(parseInt(vv[i]['CREATETIME']))).format('yyyy-MM-dd hh:mm:ss')+'</td>';
                        h_c += '<td>'+(new Date(parseInt(vv[i]['UPDATETIME']))).format('yyyy-MM-dd hh:mm:ss')+'</td>';
                        h_c +='<td>'+vv[i]["reason"]+'</td>';
                        h_c +='<td>'+vv[i]['ID']+'</td>';
                        h_c +='<td><input type="checkbox" id="'+vv[i]["ID"]+'" name="chk" kid="'+vv[i]['ID']+'_'+vv[i]['PARENT_ID']+'" '+(vv[i]["del"]==false?'':'checked="checked"')+' onclick="chkclick(\''+vv[i]["ID"]+'\')" /></td>';
                        h_c +='</tr>';
                        countChild++;
                    });
                    h_p += '<td rowspan="'+countChild+'"><a target="_blank" href="'+generateUserHomePageUrl(dd)+'">'+dd+'</a></td>'+h_c;

                    count += countChild;
                });

                html += '<tr><td rowspan="'+count+'"><a target="_blank" href="'+generateUserHomePageUrl(k)+'">'+k+'</a></td>'+h_p;
            });
        return html;
    }

    function generateUserHomePageUrl(id){
        if(id.length > 0 && id.substr(0,1)=='2'){
            return '/crm/parent/parenthomepage.vpage?parentId='+id;
        }else{
            return '/crm/student/studenthomepage.vpage?studentId='+id;
        }
    }

    function disableref(type){
        if(type == 'duplicated'){
            if(!confirm('选择的记录将被解除关联,确认要操作吗?')) return false;
        }else if(type == 'keyparent'){
            if(!confirm('选择的关键家长将被更新为非关键家长,确认要操作吗?')) return false;
        }else if(type == 'gender'){
            if(!confirm('选择的家长身份将被更新为空,确认要操作吗?')) return false;
        }else {
            return false;
        }

        var ids = '';
        $('input[checked="checked"]').each(function(i){
                if(ids.length > 0) ids += ',';
                ids +=$(this).attr('kid');
        });

        $.post('/site/parent/resetrefs.vpage',{
            ids:ids,
            type:type
        },function(data){
            if(!data.success){
                alert(data.info);
                return;
            }else{
                window.location.reload();
            }
        });
    }

    function chkclick(id){
        if($('#'+id).attr('checked') == 'checked') 
            $('#'+id).removeAttr('checked');
        else
            $('#'+id).attr('checked','checked');
    }

    //扩展Date的format方法
Date.prototype.format = function (format) {
	var o = {
		"M+": this.getMonth() + 1,
		"d+": this.getDate(),
		"h+": this.getHours(),
		"m+": this.getMinutes(),
		"s+": this.getSeconds(),
		"q+": Math.floor((this.getMonth() + 3) / 3),
		"S": this.getMilliseconds()
	}
	if (/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	}
	for (var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
}
</script>

</@layout_default.page>