package com.hwx.safelock.safelock.db;


import com.hwx.safelock.safelock.Application;

public class GreenDaoManager {
    private static GreenDaoManager mInstance; //单例
    private DaoMaster mDaoMaster; //以一定的模式管理Dao类的数据库对象
    private DaoSession mDaoSession; //管理制定模式下的所有可用Dao对象
    public GreenDaoManager() {
        if (mInstance == null) {
            // 相当于得到数据库帮助对象，用于便捷获取db
            // 这里会自动执行upgrade的逻辑.backup all table→del all table→create all new table→restore data
            UpgradeHelper helper = new UpgradeHelper(Application.getContext(), "safe_lock.db", null);//处理自动升级数据库
            //DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(Application.getContext(), "HWX_Cocktail.db", null);
            mDaoMaster = new DaoMaster(helper.getWritableDatabase());
            mDaoSession=mDaoMaster.newSession();
        }
    }
    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            synchronized (GreenDaoManager.class) {
                if (mInstance == null) {
                    mInstance = new GreenDaoManager();
                }
            }
        }
        return mInstance;
    }
    public DaoMaster getMaster() {
        return mDaoMaster;
    }
    public DaoSession getSession() {
        return mDaoSession;
    }
    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }
}