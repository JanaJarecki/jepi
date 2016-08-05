<?php
/* Copyright (c) 1998-2009 ILIAS open source, Extended GPL, see docs/LICENSE */

include_once("./Services/Form/classes/class.ilSubEnabledFormPropertyGUI.php");

/**
* This class represents a mathematik online input field in a property form.
*
* @author Helmut Schottmueller <ilias@aurealis.de> , Matthias Lohmann <lohmann@informatik.uni-koeln.de>
* @version $Id: class.ilProgQuestionInputGUI.php 1319 2010-03-03 10:26:51Z hschottm $
* @ingroup	ServicesForm
*/
class ilProgQuestionInputGUI extends ilSubEnabledFormPropertyGUI
{
	protected $exercise;
	protected $variant;
	private $plugin;

	/**
	* Constructor
	*
	* @param	string	$a_title	Title
	* @param	string	$a_postvar	Post Variable
	*/
	function __construct($a_title)
	{
		parent::__construct($a_title, '');
		$this->exercise = null;
		$this->variant = null;
	}

	/**
	 * @return object The plugin object
	 */
	public function getPlugin() 
	{
		if ($this->plugin == null)
		{
			include_once "./Services/Component/classes/class.ilPlugin.php";
			$this->plugin = ilPlugin::getPluginObject(IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion");
			
		}
		return $this->plugin;
	}

	/**
	* Set exercise.
	*
	* @param	string	$a_value	Value
	*/
	public function setExercise($a_value)
	{
		$this->exercise = $a_value;
	}

	/**
	* Get exercise.
	*
	* @return	string	Value
	*/
	public function getExercise()
	{
		return $this->exercise;
	}

	/**
	* Set variant.
	*
	* @param	string	$a_value	Value
	*/
	public function setVariant($a_value)
	{
		$this->variant = $a_value;
	}

	/**
	* Get variant.
	*
	* @return	string	Value
	*/
	public function getVariant()
	{
		return $this->variant;
	}

	/**
	* Set value by array
	*
	* @param	array	$a_values	value array
	*/
	function setValueByArray($a_values)
	{
		$this->setExercise($a_values['MOID']);
		$this->setVariant($a_values['MOVARIANT']);
	}

	/**
	* Check input, strip slashes etc. set alert, if input is not ok.
	*
	* @return	boolean		Input ok, true/false
	*/	
	function checkInput()
	{
		global $lng;

		if ($this->exercise < 1)
		{
			$this->setAlert($this->getPlugin()->txt("err_missing_exercise"));
			return false;
		}
		
		return $this->checkSubItemsInput();
	}

	/**
	* Render item
	*/
	protected function render($a_mode = "")
	{
		$pl = ilPlugin::getPluginObject(IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion");
		$tpl = $pl->getTemplate("tpl.prop_mo.html");

		if ($this->exercise)
				$tpl->setVariable("VALUE_EXERCISE", ' value="' . ilUtil::prepareFormOutput($this->exercise) . '"');
		if ($this->variant)
				$tpl->setVariable("VALUE_VARIANT", ' value="' . ilUtil::prepareFormOutput($this->variant) . '"');

		$tpl->setVariable("EXERCISE", $this->getPlugin()->txt("exercise"));
		$tpl->setVariable("VARIANT", $this->getPlugin()->txt("variant"));
		$tpl->setVariable("DOWNLOAD_EXERCISE", $this->getPlugin()->txt("add_moid"));
		return $tpl->get();
	}
	
	/**
	* Insert property html
	*
	* @return	int	Size
	*/
	function insert(&$a_tpl)
	{
		$html = $this->render();

		$a_tpl->setCurrentBlock("prop_generic");
		$a_tpl->setVariable("PROP_GENERIC", $html);
		$a_tpl->parseCurrentBlock();
	}
}
?>