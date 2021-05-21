package ru.imageella.editorium

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.FragmentToolsBinding
import ru.imageella.editorium.interfaces.ToolSelectListener

class ToolsFragment : Fragment(R.layout.fragment_tools) {

    private val binding by viewBinding(FragmentToolsBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ToolsFragment::class.java.simpleName

        fun newInstance() = ToolsFragment()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rotateToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(1)
        }

        binding.filtersToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(2)
        }
        binding.scaleToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(3)
        }
        binding.retouchingToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(6)
        }
        binding.unsharpMaskingToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(7)
        }
        binding.affineToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(8)
        }
        binding.cubeToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(9)
        }

        binding.splineToolBtn.setOnClickListener {
            (activity as? ToolSelectListener)?.onToolClick(5)
        }
    }

}
