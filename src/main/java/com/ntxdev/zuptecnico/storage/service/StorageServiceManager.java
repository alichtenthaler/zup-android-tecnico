package com.ntxdev.zuptecnico.storage.service;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

/**
 * Created by Igor on 8/3/2015.
 */
public class StorageServiceManager {
    private DB mDB;
    private Context mContext;

    private GroupService mGroupService;
    private ReportCategoryService mReportCategoryService;
    private ReportItemService mReportItemService;
    private UserService mUserService;

    public StorageServiceManager(Context context) throws SnappydbException {
        this.mContext = context;

        this.mGroupService = new GroupService(this);
        this.mReportCategoryService = new ReportCategoryService(this);
        this.mReportItemService = new ReportItemService(this);
        this.mUserService = new UserService(this);

        mDB = DBFactory.open(mContext);
    }

    public void clear() {
        this.mGroupService.clear();
        this.mReportCategoryService.clear();
        this.mReportItemService.clear();
        this.mUserService.clear();
    }

    public void close() throws SnappydbException  {
        mDB.close();
    }

    protected void commit() throws SnappydbException {
        mDB.close();
        mDB = DBFactory.open(mContext);
    }

    protected DB getDB() {
        return mDB;
    }

    public Context getContext() {
        return mContext;
    }

    public GroupService group() {
        return mGroupService;
    }

    public ReportCategoryService reportCategory() {
        return mReportCategoryService;
    }

    public ReportItemService reportItem() {
        return mReportItemService;
    }

    public UserService user() {
        return mUserService;
    }
}
