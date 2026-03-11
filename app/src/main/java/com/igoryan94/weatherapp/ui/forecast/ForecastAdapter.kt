package com.igoryan94.weatherapp.ui.forecast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.igoryan94.weatherapp.R

// Адаптер принимает пустой список по умолчанию, который мы будем обновлять
class ForecastAdapter(private var forecastList: List<ForecastDayUiModel> = emptyList()) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    // Вложенный класс ViewHolder. Хранит ссылки на View-элементы одного элемента списка.
    // Это оптимизирует работу, чтобы не вызывать findViewById каждый раз при прокрутке.
    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCondition: TextView = itemView.findViewById(R.id.tvCondition)
        val tvTempDay: TextView = itemView.findViewById(R.id.tvTempDay)
        val tvTempNight: TextView = itemView.findViewById(R.id.tvTempNight)
    }

    // Метод создания нового ViewHolder. Вызывается, когда RecyclerView нуждается в новой View.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        // "Надуваем" (inflate) XML макет item_forecast в реальный объект View
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    // Метод привязки данных. Вызывается для каждого элемента, чтобы заполнить View данными из списка.
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val currentItem = forecastList[position]
        holder.tvDate.text = currentItem.date
        holder.tvCondition.text = currentItem.condition
        holder.tvTempDay.text = currentItem.tempDay
        holder.tvTempNight.text = currentItem.tempNight
    }

    // Возвращает общее количество элементов в списке.
    override fun getItemCount(): Int {
        return forecastList.size
    }

    // Метод для обновления данных снаружи (из Фрагмента/ViewModel).
    // Получает новый список, перезаписывает старый и уведомляет адаптер об изменениях.
    fun updateData(newList: List<ForecastDayUiModel>) {
        forecastList = newList
        // Пока используем notifyDataSetChanged(),
        // позже можно будет улучшить через DiffUtil для плавных анимаций.
        notifyDataSetChanged()
    }
}