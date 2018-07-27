package com.brianhsu.socialgroup.Utilities

import android.content.Context
import com.brianhsu.socialgroup.R

//import com.material.components.model.CardViewImg
//import com.material.components.model.Image
//import com.material.components.model.Inbox
//import com.material.components.model.MusicAlbum
//import com.material.components.model.MusicSong
//import com.material.components.model.People
//import com.material.components.model.ShopCategory
//import com.material.components.model.ShopProduct
//import com.material.components.model.Social
//import com.material.components.utils.MaterialColor

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import java.util.Random

object DataGenerator {

    private val r = Random()

    fun randInt(max: Int): Int {
        val min = 0
        return r.nextInt(max - min + 1) + min
    }

    fun getStringsShort(ctx: Context): List<String> {
        val items = ArrayList<String>()
        val name_arr = ctx.resources.getStringArray(R.array.strings_short)
        for (s in name_arr) items.add(s)
        Collections.shuffle(items)
        return items
    }

    fun getNatureImages(ctx: Context): MutableList<Int> {
        val items = ArrayList<Int>()
        val drwArr = ctx.resources.obtainTypedArray(R.array.sample_images)
        for (i in 0 until drwArr.length()) {
            items.add(drwArr.getResourceId(i, -1))
        }
        items.shuffle()
        return items
    }

    fun getStringsMonth(ctx: Context): List<String> {
        val items = ArrayList<String>()
        val arr = ctx.resources.getStringArray(R.array.month)
        for (s in arr) items.add(s)
        Collections.shuffle(items)
        return items
    }

    /**
     * Generate dummy data CardViewImg
     *
     * @param ctx   android context
     * @param count total result data
     * @return list of object
     */
//    fun getCardImageData(ctx: Context, count: Int): List<CardViewImg> {
//
//        val items = ArrayList<CardViewImg>()
//
//        val images = getNatureImages(ctx)
//        val titles = getStringsShort(ctx)
//        val subtitles = getStringsShort(ctx)
//
//        for (i in 0 until count) {
//            val obj = CardViewImg()
//            obj.image = images[getRandomIndex(images.size)]
//            obj.title = titles[getRandomIndex(titles.size)]
//            obj.subtitle = subtitles[getRandomIndex(subtitles.size)]
//            items.add(obj)
//        }
//        return items
//    }

