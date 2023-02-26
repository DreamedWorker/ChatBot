package com.dream.chatbot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dream.chatbot.R
import com.dream.chatbot.database.python.LanguageEntity
import com.google.android.material.card.MaterialCardView

class PythonAdapter(private var list: List<LanguageEntity>) : RecyclerView.Adapter<PythonAdapter.PythonHolder>() {
    class PythonHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val btn: MaterialCardView = itemView.findViewById(R.id.py2zh_board)
        val label: TextView = itemView.findViewById(R.id.py2zh_time)
    }

    interface OnCodeRecordClickListener{
        fun onCodeRecordClickListener(position: Int)
    }

    interface OnCodeLongClickListener{
        fun onCodeLongClickListener(position: Int)
    }

    private var listener: OnCodeRecordClickListener? = null
    private var longListener: OnCodeLongClickListener? = null

    fun setOnCodeRecordClickListener(mListener: OnCodeRecordClickListener){
        listener = mListener
    }

    fun setOnCodeLongClickListener(mListener: OnCodeLongClickListener){
        longListener = mListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PythonHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_python_translation, parent, false)
        return  PythonHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PythonHolder, position: Int) {
        val single = list[position]
        holder.label.text = single.time!!
        holder.btn.setOnClickListener {
            listener?.onCodeRecordClickListener(position)
        }
        holder.btn.setOnLongClickListener {
            longListener?.onCodeLongClickListener(position)
            true
        }
    }
}