package com.example.mounttrack.ui.registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingRoute

class RouteAdapter(
    context: Context,
    private val routes: List<HikingRoute>
) : ArrayAdapter<HikingRoute>(context, 0, routes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_route_dropdown, parent, false)

        val route = routes[position]

        val tvRouteName = view.findViewById<TextView>(R.id.tvRouteName)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)
        val tvRouteInfo = view.findViewById<TextView>(R.id.tvRouteInfo)

        tvRouteName.text = route.name

        // Set difficulty with color
        tvDifficulty.text = route.difficulty
        val difficultyColor = when (route.difficulty) {
            HikingRoute.DIFFICULTY_EASY -> R.color.difficulty_easy
            HikingRoute.DIFFICULTY_MODERATE -> R.color.difficulty_moderate
            HikingRoute.DIFFICULTY_HARD, HikingRoute.DIFFICULTY_DIFFICULT -> R.color.difficulty_hard
            HikingRoute.DIFFICULTY_EXPERT -> R.color.difficulty_expert
            else -> R.color.difficulty_moderate
        }
        tvDifficulty.setTextColor(ContextCompat.getColor(context, difficultyColor))

        // Hide route info (distance and time)
        tvRouteInfo.visibility = View.GONE

        return view
    }
}

