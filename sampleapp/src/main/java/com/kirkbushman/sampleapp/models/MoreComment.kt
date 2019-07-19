package com.kirkbushman.sampleapp.models

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.kirkbushman.sampleapp.R

@EpoxyModelClass(layout = R.layout.item_more_comments)
abstract class MoreCommentModel : EpoxyModelWithHolder<MoreCommentHolder>() {

    @EpoxyAttribute
    lateinit var more: String

    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener

    override fun bind(holder: MoreCommentHolder) {

        holder.more.text = more
        holder.container.setOnClickListener(clickListener)
    }
}

class MoreCommentHolder : KotlinHolder() {

    val more by bind<TextView>(R.id.more)
    val container by bind<LinearLayout>(R.id.container)
}
