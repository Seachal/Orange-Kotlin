package com.zhuzichu.base.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.*
import com.zhuzichu.base.R
import com.zhuzichu.base.ext.hideSoftInput
import com.zhuzichu.base.ext.toCast
import com.zhuzichu.base.ext.toast
import com.zhuzichu.base.widget.loading.LoadingMaker
import dagger.android.support.DaggerFragment
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

abstract class BaseFragment<TParams : BaseParamModel, TBinding : ViewDataBinding, TViewModel : BaseViewModel> :
    DaggerFragment(),
    IBaseFragment {

    companion object {
        private const val KEY_PARAM = "KEY_PARAM"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding: TBinding? = null
    lateinit var viewModel: TViewModel
    lateinit var params: TParams

    lateinit var activityCtx: Activity

    private var isInitData = false
    private var isInitLazy = false

    val navController by lazy { activityCtx.findNavController(R.id.delegate_container) }

    abstract fun setLayoutId(): Int
    abstract fun bindVariableId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            params = it.getParcelable<BaseParamModel>(KEY_PARAM).toCast()
        }
        binding = DataBindingUtil.inflate(
            inflater,
            setLayoutId(),
            container,
            false
        )
        binding?.lifecycleOwner = this
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewDataBinding()
        registUIChangeLiveDataCallback()
        initVariable()
        initView()
        initViewObservable()
        if (!isInitData) {
            initData()
            isInitData = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isInitLazy) {
            initLazyData()
            isInitLazy = true
        }
    }

    private fun initViewDataBinding() {
        val type = this::class.java.genericSuperclass
        if (type is ParameterizedType) {
            viewModel = ViewModelProvider(this, viewModelFactory)
                .get(type.actualTypeArguments[2].toCast())
        }
        binding?.setVariable(bindVariableId(), viewModel)
        lifecycle.addObserver(viewModel)
        viewModel.injectLifecycleOwner(viewLifecycleOwner)
    }

    private fun registUIChangeLiveDataCallback() {

        viewModel.uc.startActivityEvent.observe(this, Observer {
            val intent = Intent(activityCtx, it.clz)
            intent.putExtra(KEY_PARAM, it.paramModel)
            startActivityForResult(intent, it.requestCode, it.options)
            if (it.isPop) {
                activityCtx.finish()
            }
        })

        viewModel.uc.startFragmentEvent.observe(this, Observer {
            navController.navigate(
                it.actionId,
                bundleOf(KEY_PARAM to it.paramModel),
                getDefaultNavOptions(it.actionId)
            )
        })

        viewModel.uc.onBackPressedEvent.observe(this, Observer {
            activityCtx.onBackPressed()
        })

        viewModel.uc.showLoadingEvent.observe(this, Observer {
            activityCtx.hideSoftInput()
            view?.postDelayed({
                LoadingMaker.showLoadingDialog(requireContext())
            }, 150)
        })

        viewModel.uc.hideLoadingEvent.observe(this, Observer {
            view?.postDelayed({
                LoadingMaker.dismissLodingDialog()
            }, 150)
        })

        viewModel.uc.toastStringResEvent.observe(this, Observer {
            it.toast(context = requireContext())
        })

        viewModel.uc.toastStringEvent.observe(this, Observer {
            it.toast(context = requireContext())
        })
    }

    private fun getDefaultNavOptions(actionId: Int): NavOptions? {
        val navOptions = navController.currentDestination?.getAction(actionId)?.navOptions
        var options: NavOptions? = null
        navOptions?.let {
            options = navOptions {
                anim {
                    enter = R.anim.slide_in_right
                    exit = R.anim.slide_out_left
                    popEnter = R.anim.slide_in_left
                    popExit = R.anim.slide_out_right
                }
                launchSingleTop = navOptions.shouldLaunchSingleTop()
                popUpTo(navOptions.popUpTo) {
                    this.inclusive = navOptions.isPopUpToInclusive
                }
            }
        }
        return options
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCtx = requireActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
    }

}