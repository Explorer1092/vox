#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR=`readlink -f $DIR`
cd $DIR/..
WEBAPPS_ROOT=`pwd`
RESIN_HOME=$WEBAPPS_ROOT/env/apps/resin-4
SVN_BASE_URL=__dummy__
if [ "$STAGE" = "" ]; then
    STAGE=__dummy__
fi
if [ "$SVN_REV" = "" ]; then
    SVN_REV="HEAD"
fi

GCLOG_DIR=/dev/shm/webapp-gclog/

#FIXME: 将来应该只做检查，非root不能这么搞
ulimit -SHn 102400

name=$1
ctl=$2

if [ "$STAGE" = "" -o "$STAGE" = "__dummy__" ]; then
    echo "bad $0 script, please rebuild"
    exit 1
fi

if [ "$name" = "" -o "$ctl" = "" ]; then
    echo "Usage: $0 <name> [update-app|stop-app|restart-app|log] [...]"
    echo "   eg: $0 washington update-app [...]"
    echo "   eg: $0 all log -f"
    exit 1
fi

if [ "$EUID" == "0" ]; then
    echo "WARNING: YOU ARE RUNNING SERVICES AS ROOT, CHANGE TO NOBODY"
    EXEC="sudo -u nobody ./env/preinit.sh"
    if [ "$RESIN_HOME" != "" ]; then
        chown -R nobody:nobody $RESIN_HOME
        chown -R nobody:nobody /tmp/dubbo
    fi
else
    EXEC=""
fi

#如果nobody运行还有问题，就先不用EXEC前缀，先用root保证系统能起来
EXEC=""

system_names="all admin washington agent wechat ucenter mizar luffy somalia andromeda galaxy"
if [[ ! ( "$system_names" =~ (^| )$name($| ) ) ]]; then
    echo "name: $system_names"
    exit 1
fi

webapp_dir_names="$name-webapp"

if [ "$name" = "admin" ]; then
    webapp_dir_names="utopia-admin-webapp";
fi


# 更新静态文件时，不操作utopia-crossdomain-webapp目录
if [ "$ctl" != "update-static" ]; then
    webapp_dir_names="utopia-crossdomain-webapp $webapp_dir_names"
fi

RESIN_CTL_CONF="$EXEC $RESIN_HOME/bin/resin.sh --conf $WEBAPPS_ROOT/env/stage/$STAGE/resin-$STAGE-$name.xml"



svnver=`svn --version | grep "version 1.7"`
if [ "$svnver" = "" ]; then
    echo "subversion 1.7 is required, install:"
    echo "yum install -y apr apr-util neon"
    echo "yum install -y http://soft.17zuoye.net/subversion-1.7.rpm"
    exit 1
fi


stop_resin_and_wait() {
    app_name=$1

    pids=`ps ax | grep "java" | grep $WEBAPPS_ROOT/ | grep "Dresin.server=$app_name" | sed 's/^ *//g' | cut -d' ' -f 1`
    if [ "$pids" != "" ]; then

        echo "trying to stop $app_name by resin ..."
        $RESIN_CTL_CONF stop --server $app_name &

        #wait resin stop first
        for i in 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59; do
            fullproc=`ps --no-heading -fp $pids`
            if [ "$fullproc" = "" ]; then break; fi
            echo "waiting for resin stop ..."
            sleep 1
        done

        for i in 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59; do
            fullproc=`ps --no-heading -fp $pids`
            if [ "$fullproc" = "" ]; then break; fi
            echo "try to kill $pids ..."
            kill $pids
            sleep 1
        done

        for i in 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59; do
            fullproc=`ps --no-heading -fp $pids`
            if [ "$fullproc" = "" ]; then break; fi
            echo "try to kill -9 $pids ..."
            kill -9 $pids
            sleep 1
        done

        fullproc=`ps --no-heading -fp $pids`
        if [ "$fullproc" != "" ]; then
            echo "failed to stop [$pids]:$app_name"
            exit -1
        fi
    fi
    sleep 1
}



stop_app() {

    has_app_process=`ps ax | grep "java" | grep "Dresin.server=app-$name"`
    if [ "$has_app_process" != "" ]; then
        app_names=`$RESIN_CTL_CONF status | grep -o "'app-$name[^']*'" | tr -d "'"`
    fi

    if [ "$app_names" = "" ]; then
        app_names="app-$name"
        echo "no resin app from resin status, clean $app_names"
    fi

    for app_name in $app_names; do
        stop_resin_and_wait $app_name
    done

}

