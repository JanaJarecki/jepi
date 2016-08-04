<?php
/**
 * Die Klasse fuer den Konfigurationsbildschirm der Programmmierfrage mit der Eingabe der URL des Bewertungssystems.
 * 
 * @author Matthias Lohmann
 *
 */
class assProgQuestionConfig {
	public static function _getStoredSettings($getDefaults = false) {
		if (! $getDefaults) {
			global $ilDB;
			$settings = array ();
			$query = 'SELECT * FROM il_qpl_qst_prog_config';
			
			$result = $ilDB->query ( $query );
			while ( $row = $ilDB->fetchAssoc ( $result ) ) {
				$settings [$row ['name']] = $row ['value'];
			}
		}
		if (! isset ( $settings ['ratingsystem_address'] ))
			$settings ['ratingsystem_address'] = 'tcp://localhost:1234';
		
		return $settings;
	}
	/**
	 * @param $parameter_name //Is the of the parameter to modify (this is the Primary Key in DB)
	 * @param $value //Is the value of the parameter
	 */
	private function saveToDB($parameter_name, $value)
	{
		global $ilDB;
		$ilDB->replace('il_qpl_qst_prog_config',
				array(
						'name' => array('text', $parameter_name)
				),
				array(
						'value' => array('clob', $value),
				)
		);
	}
	
	public function getAdminInput()
	{
		$data = ilUtil::stripSlashesRecursive($_POST);
		//Clean array
		unset($data['cmd']);
		return $data;
	}
	
	public function saveSettings()
	{
		global $ilDB;
		//Old settings
		$saved_options_data = self::_getStoredSettings();
		//New settings
		$new_options_data = $this->getAdminInput();
		
		//Save to DB
		foreach ($saved_options_data as $paremeter_name => $saved_value) {
			if (array_key_exists($paremeter_name, $new_options_data) AND $saved_options_data[$paremeter_name] != $new_options_data[$paremeter_name]) {
				$this->saveToDB($paremeter_name, $new_options_data[$paremeter_name], 'options');
			}
		}
		return TRUE;
	}
}
?>