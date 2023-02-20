/*
 * Copyright 2015-2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import coil.load
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Size
import com.hippo.drawable.PreciselyClipDrawable
import com.hippo.ehviewer.R

open class LoadImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FixedAspectImageView(context, attrs, defStyleAttr), View.OnClickListener,
    View.OnLongClickListener {
    private var mRequest: ImageRequest? = null
    private var mOffsetX = Integer.MIN_VALUE
    private var mOffsetY = Integer.MIN_VALUE
    private var mClipWidth = Integer.MIN_VALUE
    private var mClipHeight = Integer.MIN_VALUE
    private var mRetryType: Int = 0
    private var mFailed: Boolean = false
    private var task: Disposable? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LoadImageView, defStyleAttr, 0)
        setRetryType(a.getInt(R.styleable.LoadImageView_retryType, 0))
        a.recycle()
    }

    private fun clearRetry() {
        if (mRetryType == RETRY_TYPE_CLICK) {
            setOnClickListener(null)
            isClickable = false
        } else if (mRetryType == RETRY_TYPE_LONG_CLICK) {
            setOnLongClickListener(null)
            isLongClickable = false
        }
    }

    private fun setRetryType(@RetryType retryType: Int) {
        if (mRetryType != retryType) {
            val oldRetryType = mRetryType
            mRetryType = retryType

            if (mFailed) {
                if (oldRetryType == RETRY_TYPE_CLICK) {
                    setOnClickListener(null)
                    isClickable = false
                } else if (oldRetryType == RETRY_TYPE_LONG_CLICK) {
                    setOnLongClickListener(null)
                    isLongClickable = false
                }

                if (retryType == RETRY_TYPE_CLICK) {
                    setOnClickListener(this)
                } else if (retryType == RETRY_TYPE_LONG_CLICK) {
                    setOnLongClickListener(this)
                }
            }
        }
    }

    fun setClip(offsetX: Int, offsetY: Int, clipWidth: Int, clipHeight: Int) {
        mOffsetX = offsetX
        mOffsetY = offsetY
        mClipWidth = clipWidth
        mClipHeight = clipHeight
    }

    fun resetClip() {
        mOffsetX = Integer.MIN_VALUE
        mOffsetY = Integer.MIN_VALUE
        mClipWidth = Integer.MIN_VALUE
        mClipHeight = Integer.MIN_VALUE
    }

    fun load(url: String, crossfade: Boolean = true) {
        load(url) {
            size(Size.ORIGINAL)
            if (!crossfade) crossfade(false)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        var newDrawable = drawable
        if (newDrawable != null) {
            if (Integer.MIN_VALUE != mOffsetX) {
                newDrawable = PreciselyClipDrawable(drawable, mOffsetX, mOffsetY, mClipWidth, mClipHeight)
            }
            onPreSetImageDrawable(newDrawable, true)
        }
        super.setImageDrawable(newDrawable)
    }

    override fun getDrawable(): Drawable? {
        var newDrawable = super.getDrawable()
        if (newDrawable is PreciselyClipDrawable) {
            newDrawable = newDrawable.drawable
        }
        return newDrawable
    }

    fun onFailure() {
        when (mRetryType) {
            RETRY_TYPE_CLICK -> {
                setOnClickListener(this)
            }

            RETRY_TYPE_LONG_CLICK -> {
                setOnLongClickListener(this)
            }

            else -> {
                mRequest = null
            }
        }
    }

    override fun onClick(v: View) {
    }

    override fun onLongClick(v: View): Boolean {
        return true
    }

    open fun onPreSetImageDrawable(drawable: Drawable?, isTarget: Boolean) {}

    open fun onPreSetImageResource(resId: Int, isTarget: Boolean) {}

    @IntDef(RETRY_TYPE_NONE, RETRY_TYPE_CLICK, RETRY_TYPE_LONG_CLICK)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class RetryType

    companion object {
        const val RETRY_TYPE_NONE = 0
        const val RETRY_TYPE_CLICK = 1
        const val RETRY_TYPE_LONG_CLICK = 2
    }
}
