package com.example.ninjaautodetailing.ui.plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ninjaautodetailing.databinding.FragmentLoginBinding
import com.example.ninjaautodetailing.databinding.FragmentMaintPlansBinding

class PlansFragment : Fragment() {

    private lateinit var plansViewModel: PlansViewModel
    private var _binding: FragmentMaintPlansBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        plansViewModel =
            ViewModelProvider(this).get(PlansViewModel::class.java)

        _binding = FragmentMaintPlansBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPlans
        plansViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}