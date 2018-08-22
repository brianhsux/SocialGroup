package com.brianhsu.socialgroup.Utilities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.util.Pair
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.brianhsu.socialgroup.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.cloudinary.utils.StringUtils
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Tools {
    fun setSystemBarColor(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(R.color.colorPrimaryDark)
        }
    }

    fun setSystemBarColor(act: Activity, @ColorRes color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(color)
        }
    }

    fun setSystemBarLight(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = act.findViewById<View>(android.R.id.content)
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
    }

    fun clearSystemBarLight(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = act.window
            window.statusBarColor = ContextCompat.getColor(act, R.color.colorPrimaryDark)
        }
    }

    /**
     * Making notification bar transparent
     */
    fun setSystemBarTransparent(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun displayImageOriginal(ctx: Context, img: ImageView?, @DrawableRes drawable: Int) {
        try {
//            Log.v(TAG, "displayImageOriginal: " + drawable.toString())
            Glide.with(ctx).load(drawable)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img)
        } catch (e: Exception) {
        }

    }

//    fun displayImageRound(ctx: Context, img: ImageView, @DrawableRes drawable: Int) {
//        try {
//            Glide.with(ctx).load(drawable).asBitmap().centerCrop().into(object : BitmapImageViewTarget(img) {
//                protected fun setResource(resource: Bitmap) {
//                    val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ctx.resources, resource)
//                    circularBitmapDrawable.isCircular = true
//                    img.setImageDrawable(circularBitmapDrawable)
//                }
//            })
//        } catch (e: Exception) {
//        }
//
//    }

    fun displayImageOriginal(ctx: Context, img: ImageView?, url: String) {
        try {
            Glide.with(ctx).load(url)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img)
        } catch (e: Exception) {
        }

    }

    fun getFormattedDateSimple(dateTime: Long?): String {
        val newFormat = SimpleDateFormat("MMMM dd, yyyy")
        return newFormat.format(Date(dateTime!!))
    }

    fun getFormattedDateEvent(dateTime: Long?): String {
        val newFormat = SimpleDateFormat("EEE, MMM dd yyyy")
        return newFormat.format(Date(dateTime!!))
    }

    fun getFormattedTimeEvent(time: Long?): String {
        val newFormat = SimpleDateFormat("h:mm a")
        return newFormat.format(Date(time!!))
    }

    fun getEmailFromName(name: String?): String? {
        return if (name != null && name != "") {
            name.replace(" ".toRegex(), ".").toLowerCase() + "@mail.com"
        } else name
    }

    fun dpToPx(c: Context, dp: Int): Int {
        val r = c.resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

    fun copyToClipboard(context: Context, data: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("clipboard", data)
        clipboard.primaryClip = clip
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun nestedScrollTo(nested: NestedScrollView, targetView: View) {
        nested.post { nested.scrollTo(500, targetView.bottom) }
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun toggleArrow(view: View): Boolean {
        if (view.rotation == 0f) {
            view.animate().setDuration(200).rotation(180f)
            return true
        } else {
            view.animate().setDuration(200).rotation(0f)
            return false
        }
    }

    fun toggleArrow(show: Boolean, view: View): Boolean {
        return toggleArrow(show, view, true)
    }

    fun toggleArrow(show: Boolean, view: View, delay: Boolean): Boolean {
        if (show) {
            view.animate().setDuration((if (delay) 200 else 0).toLong()).rotation(180f)
            return true
        } else {
            view.animate().setDuration((if (delay) 200 else 0).toLong()).rotation(0f)
            return false
        }
    }

    fun changeNavigateionIconColor(toolbar: Toolbar, @ColorInt color: Int) {
        val drawable = toolbar.navigationIcon
        drawable!!.mutate()
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun changeMenuIconColor(menu: Menu, @ColorInt color: Int) {
        for (i in 0 until menu.size()) {
            val drawable = menu.getItem(i).icon ?: continue
            drawable.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    fun changeOverflowMenuIconColor(toolbar: Toolbar, @ColorInt color: Int) {
        try {
            val drawable = toolbar.overflowIcon
            drawable!!.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        } catch (e: Exception) {
        }

    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

//    fun toCamelCase(input: String): String {
//        var input = input
//        input = input.toLowerCase()
//        val titleCase = StringBuilder()
//        var nextTitleCase = true
//
//        for (c in input.toCharArray()) {
//            if (Character.isSpaceChar(c)) {
//                nextTitleCase = true
//            } else if (nextTitleCase) {
//                c = Character.toTitleCase(c)
//                nextTitleCase = false
//            }
//
//            titleCase.append(c)
//        }
//
//        return titleCase.toString()
//    }

    fun insertPeriodically(text: String, insert: String, period: Int): String {
        val builder = StringBuilder(text.length + insert.length * (text.length / period) + 1)
        var index = 0
        var prefix = ""
        while (index < text.length) {
            builder.append(prefix)
            prefix = insert
            builder.append(text.substring(index, Math.min(index + period, text.length)))
            index += period
        }
        return builder.toString()
    }

    fun openMediaChooser(activity: Activity?, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/jpg", "image/png", "video/*"))
        intent.type = "(*/*"
        activity?.startActivityForResult(intent, requestCode)
    }

    fun getScreenWidth(context: Context): Int {
        val window = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        window.defaultDisplay.getSize(point)
        return point.x
    }

    fun getResourceNameAndType(context: Context, uri: Uri): Pair<String, String> {
        var cursor: Cursor? = null
        var type: String? = null
        var name: String? = null

        try {
            cursor = context.contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                if (StringUtils.isNotBlank(type)) {
                    type = type!!.substring(0, type.indexOf('/'))
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }

        if (StringUtils.isBlank(type)) {
            type = "image"
        }
        return Pair(name, type)
    }

    @Throws(IOException::class)
    fun decodeBitmapStream(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        var `is` = context.contentResolver.openInputStream(uri)

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(`is`, null, options)
        `is`!!.close()
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        `is` = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(`is`, null, options)
        `is`!!.close()
        return if (bitmap == null) null else getCroppedBitmap(bitmap)
    }

    fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight && width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val dimension = if (bitmap.width < bitmap.height) bitmap.width else bitmap.height
        val output = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val horizontalDiff = (bitmap.width - dimension) / 2
        val verticalDiff = (bitmap.height - dimension) / 2
        val rect = Rect(-horizontalDiff, -verticalDiff, bitmap.width - horizontalDiff, bitmap.height - verticalDiff)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle((dimension / 2).toFloat(), (dimension / 2).toFloat(), (dimension / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

//    fun toDateStringFromIso(sdate: String?): String {
//        if ("null" == sdate || "NULL" == sdate || "" == sdate || sdate == null) { return "" }
//        val d1:Date? = null
//        try {
//            d1 = dateFormaterIsodate.get().parse(sdate);
//            sharecalendar.setTime(d1)
//            sharecalendar.set(Calendar.HOUR_OF_DAY, sharecalendar.get(Calendar.HOUR_OF_DAY)+8);
//            d1 = sharecalendar.getTime()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return ""
//        }
//        return dateFormater4.get().format(d1)
//    }
}
