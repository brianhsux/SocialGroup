package com.brianhsu.socialgroup.controller

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import com.brianhsu.socialgroup.Adapters.EffectsGalleryAdapter
import com.brianhsu.socialgroup.R

import com.brianhsu.socialgroup.model.EffectData
import com.brianhsu.socialgroup.model.Resource

import com.cloudinary.android.MediaManager
import com.cloudinary.android.ResponsiveUrl.Preset.FIT
import com.cloudinary.android.sample.core.CloudinaryHelper
import com.cloudinary.utils.StringUtils
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImageActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var resource: Resource? = null
    private var recyclerView: RecyclerView? = null
    private var thumbHeight: Int = 0
    private var descriptionTextView: TextView? = null
    private var progressBar: ProgressBar? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var exoPlayerView: SimpleExoPlayerView? = null
    private var listener: ExoPlayer.EventListener? = null
    private var currentUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById(R.id.progressBar) as ProgressBar
        progressBar!!.visibility = View.GONE
        imageView = findViewById(R.id.image_view) as ImageView
        descriptionTextView = findViewById(R.id.effectDescription) as TextView
        recyclerView = findViewById(R.id.effectsGallery) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false)
        recyclerView!!.layoutManager = layoutManager

        initExoPlayer()

        fetchImageFromIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_image_detail, menu)
        return true
    }


    private fun initExoPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        exoPlayerView = findViewById(R.id.exoPlayer) as SimpleExoPlayerView
        exoPlayerView!!.setPlayer(exoPlayer)

        listener = object : ExoPlayer.EventListener {
            override fun onTimelineChanged(timeline: Timeline, o: Any) {}

            override fun onTracksChanged(trackGroupArray: TrackGroupArray, trackSelectionArray: TrackSelectionArray) {
                if (trackGroupArray.length > 0) {
                    progressBar!!.visibility = View.GONE
                }
            }

            override fun onLoadingChanged(b: Boolean) {
                progressBar!!.visibility = if (b) View.VISIBLE else View.GONE
            }

            override fun onPlayerStateChanged(b: Boolean, i: Int) {

            }

            override fun onPlayerError(e: ExoPlaybackException) {
                progressBar!!.visibility = View.GONE
                Toast.makeText(this@ImageActivity, "Error: " + e.message, Toast.LENGTH_LONG).show()
            }

            override fun onPositionDiscontinuity() {

            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

            }
        }
    }

//    private fun initEffectGallery() {
//        recyclerView!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
//
//            override fun onPreDraw(): Boolean {
//                recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
//                thumbHeight = Math.round((recyclerView!!.width / 4).toFloat())
//
//                if (resource != null) {
//                    val data = CloudinaryHelper.generateEffectsList(this@ImageActivity, resource!!)
//                    recyclerView!!.adapter = EffectsGalleryAdapter(this@ImageActivity, data, thumbHeight, object : EffectsGalleryAdapter.ItemClickListener {
//                        override fun onItemClick(data: EffectData?) {
//                            updateMainImage(data)
//                        }
//                    })
//
//                    updateMainImage(data[0])
//                    return true
//                } else {
//                    return false
//                }
//            }
//        })
//    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer!!.release()
    }

    private fun updateMainImage(data: EffectData?) {
        currentUrl = null
        if (resource!!.resourceType.equals("image")) {
            loadImage(data)
        } else {
            loadVideo(data)
        }

        descriptionTextView!!.text = data?.description
    }

    private fun loadVideo(data: EffectData?) {
        progressBar!!.visibility = View.VISIBLE
        imageView!!.visibility = View.GONE
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "Cloudinary Sample App"), null)
        val extractorsFactory = DefaultExtractorsFactory()
        val baseUrl = MediaManager.get().url().publicId(data?.publicId).transformation(data?.transformation)
        MediaManager.get().responsiveUrl(exoPlayerView, baseUrl, FIT) { url ->
            val urlString = url.generate()
            currentUrl = urlString
            val videoSource = ExtractorMediaSource(Uri.parse(urlString), dataSourceFactory, extractorsFactory, null, null)
            exoPlayer!!.addListener(listener)
            exoPlayer!!.prepare(videoSource)
        }
    }

    private fun loadImage(data: EffectData?) {
        exoPlayer!!.removeListener(listener)
        exoPlayerView!!.setVisibility(View.GONE)
        progressBar!!.visibility = View.VISIBLE
        val picasso = Picasso.Builder(this).listener(object : Picasso.Listener {
            override fun onImageLoadFailed(picasso: Picasso, uri: Uri, exception: Exception) {
                showSnackBar("Error loading resource: " + exception.message)
            }
        }).build()

        val baseUrl = MediaManager.get().url().publicId(data?.publicId).transformation(data?.transformation)
        MediaManager.get().responsiveUrl(imageView, baseUrl, FIT) { url ->
            val uriString = url.generate()
            currentUrl = uriString
            picasso.load(Uri.parse(uriString)).into(imageView, object : Callback {
                override fun onSuccess() {
                    progressBar!!.visibility = View.GONE
                }

                override fun onError(e: Exception) {
                    progressBar!!.visibility = View.GONE
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_url -> {
                if (StringUtils.isNotBlank(currentUrl)) {
                    openUrlWithToast(currentUrl)
                }

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        fetchImageFromIntent(intent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(imageView!!, message, Snackbar.LENGTH_LONG).show()
    }

    private fun fetchImageFromIntent(intent: Intent?) {
        if (intent == null || !intent.hasExtra(RESOURCE_INTENT_EXTRA)) {
            // something wrong, nothing to load.
            Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            resource = intent.getSerializableExtra(RESOURCE_INTENT_EXTRA) as Resource
            val cloudinaryPublicId = resource!!.cloudinaryPublicId
            if (StringUtils.isEmpty(cloudinaryPublicId)) {
                Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                recyclerView!!.visibility = View.VISIBLE
//                initEffectGallery()
            }
        }
    }

    private fun openUrlWithToast(url: String?) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        ClipData.newPlainText("Cloudinary Url", url)
        Toast.makeText(this@ImageActivity, "Url copied to clipboard!", Toast.LENGTH_LONG).show()
    }

    companion object {
        val UPLOAD_IMAGE_REQUEST_CODE = 1001
        val RESOURCE_INTENT_EXTRA = "RESOURCE_INTENT_EXTRA"
    }
}