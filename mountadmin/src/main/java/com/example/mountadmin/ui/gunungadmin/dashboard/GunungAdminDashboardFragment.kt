package com.example.mountadmin.ui.gunungadmin.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.databinding.FragmentGunungAdminDashboardBinding
import com.example.mountadmin.ui.gunungadmin.mountain.GunungAdminMountainFragment
import com.example.mountadmin.utils.ImageDisplayUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import kotlin.math.ceil
import kotlin.math.max

class GunungAdminDashboardFragment : Fragment() {

    private var _binding: FragmentGunungAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GunungAdminDashboardViewModel by viewModels()

    private lateinit var routeCapacityAdapter: RouteCapacityAdapter
    private lateinit var recentRegistrationAdapter: RecentRegistrationAdapter

    private var mountainId: String = ""
    private var adminName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mountainId = it.getString(ARG_MOUNTAIN_ID, "")
            adminName = it.getString(ARG_ADMIN_NAME, "Admin")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGunungAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerViews()
        setupChart()
        setupObservers()

        if (mountainId.isNotEmpty()) {
            viewModel.loadDashboardData(mountainId)
        }
    }

    private fun setupUI() {
        binding.tvAdminName.text = "MountTrack"
        binding.tvDashboardSubtitle.text = "Admin Dashboard"

        binding.btnViewMountainDetails.setOnClickListener {
            navigateToMountainDetails()
        }

        binding.btnViewAllRegistrations.setOnClickListener {
            navigateToRegistrations()
        }

        binding.tvViewAll.setOnClickListener {
            navigateToRegistrations()
        }
    }

    private fun setupRecyclerViews() {
        // Route capacity adapter with status toggle
        routeCapacityAdapter = RouteCapacityAdapter { routeCapacity ->
            showRouteStatusDialog(routeCapacity)
        }
        binding.rvRouteCapacity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routeCapacityAdapter
            setHasFixedSize(true)
        }

        // Recent registrations adapter
        recentRegistrationAdapter = RecentRegistrationAdapter()
        binding.rvRecentRegistrations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentRegistrationAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChart() {
        binding.barChartRoutes.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            animateY(800)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.WHITE
                textSize = 10f
                granularity = 1f
                labelRotationAngle = -30f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#3D444C")
                textColor = Color.WHITE
                textSize = 10f
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
        }
    }

    private fun showRouteStatusDialog(routeCapacity: RouteCapacity) {
        val currentStatus = routeCapacity.routeStatus
        val newStatus = if (currentStatus == com.example.mountadmin.data.model.HikingRoute.STATUS_OPEN) {
            com.example.mountadmin.data.model.HikingRoute.STATUS_CLOSED
        } else {
            com.example.mountadmin.data.model.HikingRoute.STATUS_OPEN
        }

        val message = if (newStatus == com.example.mountadmin.data.model.HikingRoute.STATUS_CLOSED) {
            "Close route \"${routeCapacity.routeName}\"? Users won't be able to register for this route."
        } else {
            "Open route \"${routeCapacity.routeName}\"? Users will be able to register."
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Route Status")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.updateRouteStatus(mountainId, routeCapacity.routeId.ifEmpty { routeCapacity.routeName }, newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.mountain.observe(viewLifecycleOwner) { mountain ->
            mountain?.let {
                binding.tvMountainName.text = it.name
                binding.tvMountainElevation.text = "${it.elevation} masl"
                binding.tvMountainLocation.text = it.location
                binding.tvMountainStatus.text = if (it.isActive) "OPEN" else "CLOSED"
                binding.tvMountainStatus.setBackgroundResource(
                    if (it.isActive) R.drawable.bg_status_open else R.drawable.bg_status_closed
                )
                binding.tvDashboardTitle.text = "Dashboard – ${it.name}"

                // Load mountain image (Base64 or URL)
                ImageDisplayUtils.loadInto(
                    binding.ivMountainImage,
                    it.imageUrl,
                    R.drawable.ic_mountain_placeholder
                )
            }
        }

        viewModel.totalRegistrations.observe(viewLifecycleOwner) { total ->
            binding.tvTotalRegistrations.text = total.toString()
        }

        viewModel.pendingCount.observe(viewLifecycleOwner) { count ->
            binding.tvPendingCount.text = count.toString()
        }

        viewModel.approvedCount.observe(viewLifecycleOwner) { count ->
            binding.tvApprovedCount.text = count.toString()
        }

        viewModel.rejectedCount.observe(viewLifecycleOwner) { count ->
            binding.tvRejectedCount.text = count.toString()
        }

        viewModel.routeCapacities.observe(viewLifecycleOwner) { capacities ->
            routeCapacityAdapter.submitList(capacities)
        }

        viewModel.recentRegistrations.observe(viewLifecycleOwner) { registrations ->
            recentRegistrationAdapter.submitList(registrations)
            binding.tvNoRecentRegistrations.visibility =
                if (registrations.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.routeRegistrationChart.observe(viewLifecycleOwner) { data ->
            updateRouteChart(data)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateRouteChart(data: List<RouteRegistrationCount>) {
        if (data.isEmpty()) {
            binding.barChartRoutes.clear()
            binding.barChartRoutes.invalidate()
            binding.tvNoRouteChartData.visible()
            return
        }

        binding.tvNoRouteChartData.gone()

        val entries = data.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.count.toFloat())
        }
        val labels = data.map { it.routeName }

        val dataSet = BarDataSet(entries, "Registrations").apply {
            color = ContextCompat.getColor(requireContext(), R.color.gunung_accent)
            setDrawValues(true)
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        val maxValue = data.maxOfOrNull { it.count }?.toFloat() ?: 0f
        val axisMax = max(2f, ceil(maxValue / 2f) * 2f)

        binding.barChartRoutes.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            axisLeft.axisMaximum = axisMax
            axisLeft.setLabelCount((axisMax / 2f).toInt() + 1, true)
            this.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }
            invalidate()
        }
    }

    private fun navigateToMountainDetails() {
        val fragment = GunungAdminMountainFragment.newInstance(mountainId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToRegistrations() {
        // Trigger bottom navigation to Registration tab
        (activity as? GunungAdminDashboardActivity)?.let { activity ->
            activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_registration
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MOUNTAIN_ID = "arg_mountain_id"
        private const val ARG_ADMIN_NAME = "arg_admin_name"

        fun newInstance(mountainId: String, adminName: String): GunungAdminDashboardFragment {
            return GunungAdminDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MOUNTAIN_ID, mountainId)
                    putString(ARG_ADMIN_NAME, adminName)
                }
            }
        }
    }
}
