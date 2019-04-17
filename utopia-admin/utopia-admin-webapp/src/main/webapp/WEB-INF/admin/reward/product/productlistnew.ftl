<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='商品管理' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/reward/skin.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div id="main_container" class="span9">
    <div id="productlist_page" v-cloak>
        <div class="product_box">
            <legend>商品管理</legend>
            <div class="new_product_button" @click="jump_detail('')">新建商品</div>
        </div>
        <div class="product_list_box">
            <div class="product_list_top">
                <div class="product_list_input">
                    <div class="product_list_one">
                        <label class="input_label">商品ID：</label>
                        <input class="input_text" type="text" placeholder="商品ID" v-model="product_id" />
                    </div>
                    <div class="product_list_one">
                        <label class="input_label">商品名称：</label>
                        <input class="input_text" type="text" placeholder="商品名称" v-model="product_name" />
                    </div>
                    <div class="product_list_one">
                        <div class="label_title">一级分类：</div>
                        <div class="label_name" @click.stop="one_level_button">{{one_level_name}}<i v-bind:class="{arrow_up: one_level_show,arrow_down: !one_level_show}"></i></div>
                        <div class="tag_list" v-if="one_level_show">
                            <span @click.stop="one_level_select(list, index)" v-for="(list, index) in oneLevelCategorySelector" v-bind:class="{active: one_level_index === index}">{{list.name}}</span>
                        </div>
                    </div>
                    <div class="product_list_one" v-show="two_category_show">
                        <div class="label_title">二级分类：</div>
                        <div class="label_name" @click.stop="two_level_button">{{two_level_name}}<i v-bind:class="{arrow_up: two_level_show,arrow_down: !two_level_show}"></i></div>
                        <div class="tag_list" v-if="two_level_show">
                            <span @click.stop="two_level_select(list, index)" v-for="(list, index) in twoLevelCategorySelector" v-bind:class="{active: two_level_index === index}">{{list.name}}</span>
                        </div>
                    </div>
                    <div class="product_list_one">
                        <div class="label_title">分类集合：</div>
                        <div class="label_name" @click.stop="collection_button">{{collection_name}}<i v-bind:class="{arrow_up: collection_show,arrow_down: !collection_show}"></i></div>
                        <div class="tag_list" v-if="collection_show">
                            <span @click.stop="collection_select(list, index)" v-for="(list, index) in setSelector" v-bind:class="{active: collection_index === index}">{{list.name}}</span>
                        </div>
                    </div>
                    <div class="product_list_one">
                        <div class="label_title">上架状态：</div>
                        <div class="label_name" @click.stop="state_button">{{state_name}}<i v-bind:class="{arrow_up: state_show,arrow_down: !state_show}"></i></div>
                        <div class="tag_list" v-if="state_show">
                            <span @click.stop="state_select(list, index)" v-for="(list, index) in onlinedSelector" v-bind:class="{active: state_index === index}">
                                {{online_list[list]}}
                            </span>
                        </div>
                    </div>
                    <div class="product_list_one">
                        <div class="label_title">学段：</div>
                        <div class="label_name" @click.stop="period_button">{{period_name}}<i v-bind:class="{arrow_up: period_show,arrow_down: !period_show}"></i></div>
                        <div class="tag_list" v-if="period_show">
                            <span @click.stop="period_select(list, index)" v-for="(list, index) in schoolVisibleSelector" v-bind:class="{active: period_index === index}">
                                {{period_list[list]}}
                            </span>
                        </div>
                    </div>
                    <div class="product_list_one">
                        <div class="label_title">用户：</div>
                        <div class="label_name" @click.stop="user_button">{{user_name}}<i v-bind:class="{arrow_up: user_show,arrow_down: !user_show}"></i></div>
                        <div class="tag_list" v-if="user_show">
                            <span @click.stop="user_select(list, index)" v-for="(list, index) in userVisibleSelector" v-bind:class="{active: user_index === index}">
                                {{user_list[list]}}
                            </span>
                        </div>
                    </div>
                </div>
                <div class="product_list_check" @click="check_product_button">查询</div>
                <div style="clear:both;"></div>
            </div>
            <div class="product_list_main">
                <div>
                    <ul class="pager">
                        <li><a href="javascript:;" @click="previous_button" v-if="page_number > 1">上一页</a></li>
                        <li><a href="javascript:;" @click="next_button" v-if="page_number < total_pages">下一页</a></li>
                        <li>当前第 {{page_number}} 页 |</li>
                        <li>跳转至<input class="jump_page" type="text" v-model="jump_to_page"/>页 <span @click="jump_the_page">确定</span> |</li>
                        <li>共 {{total_pages}} 页 |</li>
                        <li>共 {{total_elements}} 个商品</li>
                    </ul>
                    <div class="product_box">
                        <table class="table table-striped table-bordered" v-if="product_list && product_list.length">
                            <tbody>
                                <tr class="tr1">
                                    <td v-for="(sort, index) in sort_data"
                                        @click="sort_button(sort, index)">
                                        <div>
                                            {{sort.sort_name}}
                                            <i class="sort_default" v-if="sort.is_sort" v-bind:class="{
                                        sort_down: current_sort_index === index && upDown === 'up',
                                        sort_up: current_sort_index === index && upDown === 'down'
                                        }"></i>
                                        </div>
                                    </td>
                                </tr>
                                <tr v-for="list in product_list">
                                    <td class="tdwidth">
                                        <div>
                                            <img v-bind:src="press_picture(list.img)">
                                        </div>
                                    </td>
                                    <td class="tdwidth"><div>{{list.id}}</div></td>
                                    <td @click="jump_detail(list.id)" class="tdwidth2 pro_name"><div>{{list.productName}}</div></td>
                                    <td class="tdwidth"><div>{{list.priceS}}</div></td>
                                    <td class="tdwidth"><div>{{list.buyingPrice}}</div></td>
                                    <td class="tdwidth"><div>{{list.soldQuantity}}</div></td>
                                    <td class="tdwidth"><div>{{list.inventory}}</div></td>
                                    <td class="tdwidth2"><div>{{list.studentOrderValue}}</div></td>
                                    <td class="tdwidth2"><div>{{list.teacherOrderValue}}</div></td>
                                    <td class="tdwidth3"><div>{{list.setNames?list.setNames:''}}</div></td>
                                    <td class="tdwidth">
                                        <div>
                                            <a class="btn down" v-if="list.onlined" @click="product_online(list, false)">下架</a>
                                            <a class="btn up" v-else @click="product_online(list, true)">上架</a>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <div class="empty" v-else>
                            暂无商品
                        </div>
                    </div>
                </div>
                <ul class="pager">
                    <li><a href="javascript:;" @click="previous_button" v-if="page_number > 1">上一页</a></li>
                    <li><a href="javascript:;" @click="next_button" v-if="page_number < total_pages">下一页</a></li>
                    <li>当前第 {{page_number}} 页 |</li>
                    <li>跳转至<input class="jump_page" type="text" v-model="jump_to_page"/>页 <span @click="jump_the_page">确定</span> |</li>
                    <li>共 {{total_pages}} 页 |</li>
                    <li>共 {{total_elements}} 个商品</li>
                </ul>
            </div>
        </div>

        <div class="wrapper_fixed" v-if="makesure_show">
            <div class="wrapper_pop tip_pop">
                <p>{{makesure_text}}</p>
                <div class="pop_button">
                    <div class="delete_button" @click="cancel_button">取消</div>
                    <div class="right_button" @click="confirm_ok">确定</div>
                </div>
            </div>
        </div>

        <div class="wrapper_fixed" v-if="tip_show">
            <div class="wrapper_pop tip_pop">
                <p>{{tip_text}}</p>
                <div class="pop_button">
                    <div class="left_button" @click="close_button">好的</div>
                </div>
            </div>
        </div>

    </div>

