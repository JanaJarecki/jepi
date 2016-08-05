<?php
include_once ("./Services/Component/classes/class.ilPluginConfigGUI.php");

/**
 * Example configuration class
 */
class ilassProgQuestionConfigGUI extends ilPluginConfigGUI {
	
	/**
	 * Handles all commmands, default is "configure"
	 */
	function performCommand($cmd) {
		$this->plugin_object->includeClass ( "class.assProgQuestionConfig.php" );
		$this->config = new assProgQuestionConfig ( $this->plugin_object );
		
		switch ($cmd) {
			default :
				$this->$cmd ();
				break;
		}
	}
	
	/**
	 * Configure
	 *
	 * @param        	
	 *
	 * @return
	 *
	 */
	function configure($restoreDefaults=false) {
		global $tpl, $ilToolbar, $ilCtrl;
		
		// ... Output configuration screen to $tpl...
		$form = $this->getSettingsForm ($restoreDefaults);
		$tpl->setContent ( $form->getHTML () );
	}
	function setDefaultSettings() {
		$this->configure(true);
	}
	function saveSettings() {
		$ok = $this->config->saveSettings();
		if ($ok) {
			ilUtil::sendSuccess($this->plugin_object->txt('config_changed_message'));
		} else {
			ilUtil::sendFailure($this->plugin_object->txt('config_error_message'));
		}
		$this->configure();
		
	}
	public function getSettingsForm($restoreDefaults=false) {
		global $ilCtrl;
		require_once ("./Services/Form/classes/class.ilPropertyFormGUI.php");
		$form = new ilPropertyFormGUI ();
		$form->setFormAction ( $ilCtrl->getFormAction ( $this ) );
		
		$config = assProgQuestionConfig::_getStoredSettings ($restoreDefaults);
		
		// BWS-URL
		
		$ratingsystem_address = new ilTextInputGUI ( $this->plugin_object->txt ( 'ratingsystem_address' ), 'ratingsystem_address' );
		$ratingsystem_address->setInfo ( $this->plugin_object->txt ( 'ratingsystem_address_info' ) );
		$ratingsystem_address->setValue ( $config ['ratingsystem_address'] );
		$form->addItem ( $ratingsystem_address );
		
		$form->setTitle ( $this->plugin_object->txt ( 'settings' ) );
		$form->addCommandButton ( "saveSettings", $this->plugin_object->txt ( "save" ) );
		$form->addCommandButton ( "configure", $this->plugin_object->txt ( "cancel" ) );
		$form->addCommandButton ( "setDefaultSettings", $this->plugin_object->txt ( "default_settings" ) );
		
		return $form;
	}
}
?>