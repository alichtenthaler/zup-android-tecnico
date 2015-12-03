package com.ntxdev.zuptecnico.storage.service;

import android.content.Context;

import com.ntxdev.zuptecnico.entities.Group;
import com.ntxdev.zuptecnico.entities.User;
import com.snappydb.DB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 8/3/2015.
 */
public class GroupService extends BaseService {
    public GroupService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("groups");
    }

    public Group getGroup(int id) {
        Group item = getObject("group_" + id, Group.class);
        return item;
    }

    public void addGroup(Group group) {
        List<Integer> ids = getObjectList("groups", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(group.getId())) {
            ids.add(group.getId());
            setList("groups", ids);
        }

        saveGroup(group);
    }

    public void saveGroup(Group group) {
        setObject("group_" + group.getId(), group);
    }
}
