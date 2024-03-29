package am5800.harmonie.android.controllers.workspace

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.ListViewController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.FragmentController
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.vm.workspace.LanguageWorkspaceItemViewModel
import am5800.harmonie.app.vm.workspace.SimpleWorkspaceItemViewModel
import am5800.harmonie.app.vm.workspace.WorkspaceViewModel
import android.widget.ListView
import java.security.InvalidParameterException

class WorkspaceController(private val vm: WorkspaceViewModel,
                          lifetime: Lifetime,
                          private val controllerStack: ControllerStack,
                          private val localizationService: LocalizationService) : FragmentController {
  override val id: Int = R.layout.workspace_screen

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {

    ListViewController.bind(view.getChild<ListView>(R.id.workspaceListView), bindingLifetime, view, vm.items, {
      if (it is LanguageWorkspaceItemViewModel) LanguageWorkspaceItemController(it, localizationService)
      else if (it is SimpleWorkspaceItemViewModel) SimpleWorkspaceItemController(it)
      else throw InvalidParameterException()
    })
  }

  init {
    vm.activationRequested.subscribe(lifetime, {
      controllerStack.push(this, javaClass.name)
    })
  }
}