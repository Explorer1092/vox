<#-- @ftlvariable name="adminDictGroupNameList" type="java.util.List<java.lang.String>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Web manage' page_num=11>
<style>
    li, label {font-size: 16px;}
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>错题因子工具</legend>
        </fieldset>
        <fieldset>
            <ul class="inline">
                <li>
                    <label style="width: 120px">用户ID</label>
                </li>
                <li>
                    <input id="userId" type="text" />
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label style="width: 120px">错题因子数 >=</label>
                </li>
                <li>
                    <input id="incorrectCount" type="text" />
                </li>
                <li>
                    <button id="submitSelected" class="btn btn-success">查询</button>
                </li>
            </ul>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <ul class="inline">
                <li>
                    <button id="clean" class="btn btn-success">清空错题因子</button>
                </li>
            </ul>
            <div id="wq_stat_chip"></div>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <legend>查看错题</legend>
        </fieldset>
        <fieldset>
            <div id="view_wq_chip"> </div>
        </fieldset>
    </div>

    <div id="view_wqimg_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>试题预览</h3>
        </div>
        <div class="modal-body">
            <img src="" id="wqimg" />
        </div>
    </div>
    <script>
        function viewWq(userId){
            $('#view_wq_chip').load('getWrongQuestionList.vpage',
                    {userId : userId}
            );
        }

        function viewWqImg(qid){
            $.ajax({
                type: 'post',
                url: 'getWrongQuestionImg.vpage',
                data: {wqId : qid},
                success: function (data){
                    if(data.success){
                        $("#wqimg").attr("src", data.info);
                        $('#view_wqimg_dialog').modal();
                    }else{
                        alert(data.info);
                    }
                }
            });
        }

        $(function() {
            $('#submitSelected').on('click', function() {
                $('#wq_stat_chip').load('getWrongQuestionStat.vpage',
                        {userId : $('#userId').val(), incorrectCount : $('#incorrectCount').val()}
                );
            });



            $('#clean').on('click', function() {
                var statIds = [];
                $("#wq_stat_chip input[name='statid']:checked").each(function(){
                    statIds.push($(this).val());
                });
                if(statIds.length == 0){
                    alert("请至少选择一条数据");
                    return;
                }
                var postData = {
                    statIds : statIds.join(",")
                };
                $.ajax({
                    type: 'post',
                    url: 'cleanWrongQuestionStat.vpage',
                    data: postData,
                    success: function (data){
                        alert(data.info);
                    }
                });
            });

        });
    </script>
</@layout_default.page>