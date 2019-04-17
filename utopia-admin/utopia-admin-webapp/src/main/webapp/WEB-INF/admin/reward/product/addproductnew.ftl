<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='商品详情' page_num=12>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<link href="${requestContext.webAppContextPath}/public/css/reward/add_product.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<#--<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>-->
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<style>
    #edui154_iframeholder{height:350px!important;}
</style>
<div id="main_container" class="span9">
    <div class="add_prize_wrap" id="add_prize_wrap" v-cloak>
        <div class="title">
            奖品管理 > {{operate_name}}
        </div>
        <!--类目信息-->
        <div class="category_info info_box">
            <div class="category_title">
                类目信息
                <span class="start">☆</span><span>为必填项</span>
            </div>
            <!--商品分类-->
            <div class="goods_category">
                <span class="start">☆</span> 商品分类 :
                <select name="" id="" v-model="data_str"  @change = "choice_first_category" >
                    <option value="">一级分类</option>
                    <option  :value="item.id + '+' + item.name+ '+'+ item.oneLevelCategoryType" v-for="(item,id) in first_list" :selected = "item.isSelected ">{{item.name}}</option>
                </select>
                <select name="" id="" v-model="second_cate_id" @change="choice_second_category" >
                    <option value="">二级分类</option>
                    <option  :value="item.id" v-for="item in second_list" >{{item.name}}</option>
                </select>
            </div>
            <!--商品商标-->
            <div class="goods_label category_label" v-cloak>
                <span class="label_name">
                    商品标签 :
                </span>
                <span class="redio_big_box">
                    <span class="radio_box" @click="choice_label(id)" v-bind:class="{'current' : labelId === id }" v-for="(item,id) in tag_list_category">
                        <label><input type="radio" name="label" :value="item.id" v-model="tagId" ><span class="radio_word">{{item.name}}</span></label>
                    </span>
                </span>
            </div>
            <!--是否新品-->
            <div class="new">
                是否新品 :
                <input type="checkbox" v-model="isNewProduct">新品
            </div>
            <!--跨分类标签-->
            <div id="mix">
                <div class="mix_category_box" v-cloak>
                    <select name="" id="" class="mix_category" @change = "choice_mix_category1" v-model="setId1">
                        <option value="" >分类集合</option>
                        <option :value="item.id" v-for="item in mix_category_list1" >{{item.name}}</option>
                    </select>
                    <span class="goods_label">
                        分类集合标签 :
                        <span class="radio_box" @click="choice_mix_label1(item.id)" v-bind:class="{'current' : mix_labelId1 === item.id }" v-for="(item,id) in tag_list_mix1">
                            <label><input type="radio" name="mix_label" :value="item.id" v-model="mix_category_id1"><span class="radio_word">{{item.name}}</span></label>
                        </span>
                    </span>
                </div>
                <div class="mix_category_box" v-cloak v-if="mix2">
                    <select name="" id="" class="mix_category" @change = "choice_mix_category2" v-model="setId2">
                        <option value="" >分类集合</option>
                        <option :value="item.id" v-for="item in mix_category_list2" >{{item.name}}</option>
                    </select>
                    <span class="goods_label">
                        分类集合标签 :
                        <span class="radio_box" @click="choice_mix_label2(item.id)" v-bind:class="{'current' : mix_labelId2 === item.id }" v-for="(item,id) in tag_list_mix2">
                            <label><input type="radio" name="mix_label2" :value="item.id" v-model="mix_category_id2"><span class="radio_word">{{item.name}}</span></label>
                        </span>
                    </span>
                </div>
                <div class="mix_category_box" v-cloak v-if="mix3">
                    <select name="" id="" class="mix_category" @change = "choice_mix_category3" v-model="setId3">
                        <option value="" >分类集合</option>
                        <option :value="item.id" v-for="item in mix_category_list3" >{{item.name}}</option>
                    </select>
                    <span class="goods_label">
                        分类集合标签 :
                        <span class="radio_box" @click="choice_mix_label3(item.id)" v-bind:class="{'current' : mix_labelId3 === item.id }" v-for="(item,id) in tag_list_mix3">
                            <label><input type="radio" name="mix_label3" :value="item.id" v-model="mix_category_id3"><span class="radio_word">{{item.name}}</span></label>
                        </span>
                    </span>
                </div>
                <div class="mix_category_box" v-cloak v-if="mix4">
                    <select name="" id="" class="mix_category" @change = "choice_mix_category4" v-model="setId4">
                        <option value="" >分类集合</option>
                        <option :value="item.id" v-for="item in mix_category_list4" >{{item.name}}</option>
                    </select>
                    <span class="goods_label">
                        分类集合标签 :
                        <span class="radio_box" @click="choice_mix_label4(item.id)" v-bind:class="{'current' : mix_labelId4 === item.id }" v-for="(item,id) in tag_list_mix4">
                            <label><input type="radio" name="mix_label4" :value="item.id" v-model="mix_category_id4"><span class="radio_word">{{item.name}}</span></label>
                        </span>
                    </span>
                </div>
            </div>

            <!--增加一组sku-->
            <div class="add_mix_categray"  @click="add_mix_categray">增加一组分类集合</div>
        </div>
        <!--基础信息-->
        <div class="base_info info_box" style="display: block" v-cloak >
            <div class="category_title" v-cloak>
                基础信息({{first_porduction_name}})
                <span class="category_title_tip">除兑换码和备注外都需要填写</span>
            </div>
            <!--商品名称-->
            <div class="data_box">
                <span class="input_box">
                    商品名称 : <input type="text"  v-model="productName">
                    <span class="start">☆</span>
                </span>
                <span class="input_box" v-if=" oneLevelCategoryType === 1 || oneLevelCategoryType === 5 || oneLevelCategoryType === 2" >
                    成本价(元) : <input type="number"  v-model="buyingPrice" @change="verify_num">
                    <span class="start">☆</span>
                </span>
                <span class="input_box" v-if=" oneLevelCategoryType === 3 || oneLevelCategoryType === 7">
                    有效期(天) : <input type="number"  v-model="expiryDate" @change="verify_num">
                    <span class="start">☆</span>
                </span>
                <span class="input_box" v-if="  oneLevelCategoryType === 6">
                    使用网址 : <input type="text"  v-model="usedUrl">
                    <span class="start">☆</span>
                </span>
                <span class="input_box" v-if=" oneLevelCategoryType === 4">
                    使用网址 : <input type="text"  v-model="relateVirtualItemContent">
                    <span class="start">☆</span>
                </span>
                <div v-show="oneLevelCategoryType === 1" id="sku_box">
                    <div >
                        <span class="input_box">
                            SKU显示 : <input type="text"  name="sku" class="sku">
                            <span class="start">☆</span>
                        </span>
                        <span class="input_box">
                            库存 : <input type="number"  name="库存" class="sku">
                            <span class="start">☆</span>
                        </span>
                    </div>
                </div>
                <!--增加一组sku-->
                <div class="add_mix_categray" v-if="oneLevelCategoryType === 1" @click="add_sku">增加一组SKU</div>
            </div>
        <#--选择支付方式-->
            <div class="goods_label">
                <span class="radio_box " @click="pay_methods(1)" v-bind:class="{'current' : pay_method === 1}"  v-if="oneLevelCategoryType === 7">
                    <label>
                        <input type="radio" name="change_method" :value="0" v-model="spendType"><span class="radio_word">学豆兑换</span>
                    </label>
                </span>
                <span class="radio_box" @click="pay_methods(2)" v-bind:class="{'current' : pay_method === 2} " v-if="oneLevelCategoryType === 7">

                    <label ><input type="radio" name="change_method" :value="1" v-model="spendType"><span class="radio_word">碎片兑换</span></label>
                </span>
                <div class="data_box">
                <span class="input_box" >
                    原价(学豆) : <input type="number"  v-model="priceS" @change="verify_num">
                    <span class="start">☆</span>
                </span>
                    <span class="input_box" >
                    售价(学豆) : <input type="number"  v-model="priceOldS" @change="verify_num">
                    <span class="start">☆</span>
                </span>
                </div>
            </div>
        <#--是否可重复对换,来源-->
            <select name="" id="" v-if="oneLevelCategoryType === 5 || oneLevelCategoryType === 2" v-model="repeatExchanged " style="margin-bottom: 10px">
                <option value="">是否可重复兑换</option>
                <option value=true>是</option>
                <option value=false>否</option>
            </select>
            <select name="" id="" v-if="oneLevelCategoryType === 5 || oneLevelCategoryType === 2" v-model="couponResource" style="margin-bottom: 10px">
                <option value="">来源</option>
                <option value="ZUOYE">自有</option>
                <option value="DUIBA">兑吧</option>
            </select>
            <select name="" id="" v-if=" oneLevelCategoryType === 2" v-model="relateVirtualItemId" style="margin-bottom: 10px">
                <option value="">运营商</option>
                <option value="ALL">全部</option>
                <option value="MOBILE">移动</option>
                <option value="UNICOM">联通</option>
                <option value="TELECOM">电信</option>
            </select>
            <select name="" id="" v-if=" oneLevelCategoryType === 2" v-model=" relateVirtualItemContent" style="margin-bottom: 10px">
                <option value="">流量包大小(MB)</option>
                <option value="30">30</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="500">500</option>
                <option value="1024">1024</option>
            </select>
            <div class="sort" v-if="couponResource === 'DUIBA'" style="margin-top: 10px">
                <span class="start">☆</span>
                使用网址 :
                <span class="input_box">
                    <input type="text"  v-model="usedUrl">
                </span>
            </div>
            <!--站内短信提示-->
            <div class="alter" v-show="oneLevelCategoryType === 5 || oneLevelCategoryType === 6">
                站内提示:
                <textarea class="alter_area" name="" id="" v-model="msgContent"></textarea>
            </div>
            <!--站内文案提示-->
            <div class="alter" v-show="oneLevelCategoryType === 5">
                短信提醒:
                <textarea class="alter_area" name="" id="" v-model="smsContent"></textarea>
            </div>
            <div class="control-group">
                <div class="controls" >
                    <script id="container_new" name="container_new" type="text/plain"></script>
                    <#--<textarea style="width: 100%; height: 150px;" id="description_box" v-model="description"></textarea>-->
                </div>
            </div>

            <!--上传封面图-->
            <div class="browse_pic">
                <div class="uploding_facepic_box">
                    <span class="uploding_btn_box">
                        <input type="file" class="uploding_btn1" @change="upfile_init" id="file_dom">
                        <span class="uploding_btn2" >上传封面图</span>
                    </span>
                    <span class="uploding_info">图片规格600*420</span>
                    <span class="show_list" v-if="face_img_list.length > 0">
                        <span class="pic_box" v-for="(item,id) in face_img_list">
                            <span class="close_btn" @click="delect_pic(id,item.id)"></span>
                            <img :src="press_picture(item.fileName)" alt="">
                        </span>
                    </span>
                </div>
                <div class="uploding_material_box" v-if="oneLevelCategoryType === 3 || oneLevelCategoryType === 7">
                    <span class="uploding_btn_box">
                        <input type="file" class="uploding_btn1" @change="upmaterial_iniit" id="material_dom">
                        <span class="uploding_btn4" >上传素材(png)</span>
                    </span>
                    <span class="show_list" v-if="material_img_list.length > 0">
                        <span class="pic_box" v-for="item in material_img_list">
                            <span class="close_btn" @click="delect_material_pic"></span>
                            <img :src="press_picture(item)" alt="">
                        </span>
                    </span>
                </div>
                <div class="alter" v-if="oneLevelCategoryType === 5">
                    导入兑换码:
                    <textarea class="alter_area" name="" id="" v-model="couponNo"></textarea>
                </div>
            </div>
            <div class="remark">
                <span class="input_box">
                    备注 : <input type="text"  v-model="remarks">
                </span>
            </div>
        </div>
        <!--展示配置-->
        <div class="info_box show_data" >
            <div class="category_title">
                展示配置
            </div>
            <div class="new port">
                <span class="start">☆</span>
                端口 :
                <input type="checkbox" v-model="mobileTerminalType" value="Mobile" class="displayTerminal">移动端
                <input type="checkbox" v-model="pcTerminalType" value="PC" class="displayTerminal">pc端
            </div>
            <!--用户-->
            <div class="new">
                用户 :
                <input type="checkbox" v-model="studentVisible">学生
                <input type="checkbox" v-model="teacherVisible">老师
            </div>
            <!--学段-->
            <div class="new">
                学段 :
                <span class="stage" ><input type="checkbox"  v-model="primarySchoolVisible" >小学</span>
                <span class="stage"><input type="checkbox" v-model="juniorSchoolVisible" >中学</span>
            </div>
            <!--排序-->
            <div class="sort new">
                <span class="start">☆</span>
                排序 :
                <span class="input_box">
                    <input type="number" placeholder="学生排序值" v-model="studentOrderValue" @change="verify_num">
                </span>
                <span class="input_box">
                    <input type="number" placeholder="老师排序值" v-model="teacherOrderValue" @change="verify_num">
                </span>
            </div>
        </div>seave_data
        <!--底部按钮-->
        <div class="footer_btn">
            <span class="seave_btn" @click="seave_data(false)">全部保存</span>
            <span class="setting_area" @click="seave_data(true)">保存并设置投放区域</span>
        </div>
        <!--导入兑换码成功-->
        <div class="popup_success_wrap popup_wrap" style="display: none" v-show="show_success_pops">
            <div class="popup_content">
                <div class="popup_title">
                    保存成功
                </div>
                <div class="already_import">
                    {{success_info}}
                </div>
                <div class="popup_btn_box">
                    <span class="popup_btn affirm" @click="jump">完成</span>
                </div>
            </div>
        </div>
    <#--提示框-->
        <div class="tip" v-show="show_tip" v-text="tip_word" v-cloak>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var ue_new = UE.getEditor('container_new', {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            zIndex: 1,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist','formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                '|', 'preview'
            ]]
        });
    var vm = new Vue ({
        el: '#add_prize_wrap',
        data: {
            operate_name:'添加商品',
            labelId:'',//选择商品标签加current
            mix_labelId1:'',//分类集合标签加current
            mix_labelId2:'',
            mix_labelId3:'',
            mix_labelId4:'',
            mix_categroy_label: '',//选择分类集合标签加current
            pay_method:'', //选择支付方式加current
            show_success_pops: false,//展示兑换码导入成功弹窗
            show_pic_list: [],//封面图列表
            productionId:'',//商品id
            data_result:{},
            first_list: [],//一级列表数据
            second_list:[],//二级列表数据
            mix_category_list1:[],//分类集合列表数据
            mix_category_list2:[],
            mix_category_list3:[],
            mix_category_list4:[],
            mix_category_list5:[],
            tag_list_category:[],//分类标签列表
            tag_list_mix1:[],//分类集合标签列表
            tag_list_mix2:[],
            tag_list_mix3:[],
            tag_list_mix4:[],
            data_str:'',
            oneLevelCategoryType:'',//一级列表类型
            oneLevelCategoryId:'',//一級表單id
            second_cate_id:'',//二级列表id
            tagId:'',//二级分类标签id
            setId1:'',//分类集合id
            setId2:'',
            setId3:'',
            setId4:'',
            mix_category_id1:'',//分类集合标签id
            mix_category_id2:'',
            mix_category_id3:'',
            mix_category_id4:'',
            first_porduction_name:'',//一级分类的名字
            face_img_list:[],//渲染用封面图片列表
            material_img_list:[],//素材图列表
            show_tip:false,//展示提示窗
            tip_word:'',//提示文案
            launch:false,
            success_info:'',
            primary_school:false,
            middle_school:false,
            juniorSchoolVisible:false,
            primarySchoolVisible:false,
            mix2:false,
            mix3:false,
            mix4:false,
            mix_num:1,
            primary_graduate:false,//
            middle_graduate:false,//
            pcTerminalType:false,//pc终端
            mobileTerminalType:false,//移动终端
            priceS:'',//原价 必
            priceOldS:'',//售价 必
            buyingPrice:'',//成本价 必
            productName:'',//商品名称 必
            description:'',//商品描述
            displayTerminal:'',//显示终端 必
            remarks:'',//备注
            isNewProduct: '',//是否新品b
            repeatExchanged:'',//是否可重复兑换
            couponResource:'ZUOYE',//来源
            spendType:'',//支付类型
            studentOrderValue:'',//学生排序值 必
            teacherOrderValue:'',//老师排序值 必
            teacherVisible:'',//老师用户
            studentVisible:'',//学生用户
            msgContent:'',//站内提示
            needSendSms:'',//
            smsContent:'',//短信提示
            needSendMsg:'',//
            expiryDate:'',//有效期
            usedUrl:'',//使用网址
            couponNo:'',//优惠券
            twoLevelCategoryMapper:{},//二级分类及标签集合
            setMapperList:[],//分类集合字段及标签
            skus:[],
            headwearMapper:{},//素材字段
            setMappers:[],//接收的分类集合数据
            images:[],//接收的图片数据
            relateVirtualItemId:'',//运营商
            relateVirtualItemContent: '30',//流量包大小
            onlined:'',//是否上架
        },
        created: function () {
            var vm = this;
            vm.productionId = vm.getQuery('productId') || '';
            if (vm.getQuery('productId')){
                vm.operate_name = '编辑商品';
                $.ajax({
                    type: 'GET',
                    url: "/reward/crmproduct/detail.vpage",
                    data: {productId:vm.productionId},
                    dataType:'JSON',
                    contentType:'application/json',
                    async: false,
                    success: function (data) {
                        if (data.success === true){
                            vm.isNewProduct = data.product.isNewProduct;
                            vm.displayTerminal = data.product.displayTerminal;
                            vm.expiryDate = data.product.expiryDate;
                            vm.first_list = data.oneLevelCategoryMapper;
                            vm.second_list = data.twoLevelCategoryMappers;
                            vm.productName = data.product.productName;
                            vm.priceS = data.product.priceS;
                            vm.priceOldS = data.product.priceOldS;
                            vm.buyingPrice = data.product.buyingPrice;
                            vm.description = data.product.description;
                            ue_new.ready(function () {
                                //ue_new.setContent(vm.description.replace(/\n/g, '<p><br/></p>'));
                                ue_new.setContent(vm.description.replace('<br/>', ''));
                            });

                            vm.remarks = data.product.remarks;
                            vm.spendType = data.product.spendType;
                            vm.studentOrderValue =data.product.studentOrderValue;
                            vm.teacherOrderValue = data.product.teacherOrderValue;
                            vm.teacherVisible = data.product.teacherVisible;
                            vm.studentVisible = data.product.studentVisible;
                            vm.tag_list_category = data.categoryTagMappers || [];
                            vm.setMappers = data.setMappers ;
                            vm.images =data.images || [];
                            vm.couponResource = data.product.couponResource || '';
                            vm.repeatExchanged = data.product.repeatExchanged;
                            vm.skus = data.skus;
                            vm.primarySchoolVisible = data.product.primarySchoolVisible;
                            vm.juniorSchoolVisible = data.product.juniorSchoolVisible;
                            if ($.isEmptyObject(data.headWear)) {
                                vm.headwearMapper = null;
                                vm.material_img_list = [];
                            } else {
                                vm.headwearMapper = {id:data.headWear.id,fileName:data.headWear.location};
                                vm.material_img_list[0] = data.headWear.location;
                            }
                            vm.usedUrl = data.product.usedUrl;
                            vm.msgContent = data.msgContent || '';
                            vm.smsContent = data.smsTpl || '';
                            vm.onlined = data.product.onlined;
                            vm.relateVirtualItemId = data.product.relateVirtualItemId;
                            vm.relateVirtualItemContent = data.product.relateVirtualItemContent
                        };
                    }
                });
//                处理接收的端信息
                if(vm.displayTerminal === 'PC') {
                    vm.pcTerminalType =true;
                }else if(vm.displayTerminal === 'Mobile') {
                    vm.mobileTerminalType =true;
                }else if(vm.displayTerminal === 'PC,Mobile') {
                    vm.pcTerminalType = true;
                    vm.mobileTerminalType = true;
                };
//                处理一级列表默认选中
                for (var i = 0;i< vm.first_list.length;i++){
                    if (vm.first_list[i].isSelected === true){
                        vm.data_str = vm.first_list[i].id + "+" + vm.first_list[i].name + "+" + vm.first_list[i].oneLevelCategoryType;
                        vm.first_porduction_name = vm.first_list[i].name;
                        vm.oneLevelCategoryId = vm.first_list[i].id;
                        vm.oneLevelCategoryType = vm.first_list[i].oneLevelCategoryType;
                    };
                };
//              处理二级分类列表默认选中
                for (var i = 0;i< vm.second_list.length;i++){
                    if (vm.second_list[i].isSelected === true){
                        vm.second_cate_id = vm.second_list[i].id ;
                    };
                };
//                处理分类列标签
                if(vm.tag_list_category) {
                    for (var i = 0 ; i<vm.tag_list_category.length ; i++){
                        if(vm.tag_list_category[i].isSelected === true){
                            vm.tagId = vm.tag_list_category[i].id;
                        }
                    };
                }

//                处理接收分类集合数据
                if(!vm.setMappers){
                    $.ajax({
                        type: 'Get',
                        url: "/reward/newset/list.vpage",
                        data: {},
                        async: false,
                        success: function (data) {
                            if(data.success === true){
                                vm.mix_category_list1 = data.setList;
                                vm.mix_category_list2 = data.setList;
                                vm.mix_category_list3 = data.setList;
                                vm.mix_category_list4 = data.setList;
                            }
                        }
                    });
                }else {
                    for(var z = 0; z< vm.setMappers.length; z++){
                        for(var i = 0 ; i < vm.setMappers[z].length; i++){
                            if( vm.setMappers[z][i].isSelected === true){
                                vm['setId' + (z+1)] = vm.setMappers[z][i].id;
                                if(vm.setMappers[z][i].tagMappers !== null) {
                                    for(var y =0; y<vm.setMappers[z][i].tagMappers.length; y++){
                                        if(vm.setMappers[z][i].tagMappers[y].isSelected === true) {
                                            vm['mix_category_id' + (z+1)] = vm.setMappers[z][i].tagMappers[y].id
                                        }
                                        vm['tag_list_mix' + (z+1)].push({
                                            id: vm.setMappers[z][i].tagMappers[y].id,
                                            name: vm.setMappers[z][i].tagMappers[y].name
                                        });
                                    }
                                }

                            };
                            vm['mix_category_list' + (z+1)].push({
                                id: vm.setMappers[z][i].id,
                                name: vm.setMappers[z][i].name
                            });
                        };
                        vm['mix' + (z+1)] = true;
                    };
                    if (z !== 5){
                        $.ajax({
                            type: 'Get',
                            url: "/reward/newset/list.vpage",
                            data: {},
                            async: false,
                            success: function (data) {
                                if(data.success === true){
                                    vm.mix_category_list5 = data.setList

                                }
                            }
                        });
                        for (var w = z; w < 5; w++){
                            vm['mix_category_list' + w] = vm.mix_category_list5;
                        };
                    }
                }

//                  处理接收的图片信息
                for(var i=0; i< vm.images.length; i++){
                    vm.face_img_list[i] = {
                        id : vm.images[i].id,
                        fileName:vm.images[i].location
                    };
                };
//              处理接收的skus数据
                $('#sku_box').html("");
                for(var i = 0; i<vm.skus.length;i++){
                    $('#sku_box').append(
                            "<div >\n" +
                            "<span class=\"input_box\">\n" +
                            "SKU显示 : <input type=\"text\"  name=\"sku\" class=\"sku\" value="+ vm.skus[i].skuName +">\n" +
                            "<span class=\"start\">☆</span>\n" +
                            "</span>\n" +
                            "<span class=\"input_box\">\n" +
                            "库存 : <input type=\"number\"  name=\"库存\" class=\"sku\" value="+ vm.skus[i].inventorySellable +" >\n" +
                            "<span class=\"start\">☆</span>\n" +
                            "</span>\n" +
                            "</div>")
                };
            }else {

                $.ajax({
                    type: 'Get',
                    url: "/reward/newcategory/list.vpage",
                    data: {parentId: vm.productionId},
                    async: false,
                    success: function (data) {
                        vm.first_list = data.categoryList || [];
                    }
                });
                $.ajax({
                    type: 'Get',
                    url: "/reward/newset/list.vpage",
                    data: {},
                    async: false,
                    success: function (data) {
                        if(data.success === true){
                            vm.mix_category_list1 = data.setList;
                            vm.mix_category_list2 = data.setList;
                            vm.mix_category_list3 = data.setList;
                            vm.mix_category_list4 = data.setList;
                        }
                    }
                });
            };

        },
        methods: {
//            验证数字不为负
            verify_num:function () {
                var vm = this;
                if(vm.buyingPrice < 0) {
                    vm.buyingPrice = '';
                    vm.tip('不能为负数~')
                }else if(vm.expiryDate < 0){
                    vm.expiryDate = '';
                    vm.tip('不能为负数~')
                }else if(vm.priceS < 0) {
                    vm.priceS = '';
                    vm.tip('不能为负数~')
                }else if(vm.priceOldS < 0){
                    vm.priceOldS = '';
                    vm.tip('不能为负数~')
                }else if(vm.studentOrderValue < 0){
                    vm.studentOrderValue = '';
                    vm.tip('不能为负数~');
                }else if(vm.teacherOrderValue < 0){
                    vm.teacherOrderValue = '';
                    vm.tip('不能为负数~');
                }
            },
            jump:function () {
                location.replace('/reward/crmproduct/products.vpage');
            },
            getQuery: function(item){
                var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
                return svalue ? decodeURIComponent(svalue[1]) : '';
            },
            choice_label: function (labelId) {
                var vm = this;
                vm.labelId = labelId;
            },
            choice_mix_label1:function (labelId) {
                vm.mix_labelId1 = labelId;
            },
            choice_mix_label2:function (labelId) {
                vm.mix_labelId2 = labelId;
            },
            choice_mix_label3:function (labelId) {
                vm.mix_labelId3 = labelId;
            },
            choice_mix_label4:function (labelId) {
                vm.mix_labelId4 = labelId;
            },
            mix_category :function (mix_category_label) {
                var vm = this;
                vm.mix_categroy_label = mix_category_label;
            },
            pay_methods : function (pay_methodId) {
                var vm = this;
                vm.pay_method = pay_methodId;
            },
            show_import_code: function () {
                var vm = this;
                vm.show_code_pops = true;
            },
            close_code_pops :function () {
                var vm = this;
                vm.show_code_pops = false;
            },
//            选择一级菜单
            choice_first_category:function () {
                var vm = this;

                var data_arr = vm.data_str.split("+");
                vm.oneLevelCategoryId = data_arr[0];
                vm.first_porduction_name = data_arr[1];
                vm.oneLevelCategoryType = Number(data_arr[2]);
                $.ajax({
                    type: 'Get',
                    url: "/reward/newcategory/list.vpage",
                    data: {
                        parentId: vm.oneLevelCategoryId
                    },
                    async: false,
                    success: function (data) {
                        vm.second_list = data.categoryList || [];
                        var fileObj = document.getElementById('file_dom');
                        fileObj.value = '';
                        if (vm.oneLevelCategoryId === 7 || vm.oneLevelCategoryId === 3) {
                            var materialObj = document.getElementById('material_dom');
                            materialObj.value = '';
                        }
                    }
                })
            },
//            选择二级菜单
            choice_second_category: function () {
                var vm = this;
                $.ajax({
                    type: 'Get',
                    url: "/reward/newtag/list.vpage",
                    data: {
                        parentId: vm.second_cate_id,
                        parentType: 1
                    },
                    async: false,
                    success: function (data) {
                        if (data.success === true) {
                            vm.tag_list_category = data.tagList;
                        };
                    }
                })
            },
//            点击分类选框
            choice_mix_category1: function () {
                var vm = this;
                $.ajax({
                    type: 'Get',
                    url: "/reward/newtag/list.vpage",
                    data: {
                        parentId: vm.setId1,
                        parentType: 2
                    },
                    async: false,
                    success: function (data) {
                        if (data.success === true){
                            vm.tag_list_mix1 = data.tagList
                        }
                    }
                })
            },
            choice_mix_category2: function () {
                var vm = this;
                $.ajax({
                    type: 'Get',
                    url: "/reward/newtag/list.vpage",
                    data: {
                        parentId: vm.setId2,
                        parentType: 2
                    },
                    async: false,
                    success: function (data) {
                        if (data.success === true){
                            vm.tag_list_mix2 = data.tagList
                        }
                    }
                })
            },
            choice_mix_category3: function () {
                var vm = this;
                $.ajax({
                    type: 'Get',
                    url: "/reward/newtag/list.vpage",
                    data: {
                        parentId: vm.setId3,
                        parentType: 2
                    },
                    async: false,
                    success: function (data) {
                        if (data.success === true){
                            vm.tag_list_mix3 = data.tagList
                        }
                    }
                })
            },
            choice_mix_category4: function () {
                var vm = this;
                $.ajax({
                    type: 'Get',
                    url: "/reward/newtag/list.vpage",
                    data: {
                        parentId: vm.setId4,
                        parentType: 2
                    },
                    async: false,
                    success: function (data) {
                        if (data.success === true){
                            vm.tag_list_mix4 = data.tagList
                        }
                    }
                })
            },
//            点击小学学段
            choick_primary: function () {
                var vm = this;
                if(vm.primary_class === false) {
                    vm.class1 = vm.class2 = vm.class3 = vm.class4 = vm.class5 = vm.class6 = false;
                }
            },
//            点击中学学段
            choick_middle:function () {
                var vm = this;
                if(vm.middle_class === false) {
                    vm.class7 = vm.class8 = vm.class9 = false;
                }
            },
//            点击年级
            choice_class: function () {
                var vm = this;
                if ( vm.class1 !== true && vm.class2 !== true && vm.class3 !== true &&vm.class4 !== true &&vm.class5 !== true &&
                        vm.class6 !== true ){
                    vm.primary_school =false;
                } else{
                    vm.primary_school =true;
                }
            },
//            点击中学年级
            choice_middle_class:function () {
                if(vm.class7 !== true &&vm.class8 !== true &&vm.class9 !== true) {
                    vm.middle_school =false;
                }else {
                    vm.middle_school =true;
                }
            },
//            点击曾加一组SKU
            add_sku:function () {
                $('#sku_box').append(

                        "<div >\n" +
                        "<span class=\"input_box\">\n" +
                        "SKU显示 : <input type=\"text\"  name=\"sku\" class=\"sku\">\n" +
                        "</span>\n" +
                        "<span class=\"input_box\">\n" +
                        "库存 : <input type=\"number\"  name=\"库存\" class=\"sku\">\n" +
                        "</span>\n" +
                        "</div>")
            },
            //点击曾加一组分类集合
            add_mix_categray:function () {
                var vm = this;
                vm.mix_num ++;
                vm['mix' + vm.mix_num] = true;
            },
//            展示提示框
            tip: function (word) {
                var vm = this;
                vm.tip_word = word;
                vm.show_tip = true;
                setTimeout(function(){
                    vm.show_tip = false;
                },1500)
            },
//            点击保存按钮
            seave_data :function (is_jump) {
                var vm = this;
                var sku_input = $('.sku');
                var sku_arr = [];


                // editor.sync();

                for(var i = 0;i < sku_input.length;i++){
                    sku_arr.push(sku_input[i].value);
                };

                if (vm.getQuery('productId')){
                    for( var i = 0;i<sku_arr.length/2;i++){
                        if(sku_arr[(i*2+1)] < 0) {
                            vm.tip('库存不能为负数~');
                            return;
                        }
                        if (i < vm.skus.length) {
                            vm.skus[i].skuId =  vm.skus[i].id;
                            vm.skus[i].skuName = sku_arr[i*2];
                            vm.skus[i].skuQuantity =  sku_arr[(i*2+1)];
                        } else {
                            vm.skus.push({
                                skuName: sku_arr[i*2],
                                skuQuantity: sku_arr[(i*2+1)]
                            });
                        }
                    }
                } else {
                    for( var i = 0;i<sku_arr.length/2;i++){
                        if(sku_arr[(i*2+1)] < 0) {
                            vm.tip('库存不能为负数~');
                            return
                        }
                        vm.skus.push({
                            skuName: sku_arr[i*2],
                            skuQuantity: sku_arr[(i*2+1)]
                        });
                    }
                }

                if(vm.smsContent){
                    vm.needSendSms = true;
                };
                if(vm.msgContent){
                    vm.needSendMsg = true;
                };


//                处理二级分类数据
                vm.twoLevelCategoryMapper={
                    twoLevelCategoryId:vm.second_cate_id,
                    tagId:vm.tagId
                };
//                处理分类集合数据
                for(var i=1;i<5;i++){

                    if( vm.setId1 === ''){
                        vm.setMapperList =[];
                    }else {
                        vm.setMapperList[i-1] = {
                            setId: vm['setId' + i],
                            tagId: vm['mix_category_id' + i]
                        };
                    };

                }

//                处理终端类型数据
                if(vm.pcTerminalType === true && vm.mobileTerminalType === false) {
                    vm.displayTerminal = 'PC'
                }else if(vm.pcTerminalType === false && vm.mobileTerminalType === true){
                    vm.displayTerminal = 'Mobile'
                }else if(vm.pcTerminalType === true && vm.mobileTerminalType === true){
                    vm.displayTerminal = 'PC,Mobile'
                }else if(vm.pcTerminalType === false && vm.mobileTerminalType === false) {
                    vm.displayTerminal = ''
                };
//                处理素材图片数据
                if(vm.headwearMapper){
                    vm.headwearMapper.fileName = vm.material_img_list[0]
                }else {
                    vm.headwearMapper = {
                        id : '',
                        fileName:vm.material_img_list[0]
                    }
                }

//                处理discription数据

                // vm.description =editor.html();
                // var new_content = ue_new.getContent();
                vm.description=ue_new.getContent();

                var data = {
                    productId :vm.productionId,
                    productName:vm.productName,
                    oneLevelCategoryId:vm.oneLevelCategoryId,
                    twoLevelCategoryMapper: vm.twoLevelCategoryMapper,
                    setMapperList: vm.setMapperList,
                    skus:vm.skus,
                    displayTerminal:vm.displayTerminal,
                    isNewProduct:vm.isNewProduct,
                    usedUrl:vm.usedUrl,
                    priceS:vm.priceS,
                    priceOldS:vm.priceOldS,
                    buyingPrice:vm.buyingPrice,
                    description:vm.description,
                    remarks:vm.remarks,
                    repeatExchanged:vm.repeatExchanged,
                    couponResource:vm.couponResource,
                    spendType:vm.spendType-0,
                    studentOrderValue:vm.studentOrderValue,
                    teacherOrderValue:vm.teacherOrderValue,
                    teacherVisible:vm.teacherVisible,
                    studentVisible:vm.studentVisible,
                    msgContent:vm.msgContent,
                    needSendMsg:vm.needSendMsg,
                    smsContent:vm.smsContent,
                    needSendSms:vm.needSendSms,
                    expiryDate:vm.expiryDate,
                    couponNo:vm.couponNo,
                    imgMapperList:vm.face_img_list,
                    headwearMapper:vm.headwearMapper,
                    onlined: vm.onlined,
                    relateVirtualItemId: vm.relateVirtualItemId,
                    relateVirtualItemContent: vm.relateVirtualItemContent,
                    juniorSchoolVisible:vm.juniorSchoolVisible,
                    primarySchoolVisible:vm.primarySchoolVisible

                };
//                1，食物,2流量包,3,头饰,4,微课,5优惠券,6教学资源,7托比装扮
                if(vm.oneLevelCategoryType === 1){
                    data.msgContent = '';
                    data.smsContent = '';
                    data.couponNo = '';
                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.buyingPrice === '' || vm.skus.length === 0 ||
                            vm.priceS === '' || vm.priceOldS === '' ||vm.description ==='' || vm.face_img_list.length === 0  || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }if(vm.oneLevelCategoryType === 2){
                    data.msgContent = '';
                    data.smsContent = '';
                    data.couponNo = '';
                    if( vm.oneLevelCategoryId === '' || vm.productName === '' || vm.priceS === '' || vm.priceOldS === '' || vm.buyingPrice === '' ||  vm.face_img_list.length === 0
                            || vm.repeatExchanged === '' || vm.couponResource === '' ||vm.description ==='' || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === ''|| vm.relateVirtualItemContent === '' || vm.relateVirtualItemId === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }else if(vm.oneLevelCategoryType === 3) {
                    data.couponNo = '';
                    data.msgContent = '';
                    data.smsContent = '';
                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.expiryDate === '' ||
                            vm.priceS === '' || vm.headwearMapper === '' || vm.priceOldS === '' ||vm.description ==='' || vm.face_img_list.length === 0  || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }else if(vm.oneLevelCategoryType === 4) {
                    data.couponNo = '';
                    data.msgContent = '';
                    data.smsContent = '';
                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.relateVirtualItemContent === '' ||
                            vm.priceS === '' || vm.priceOldS === '' ||vm.description ==='' || vm.face_img_list.length === 0 ||  vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }else if(vm.oneLevelCategoryType === 6) {
                    data.couponNo = '';
                    data.smsContent = '';
                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.usedUrl === '' ||
                            vm.priceS === ''|| vm.msgContent === '' || vm.priceOldS === '' ||vm.description ==='' || vm.face_img_list.length === 0  || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }else  if(vm.oneLevelCategoryType === 5 ) {

                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.buyingPrice === '' ||
                            vm.priceS === ''||  vm.msgContent === ''|| vm.smsContent === '' || vm.repeatExchanged === '' || vm.couponResource === '' || vm.priceOldS === '' ||vm.description ==='' || vm.face_img_list.length === 0  || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else if(vm.couponResource ==='DUIBA' && vm.usedUrl ===''){
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }else if(vm.oneLevelCategoryType === 7) {
                    data.couponNo = '';
                    data.msgContent = '';
                    data.smsContent = '';
                    if(vm.oneLevelCategoryId === ''||  vm.twoLevelCategoryMapper === null || vm.productName === ''|| vm.expiryDate === ''||
                            vm.priceS === '' ||  vm.priceOldS === ''|| vm.spendType === '' ||vm.description ==='' || vm.face_img_list === 0 ||  vm.headwearMapper === null || vm.displayTerminal === '' || vm.studentOrderValue === '' || vm.teacherOrderValue === '') {
                        vm.tip('必填项不能为空~')
                    }else {
                        vm.seave_ajax(data,is_jump);
                    }
                }

            },
//            保存时发送请求
            seave_ajax:function (data,is_jump) {
                var vm = this;
                $.ajax({
                    type: 'POST',
                    url: "/reward/crmproduct/upsert.vpage",
                    data: JSON.stringify(data),
                    dataType:'JSON',
                    contentType:'application/json',
                    async: false,
                    success: function (data) {
                        if(data.success){
                            vm.show_success_pops = true;
                            vm.success_info = data.info;
                            vm.productId = data.productId;
                            if (is_jump) {
                                location.replace('/reward/product/producttarget.vpage?productId=' + vm.productionId);
                            }
                        }else{
                            vm.tip(data.info);
                        }
                    }
                });
            },
            upload_stage: function (formData,pic_type) {

                var vm = this;
                if (pic_type === 'face' ) {
                    var url = '/reward/crmproduct/uploadproductimage.vpage';
                    if(vm.face_img_list.length === 5){
                        vm.tip ('最多只能传五张~');
                        return;
                    };
                }else if(pic_type === 'material' && vm.oneLevelCategoryType !== 7) {
                    var url = '/reward/crmproduct/uploadheadwearimg.vpage';
                    if(vm.material_img_list.length === 1){
                        vm.tip ('最多只能传一张~');
                        return;
                    };
                }else if(pic_type === 'material' && vm.oneLevelCategoryType === 7) {
                    var url = '/reward/crmproduct/uploadtobyimg.vpage';
                    if(vm.material_img_list.length === 1){
                        vm.tip ('最多只能传一张~');
                        return;
                    };
                }
                $.ajax({
                    url: url,
                    type: 'POST',
                    processData: false,
                    contentType: false,
                    data: formData,
                    async:false,
                    success: function(data){
                        if (data.success){
                            if(pic_type === 'face'){
                                vm.face_img_list.push({
                                    id:'',
                                    fileName:data.fileName
                                });
                            } else if(pic_type === 'material') {
                                vm.material_img_list.push(data.fileName);
                            }
                        }else {
                            vm.tip ('上传图片失败~');
                        }
                    },
                    error: function(xhr, status){
                        vm.uploading_show = false;
                        vm.file_name = '作品上传失败';
                    }
                });
            },
            upfile_init: function () {
                var vm = this;
                var fileObj = document.getElementById('file_dom').files[0];
                var formData = new FormData();
                formData.append('file', fileObj); // 文件对象
                var pic_type = 'face';
                vm.upload_stage(formData,pic_type);
            },
            upmaterial_iniit: function () {
                var vm = this;
                var fileObj = document.getElementById('material_dom').files[0];
                var formData = new FormData();
                formData.append('file', fileObj); // 文件对象
                var pic_type = 'material';
                vm.upload_stage(formData,pic_type);
            },
//             删除素材
            delect_material_pic: function () {
                var vm = this;
                vm.material_img_list.splice(0,1)
            },
//            删除图片
            delect_pic :function (index,pic_id) {
                if(vm.productionId === ''){
                    vm.face_img_list.splice(index,1)
                }else {
                    vm.face_img_list.splice(index,1);
                    $.ajax({
                        url: '/reward/crmproduct/deleteproductimage.vpage',
                        type: 'POST',
                        data: {imageId:pic_id},
                        success:function (data) {
                        }
                    })
                }

            },
            press_picture: function(link){
                if (link && link !== '' && link.indexOf("http") > -1) {
                    if(link.indexOf('oss-image.17zuoye.com') > -1 || link.indexOf('cdn-portrait.17zuoye.cn') > -1 || link.indexOf('cdn-portrait.test.17zuoye.net') > -1){
                        return link + "?x-oss-process=image/resize,w_200/quality,Q_90";
                    } else {
                        return link;
                    }
                } else {
                    return '<@app.avatar href='/'/>' + link;
                }
            },

        }
    })
    });
</script>
</@layout_default.page>