start_app() {
    mkdir -p ${GCLOG_DIR}

    #echo "if failed, comment out the 'Defaults requiretty' in /etc/sudoers, or use su -s /bin/bash -c 'xxxxx', or use force tty 'ssh -t -t' "
    #nohup $RESIN_CTL_CONF start-all &
    #tail -f $RESIN_HOME/log/jvm-*$name*.log -n 0
    #change to no block mode
    nohup $RESIN_CTL_CONF start-all &
    sleep 1s
    local _jvm_app_log="$RESIN_HOME/log/jvm-*$name*.log"
    if test -e ${_jvm_app_log} ; then
        local _log_line_count=$(wc -l ${_jvm_app_log} | cut -d ' ' -f 1)
        local _start_line=$(( _log_line_count + 1 ))
        until tail -n +${_log_line_count} ${_jvm_app_log} | grep -i 'WebApp\[production/webapp/default/ROOT\] active' -q ; do
            sleep 0.3s
            _end_line=$(wc -l ${_jvm_app_log} | cut -d ' ' -f 1)
            # if log file had been cuted during restart,searching from line 1 for 'WebApp[production/webapp/default/ROOT] active'
            if test ${_end_line} -lt ${_log_line_count}; then
                _start_line=1
                _log_line_count=1
            fi
            sed -n "${_start_line},${_end_line}p" ${_jvm_app_log}
            _start_line=$(( _end_line + 1))
        done
        tail -n +${_start_line} ${_jvm_app_log}
    else
        echo "Not found log file : ${_jvm_app_log}"
    fi	      
}

restart_app() {
    stop_app
    start_app
}

svn_update_ext() {
    if svn up --force --accept tf -r $SVN_REV $1 2>&1 | tee /tmp/deployer-svnup-${name}.log; then
        return 0
    fi

    if grep E155004 /tmp/deployer-svnup.log > /dev/null; then
        echo "INFO: $1 is locked, and clean up later"
        svn cleanup $1
        echo "INFO: retry to update $1"
        if svn up --force --accept tf -r $SVN_REV $1; then
            return 0
        fi
    fi

    echo "WARNING: FAILED TO UPDATE $1"
    return 1
}

svn_update_env() {
    if svn_update_ext $WEBAPPS_ROOT/env; then
        true
    else
        echo "WARNING: FAILED TO UPDATE ENV. You can ignore this warning if there is no change to webappctl.sh/resin-config"
    fi
}

svn_update(){

    cd $WEBAPPS_ROOT
    svn_update_env

    for webapp_dir_name in $webapp_dir_names; do

        if [ ! -e $WEBAPPS_ROOT/$webapp_dir_name ]; then
            if svn co -r $SVN_REV "$SVN_BASE_URL/$webapp_dir_name" $webapp_dir_name; then
                true
            else
                echo "failed to svn checkout $SVN_BASE_URL/$webapp_dir_name"
                exit 1
            fi
        else
            if svn_update_ext $WEBAPPS_ROOT/$webapp_dir_name; then
                true
            else
                if [ "$webapp_dir_name" != "utopia-crossdomain-webapp" ]; then
                    echo "failed to svn update $webapp_dir_name"
                    exit 1
                fi
            fi
        fi

        if [ "$EUID" == "0" ]; then
            mkdir -p $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/tmp
            chown -R nobody:nobody $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/tmp
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.properties.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.properties; then
            true
        else
            echo "failed to export product.properties.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.ini.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.ini; then
            true
        else
            echo "failed to export product.ini.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.config.json.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.config.json; then
            true
        else
            echo "failed to export product.config.json.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.rsa.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.rsa; then
            true
        else
            echo "failed to export product.rsa.$STAGE"
            exit 1
        fi
        
        if test "X${webapp_dir_name}" != "Xutopia-crossdomain-webapp" ; then
            FILE_NAME="$WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/nt-config.xml"
            SERVICE_NAME=$name
            GROUP_NAME=${STAGE}
            COMMON_GROUP_NAME=`echo "${STAGE}" | cut -d '-' -f 1`
            echo "STAGE:${STAGE}"

            if test "X${STAGE//test/}" != "X${STAGE}" ; then
                IP_PORT='cdo1a.test.17zuoye.net'
				if [ ${STAGE} == "test-dragon" ]; then
					COMMON_GROUP_NAME="test-dragon";
				fi
				if [ ${STAGE} == "test-content" ]; then
					COMMON_GROUP_NAME="test-content";
				fi
				if [ ${STAGE} == "test-hydra" ]; then
					COMMON_GROUP_NAME="test-hydra";
				fi
            else
                COMMON_GROUP_NAME="${COMMON_GROUP_NAME}-zw"
                IP_PORT='cdi7a.17zuoye.net'
            fi
            cat - > ${FILE_NAME} << EOF
<?xml version="1.0" encoding="utf-8"?>
        <configurations>
                <configuration serviceName="${SERVICE_NAME}" groupName="${GROUP_NAME}" commonGroupName="${COMMON_GROUP_NAME}" server="${IP_PORT}"/>
        </configurations>
EOF
        fi      
        
    done

}

