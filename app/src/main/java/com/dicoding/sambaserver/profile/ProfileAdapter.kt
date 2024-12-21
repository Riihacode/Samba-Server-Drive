package com.dicoding.sambaserver.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.sambaserver.R

class ProfileAdapter (private val listMembers: ArrayList<ProjectMembers>)
    : RecyclerView.Adapter<ProfileAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_row_member, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (name, descriptionNim, photo, roleMember, dutyMember) = listMembers[position]
        holder.tvName.text = name
        holder.tvDescriptionNim.text = descriptionNim
        Glide.with(holder.itemView.context)
            .load(photo) // URL Gambar)
            .placeholder(R.drawable.ic_launcher_foreground) // Gambar default
            .error(R.drawable.ic_launcher_foreground) // Gambar jika gagal memuat
            //.circleCrop()
            .into(holder.imgPhoto)

        holder.tvRoleMember.text = roleMember
        holder.tvDutyMember.text = dutyMember
    }

    override fun getItemCount(): Int = listMembers.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvDescriptionNim: TextView = itemView.findViewById(R.id.tv_item_description_nim)
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_item_photo)
        val tvRoleMember: TextView = itemView.findViewById(R.id.tv_item_role)
        val tvDutyMember: TextView = itemView.findViewById(R.id.tv_item_note)
    }
}