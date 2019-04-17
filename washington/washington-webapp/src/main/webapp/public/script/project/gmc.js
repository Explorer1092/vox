/**
 * Created by Administrator on 2016/10/25.
 */
define(["jquery","voxLogs","weui"], function ($) {
    $(function () {
        if ($userType == 2) {
            YQ.voxLogs({
                database: 'parent',
                userType: $userType,
                userId: $userId,
                module: "m_EnTJRrch",
                op: "o_Uaz0LhTV"
            });

            $(".btn01").on("click", function () {
                $.showLoading();
                location.href = "/redirector/apps/go.vpage?app_key=GlobalMath&sid=" + _sid;
                YQ.voxLogs({
                    database: 'parent',
                    userType: $userType,
                    userId: $userId,
                    module: "m_EnTJRrch",
                    op: "o_n2N0MSJD"
                });
            });

            $(".btn02").on("click", function () {
                $.showLoading();
                location.href = "/redirector/apps/go.vpage?basic=gmc_4th&app_key=GlobalMath&sid=" + _sid;
               YQ.voxLogs({
                    database: 'parent',
                    userType: $userType,
                    userId: $userId,
                    module: "m_EnTJRrch",
                    op: "o_uTlKIx5S"
                });
            });
        }
            //student
            if ($userType == 3) {
                YQ.voxLogs({
                    database: 'student',
                    userType: $userType,
                    userId: $userId,
                    module: "m_EnTJRrch",
                    op: "o_Uaz0LhTV"
                });

                $(".btn01").on("click", function () {
                    $.showLoading();
                    location.href = "/redirector/apps/go.vpage?app_key=GlobalMath";
                    YQ.voxLogs({
                        database: 'student',
                        userType: $userType,
                        userId: $userId,
                        module: "m_EnTJRrch",
                        op: "o_n2N0MSJD"
                    });
                });

                $(".btn02").on("click", function () {
                    $.showLoading();
                    location.href = "/redirector/apps/go.vpage?basic=gmc_4th&app_key=GlobalMath";
                    YQ.voxLogs({
                        database: 'student',
                        userType: $userType,
                        userId: $userId,
                        module: "m_EnTJRrch",
                        op: "o_uTlKIx5S"
                    });
                })
            }

    })
})
