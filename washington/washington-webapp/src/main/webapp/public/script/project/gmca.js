/**
 * Created by Administrator on 2016/10/25.
 */
define(["jquery","voxLogs"], function ($) {
        $(function () {
        YQ.voxLogs({
            database: 'web_student_logs',
            userType: $userType,
            userId: $userId,
            module: "m_EnTJRrch",
            op: "o_Uaz0LhTV"
        });

        $(".btn01").on("click", function () {
            YQ.voxLogs({
                database: 'web_student_logs',
                userType: $userType,
                userId: $userId,
                module: "m_EnTJRrch",
                op: "o_n2N0MSJD"
            });
        });

        $(".btn02").on("click", function () {
            YQ.voxLogs({
                database: 'web_student_logs',
                userType: $userType,
                userId: $userId,
                module: "m_EnTJRrch",
                op: "o_uTlKIx5S"
            });
        });

    })
})
