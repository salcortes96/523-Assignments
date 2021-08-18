package com.example.ninjaautodetailing.ui.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ninjaautodetailing.databinding.FragmentLoginBinding
import com.example.ninjaautodetailing.databinding.FragmentRateUsBinding

class RateFragment : Fragment() {

    private lateinit var rateViewModel: RateViewModel
    private var _binding: FragmentRateUsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rateViewModel =
            ViewModelProvider(this).get(RateViewModel::class.java)

        _binding = FragmentRateUsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRate
        rateViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}