#!/bin/bash


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR=`readlink -f $DIR`
cd $DIR


SVN_BASE_URL=__dummy__
if [ "$STAGE" = "" ]; then
    STAGE=__dummy__
fi
if [ "$SVN_REV" = "" ]; then
    SVN_REV="HEAD"
fi

#FIXME: 将来应该只做检查，非root不能这么搞
ulimit -SHn 102400

if [ "$STAGE" = "" -o "$STAGE" = "__dummy__" ]; then
    echo "bad $0 script, please rebuild"
    exit 1
fi

#LOG_DIR=$DIR/logs
#因为磁盘io会影响gc写入从而造成gc卡顿，迁移至tmpfs
LOG_DIR=$DIR/logs
GCLOG_DIR=/dev/shm/service-gclog/


if [ "$EUID" = "0" ]; then
    echo "WARNING: YOU ARE RUNNING SERVICES AS ROOT, CHANGE TO NOBODY"
    EXEC="sudo -u nobody ./preinit.sh"

    if [ "$LOG_DIR" != "" ]; then
        chown -R nobody:nobody $LOG_DIR
        chown -R nobody:nobody /tmp/dubbo
    fi
else
    EXEC=""
fi


#如果nobody运行还有问题，就先不用EXEC前缀，先用root保证系统能起来
EXEC=""

ctl=$1

