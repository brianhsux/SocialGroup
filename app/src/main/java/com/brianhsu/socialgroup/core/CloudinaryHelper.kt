package com.cloudinary.android.sample.core

import android.content.Context
import android.net.Uri
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.Tools
import com.brianhsu.socialgroup.controller.App
import com.brianhsu.socialgroup.model.EffectData
import com.brianhsu.socialgroup.model.Resource

import com.cloudinary.Transformation
import com.cloudinary.android.MediaManager
import com.cloudinary.android.UploadRequest
import com.cloudinary.android.policy.TimeWindow
import com.cloudinary.android.preprocess.BitmapEncoder
import com.cloudinary.android.preprocess.ImagePreprocessChain
import com.cloudinary.utils.ObjectUtils
import java.util.*

object CloudinaryHelper {

    val cloudName: String
        get() = MediaManager.get().cloudinary.config.cloudName

    fun uploadResource(resource: Resource?, preprocess: Boolean): String {
        val request = MediaManager.get().upload(Uri.parse(resource?.localUri))
                .option("folder", "SocialGroup/postImage/")
                .unsigned("sample_app_preset")
                .constrain(TimeWindow.getDefault())
                .option("resource_type", "auto")
                .maxFileSize((100 * 1024 * 1024).toLong()) // max 100mb
                .policy(MediaManager.get().globalUploadPolicy.newBuilder().maxRetries(2).build())
        if (preprocess) {
            // scale down images above 2000 width/height, and re-encode as webp with 80 quality to save bandwidth
            request.preprocess(ImagePreprocessChain.limitDimensionsChain(2000, 2000)
                    .saveWith(BitmapEncoder(BitmapEncoder.Format.WEBP, 80)))

        }

        return request.dispatch(App.instance)
    }

    fun getCroppedThumbnailUrl(size: Int, resource: Resource): String {

        return MediaManager.get().cloudinary.url()
                .resourceType(resource.resourceType)
                .transformation(Transformation<Transformation<*>>().gravity("auto").width(size).height(size))
                .format("webp")
                .generate(resource.cloudinaryPublicId)
    }

    fun getOriginalSizeImage(imageId: String): String {
        return MediaManager.get().cloudinary.url().generate(imageId)
    }

    fun getUrlForMaxWidth(context: Context, imageId: String): String {
        val width = Tools.getScreenWidth(context)
        return MediaManager.get().cloudinary.url().transformation(Transformation<Transformation<*>>().width(width)).generate(imageId)
    }

    fun deleteByToken(token: String, callback: DeleteCallback) {
        Thread(Runnable {
            try {
                val res = MediaManager.get().cloudinary.uploader().deleteByToken(token)
                App.instance.runOnMainThread(Runnable {
                    if (res != null && res.containsKey("result") && res["result"] == "ok") {
                        callback.onSuccess()
                    } else {
                        callback.onError("Unknown error.")
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onError(e.message)
            }
        }).start()
    }

    fun generateEffectsList(context: Context, resource: Resource): List<EffectData> {
        return if (resource.resourceType.equals("video")) {
            generateVideoEffects(context, resource)
        } else {
            generateImageEffects(context, resource)
        }
    }

    private fun generateImageEffects(context: Context, resource: Resource): List<EffectData> {
        val effects = ArrayList<EffectData>()

        var transformation: Transformation<*>

        transformation = Transformation<Transformation<*>>().effect("sharpen", 250).fetchFormat("webp")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_name_sharpen), context.getString(R.string.effect_desc_face_sharpen)))

        transformation = Transformation<Transformation<*>>().effect("oil_paint", 100).fetchFormat("webp")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_name_oil_paint), context.getString(R.string.effect_desc_face_oilpaint)))

        transformation = Transformation<Transformation<*>>().effect("sepia").fetchFormat("webp")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_name_sepia), context.getString(R.string.effect_desc_narrow_sepia)))

        transformation = Transformation<Transformation<*>>().radius(50).effect("saturation", 100)
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_name_round_corners), context.getString(R.string.effect_desc_face_sat_round)))

        transformation = Transformation<Transformation<*>>().effect("blue:100").fetchFormat("webp")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_name_blue), context.getString(R.string.effect_desc_wide_blue)))

        return effects
    }

    private fun generateVideoEffects(context: Context, resource: Resource): List<EffectData> {
        val effects = ArrayList<EffectData>()

        var transformation: Transformation<*>

        transformation = Transformation<Transformation<*>>().angle(20)
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_rotation), context.getString(R.string.effect_video_rotate)))

        transformation = Transformation<Transformation<*>>().effect("fade", 1000)
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_fade_in), context.getString(R.string.effect_video_fade_in)))

        transformation = Transformation<Transformation<*>>().chain().overlay("video:" + resource.cloudinaryPublicId!!).width(0.5).flags("relative").gravity("north_east")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_overlay), context.getString(R.string.effect_desc_video_overlay)))

        transformation = Transformation<Transformation<*>>().effect("noise", 50)
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_noise), context.getString(R.string.effect_desc_video_noise)))

        transformation = Transformation<Transformation<*>>().effect("blur", 200)
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_blur), context.getString(R.string.effect_desc_video_blur)))

        transformation = Transformation<Transformation<*>>().effect("reverse")
        effects.add(EffectData(resource.cloudinaryPublicId!!, transformation, context.getString(R.string.effect_video_name_reverse), context.getString(R.string.effect_desc_video_reverse)))

        return effects
    }

    interface DeleteCallback {
        fun onSuccess()

        fun onError(error: String?)
    }
}
