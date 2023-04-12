package com.jrolph.proximityreminder

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.jrolph.proximityreminder.database.Reminder

class ReminderAdapter(private val context: Context, private val clickListener: OnItemClickListener, private val longClickListener: OnItemLongClickListener)
    : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>(), DragSelectReceiver {

    private var selectedIndices: MutableList<Int> = mutableListOf()
    private lateinit var items: List<Reminder>

    fun setData(data: List<Reminder>) {
        this.items = data;
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder{
        return ReminderViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.reminder_item_view,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReminderViewHolder, pos: Int) {
        val item = items[pos]
        val radius = item.radius
        val long = item.longitude
        val lat = item.latitude
        var locationTitle = item.name ?: "long: $long | lat: $lat"
        Log.d("log", locationTitle)
        holder.reminderCondition.text = "Within $radius km of $locationTitle"
        holder.reminderNote.text = item.note
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun isIndexSelectable(index: Int): Boolean = true

    override fun isSelected(index: Int): Boolean = isSelectedAt(index)

    override fun setSelected(index: Int, selected: Boolean) {
        if(selected && !selectedIndices.contains(index)) {
            selectedIndices.add(index)
        } else if(!selected) {
            selectedIndices.remove(index)
        }
        notifyItemChanged(index)
    }



    private fun isSelectedAt(index: Int): Boolean {
        if (selectedIndices.indexOf(index)==-1) // if index is not in list
            return false
        return true
    }

    inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        val reminderCondition: TextView = view.findViewById(R.id.condition_text)
        val reminderNote: TextView = view.findViewById(R.id.note_text)
        private val card: CardView = view.findViewById(R.id.reminder_cardView)
        init {
            card.setOnClickListener(this)
            card.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                longClickListener.onItemLongClicked(position)
            }
            return true
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(initialPosition: Int)
    }

}