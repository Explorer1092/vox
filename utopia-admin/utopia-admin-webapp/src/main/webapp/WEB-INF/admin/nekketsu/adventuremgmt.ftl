<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
    <legend>小游戏缓存信息</legend>
    <button type="button" class="btn btn-primary" id="app_cache_info_btn">刷新小游戏缓存</button>

    <br>
    <legend>奇幻探险缓存信息</legend>
    <table>
        <tr>
            <td colspan="12">个人缓存信息</td>
        </tr>
        <tr>
            <td>用户ID</td><td><input id="userId" name="userId" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="cache_info_btn">缓存信息</button>
    <button type="button" class="btn btn-primary" id="refresh_cache_info_btn">刷新缓存信息</button>

    <br>
    <legend>初始化“奇幻探险”信息</legend>
    <table>
        <tr>
            <td colspan="12">初始化“奇幻探险”信息</td>
        </tr>
        <tr>
            <td>用户ID</td><td><input id="userId_1" name="userId_1" value="" type="text"/></td>
            <td>教材ID</td><td><input id="bookId_1" name="bookId_1" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="init_user_adventure_btn">初始化“奇幻探险”信息</button>
    <button type="button" class="btn btn-primary" id="delete_user_adventure_btn">删除“奇幻探险”信息</button>
    <button type="button" class="btn btn-primary" id="init_user_book_stage_btn">初始化“学生-教材-关卡”信息</button>
    <button type="button" class="btn btn-primary" id="delete_user_book_stage_btn">删除“学生-教材-关卡”信息</button>

    <br>
    <legend>修改“奇幻探险”信息</legend>
    <table>
        <tr>
            <td colspan="12">修改“奇幻探险”信息</td>
        </tr>
        <tr>
            <td>用户ID</td><td><input id="userId_2" name="userId_2" value="" type="text"/></td>
            <td>数据KEY</td><td><input id="key_2" name="key_2" value="" type="text"/></td>
            <td>数据Value</td><td><input id="value_2" name="value_2" value="" type="text"/></td>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="update_user_adventure_btn">修改“用户奇幻探险”信息</button>
    <button type="button" class="btn btn-primary" id="update_user_book_stage_btn">修改“学生-教材-关卡”信息</button>



    <div id="msg" class="modal hide fade" style="width: 800; height: 600">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>提示</h3>
        </div>
        <div class="modal-body">

        </div>
    </div>

</@layout_default.page>


<script>
    $(function () {

        $('#app_cache_info_btn').click(function () {
            if (confirm('刷新缓存信息，确认继续？')) {
                var postData = {
                    userId: $('#userId').val()
                };
                $.post('refreshAppCache.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#cache_info_btn').click(function () {
            if (confirm('查看缓存信息，确认继续？')) {
                var postData = {
                    userId: $('#userId').val()
                };
                $.post('getCache.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#refresh_cache_info_btn').click(function () {
            if (confirm('刷新缓存信息，确认继续？')) {
                var postData = {
                    userId: $('#userId').val()
                };
                $.post('refreshCache.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });



        $('#init_user_adventure_btn').click(function () {
            if (confirm('初始化“用户奇幻探险”信息，确认继续？')) {
                var postData = {
                    userId: $('#userId_1').val()
                };
                $.post('initUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#delete_user_adventure_btn').click(function () {
            if (confirm('删除“用户奇幻探险”信息，确认继续？')) {
                var postData = {
                    userId: $('#userId_1').val()
                };
                $.post('deleteUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });


        $('#init_user_book_stage_btn').click(function () {
            if (confirm('初始化“学生-教材-关卡”前请确认用户数据为空，确认继续？')) {
                var postData = {
                    userId: $('#userId_1').val()
                };
                $.post('initUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#delete_user_book_stage_btn').click(function () {
            if (confirm('删除“学生-教材-关卡”信息，确认继续？')) {
                var postData = {
                    userId: $('#userId_1').val()
                };
                $.post('deleteUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });



        $('#update_user_adventure_btn').click(function () {
            if (confirm('修改“用户奇幻探险”信息，确认继续？')) {
                var postData = {
                    userId: $('#userId_2').val(),
                    key: $('#key_2').val()
                };
                $.post('initUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

        $('#update_user_book_stage_btn').click(function () {
            if (confirm('修改“学生-教材-关卡”信息，确认继续？')) {
                var postData = {
                    userId: $('#userId_2').val(),
                    key: $('#key_2').val(),
                    value: $('#value_2').val()
                };
                $.post('updateUserBookStage.vpage', postData, function (data) {
                    $('#msg .modal-body').text(JSON.stringify(data));
                    $('#msg ').modal("show");
                });
            }
        });

    });

</script>