package com.dream.chatbot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dream.chatbot.R
import com.dream.chatbot.database.keywords.KeywordEntity
import com.google.android.material.card.MaterialCardView

class KeywordAdapter(private var list: List<KeywordEntity>) : RecyclerView.Adapter<KeywordAdapter.KeywordHolder>(){
    class KeywordHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val btn: MaterialCardView = itemView.findViewById(R.id.py2zh_board)
        val label: TextView = itemView.findViewById(R.id.py2zh_time)
    }

    interface OnKeywordClickListener{
        fun onKeywordClickListener(position: Int)
    }

    private var listener: OnKeywordClickListener? = null

    fun setOnKeywordClickListener(mListener: OnKeywordClickListener){
        listener = mListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_python_translation, parent, false)
        return KeywordHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: KeywordHolder, position: Int) {
        val single = list[position]
        holder.label.text = single.time!!
        holder.btn.setOnClickListener {
            listener?.onKeywordClickListener(position)
        }
    }
}