</div>
<script type="text/javascript">
    new Vue({
        el:'#productlist_page',
        data:{
            total_pages: 0,
            total_elements: 0,
            page_number: 1,
            makesure_show: false,
            makesure_text: '',
            tip_text: '',
            tip_show: false,
            product_list: [],
            current_item: null,
            current_select_online: false,
            product_id: '',
            product_name: '',
            one_level_name: '全部',
            two_level_name: '全部',
            collection_name: '全部',
            state_name: '上架',
            period_name: '全部',
            user_name: '全部',
            online_list: ['全部', '上架', '下架'],
            period_list: ['全部', '小学', '中学'],
            user_list: ['全部', '学生', '老师'],
            oneLevelCategoryId: '',
            twoLevelCategoryId: '',
            setId: '',
            onlined: 1,
            schoolVisible: 0,
            userVisible: 0,
            orderBy: '',
            oneLevelCategorySelector: [],
            twoLevelCategorySelector: [],
            onlinedSelector: [],
            schoolVisibleSelector: [],
            setSelector: [],
            userVisibleSelector: [],
            two_category_show: false,
            one_level_show: false,
            two_level_show: false,
            collection_show: false,
            state_show: false,
            period_show: false,
            user_show: false,
            one_level_index: 0,
            two_level_index: 0,
            collection_index: 0,
            state_index: 1,
            period_index: 0,
            user_index: 0,
            current_sort_index: -1,
            jump_to_page: '',
            sort_data: [
                {
                    sort_name: '图片',
                    type: 'img',
                    is_sort: false
                },
                {
                    sort_name: 'ID',
                    type: 'id',
                    is_sort: true
                },
                {
                    sort_name: '名称',
                    type: 'name',
                    is_sort: false
                },
                {
                    sort_name: '售价',
                    type: 'priceS',
                    is_sort: true
                },
                {
                    sort_name: '成本',
                    type: 'buyingPrice',
                    is_sort: true
                },
                {
                    sort_name: '已售',
                    type: 'soldQuantity',
                    is_sort: true
                },
                {
                    sort_name: '库存',
                    type: 'inventory',
                    is_sort: true
                },
                {
                    sort_name: '学生排序',
                    type: 'studentOrderValue',
                    is_sort: true
                },
                {
                    sort_name: '老师排序',
                    type: 'teacherOrderValue',
                    is_sort: true
                },
                {
                    sort_name: '分类集合',
                    type: 'categorylist',
                    is_sort: false
                },
                {
                    sort_name: '操作',
                    type: 'set',
                    is_sort: false
                }
            ],
        },
        methods:{
            level_state: function (state1, state2, state3, state4, state5, state6) {
                var vm = this;
                vm.one_level_show = state1;
                vm.two_level_show = state2;
                vm.collection_show = state3;
                vm.state_show = state4;
                vm.period_show = state5;
                vm.user_show = state6;
            },
            one_level_button: function () {
                var vm = this;
                vm.level_state(!vm.one_level_show, false,false,false,false,false);
            },
            two_level_button: function () {
                var vm = this;
                vm.level_state(false, !vm.two_level_show,false,false,false,false);
            },
            collection_button: function () {
                var vm = this;
                vm.level_state(false, false,!vm.collection_show,false,false,false);
            },
            state_button: function () {
                var vm = this;
                vm.level_state(false, false,false,!vm.state_show,false,false);
            },
            period_button: function () {
                var vm = this;
                vm.level_state(false, false,false,false,!vm.period_show,false);
            },
            user_button: function () {
                var vm = this;
                vm.level_state(false, false,false,false,false,!vm.user_show);
            },
            one_level_select: function (item, index) {
                var vm = this;
                vm.one_level_index = index;
                vm.one_level_show = false;
                vm.one_level_name = item.name;
                vm.oneLevelCategoryId = item.id;
                vm.twoLevelCategorySelector = [];
                vm.two_level_init();
            },
            two_level_select:function (item, index) {
                var vm = this;
                vm.two_level_index = index;
                vm.two_level_show = false;
                vm.two_level_name = item.name;
                vm.twoLevelCategoryId = item.id;
            },
            collection_select: function (item, index) {
                var vm = this;
                vm.collection_index = index;
                vm.collection_show = false;
                vm.collection_name = item.name;
                vm.setId = item.id;
            },
            state_select: function (item, index) {
                var vm = this;
                vm.state_index = index;
                vm.state_show = false;
                vm.state_name = vm.online_list[item];
                vm.onlined = item;
            },
            period_select: function (item, index) {
                var vm = this;
                vm.period_index = index;
                vm.period_show = false;
                vm.period_name = vm.period_list[item];
                vm.schoolVisible = item;
            },
            user_select: function (item, index) {
                var vm = this;
                vm.user_index = index;
                vm.user_show = false;
                vm.user_name = vm.user_list[item];
                vm.userVisible = item;
            },
            previous_button: function () {
                var vm = this;
                vm.page_number = parseInt(vm.page_number) - 1;
                vm.product_list_init();
            },
            next_button: function () {
                var vm = this;
                vm.page_number = parseInt(vm.page_number) + 1;
                vm.product_list_init();
            },
            jump_the_page:function () {
                var vm = this;
                vm.page_number = vm.jump_to_page;
                vm.product_list_init();
            },
            sort_button: function (sort,index) {
                var vm = this;

                if (!sort.is_sort) {
                    return;
                }

                if (sort.type !== vm.orderBy) {
                    vm.upDown = 'up';
                } else {
                    if (vm.upDown === '') {
                        vm.upDown = 'up';
                    } else if (vm.upDown === 'up') {
                        vm.upDown = 'down';
                    } else {
                        vm.upDown = '';
                    }
                }

                vm.orderBy = sort.type;
                vm.current_sort_index = index;
                vm.page_number = 1;
                vm.product_list_init();
            },
            jump_detail: function (id) {
                if (id) {
                    window.open('editproduct.vpage?productId=' + id);
                } else {
                    window.open('editproduct.vpage');
                }
            },
            two_level_init: function () {
                var vm = this;
                $.ajax({
                    url: "/reward/newcategory/list.vpage",
                    data: {
                        parentId: vm.oneLevelCategoryId
                    },
                    success: function (res) {
                        vm.twoLevelCategorySelector = res.categoryList || [];
                        if (vm.twoLevelCategorySelector.length) {
                            vm.two_category_show = true;
                            vm.twoLevelCategorySelector.unshift({
                                id: null,
                                isSelected: false,
                                name: '全部'
                            });
                        } else {
                            vm.two_category_show = false;
                        }
                    }
                });
            },
            press_picture: function (link) {
                if (link && link != "" && (link.indexOf('oss-image.17zuoye.com') > -1 || link.indexOf('cdn-portrait.17zuoye.cn') > -1 || link.indexOf('cdn-portrait.test.17zuoye.net') > -1)) {
                    return link + '?x-oss-process=image/resize,w_200/quality,Q_90';
                } else {
                    return link;
                }
            },
            check_product_button: function () {
                var vm = this;
                vm.page_number = 1;
                vm.jump_to_page = '';
                vm.product_list_init();
            },
            selector_init: function () {
                var vm = this;
                $.ajax({
                    url: "/reward/crmproduct/selector.vpage",
                    success: function (res) {
                        vm.oneLevelCategorySelector = res.oneLevelCategorySelector || [];
                        vm.onlinedSelector = res.onlinedSelector || [];
                        vm.schoolVisibleSelector = res.schoolVisibleSelector || [];
                        vm.setSelector = res.setSelector || [];
                        vm.userVisibleSelector = res.userVisibleSelector || [];
                    }
                });
            },
            product_list_init: function () {
                var vm = this;
                $.ajax({
                    url: "/reward/crmproduct/productlist.vpage",
                    data: {
                        productId: $.trim(vm.product_id),
                        productName: $.trim(vm.product_name),
                        oneLevelCategoryId: vm.oneLevelCategoryId,
                        twoLevelCategoryId: vm.twoLevelCategoryId,
                        setId: vm.setId,
                        onlined: vm.onlined,
                        schoolVisible: vm.schoolVisible,
                        userVisible: vm.userVisible,
                        orderBy: vm.orderBy,
                        upDown: vm.upDown,
                        pageNumber: vm.page_number
                    },
                    success: function (res) {
                        if (res.success) {
                            vm.product_list = res.productPage.content || [];
                            vm.total_elements = res.productPage.totalElements || 0;
                            vm.total_pages = res.productPage.totalPages || 0;

                        }
                    }
                });
            },
            product_online: function (item, online) {
                var vm = this;

                var txt = online ? '上架':'下架';
                vm.makesure_text = '确定' + txt + '吗？';
                vm.makesure_show = true;
                vm.current_item = item;
                vm.current_select_online = online;
            },
            cancel_button: function () {
                var vm = this;
                vm.makesure_show = false;
            },
            confirm_ok: function () {
                var vm =this;
                $.ajax({
                    url: "/reward/crmproduct/updownlined.vpage",
                    type: 'POST',
                    data: {
                        productId: vm.current_item.id,
                        onLined: vm.current_select_online
                    },
                    success: function (res) {
                        vm.makesure_show = false;
                        vm.tip_text = res.info;
                        vm.tip_show = true;
                        vm.product_list_init();
                    }
                });
            },
            close_button: function () {
                this.tip_show = false;
            }
        },
        created:function(){
            var vm = this;

            vm.product_list_init();
            vm.selector_init();
            $(document).on('click', function () {
                vm.level_state(false, false,false,false,false,false);
            });

            /*enter键...*/
            $(document).keydown(function (event) {
                if (event.keyCode === 13) {
                    vm.product_list_init();
                }
            });
        }
    });

</script>
</@layout_default.page>