<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <p>在此跳转长地址:</p>
    <div>
        <textarea id="url" name="url" style="width:450px;height:150px;"
                  placeholder="输入一个跳转地址"></textarea>
        <input id="btn_submit" class="btn" type="button" value="生成"/>
    </div>
    <p>微信菜单地址:</p>
    <pre id="menuUrl"></pre>
</div>
<script type="text/javascript">
    $(function () {
        $('#btn_submit').on('click', function () {
            var url = $('#url').val();

            if (url.length == 0) {
                alert('请输入跳转地址');
                return;
            }
            $.post('menuUrl.vpage', {url: url}, function (data) {
                if (data.success) {
                    $('#menuUrl').html(data.menuUrl);
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>

</@layout_default.page>
