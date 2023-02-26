package com.dream.chatbot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dream.chatbot.R
import com.dream.chatbot.database.image.ImageEntity

class ImageAdapter(private var list: List<ImageEntity>, private var mContext: Context) :
    RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val manRequirement: TextView = itemView.findViewById(R.id.item_image_context)
        val gptReturn: ImageView = itemView.findViewById(R.id.item_image_container)
        val btn: LinearLayout = itemView.findViewById(R.id.item_image_board)
    }

    interface OnDownloadSelectedListener {
        fun onDownloaderSelectedListener(position: Int)
    }

    private var listener: OnDownloadSelectedListener? = null

    fun setOnDownloadSelectedListener(mListener: OnDownloadSelectedListener){
        listener = mListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val singleEntity = list[position]
        holder.manRequirement.text = singleEntity.context!!
        Glide.with(mContext).load(singleEntity.imageUrl!!).into(holder.gptReturn)
        holder.btn.setOnClickListener {
            listener?.onDownloaderSelectedListener(position)
        }
    }
}