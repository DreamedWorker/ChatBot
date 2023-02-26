package com.dream.chatbot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dream.chatbot.R
import com.dream.chatbot.database.completion.CompletionEntity

class CompletionAdapter(private var list: ArrayList<CompletionEntity>) : RecyclerView.Adapter<CompletionAdapter.CompletionHolder>() {
    class CompletionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val aiImage: ImageView = itemView.findViewById(R.id.item_completion_ai)
        val chatData: TextView = itemView.findViewById(R.id.item_completion_context)
        val btn: LinearLayout =itemView.findViewById(R.id.item_completion_board)
    }

    interface OnCompletionSelectedListener {
        fun onCompletionSelectedListener(position: Int)
    }

    private var listener: OnCompletionSelectedListener? = null

    fun setOnCompletionSelectedListener(mListener: OnCompletionSelectedListener){
        listener = mListener
    }

    fun insertData(entity: CompletionEntity){
        list.add(entity)
        notifyItemInserted(list.size)
    }

    fun queryData(position: Int) : CompletionEntity = list[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completion, parent, false)
        return CompletionHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CompletionHolder, position: Int) {
        val mSender = list[position].sender!!
        if (mSender == "man"){
            holder.aiImage.setImageResource(R.drawable.ic_human)
        } else if (mSender == "ai"){
            holder.aiImage.setImageResource(R.drawable.ic_robot)
        }
        holder.chatData.text = list[position].chatContext!!
        holder.btn.setOnClickListener {
            listener?.onCompletionSelectedListener(position)
        }
    }
}