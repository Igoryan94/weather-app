package com.igoryan94.weatherapp.ui.forecast

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.igoryan94.weatherapp.R

// Адаптер принимает пустой список по умолчанию, который мы будем обновлять
class ForecastAdapter(
    private var forecastList: List<ForecastDayUiModel> = emptyList(),
    // Лямбда для обработки клика: передаем саму View (для анимации) и данные
    private val onItemClick: (View, ForecastDayUiModel) -> Unit
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    /**
     * Вложенный класс ViewHolder.
     * Инициализирует и хранит ссылки на View-элементы одной карточки прогноза,
     * чтобы не вызывать findViewById каждый раз при прокрутке списка.
     */
    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCondition: TextView = itemView.findViewById(R.id.tvCondition)
        val tvTempDay: TextView = itemView.findViewById(R.id.tvTempDay)
        val tvTempNight: TextView = itemView.findViewById(R.id.tvTempNight)
    }

    /**
     * Метод onCreateViewHolder.
     * Вызывается компонентом RecyclerView, когда ему требуется создать новый ViewHolder.
     * Он берет XML-макет (item_forecast.xml) и "надувает" (inflate) его в реальный View-объект.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        // "Надуваем" (inflate) XML макет item_forecast в реальный объект View
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    /**
     * Метод onBindViewHolder.
     * Вызывается для каждого элемента списка, чтобы заполнить созданный View данными.
     * Здесь же устанавливается слушатель клика на весь элемент списка (itemView).
     */
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val currentItem = forecastList[position]
        holder.tvDate.text = currentItem.date
        holder.tvCondition.text = currentItem.condition
        holder.tvTempDay.text = currentItem.tempDay
        holder.tvTempNight.text = currentItem.tempNight

        // Задаем уникальное имя для Shared Element Transition на основе даты
        holder.itemView.transitionName = "forecast_card_transition_${currentItem.date}"

        // Вешаем слушатель клика на весь элемент списка
        holder.itemView.setOnClickListener {
            onItemClick(holder.itemView, currentItem)
        }
    }

    /**
     * Метод getItemCount.
     * Сообщает RecyclerView общее количество элементов, которые нужно отобразить.
     */
    override fun getItemCount(): Int {
        return forecastList.size
    }

    /**
     * Метод updateData.
     * Используется для обновления списка данных снаружи (из ViewModel или Фрагмента).
     * Заменяет старый список на новый и дает команду адаптеру перерисовать элементы.
     */
    fun updateData(newList: List<ForecastDayUiModel>) {
        forecastList = newList
        // Пока используем notifyDataSetChanged(),
        // позже можно будет FIXME улучшить через DiffUtil для плавных анимаций.
        @SuppressLint("NotifyDataSetChanged")
        notifyDataSetChanged()
    }
}