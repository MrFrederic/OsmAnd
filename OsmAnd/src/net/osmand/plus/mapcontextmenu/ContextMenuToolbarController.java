package net.osmand.plus.mapcontextmenu;

import static net.osmand.plus.views.mapwidgets.TopToolbarController.TopToolbarControllerType.CONTEXT_MENU;

import androidx.annotation.NonNull;

import net.osmand.plus.R;
import net.osmand.plus.views.mapwidgets.TopToolbarController;

public class ContextMenuToolbarController extends TopToolbarController {

	private final MenuController controller;

	public ContextMenuToolbarController(@NonNull MenuController controller) {
		super(CONTEXT_MENU);
		this.controller = controller;
		setBgIds(R.color.app_bar_main_light, R.color.app_bar_main_dark,
				R.color.app_bar_main_light, R.color.app_bar_main_dark);
		setBackBtnIconClrIds(R.color.active_buttons_and_links_text_light, R.color.active_buttons_and_links_text_dark);
		setCloseBtnIconClrIds(R.color.active_buttons_and_links_text_light, R.color.active_buttons_and_links_text_dark);
		setTitleTextClrIds(R.color.active_buttons_and_links_text_light, R.color.active_buttons_and_links_text_dark);
	}

	public MenuController getController() {
		return controller;
	}
}
