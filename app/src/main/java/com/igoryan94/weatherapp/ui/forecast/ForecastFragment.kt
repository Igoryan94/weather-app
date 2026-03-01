package com.igoryan94.weatherapp.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.databinding.FragmentForecastBinding

class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    // Свойство действительно только между onCreateView и onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val forecastViewModel =
            ViewModelProvider(this).get(ForecastViewModel::class.java)

        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textForecast
        forecastViewModel.text.observe(viewLifecycleOwner) { textValue ->
            textView.text = textValue
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}