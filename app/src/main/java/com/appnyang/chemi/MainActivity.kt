package com.appnyang.chemi

import android.Manifest
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.apache.commons.lang3.time.FastDateFormat

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
        LoaderManager.LoaderCallbacks<Cursor> {

    private val loaderURL: Int = 10
    private val permissionRequestReadCallLog: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(Array(1, {Manifest.permission.READ_CALL_LOG}), permissionRequestReadCallLog)
        } else {
            loaderManager.initLoader(loaderURL, null, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionRequestReadCallLog) {
            loaderManager.initLoader(loaderURL, null, this)
        } else {
            TODO("Add action when permission is not granted")
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        if (data != null) {
            val numberIndex: Int = data.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex: Int = data.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex: Int = data.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex: Int = data.getColumnIndex(CallLog.Calls.DURATION)

            val dateFormat = FastDateFormat.getInstance("MM-yyyy")

            val monthlyCallVolume: MutableMap<String, Int> = mutableMapOf()

            while (data.moveToNext()) {
                val number: String = data.getString(numberIndex)
                val type: Int = data.getInt(typeIndex)
                val date: Long = data.getLong(dateIndex)
                val duration: Int = data.getInt(durationIndex)

                var readableType: String? = null
                when (type) {
                    CallLog.Calls.OUTGOING_TYPE -> readableType = "Outgoing"
                    CallLog.Calls.INCOMING_TYPE -> readableType = "Incoming"
                    CallLog.Calls.MISSED_TYPE -> readableType = "Missed"
                }

                // Calculate the monthly call volume
                val dateReadable = dateFormat.format(date)

                if (monthlyCallVolume.containsKey(dateReadable)) {
                    if (monthlyCallVolume[dateReadable] != null) {
                        val value: Int = monthlyCallVolume[dateReadable]!!
                        monthlyCallVolume[dateReadable] =  value + duration
                    }
                } else {
                    monthlyCallVolume[dateReadable] = duration
                }
            }

            data.close()

            val stringBuilder: StringBuilder = StringBuilder()
            for ((k, v) in monthlyCallVolume) {
                val timeInMinutes: Int = v / 60
                Log.d("monthly call", "date: $k, duration: $timeInMinutes" )
                stringBuilder.append("$k, 통화 시간: $timeInMinutes 분\n\n")
            }

            textCallLogTime.text = stringBuilder.toString()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }

    /**
     * Create a loader by receiving a cursor loader creation request.
     *
     * @see onCreate
     *
     * @param id Requested id.
     * @param args Bundle.
     * @return A new cursor loader or null.
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        if (loaderURL == id) {
            return CursorLoader(this, CallLog.Calls.CONTENT_URI, null, null, null, null)
        }

        return null
    }
}
