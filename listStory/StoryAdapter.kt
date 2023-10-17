package com.app.dicodingstoryapp.listStory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.dicodingstoryapp.Story
import com.app.dicodingstoryapp.databinding.ItemListStoryBinding
import com.app.dicodingstoryapp.model.StoryModel
import com.bumptech.glide.Glide

class StoryAdapter : PagingDataAdapter<Story, StoryAdapter.ViewHolder>(DIFF_CALLBACK){

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

    private var onItemClickCallback: OnItemClickCallback? = null

    class ViewHolder(val binding: ItemListStoryBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickCallback {
        fun onItemClicked(list: Story)
    }

    fun setOnItemClickedCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListStoryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

//    override fun getItemCount() = listStory.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Glide.with(holder.itemView.context)
            .load(item?.photoUrl)
            .into(holder.binding.imgItemPoster)

        holder.binding.tvItemTitle.text = item?.name
        holder.binding.tvItemDescription.text = item?.description
        holder.binding.root.setOnClickListener {
            if (item != null) {
                onItemClickCallback?.onItemClicked(item)
            }
        }
    }
}