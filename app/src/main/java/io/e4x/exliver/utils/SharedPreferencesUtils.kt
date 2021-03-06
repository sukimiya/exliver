package io.e4x.exliver.utils

import android.content.Context
import android.content.SharedPreferences
import io.e4x.exliver.app.App

class SharedPreferencesUtils {
    private var share: SharedPreferences
    private var editor: SharedPreferences.Editor
    private val SHARED_NAME = "record" //sp的文件名 自定义
    init {
        share = App.getContext().getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
        editor = share!!.edit()
    }

    fun putInt(spName: String?, value: Int) {
        editor!!.putInt(spName, value)
        editor!!.commit()
    }

    fun getInt(spName: String?, defaultvalue: Int): Int {
        return share!!.getInt(spName, defaultvalue)
    }

    /**
     * ------- String ---------
     */
    fun putString(spName: String?, value: String?) {
        editor!!.putString(spName, value)
        editor!!.commit()
    }

    fun getString(spName: String?, defaultvalue: String?): String? {
        return share!!.getString(spName, defaultvalue)
    }

    fun getString(spName: String?): String? {
        return share!!.getString(spName, "")
    }


    /**
     * ------- boolean ---------
     */
    fun putBoolean(key: String?, value: Boolean) {
        editor!!.putBoolean(key, value)
        editor!!.commit()
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return share!!.getBoolean(key, defValue)
    }

    /**
     * ------- float ---------
     */
    fun putFloat(key: String?, value: Float) {
        editor!!.putFloat(key, value)
        editor!!.commit()
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return share!!.getFloat(key, defValue)
    }


    /**
     * ------- long ---------
     */
    fun putLong(key: String?, value: Long) {
        editor!!.putLong(key, value)
        editor!!.commit()
    }

    fun getLong(key: String?, defValue: Long): Long {
        return share!!.getLong(key, defValue)
    }

    /**
     * 清空SP里所有数据 谨慎调用
     */
    fun clear() {
        editor!!.clear() //清空
        editor!!.commit() //提交
    }

    /**
     * 删除SP里指定key对应的数据项
     *
     * @param key
     */
    fun remove(key: String?) {
        editor!!.remove(key) //删除掉指定的值
        editor!!.commit() //提交
    }

    /**
     * 查看sp文件里面是否存在此 key
     *
     * @param key
     * @return
     */
    operator fun contains(key: String?): Boolean {
        return share!!.contains(key)
    }

    companion object{
        //建议 所有的Key 以为常量的形式保存在此类里面
        val USER_NAME = "name" //例如
        /**
         * 单例模式
         */
        private var instance //单例模式 双重检查锁定
                : SharedPreferencesUtils? = null
        fun getInstance(): SharedPreferencesUtils? {
            if (instance == null) {
                synchronized(SharedPreferencesUtils::class.java) {
                    if (instance == null) {
                        instance = SharedPreferencesUtils()
                    }
                }
            }
            return instance
        }
    }
}