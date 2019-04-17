<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<div class="w-base">
    <div class="w-base-title">
        <h3>学生名单使用说明</h3>
    </div>
    <!--template container-->
    <div class="w-base-container">
        <!--//start-->
        <div class="plateOverBox">
            <h6><span>1</span>下载学号密码</h6>
            <div class="w-pad-10 w-ag-center">
                <a id="download_letter" href="/teacher/clazz/batchdownload.vpage?clazzIds=${info}" class="w-btn w-btn-green w-btn-small v-download-clazz">下载学生学号密码</a>
                <span class="inline_df_share text_gray_9 edge_vox">如果您刚刚下载名单失败，请点击左侧下载按钮重新下载</span>
            </div>
            <h6><span>2</span>打印学生名单</h6>
            <div class="picture_print"></div>
            <h6><span>3</span>把号码条发给相应的同学</h6>
            <div class="picture_table"></div>
            <h6><span>4</span>要求学生一周内使用账号和密码登录，修改姓名，完成第一次作业</h6>
            <div class="w-pad-15 w-ag-center">
                <a id="set_homework" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/homework/batchassignhomework.vpage" class="w-btn w-btn-small">立即布置作业</a>
            </div>
        </div>
        <!--end//-->
    </div>
</div>

    <script type="text/javascript">
        $(function(){
            if("${info}" == "0"){
                setTimeout(function(){ location.href = "/teacher/clazz/clazzlist.vpage"; }, 200);
            }

            $("#download_letter").live("click", function(){
                if("${addtype}" == "batch"){
                    $17.tongji("建班-有名单-下载学号");
                }else{
                    $17.tongji("建班-无名单-下载学号");
                }
            });

            $("#set_homework").live("click", function(){
                if("${addtype}" == "batch"){
                    $17.tongji("建班-有名单-名单使用说明页-布置作业");
                }else{
                    $17.tongji("建班-无名单-名单使用说明页-布置作业");
                }
            });
            LeftMenu.focus("clazzmanager");

            $(".v-download-clazz").on("click", function(){
                $17.voxLog({
                    module: "common",
                    op : "downloadletter"
                });
            });
        });
    </script>
</@shell.page>