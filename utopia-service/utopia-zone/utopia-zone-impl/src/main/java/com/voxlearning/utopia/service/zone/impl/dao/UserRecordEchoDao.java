package com.voxlearning.utopia.service.zone.impl.dao;

import com.voxlearning.alps.dao.aerospike.persistence.StaticAsyncAerospikePersistence;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;

import javax.inject.Named;

/**
 * UserRecordEcho Dao
 * Created by Yuechen.Wang on 17/11/06.
 */
@Named
public class UserRecordEchoDao extends StaticAsyncAerospikePersistence<UserRecordEcho, String> {

}
