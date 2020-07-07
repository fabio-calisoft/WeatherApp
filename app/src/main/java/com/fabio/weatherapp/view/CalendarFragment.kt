package com.fabio.weatherapp.view

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fabio.weatherapp.DateHelper
import com.fabio.weatherapp.R
import com.fabio.weatherapp.databinding.FragmentCalendarBinding
import ru.cleverpumpkin.calendar.CalendarDate
import ru.cleverpumpkin.calendar.CalendarView


class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarView: CalendarView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("fdl.DetailsFragment", "onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)

        Log.d("fdl.CalendarFragment", "onCreateView")
        binding.fragment = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("fdl.CalendarFragment", "onViewCreated")

        calendarView = view.findViewById(R.id.calendar_view)

        // calendar
        setupCalendar()
    }

    private fun setupCalendar() {

        val calendar = Calendar.getInstance()

        // Initial date
//        calendar.set(2018, Calendar.JUNE, 1)
//        val initialDate = CalendarDate(calendar.time)

        // Minimum available date
        calendar.set(2014, Calendar.JANUARY, 1)
        val minDate = CalendarDate(calendar.time)

        // get today date
        val today = DateHelper.getTodayDate()

        // Maximum available: today date
        calendar.set(today["year"] ?: 2020, today["month"] ?: 12, today["day"] ?: 31)
        val maxDate = CalendarDate(calendar.time)

        // The first day of week
        val firstDayOfWeek = java.util.Calendar.MONDAY

        // Set up calendar with all available parameters
        calendarView.setupCalendar(
            selectionMode = CalendarView.SelectionMode.SINGLE,
            minDate = minDate,
            maxDate = maxDate,
            firstDayOfWeek = firstDayOfWeek,
            showYearSelectionView = true
        )
        //            initialDate = initialDate,

        // Set date click callback
        calendarView.onDateClickListener = { date ->
            Log.d("fdl.calendar", "calendar selected date")
            val bundle = bundleOf(
                "YEAR" to date.year,
                "MONTH" to (date.month + 1),
                "DAY" to date.dayOfMonth
            )
            parentFragment?.findNavController()
                ?.navigate(R.id.action_calendarFragment_to_detailsFragment, bundle)
        }


    }

    fun close() {
        parentFragment?.findNavController()
            ?.navigate(R.id.action_calendarFragment_to_detailsFragment)
    }


}
