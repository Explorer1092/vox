<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="" pageJs="" footerIndex=2>
    <@sugar.capsule css=['custSer']/>
<a class="return orange-color js-subBtn" href="javascript:void(0);" style="display:none;">提交</a>
    <div class="res-content">
    </div>
    <div class="aut-title">请选择需要客服协助的事项</div>
    <div class="c-opts gap-line tab-head c-flex c-flex-3" style="border-top: 1px solid #cdd3dc; ">
        <span class="js-first js-sort the"><a href="/mobile/task/change_school_page.vpage?teacherId=${teacherId!0}">转校</a></span>
        <span class="js-sort"><a href="/mobile/task/create_class_page.vpage?teacherId=${teacherId!0}">新建班级</a></span>
        <span class="js-sort"><a href="/mobile/task/bind_mobile_page.vpage?teacherId=${teacherId!0}">绑定/解绑手机</a></span>
    </div>
    <div class="content">
        <div class="flow switchContent" style="display: none;">
            <div class="aut-title">具体协助内容</div>
            <div class="item">
                转入学校
                <span class="inner-right">请选择</span>
            </div>
            <div class="item GPS clearfix">
                是否带班转校
                <div>
                    <div class="btn-stroke withclass" data-type="1">
                        带班转校
                    </div>
                    <div class="btn-stroke withclass" data-type="2">
                        不带班转校
                    </div>
                </div>
            </div>
            <div class="item GPS clearfix">
                带班班级
                <div>
                    <div class="btn-stroke classitem" data-type="1">
                        一年级一班
                    </div>
                    <div class="btn-stroke classitem" data-type="2">
                        一年级二班
                    </div>
                    <div class="btn-stroke classitem" data-type="1">
                        一年级三班
                    </div>
                    <div class="btn-stroke classitem" data-type="2">
                        一年级四班
                    </div>
                    <div class="btn-stroke classitem" data-type="1">
                        一年级五班
                    </div>
                    <div class="btn-stroke classitem" data-type="2">
                        一年级六班
                    </div>
                </div>
            </div>

        </div>
        <div class="flow newContent" style="display: none;">
            <div class="aut-title">请填写年级和班号</div>
            <div class="classList">
                <div>
                    <span class="js-gradle"></span>年级 <span class="js-class"></span>班
                    <div>
                        <span class="js-add"> + </span>
                        <span class="js-remove"> - </span>
                    </div>
                </div>
            </div>
        </div>
        <div class="flow bindContent">
            <div class="aut-title">提示:只解绑手机时,可不填写绑定手机号</div>
            <div class="bind">
                解绑手机号
                <span class="inner-right">
                    <input type="tel" id="phone">
                </span>
            </div>
            <div class="bind">
                绑定手机号
                <span class="inner-right">
                    <input type="tel" id="newPhone" placeholder="请输入绑定的新手机号">
                </span>
            </div>



        </div>

    </div>

    <div class="custItem">
        <div class="title">备注(选填)</div>
        <div class="custText">
            <textarea maxlength="50" placeholder="特殊说明,如客服方便和老师电话沟通的时间等(50字内)" id="markMsg"></textarea>
        </div>
    </div>

</div>

<script>
    var AT = new agentTool();
    //拒绝按钮
    $(".js-AuditOpinionChange").on("click",function(){
        var data=$(this).data();
        var reqData={
            recordId    : data.aid,
            respondent  : data.respondent,
            alterType   : data.type
        };
        $.post("reject_alter.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("拒绝成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
    //同意按钮
    $(".js-agree").on("click",function(){
        var data=$(this).data();
        var reqData={
            recordId    : data.aid,
            respondent  : data.respondent,
            alterType   : data.type
        };
        $.post("approve_alter.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("通过成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $this.addClass("the").siblings().removeClass("the");
        $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });
</script>
</@layout.page>