package org.romaframework.frontend.domain.wizard;

import java.util.HashMap;
import java.util.Map;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.core.annotation.CoreField;
import org.romaframework.aspect.validation.annotation.ValidationAction;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.ViewCallback;
import org.romaframework.aspect.view.feature.ViewFieldFeatures;
import org.romaframework.core.Roma;
import org.romaframework.frontend.RomaFrontend;

public class FormWizard<T> implements ViewCallback {
	protected FormWizardStep[]		steps;
	protected int									currentStep		= 0;

	@CoreField(useRuntimeType = AnnotationConstants.TRUE)
	protected FormWizardStep			content;
	protected Map<String, Object>	configuration	= new HashMap<String, Object>();

	public FormWizard(FormWizardStep[] steps) {
		this.steps = steps;
	}

	public void onShow() {
		showContent();
	}

	public void begin() {
		if (content.onBegin()) {
			beginAction();
		}
	}

	public void back() {
		if (currentStep > 0) {
			if (content.onBack()) {
				backAction();
			}
		}
	}

	@ValidationAction(validation = AnnotationConstants.TRUE)
	public void next() {
		if (currentStep < steps.length - 1) {

			if (content.onNext()) {
				nextAction();
			}
		}
	}

	@ValidationAction(validation = AnnotationConstants.TRUE)
	public void finish() {
		if (content.onFinish()) {

			finishAction();
		}
	}

	protected void finishAction() {
		currentStep = steps.length - 1;
		showContent();
	}

	public void cancel() {
		if (content.onCancel()) {
			cancelAction();
		}
	}

	public FormWizardStep getContent() {
		return content;
	}

	public FormWizardStep getStep(int iIndex) {
		return steps[iIndex];
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> T getStep(Class<T> iClass) {
		for (FormWizardStep s : steps)
			if (s.getClass().equals(iClass))
				return (T) s;

		return null;
	}

	public void onDispose() {
		for (FormWizardStep step : steps) {
			step.onDispose();
		}
	}

	protected void beginAction() {
		currentStep = 0;
		showContent();
	}

	protected void backAction() {
		currentStep--;
		showContent();
	}

	protected void nextAction() {
		currentStep++;
		showContent();
	}

	protected void cancelAction() {
		RomaFrontend.flow().back();
	}

	protected void showContent() {
		onBeforeShowContent();

		changeContent();

		onAfterShowContent();
	}

	protected void changeContent() {
		content = steps[currentStep];

		content.setContainer(this);
		content.onShow();
		Roma.fieldChanged(this, "content");
	}

	protected void onBeforeShowContent() {
		if (content != null)
			content.onDispose();
	}

	protected void onAfterShowContent() {
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "begin", ViewFieldFeatures.ENABLED, currentStep > 0);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "back", ViewFieldFeatures.ENABLED, currentStep > 0);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "next", ViewFieldFeatures.ENABLED, currentStep < steps.length - 1);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "finish", ViewFieldFeatures.ENABLED, currentStep == steps.length - 1);
	}

}
