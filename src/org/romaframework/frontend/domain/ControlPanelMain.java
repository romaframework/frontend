package org.romaframework.frontend.domain;

import org.romaframework.aspect.security.annotation.SecurityClass;
import org.romaframework.frontend.domain.page.ContainerPage;

@SecurityClass(readRoles = "profile:Administrator")
public class ControlPanelMain extends ContainerPage {

	public ControlPanelMain() {
	}

}
