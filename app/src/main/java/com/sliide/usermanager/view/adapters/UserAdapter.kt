package com.sliide.usermanager.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.sliide.usermanager.R
import com.sliide.usermanager.databinding.UserItemBinding
import com.sliide.usermanager.model.User

class UserAdapter(
    var onLongClickListener: OnLongClickListener? = null
) : RecyclerView.Adapter<UserAdapter.GenericViewHolder>() {

    private var userList = ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val itemBinding: UserItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.user_item,
            parent,
            false
        )

        return GenericViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

        if (user.status == "active") {
            holder.binding.textStatus.setTextColor(Color.GREEN)
        } else {
            holder.binding.textStatus.setTextColor(Color.RED)
        }

        holder.itemView.setOnLongClickListener {
            onLongClickListener?.onLongClick(user)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setUserList(userList: ArrayList<User>) {
        this.userList = userList
    }

    class GenericViewHolder(
        val binding: UserItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User?) {
            binding.setVariable(BR.user, user)
            binding.executePendingBindings()
        }
    }

    interface OnLongClickListener {
        fun onLongClick(user: User)
    }
}