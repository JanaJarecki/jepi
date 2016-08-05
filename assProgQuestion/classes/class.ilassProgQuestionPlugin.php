<?php
	include_once "./Modules/TestQuestionPool/classes/class.ilQuestionsPlugin.php";
	
	/**
	 * Die Basisklasse des Programmierfragenplugins.
	 *
	 * @author Matthias Lohmann <lohmann@informatik.uni-koeln.de>
	 */
	class ilassProgQuestionPlugin extends ilQuestionsPlugin
	{
		/**
		 * check!
		 * Gibt den Namen des Plugins zurueck.
		 */
		final function getPluginName()
		{
			return "assProgQuestion";
		}
		
		/**
		 * check!
		 * Gibt den Fragetypen zurueck.
		 */
		final function getQuestionType()
		{
			return "assProgQuestion";
		}
		
		/**
		 * check!
		 * Gibt die Uebersetzung des Fragetypen zurueck.
		 */
		final function getQuestionTypeTranslation()
		{
			return $this->txt($this->getQuestionType());
		}
	}
?>