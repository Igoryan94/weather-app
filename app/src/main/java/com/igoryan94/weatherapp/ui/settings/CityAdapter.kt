package com.igoryan94.weatherapp.ui.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(
    private val onCityClick: (String) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    private val cities = mutableListOf<String>()

    // Для загрузки ПЕРВОЙ страницы или полного обновления списка
    fun submitList(newCities: List<String>) {
        cities.clear()
        cities.addAll(newCities)

        @SuppressLint("NotifyDataSetChanged")
        notifyDataSetChanged()
    }

    // Для пагинации: добавляем элементы в конец списка
    fun addItems(newCities: List<String>) {
        val startPosition = cities.size
        cities.addAll(newCities)
        notifyItemRangeInserted(startPosition, newCities.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.bind(city)
    }

    override fun getItemCount(): Int = cities.size

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(city: String) {
            textView.text = city
            itemView.setOnClickListener {
                onCityClick(city)
            }
        }
    }
}