    /**
     * Generate dummy data people
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getPeopleData(ctx: Context): List<People> {
//        val items = ArrayList<People>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.people_images)
//        val name_arr = ctx.resources.getStringArray(R.array.people_names)
//
//        for (i in 0 until drw_arr.length()) {
//            val obj = People()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.name = name_arr[i]
//            obj.email = Tools.getEmailFromName(obj.name)
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        Collections.shuffle(items)
//        return items
//    }

    /**
     * Generate dummy data social
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getSocialData(ctx: Context): List<Social> {
//        val items = ArrayList<Social>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.social_images)
//        val name_arr = ctx.resources.getStringArray(R.array.social_names)
//
//        for (i in 0 until drw_arr.length()) {
//            val obj = Social()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.name = name_arr[i]
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        Collections.shuffle(items)
//        return items
//    }

    /**
     * Generate dummy data inbox
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getInboxData(ctx: Context): List<Inbox> {
//        val items = ArrayList<Inbox>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.people_images)
//        val name_arr = ctx.resources.getStringArray(R.array.people_names)
//        val date_arr = ctx.resources.getStringArray(R.array.general_date)
//
//        for (i in 0 until drw_arr.length()) {
//            val obj = Inbox()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.from = name_arr[i]
//            obj.email = Tools.getEmailFromName(obj.from)
//            obj.message = ctx.resources.getString(R.string.lorem_ipsum)
//            obj.date = date_arr[randInt(date_arr.size - 1)]
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        Collections.shuffle(items)
//        return items
//    }

    /**
     * Generate dummy data image
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getImageDate(ctx: Context): List<Image> {
//        val items = ArrayList<Image>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.sample_images)
//        val name_arr = ctx.resources.getStringArray(R.array.sample_images_name)
//        val date_arr = ctx.resources.getStringArray(R.array.general_date)
//        for (i in 0 until drw_arr.length()) {
//            val obj = Image()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.name = name_arr[i]
//            obj.brief = date_arr[randInt(date_arr.size - 1)]
//            obj.counter = if (r.nextBoolean()) randInt(500) else null
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        Collections.shuffle(items)
//        return items
//    }

    /**
     * Generate dummy data shopping category
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getShoppingCategory(ctx: Context): List<ShopCategory> {
//        val items = ArrayList<ShopCategory>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.shop_category_icon)
//        val drw_arr_bg = ctx.resources.obtainTypedArray(R.array.shop_category_bg)
//        val title_arr = ctx.resources.getStringArray(R.array.shop_category_title)
//        val brief_arr = ctx.resources.getStringArray(R.array.shop_category_brief)
//        for (i in 0 until drw_arr.length()) {
//            val obj = ShopCategory()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.image_bg = drw_arr_bg.getResourceId(i, -1)
//            obj.title = title_arr[i]
//            obj.brief = brief_arr[i]
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        return items
//    }

    /**
     * Generate dummy data shopping product
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getShoppingProduct(ctx: Context): List<ShopProduct> {
//        val items = ArrayList<ShopProduct>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.shop_product_image)
//        val title_arr = ctx.resources.getStringArray(R.array.shop_product_title)
//        val price_arr = ctx.resources.getStringArray(R.array.shop_product_price)
//        for (i in 0 until drw_arr.length()) {
//            val obj = ShopProduct()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.title = title_arr[i]
//            obj.price = price_arr[i]
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        return items
//    }


    /**
     * Generate dummy data music song
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getMusicSong(ctx: Context): List<MusicSong> {
//        val items = ArrayList<MusicSong>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.album_cover)
//        val song_name = ctx.resources.getStringArray(R.array.song_name)
//        val album_name = ctx.resources.getStringArray(R.array.album_name)
//        for (i in 0 until drw_arr.length()) {
//            val obj = MusicSong()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.title = song_name[i]
//            obj.brief = album_name[i]
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        Collections.shuffle(items)
//        return items
//    }

    /**
     * Generate dummy data music album
     *
     * @param ctx android context
     * @return list of object
     */
//    fun getMusicAlbum(ctx: Context): List<MusicAlbum> {
//        val items = ArrayList<MusicAlbum>()
//        val drw_arr = ctx.resources.obtainTypedArray(R.array.album_cover)
//        val album_name = ctx.resources.getStringArray(R.array.album_name)
//        for (i in 0 until drw_arr.length()) {
//            val obj = MusicAlbum()
//            obj.image = drw_arr.getResourceId(i, -1)
//            obj.name = album_name[i]
//            obj.brief = getRandomIndex(15).toString() + " MusicSong (s)"
//            obj.color = MaterialColor.getColor(ctx, obj.name, i)
//            obj.imageDrw = ctx.resources.getDrawable(obj.image)
//            items.add(obj)
//        }
//        return items
//    }

    fun formatTime(time: Long): String {
        // income time
        val date = Calendar.getInstance()
        date.timeInMillis = time

        // current time
        val curDate = Calendar.getInstance()
        curDate.timeInMillis = System.currentTimeMillis()

        var dateFormat: SimpleDateFormat? = null
        if (date.get(Calendar.YEAR) == curDate.get(Calendar.YEAR)) {
            if (date.get(Calendar.DAY_OF_YEAR) == curDate.get(Calendar.DAY_OF_YEAR)) {
                dateFormat = SimpleDateFormat("h:mm a", Locale.US)
            } else {
                dateFormat = SimpleDateFormat("MMM d", Locale.US)
            }
        } else {
            dateFormat = SimpleDateFormat("MMM yyyy", Locale.US)
        }
        return dateFormat.format(time)
    }

    private fun getRandomIndex(max: Int): Int {
        return r.nextInt(max - 1)
    }
}