svn_update_config(){

    cd $WEBAPPS_ROOT

    for webapp_dir_name in $webapp_dir_names; do

        if [ "$EUID" == "0" ]; then
            mkdir -p $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/tmp
            chown -R nobody:nobody $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/tmp
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.properties.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.properties; then
            true
        else
            echo "failed to export product.properties.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.ini.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.ini; then
            true
        else
            echo "failed to export product.ini.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.config.json.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.config.json; then
            true
        else
            echo "failed to export product.config.json.$STAGE"
            exit 1
        fi

        if svn export -r $SVN_REV --force $SVN_BASE_URL/product.rsa.$STAGE $WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/product.rsa; then
            true
        else
            echo "failed to export product.rsa.$STAGE"
            exit 1
        fi

        if test "X${webapp_dir_name}" != "Xutopia-crossdomain-webapp" ; then
            FILE_NAME="$WEBAPPS_ROOT/$webapp_dir_name/webroot/WEB-INF/nt-config.xml"
            SERVICE_NAME=$name
            GROUP_NAME=${STAGE}
            COMMON_GROUP_NAME=`echo "${STAGE}" | cut -d '-' -f 1`
            echo "STAGE:${STAGE}"

            if test "X${STAGE//test/}" != "X${STAGE}" ; then
                IP_PORT='cdo1a.test.17zuoye.net'
				if [ ${STAGE} == "test-dragon" ]; then
					COMMON_GROUP_NAME="test-dragon";
				fi
				if [ ${STAGE} == "test-content" ]; then
					COMMON_GROUP_NAME="test-content";
				fi
				if [ ${STAGE} == "test-hydra" ]; then
					COMMON_GROUP_NAME="test-hydra";
				fi
            else
                COMMON_GROUP_NAME="${COMMON_GROUP_NAME}-zw"
                IP_PORT='cdi7a.17zuoye.net'
            fi
            cat - > ${FILE_NAME} << EOF
<?xml version="1.0" encoding="utf-8"?>
        <configurations>
                <configuration serviceName="${SERVICE_NAME}" groupName="${GROUP_NAME}" commonGroupName="${COMMON_GROUP_NAME}" server="${IP_PORT}"/>
        </configurations>
EOF
        fi

    done

}

# 更新静态文件时，不需要更新env目录，直接更新指定的静态文件目录
svn_update_static() {
    static_subdirs=""
    if [ "$name" = "washington" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/ftl/"
    elif [ "$name" = "admin" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/admin/"
    elif [ "$name" = "agent" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/agent/"
    elif [ "$name" = "wechat" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/ftl/"
    elif [ "$name" = "ucenter" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/ftl/"
	elif [ "$name" = "mizar" ]; then
        static_subdirs="webroot/public/ webroot/WEB-INF/mizar/"
    elif [ "$name" = "luffy" ]; then
        static_subdirs="webroot/public/"
    else
        return
    fi

    cd $WEBAPPS_ROOT

    webapp_dir_name=$webapp_dir_names
    if [ ! -e $WEBAPPS_ROOT/$webapp_dir_name ]; then
        echo "no found app - $WEBAPPS_ROOT/$webapp_dir_name"
        exit 1
    fi

    for subdir in ${static_subdirs}; do
        if svn_update_ext $WEBAPPS_ROOT/$webapp_dir_name/$subdir; then
            true
        else
            echo "failed to update $WEBAPPS_ROOT/$webapp_dir_name/$subdir"
            exit 1
        fi
    done
}


update_restart_app() {
    stop_app
    svn_update
    start_app
}

update_static() {
    svn_update_static
}

update_config() {
    svn_update_config
}


if [ "$name" = "all" ]; then

    if [ "$ctl" = "log" ]; then

        shift
        shift
        tail $RESIN_HOME/log/jvm-*.log $@

    elif [ "$ctl" = "history" ]; then

        svn log -l 10 $SVN_BASE_URL

    elif [ "$ctl" = "ps-grep" ]; then

        ps ax | grep "java" | grep "$WEBAPPS_ROOT/" | grep "Dresin.\(server\|watchdog\)=.*$3"

    elif [ "$ctl" = "ps-pid" ]; then

        ps ax | grep "java" | grep "$WEBAPPS_ROOT/" | grep "Dresin.\(server\|watchdog\)=.*$3" | sed 's/^ *//g' | cut -d' ' -f 1

    else
        echo "unknown command"
    fi

elif [ "$ctl" = "update-app" -o "$ctl" = "update-restart" ]; then

    update_restart_app

elif [ "$ctl" = "update-static" ]; then

    update_static

elif [ "$ctl" = "update-config" ]; then

    update_config

elif [ "$ctl" = "restart-app" -o "$ctl" = "restart" ]; then

    restart_app

elif [ "$ctl" = "svn-update" -o "$ctl" = "update" ]; then

    svn_update

elif [ "$ctl" = "stop-app" -o "$ctl" = "stop" ]; then

    stop_app

elif [ "$ctl" = "log" ]; then

    shift
    shift
    tail $RESIN_HOME/log/jvm-*-$name*.log $@

else

    shift
    $RESIN_CTL_CONF $@

fi
