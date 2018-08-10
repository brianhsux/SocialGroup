package com.brianhsu.socialgroup.Utilities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.brianhsu.socialgroup.model.Resource

import com.cloudinary.android.Logger

import java.util.ArrayList
import java.util.Date

class CloudinarySqliteHelper(context: Context) : SQLiteOpenHelper(context, "cloudinary", null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LOCAL_ID_COL + " TEXT, "
                + PUBLIC_ID_COL + " TEXT,"
                + REQUEST_ID_COL + " TEXT,"
                + RESOURCE_TYPE_COL + " TEXT,"
                + STATUS_COL + " TEXT,"
                + LAST_ERROR_COL + " INTEGER,"
                + LAST_ERROR_DESC_COL + " TEXT, "
                + STATUS_TIMESTAMP_COL + " INTEGER,"
                + NAME_COL + " TEXT,"
                + DELETE_TOKEN_COL + " TEXT);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (newVersion) {
            2 -> db.execSQL("ALTER TABLE $TABLE ADD COLUMN $NAME_COL TEXT;")
        }
    }

    fun setUploadResultParams(requestId: String, publicId: String?, deleteToken: String?, status: Resource.UploadStatus, lastError: Int, lastErrorDesc: String?): Int {
        val values = ContentValues()
        values.put(PUBLIC_ID_COL, publicId)
        values.put(DELETE_TOKEN_COL, deleteToken)
        values.put(STATUS_TIMESTAMP_COL, Date().time)
        values.put(STATUS_COL, status.name)
        values.put(LAST_ERROR_COL, lastError)
        values.put(LAST_ERROR_DESC_COL, lastErrorDesc)
        return writableDatabase.update(TABLE, values, "$REQUEST_ID_COL=?", arrayOf(requestId))
    }

    fun insertOrUpdateQueuedResource(localId: String?, name: String?, requestId: String?, resourceType: String?, status: Resource.UploadStatus): Boolean {
        val values = ContentValues()

        values.put(REQUEST_ID_COL, requestId)
        values.put(NAME_COL, name)
        values.put(RESOURCE_TYPE_COL, resourceType)
        values.put(STATUS_COL, status.name)
        values.put(STATUS_TIMESTAMP_COL, Date().time)

        var exists = exists(localId)
        if (exists) {
            val updated = writableDatabase.update(TABLE, values, "$LOCAL_ID_COL=?", arrayOf(localId))
            Logger.d(TAG, String.format("Setting request id %s for local id %s, updated rows: %d", requestId, localId, updated))
            exists = true
        } else {
            values.put(LOCAL_ID_COL, localId)
            writableDatabase.insert(TABLE, null, values)
        }

        return exists
    }

    fun exists(localId: String?): Boolean {
        val query = readableDatabase.query(TABLE, null, "$LOCAL_ID_COL=?", arrayOf(localId), null, null, null)
        val exists = query.moveToFirst()
        query.close()
        return exists
    }

    fun listAll(): List<Resource> {
        val cursor = readableDatabase.query(TABLE, null, null, null, null, null, ID_COL)
        val res = ArrayList<Resource>()
        try {
            buildResource(cursor!!, res)
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return res
    }

    private fun buildResource(cursor: Cursor, res: MutableList<Resource>) {
        if (cursor.moveToFirst()) {
            val localIdIdx = cursor.getColumnIndex(LOCAL_ID_COL)
            val remoteIdIdx = cursor.getColumnIndex(PUBLIC_ID_COL)
            val requestIdIdx = cursor.getColumnIndex(REQUEST_ID_COL)
            val timestampIdx = cursor.getColumnIndex(STATUS_TIMESTAMP_COL)
            val deleteTokenIdx = cursor.getColumnIndex(DELETE_TOKEN_COL)
            val resourceTypeIdx = cursor.getColumnIndex(RESOURCE_TYPE_COL)
            val statusIdx = cursor.getColumnIndex(STATUS_COL)
            val errorIdx = cursor.getColumnIndex(LAST_ERROR_COL)
            val errorDescIdx = cursor.getColumnIndex(LAST_ERROR_DESC_COL)
            val nameIdx = cursor.getColumnIndex(NAME_COL)

            do {
                val resource = Resource()
                resource.localUri = cursor.getString(localIdIdx)
                resource.cloudinaryPublicId = cursor.getString(remoteIdIdx)
                resource.requestId = cursor.getString(requestIdIdx)
                resource.deleteToken = cursor.getString(deleteTokenIdx)
                resource.statusTimestamp = if (cursor.isNull(timestampIdx)) null else Date(cursor.getLong(timestampIdx))
                resource.resourceType = cursor.getString(resourceTypeIdx)
                resource.status = Resource.UploadStatus.valueOf(cursor.getString(statusIdx))
                resource.lastErrorCode = cursor.getInt(errorIdx)
                resource.lastErrorDesc = cursor.getString(errorDescIdx)
                resource.name = cursor.getString(nameIdx)
                res.add(resource)
            } while (cursor.moveToNext())
        }
    }

    fun deleteAllImages() {
        writableDatabase.execSQL("DELETE FROM $TABLE")
    }

    fun delete(localId: String) {
        writableDatabase.execSQL("DELETE FROM $TABLE WHERE $LOCAL_ID_COL=?", arrayOf<Any>(localId))
    }

    fun getLocalUri(requestId: String): String? {
        val cursor = readableDatabase.query(TABLE, null, "$REQUEST_ID_COL=?", arrayOf(requestId), null, null, null)
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(LOCAL_ID_COL))
        } else null

    }

    fun findByUri(uri: String): Resource? {
        return findResource(LOCAL_ID_COL, uri)
    }

    private fun findResource(colToSearch: String, searchTerm: String?): Resource? {
        val cursor = readableDatabase.query(TABLE, null, "$colToSearch=?", arrayOf(searchTerm), null, null, null)
        val res = ArrayList<Resource>(1)
        buildResource(cursor, res)
        return if (res.size > 0) res[0] else null
    }

    fun findByRequestId(requestId: String?): Resource? {
        return findResource(REQUEST_ID_COL, requestId)
    }

    fun list(statuses: Array<String?>): List<Resource> {
        val cursor = readableDatabase.query(TABLE, null, STATUS_COL + makeInQueryString(statuses.size), statuses, null, null, ID_COL)
        val res = ArrayList<Resource>()
        try {
            buildResource(cursor!!, res)
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return res
    }

    fun listAllUploadedAfter(cutoff: Long): List<Resource> {
        val cursor = readableDatabase.query(TABLE, null, "$STATUS_COL=? AND $STATUS_TIMESTAMP_COL>?", arrayOf(Resource.UploadStatus.UPLOADED.toString(), cutoff.toString()), null, null, ID_COL)
        val res = ArrayList<Resource>()
        try {
            buildResource(cursor!!, res)
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return res
    }

    companion object {
        val ID_COL = "ID"
        val VERSION = 2
        private val TABLE = "images"
        private val LOCAL_ID_COL = "localId"
        private val PUBLIC_ID_COL = "publicId"
        private val RESOURCE_TYPE_COL = "resourceType"
        private val REQUEST_ID_COL = "requestId"
        private val STATUS_TIMESTAMP_COL = "statusTimestamp"
        private val DELETE_TOKEN_COL = "deleteToken"
        private val LAST_ERROR_COL = "lastError"
        private val LAST_ERROR_DESC_COL = "lastErrorDesc"
        private val STATUS_COL = "status"
        private val NAME_COL = "name"
        private val TAG = CloudinarySqliteHelper::class.java.simpleName

        fun makeInQueryString(size: Int): String {
            val sb = StringBuilder()
            if (size > 0) {
                sb.append(" IN ( ")
                var placeHolder = ""
                for (i in 0 until size) {
                    sb.append(placeHolder)
                    sb.append("?")
                    placeHolder = ","
                }
                sb.append(" )")
            }
            return sb.toString()
        }
    }
}
