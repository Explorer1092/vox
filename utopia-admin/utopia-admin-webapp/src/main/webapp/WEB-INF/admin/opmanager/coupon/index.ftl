<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="优惠券管理" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <strong>优惠券管理</strong>
    </legend>
    <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    <ul class="inline">
        <li>
            <label>优惠券名称：&nbsp;
                <input id="couponName" name="couponName" type="text" value="" placeholder="请输入优惠券名称" style="width: 100px">
            </label>
        </li>
        <li>
            <button type="button" class="btn btn-primary" id="searchBtn">查  询</button>
        </li>
        <li>
            <button type="button" class="btn btn-success js-couponOption" data-type="add">新增优惠券</button>
        </li>
    </ul>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="210px">ID</th>
                        <th>类型</th>
                        <th>名称</th>
                        <th>折扣力度</th>
                        <th>状态</th>
                        <th>可使用次数</th>
                        <th>总发行数量</th>
                        <th>剩余领取次数</th>
                        <th>使用限制时长（天）</th>
                        <th>过期日期</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if couponPage?? >
                            <#list couponPage as cp >
                            <tr>
                                <td>${cp.id!'--'}</td>
                                <td>${cp.couponType.getDesc()!'--'}</td>
                                <td>${cp.name!'--'}</td>
                                <td>${cp.typeValue!'--'}</td>
                                <td>${cp.status.getDesc()!'--'}</td>
                                <td>${cp.usableCount!'--'}</td>
                                <td>${cp.totalCount!'--'}</td>
                                <td>${cp.leftCount!'--'}</td>
                                <td>${cp.effectiveDay!'--'}</td>
                                <td>${cp.limitDate!'--'}</td>
                                <td>
                                    <a href="javascript:;" class="js-couponOption btn" data-type="edit" data-cid="${cp.id!''}">编辑</a>
                                    <#--<a href="javascript:;" class="js-couponOption" data-type="delete" data-cid="${cp.id!''}">删除</a>-->
                                    <#--<a href="javascript:;" class="js-couponOption btn" data-type="detail" data-cid="${cp.id!''}">详情</a>-->
                                    <a href="javascript:;" class="js-sendCoupon btn btn-success" data-cid="${cp.id!''}">发券</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
<div id="deleteCouponDialog" class="modal fade hide">
    <input id="raise-up-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">提示</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <p style="text-align: center;">确定要删除该条优惠券吗？</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="sureDeleteBtn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="sendCouponDialog" class="modal fade hide">
    <input id="raise-up-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">发券给用户</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div style="text-align: center;margin: 20px;">
                        <span style="padding-right: 20px;">用户ID:</span><input id="targetUserId" name="coupon" type="text" value="" placeholder="请输入用户ID" maxlength="20" style="width: 200px;" data-cid="">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="sureSendCouponBtn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        //操作
        $(document).on('click',".js-couponOption",function () {
           var $this = $(this),
               type = $this.data('type'),
               cid = $this.data('cid'),
               mapLink = {
                   'add':'',
                   'edit':'?couponId='+cid,
                   'detail':'?couponId='+cid
               };
           if(type == "delete"){
                $('#deleteCouponDialog').modal();
           }else{
               location.href = 'coupondetail.vpage'+mapLink[type];
           }
        });

        //确定删除
        $(document).on('click',"#sureDeleteBtn",function () {
            //TODO:删除

            $('#deleteCouponDialog').modal('hide');
            location.reload();
        });

        $(document).on('click',".js-sendCoupon",function () {
            var $this = $(this),
                    cid = $this.data('cid');
            if(cid){
                $('#targetUserId').val("");
                $("#targetUserId").attr("data-cid",cid);
                $('#sendCouponDialog').modal();
            }
        });
        $(document).on('click',"#sureSendCouponBtn",function () {
            var $tNode = $('#targetUserId'),
                    cid = $tNode.data('cid'),
                    uid = $tNode.val();
            if(cid && uid){
                $.post('/opmanager/coupon/sendcoupon.vpage',{couponId:cid,userId:uid},function (res) {
                    if(res.success){
                        alert('发送成功');
                        location.reload();
                    }else{
                        alert(res.info);
                    }
                })
            }else{
                alert('请填写用户ID');
            }
        });

    });

    $('#searchBtn').on("click",function () {
        var cName = $("#couponName").val();
        if(cName){
            location.href = 'index.vpage?page=1&couponName='+cName;
        }else{
            alert('请输入优惠券名称');
        }
    });

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        location.href = 'index.vpage?page='+$("#pageNum").val()+'&couponName='+$("#couponName").val();
    }

</script>
</@layout_default.page>