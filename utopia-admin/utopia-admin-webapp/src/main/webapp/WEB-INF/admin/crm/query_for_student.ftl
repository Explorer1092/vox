<form>
    <input type="input" name="userType" value="student" style="display: none">
    <DIV id="pd0u202" style="" data-label="State1">
        <div id="u203" class="u203_container"   >
            <div id="u203_img" class="u203_normal detectCanvas"></div>
            <div id="u204" class="u204" style="visibility:hidden;"  >
                <div id="u204_rtf"></div>
            </div>
        </div>
        <INPUT id="content_1" type=text value="查询内容" class="u205" data-label="textfield"    >
        <SELECT id="condition_1" class="u206"   >
            <OPTION selected value="查询条件">查询条件1</OPTION>
            <OPTION  value="学号">学号</OPTION>
            <OPTION  value="姓名">姓名</OPTION>
            <OPTION  value="邮件">邮件</OPTION>
            <OPTION  value="手机">手机</OPTION>
        </SELECT>

        <INPUT id="query" type="button" class="u207" value="查询"   >

        <INPUT id="content_2" type=text value="查询内容" class="u208" data-label="textfield"    >
        <SELECT id="condition_2" class="u209"   >
            <OPTION selected value="查询条件">查询条件2</OPTION>
            <OPTION  value="学号">学号</OPTION>
            <OPTION  value="姓名">姓名</OPTION>
            <OPTION  value="邮件">邮件</OPTION>
            <OPTION  value="手机">手机</OPTION>
        </SELECT>

        <INPUT id="content_3" type=text value="查询内容" class="u210" data-label="textfield"    >
        <SELECT id="condition_3" class="u211"   >
            <OPTION selected value="查询条件">查询条件3</OPTION>
            <OPTION  value="学号">学号</OPTION>
            <OPTION  value="姓名">姓名</OPTION>
            <OPTION  value="邮件">邮件</OPTION>
            <OPTION  value="手机">手机</OPTION>
        </SELECT>
    </DIV>
</form>
<script>
$("#query").click(function(){
    var array = ["jc", "ch", "dy", "sheng", "shi", "qu"];
    var _array = [];
    for(var key in array){
        if($("#" + array[key]).val() == -1){
            _array.push("0");
        }else{
            if(key > 2){
                 _array.push($("#" + array[key]).val().split("#")[1]);
            }else{
            _array.push($("#" + array[key]).val());
            }
        }
    }

var zjr = $("#zjr").val();
if(zjr == null || zjr ==''){
_array.push(0);
}else{
_array.push(zjr);
}

var queryUrl = "crm/user";
switch($(".searchNavsBox a[class='sel']").attr("id")){
case "inside":
queryUrl = "exampaperlist-" + $("#pageNumber").val() + ".vpage";
break;
case "teacher":
queryUrl = "teacherexampaperlist-" + $("#pageNumber").val() + ".vpage";
break;
case "paperGames"
queryUrl = "teacherexampaperlist-" + $("#pageNumber").val() + ".vpage";
break;
}

    $.ajax({
        type: "post",
        url: queryUrl,
        data: {
            version : _array[0],
            bookid  : _array[1],
            unitid  : _array[2],
            province: _array[3],
            city    : _array[4],
            county  : _array[5],
            provider: _array[6]
        },
        success: function (data){
            $("#centent").html(data);
        }
    });
});
</script>
