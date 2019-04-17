<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择下属进校记录" pageJs="common"  footerIndex=4>
    <@sugar.capsule css=['res',"home"]/>
<div style="background:#fff">
    <div class="c-search" id="searchItem">
        <input placeholder="请输入姓名" maxlength="30" id="roleSearch">
        <span class="js-search" data-roleType="13">搜索</span>
    </div>
</div>
<div class="new_man vacation_list">
</div>
<script type="text/html" id="new_man">
    <ul>
        <%if(data.length>0){%>
            <%for(var i=0;i < data.length; i++){%>
                <%var res = data[i]%>
                <li class="js-todoRecord" data-id="<%=res.id%>" data-school="<%=res.schoolId%>" style="cursor: pointer">
                    <div class="student_name">
                        <div class="icon_info" style="color:#636880"><%=res.workTime%></div>
                        <%=res.workerName%>
                    </div>
                    <div class="side">
                        <span><%=res.schoolName%></span>
                    </div>
                </li>
            <%}%>
        <%}else{%>
            <li style="text-align: center;">暂无数据</li>
        <%}%>
    </ul>
</script>
<script>
    var AT = new agentTool();
    Date.prototype.Format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }
    $(document).on('click','.js-search',function(){
        var roleInput = $("#roleSearch").val();
        if(roleInput != ""){
            searchFn(roleInput);
        }else{
            AT.alert("请输入姓名");
        }
    });

    var searchFn = function (roleInput) {

        var data = {};
        if(roleInput != null && roleInput != ""){
            data.keyWords = roleInput ;
        }
        $.post("search_into_school_data.vpage",data,function(res){
            if(res){
                if(res.length>0){
                    for(var i=0;i< res.length;i++){
                        if(res[i].workTime){
                            res[i].workTime = new Date(res[i].workTime).Format("hh:mm") ;
                        }
                    }
                }

                renderTemplate("new_man",{"data":res},".new_man");

            }else{
                AT.alert("查询失败");
            }
        });

    };
    $(document).ready(function () {
        searchFn(null);
    });

    var renderTemplate = function(tempSelector,data,container){
        var contentHtml = template(tempSelector, data);
        $(container).html(contentHtml);
    };
    $(document).on("click",".js-todoRecord",function () {
        window.location.href = "addVisit.vpage?schoolRecordId="+$(this).data("id")+"&schoolId="+$(this).data("school");
    })
</script>
</@layout.page>