svn_update_ext() {
    if svn up --force --accept tf -r $SVN_REV $1 > /tmp/deployer-svnup.log 2>&1; then
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

svn_update() {

    if svn export -r $SVN_REV --force $SVN_BASE_URL/product.properties.$STAGE product.properties; then
        echo "export product.properties.$STAGE done"
    else
        echo "failed to export product.properties.$STAGE"
        exit 1
    fi

    if svn export -r $SVN_REV --force $SVN_BASE_URL/product.ini.$STAGE product.ini; then
            echo "export product.ini.$STAGE done"
        else
            echo "failed to export product.ini.$STAGE"
            exit 1
        fi

    if svn export -r $SVN_REV --force $SVN_BASE_URL/product.config.json.$STAGE product.config.json; then
        echo "export product.config.json.$STAGE done"
    else
        echo "failed to export product.config.json.$STAGE"
        exit 1
    fi

    if svn export -r $SVN_REV --force $SVN_BASE_URL/product.rsa.$STAGE product.rsa; then
        echo "export product.rsa.$STAGE done"
    else
        echo "failed to export product.rsa.$STAGE"
        exit 1
    fi

    if svn_update_ext $DIR; then
        echo "svn update done"
    else
        echo "failed to svn update"
        exit 1
    fi
    
    FILE_NAME='nt-config.xml'
    SERVICE_NAME=`basename $DIR`
	  GROUP_NAME=${STAGE}
	  COMMON_GROUP_NAME=`echo "${STAGE}" | cut -d '-' -f 1`
	
	  if test "X${STAGE//test/}" != "X${STAGE}" ; then
		    IP_PORT='cdo1a.test.17zuoye.net'
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
    
}

start() {
    logrotate
    MAIN_JAR_PATH=""
    MAIN_JAR_FN=""
    for fn in *.jar
       do
          if [ "$MAIN_JAR_PATH" != "" ]; then
            echo "Error: too many jar files in $DIR?"
          exit 1
       fi
       MAIN_JAR_FN=$fn
       MAIN_JAR_PATH=$DIR/$fn
    done

    echo "jar found: $MAIN_JAR_PATH"

    mkdir -p ${LOG_DIR}
    mkdir -p ${GCLOG_DIR}
    buildver=`cat BUILD.txt`
    pids=`ps ax | grep "java" | grep "$MAIN_JAR_PATH" | sed 's/^ *//g' | cut -d' ' -f 1`
    if [ "$pids" = "" ]; then

        JAVA_OPTS=""

        # heap, etc
        if [[ "$STAGE" == test ]] || [[ "$STAGE" =~ ^test-.* ]] || [[ "$STAGE" == staging ]]; then
            JAVA_OPTS="$JAVA_OPTS -Xmx2g"
        else
            SNAME=`basename $DIR`
            if [ "$SNAME" == utopia-schedule ]; then
                JAVA_OPTS="$JAVA_OPTS -Xmx12g"
            elif [ "$SNAME" == utopia-user-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            elif [ "$SNAME" == utopia-newhomework-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            elif [ "$SNAME" == utopia-wonderland-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            elif [ "$SNAME" == utopia-task-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            elif [ "$SNAME" == utopia-question-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            elif [ "$SNAME" == utopia-ai-provider ]; then
                JAVA_OPTS="$JAVA_OPTS -Xmx8g"
            elif [ "$SNAME" == utopia-hydra-agent ]; then
                JAVA_OPTS="$JAVA_OPTS -Xms12g -Xmx12g"
            else
                JAVA_OPTS="$JAVA_OPTS -Xmx4g"
            fi
        fi

        JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"

        JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow"

        JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=0"
        JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

        # http://stackoverflow.com/questions/137212/how-to-solve-performance-problem-with-java-securerandom
        JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"

        # http://stackoverflow.com/questions/27105004/what-means-javax-net-ssl-sslhandshakeexception-server-certificate-change-is-re
        JAVA_OPTS="$JAVA_OPTS -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true"

        # https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
        # use IPv4 only sockets
        JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

        # crash dump & heap dump
        JAVA_OPTS="$JAVA_OPTS -XX:ErrorFile=/tmp/jvm-crash-$MAIN_JAR_FN.log -XX:HeapDumpPath=/tmp"

        if [[ "$STAGE" == test ]] || [[ "$STAGE" =~ ^test-.* ]] || [[ "$STAGE" == staging ]]; then
            JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0"
        fi

        if [ "$STAGE" == production ]; then
            SERVICE_NAME=`basename $DIR`
            JAVA_OPTS="$JAVA_OPTS -Xloggc:$GCLOG_DIR/jvmgc-${SERVICE_NAME}-$(date +%y%m%d%H%M%S).log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime"
        fi

        LOG_OPTS="-DLOG_DIR=${LOG_DIR}"

        #JAVA_TOOL_OPTIONS: http://docs.oracle.com/javase/7/docs/webnotes/tsg/TSG-VM/html/envvars.html#gbmsy
        export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS $JAVA_OPTS $LOG_OPTS"
        $EXEC nohup java -server -d64 -D_BUILD_VERSION=$buildver -jar ${MAIN_JAR_PATH} > ${LOG_DIR}/log.out &
        echo "starting $MAIN_JAR_FN, JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS'"
        sleep 1 # must wait for some time, or the 'java' will exit soon
    else
        echo "$MAIN_JAR_FN already started: $pids"
    fi
}

stop() {

    MAIN_JAR_PATH=""
    MAIN_JAR_FN=""
    for fn in *.jar
       do
          if [ "$MAIN_JAR_PATH" != "" ]; then
            echo "Error: too many jar files in $DIR?"
          exit 1
       fi
       MAIN_JAR_FN=$fn
       MAIN_JAR_PATH=$DIR/$fn
    done

    echo "jar found: $MAIN_JAR_PATH"
    
    pids=`ps ax | grep "java" | grep "$MAIN_JAR_PATH" | sed 's/^ *//g' | cut -d' ' -f 1`
    if [ "$pids" != "" ]; then

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
            echo "failed to stop [$pids]:$MAIN_JAR_PATH"
            exit -1
        fi
    fi

    sleep 1
}

logrotate() {
    limit=4
    index=1
    logfilecount=$(ls ${LOG_DIR}|grep jvmgc|wc -l)
        if [ ${logfilecount} -gt $limit ]; then
            for file in $(ls ${LOG_DIR} -t|grep jvmgc); do
                if [ $index -gt $limit ]; then
                    echo "remove log $LOG_DIR/$file"
                    rm ${LOG_DIR}/${file}
                fi
                ((index++))
            done
        fi
}

if [ "$ctl" == "start" ]; then
    start
elif [ "$ctl" == "stop" ]; then
    stop
elif [ "$ctl" == "restart" ]; then
    stop
    start
elif [ "$ctl" == "update" ]; then
    svn_update
elif [ "$ctl" == "update-restart" ]; then
    stop
    svn_update
    start
elif [ "$ctl" == "log" ]; then
    shift
    tail ${LOG_DIR}/log.out $@
else
    echo "usage: $0 <start|stop|restart|update-restart|log>"
fi

