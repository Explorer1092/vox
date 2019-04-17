<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <div class="row-fluid">
        <div class="span12">
        </div>
    </div>
    <div class="row-fluid">
        <div class="span1">
        </div>

        <div class="span10" style="text-align: center">
            <form id="addRegion" name="addRegion" action="addRegion.vpage" method="post" class="well form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="addRegion_province">省</label>
                        <div class="controls">
                            <select name="addRegion_province" id="addRegion_province"> </select>

                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="addRegion_city">市</label>
                        <div class="controls">
                            <select name="addRegion_city" id="addRegion_city"></select>

                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="addRegion_region">地区</label>
                        <div class="controls">
                            <input type="text" name="addRegion_region" value="" id="addRegion_region" class="input"/>
                        </div>
                    </div>

                    <input type="submit" id="addRegion_0" value="添加" class="btn"/>

                </fieldset>
            </form>
        </div>

        <div class="span1">
        </div>
    </div>
</div>
<script>
    $(function(){
        function init(){
            initProvince();
        }

        function initProvince(){
            $("#addRegion_province").empty();
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
                var html = '';
                var active = false;
                $.each(data, function(){
                    if(!active){
                        html += '<option selected="selected" value="' + this.id + '">' + this.text + '</option>';
                        active = true;
                        cities(this.id);
                    }else{
                        html += '<option value="' + this.id + '">' + this.text + '</option>';
                    }
                });
                $("#addRegion_province").html(html);
                $("#addRegion_province").on("change", function(){
                    cities($(this).val());
                });
            });
        }
        function cities(code){
            $("#addRegion_city").empty();
            $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-" + code + ".vpage", function(data) {
                if(data.length < 1){
                    $("#addRegion_city").html('<option value="">没有相关数据</option>');
                    return false;
                }
                var html = '';
                var active = false;
                $.each(data,function(){
                    if(!active){
                        html += '<option selected="selected" value="' + this.id + '">' + this.text + '</option>';
                        active = true;
                    }else{
                        html += '<option value="' + this.id + '">' + this.text + '</option>';
                    }
                });

                $("#addRegion_city").html(html);
            });
        }

        init();
    });

</script>
</@layout_default.page>