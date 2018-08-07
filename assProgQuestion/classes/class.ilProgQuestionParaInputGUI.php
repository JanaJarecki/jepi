<?php
/* Copyright (c) 1998-2013 ILIAS open source, Extended GPL, see docs/LICENSE */

/**
 * This class represents a single choice wizard property in a property form.
 *
 * @author  Helmut Schottmueller <ilias@aurealis.de>, Matthias Lohmann <lohmann@informatik.uni-koeln.de>, Sebastian Koch <koch@informatik.uni-koeln.de>
 * @version $Id: class.ilSingleChoiceWizardInputGUI.php 48551 2014-03-12 11:36:55Z bheyser $
 * @ingroup ServicesForm
 *
 */
/*
 * Klasse fuer das Programmierfragenplugin erstellt.
 */

class ilProgQuestionParaInputGUI extends ilTextInputGUI {

	protected $params = array();
	protected $allowMove = true;
	protected $singleline = true;
	protected $qstObject = NULL;
	protected $suffixes = array();
	protected $showPoints = true;
	protected $showNames = true;
	protected $hideImages = false;


	/**
	 * Constructor
	 *
	 * @param string $a_title
	 *            Title
	 * @param string $a_postvar
	 *            Post Variable
	 */
	function __construct($a_title = "", $a_postvar = "") {
		parent::__construct($a_title, $a_postvar);
		$this->setSuffixes(array(
			"jpg",
			"jpeg",
			"png",
			"gif"
		));
		$this->setSize('25');
		$this->validationRegexp = "";
	}


	/**
	 * Set Value.
	 *
	 * @param string $a_value
	 *            Value
	 */
	function setValue($params) {
		$a_value = [];
		$this->params = array();
		if (is_array($params)) {
			if (is_array($a_value ['params'])) {
				foreach ($a_value ['params'] as $index => $param) {
					include_once "./Modules/TestQuestionPool/classes/class.assAnswerBinaryStateImage.php";
					$answer = new assProgQuestionAnswer ($a_value ['name'] [$index], $param, $a_value ['points'] [$index], $index, 1);
					array_push($this->params, $answer);
				}
			}
		}
	}


	/**
	 * Set Accepted Suffixes.
	 *
	 * @param array $a_suffixes
	 *            Accepted Suffixes
	 */
	function setSuffixes($a_suffixes) {
		$this->suffixes = $a_suffixes;
	}


	/**
	 * Set hide images.
	 *
	 * @param array $a_hide
	 *            Hide images
	 */
	function setHideImages($a_hide) {
		$this->hideImages = $a_hide;
	}


	/**
	 * Get Accepted Suffixes.
	 *
	 * @return array Accepted Suffixes
	 */
	function getSuffixes() {
		return $this->suffixes;
	}


	public function setShowPoints($a_value) {
		$this->showPoints = $a_value;
	}


	public function getShowPoints() {
		return $this->showPoints;
	}


	public function setShowNames($bool) {
		$this->showNames = $bool;
	}


	public function getShowNames() {
		return $this->showNames;
	}


	/**
	 * Set Values
	 *
	 * @param array $a_value
	 *            Value
	 */
	function setValues($a_values) {
		$this->params = $a_values;
	}


	/**
	 * Get Values
	 *
	 * @return array Values
	 */
	function getValues() {
		return $this->params;
	}


	/**
	 * Set singleline
	 *
	 * @param boolean $a_value
	 *            Value
	 */
	function setSingleline($a_value) {
		$this->singleline = $a_value;
	}


	/**
	 * Get singleline
	 *
	 * @return boolean Value
	 */
	function getSingleline() {
		return $this->singleline;
	}


	/**
	 * Set question object
	 *
	 * @param object $a_value
	 *            test object
	 */
	function setQuestionObject($a_value) {
		$this->qstObject = &$a_value;
	}


	/**
	 * Get question object
	 *
	 * @return object Value
	 */
	function getQuestionObject() {
		return $this->qstObject;
	}


	/**
	 * Set allow move
	 *
	 * @param boolean $a_allow_move
	 *            Allow move
	 */
	function setAllowMove($a_allow_move) {
		$this->allowMove = $a_allow_move;
	}


	/**
	 * Get allow move
	 *
	 * @return boolean Allow move
	 */
	function getAllowMove() {
		return $this->allowMove;
	}


	/**
	 * Check input, strip slashes etc.
	 * set alert, if input is not ok.
	 *
	 * @return boolean Input ok, true/false
	 */
	function checkInput() {
		global $lng;

		include_once "./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php";

		if (is_array($_POST [$this->getPostVar()])) {
			$_POST [$this->getPostVar()] = ilUtil::stripSlashesRecursive($_POST [$this->getPostVar()], true, ilObjAdvancedEditing::_getUsedHTMLTagsAsString("assessment"));
		}
		$foundvalues = $_POST [$this->getPostVar()];
		if (is_array($foundvalues)) {
			// check answers
			if (is_array($foundvalues ['answer'])) {
				foreach ($foundvalues ['answer'] as $aidx => $answervalue) {
					if (((strlen($answervalue)) == 0) && (strlen($foundvalues ['imagename'] [$aidx]) == 0)) {
						$this->setAlert($lng->txt("msg_input_is_required"));

						return false;
					}
				}
			}
			// check names
			if (is_array($foundvalues ['name'])) {
				foreach ($foundvalues ['name'] as $aidx => $answervalue) {
					if (((strlen($answervalue)) == 0) && (strlen($foundvalues ['name'] [$aidx]) == 0)) {
						$this->setAlert($lng->txt("msg_input_is_required"));

						return false;
					}
				}
			}
			// check points
			$max = 0;
			if (is_array($foundvalues ['points'])) {
				foreach ($foundvalues ['points'] as $points) {
					if ($points > $max) {
						$max = $points;
					}
					if (((strlen($points)) == 0) || (!is_numeric($points))) {
						$this->setAlert($lng->txt("form_msg_numeric_value_required"));

						return false;
					}
				}
			}
			if ($max == 0) {
				$this->setAlert($lng->txt("enter_enough_positive_points"));

				return false;
			}

			if (is_array($_FILES) && count($_FILES) && $this->getSingleline() && (!$this->hideImages)) {
				if (is_array($_FILES [$this->getPostVar()] ['error'] ['image'])) {
					foreach ($_FILES [$this->getPostVar()] ['error'] ['image'] as $index => $error) {
						// error handling
						if ($error > 0) {
							switch ($error) {
								case UPLOAD_ERR_INI_SIZE :
									$this->setAlert($lng->txt("form_msg_file_size_exceeds"));

									return false;
									break;

								case UPLOAD_ERR_FORM_SIZE :
									$this->setAlert($lng->txt("form_msg_file_size_exceeds"));

									return false;
									break;

								case UPLOAD_ERR_PARTIAL :
									$this->setAlert($lng->txt("form_msg_file_partially_uploaded"));

									return false;
									break;

								case UPLOAD_ERR_NO_FILE :
									if ($this->getRequired()) {
										if ((!strlen($foundvalues ['imagename'] [$index])) && (!strlen($foundvalues ['answer'] [$index]))) {
											$this->setAlert($lng->txt("form_msg_file_no_upload"));

											return false;
										}
									}
									break;

								case UPLOAD_ERR_NO_TMP_DIR :
									$this->setAlert($lng->txt("form_msg_file_missing_tmp_dir"));

									return false;
									break;

								case UPLOAD_ERR_CANT_WRITE :
									$this->setAlert($lng->txt("form_msg_file_cannot_write_to_disk"));

									return false;
									break;

								case UPLOAD_ERR_EXTENSION :
									$this->setAlert($lng->txt("form_msg_file_upload_stopped_ext"));

									return false;
									break;
							}
						}
					}
				} else {
					if ($this->getRequired()) {
						$this->setAlert($lng->txt("form_msg_file_no_upload"));

						return false;
					}
				}

				if (is_array($_FILES [$this->getPostVar()] ['tmp_name'] ['image'])) {
					foreach ($_FILES [$this->getPostVar()] ['tmp_name'] ['image'] as $index => $tmpname) {
						$filename = $_FILES [$this->getPostVar()] ['name'] ['image'] [$index];
						$filename_arr = pathinfo($filename);
						$suffix = $filename_arr ["extension"];
						$mimetype = $_FILES [$this->getPostVar()] ['type'] ['image'] [$index];
						$size_bytes = $_FILES [$this->getPostVar()] ['size'] ['image'] [$index];
						// check suffixes
						if (strlen($tmpname) && is_array($this->getSuffixes())) {
							if (!in_array(strtolower($suffix), $this->getSuffixes())) {
								$this->setAlert($lng->txt("form_msg_file_wrong_file_type"));

								return false;
							}
						}
					}
				}

				if (is_array($_FILES [$this->getPostVar()] ['tmp_name'] ['image'])) {
					foreach ($_FILES [$this->getPostVar()] ['tmp_name'] ['image'] as $index => $tmpname) {
						$filename = $_FILES [$this->getPostVar()] ['name'] ['image'] [$index];
						$filename_arr = pathinfo($filename);
						$suffix = $filename_arr ["extension"];
						$mimetype = $_FILES [$this->getPostVar()] ['type'] ['image'] [$index];
						$size_bytes = $_FILES [$this->getPostVar()] ['size'] ['image'] [$index];
						// virus handling
						if (strlen($tmpname)) {
							$vir = ilUtil::virusHandling($tmpname, $filename);
							if ($vir [0] == false) {
								$this->setAlert($lng->txt("form_msg_file_virus_found") . "<br />" . $vir [1]);

								return false;
							}
						}
					}
				}
			}
		} else {
			$this->setAlert($lng->txt("msg_input_is_required"));

			return false;
		}

		return $this->checkSubItemsInput();
	}


	/**
	 * Insert property html
	 *
	 * @return int Size
	 */
	function insert($a_tpl) {
		global $lng;

		// Neu: Pluginobjekt wird benoetigt um auf die Sprachdateien zuzugreifen
		$pl = ilassProgQuestionPlugin::getInstance();
		$tpl = new ilTemplate ("tpl.prop_progquestionparainput.html", true, true, "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion");

		if ($this->getShowNames()) {
			$tpl->setCurrentBlock("name_heading");
			$tpl->setVariable("NAME_TEXT", $lng->txt('name'));
			$tpl->parseCurrentBlock();
		}

		if ($this->getShowPoints()) {
			$tpl->setCurrentBlock("points_heading");
			$tpl->setVariable("POINTS_TEXT", $lng->txt('points'));
			$tpl->parseCurrentBlock();
		}

		$i = 0;
		foreach ($this->params as $value) {

			if ($this->getShowNames()) {
				$tpl->setCurrentBlock("name");
				$tpl->setVariable("SIZE", $this->getSize());
				$tpl->setVariable("NAME_ID", $this->getPostVar() . "[name][$i]");
				$tpl->setVariable("NAME_POST_VAR", $this->getPostVar());
				$tpl->setVariable("NAME_ROW_NUMBER", $i);
				$tpl->setVariable("PROPERTY_VALUE", ilUtil::prepareFormOutput($value->getName()));
				$tpl->parseCurrentBlock();
			}

			if ($this->getShowPoints()) {
				$tpl->setCurrentBlock("points");
				$tpl->setVariable("POINTS_ID", $this->getPostVar() . "[points][$i]");
				$tpl->setVariable("POINTS_POST_VAR", $this->getPostVar());
				$tpl->setVariable("POINTS_ROW_NUMBER", $i);
				$tpl->setVariable("PROPERTY_VALUE", ilUtil::prepareFormOutput($value->getPoints()));
				$tpl->parseCurrentBlock();
			}

			if ($this->getSingleline()) {

				$tpl->setCurrentBlock('singleline');
				$tpl->setVariable("PROPERTY_VALUE", ilUtil::prepareFormOutput($value->getParams()));
				$tpl->setVariable("SIZE", $this->getSize());
				$tpl->setVariable("SINGLELINE_ID", $this->getPostVar() . "[answer][$i]");
				$tpl->setVariable("SINGLELINE_ROW_NUMBER", $i);
				$tpl->setVariable("SINGLELINE_POST_VAR", $this->getPostVar());
				$tpl->setVariable("MAXLENGTH", $this->getMaxLength());
				if ($this->getDisabled()) {
					$tpl->setVariable("DISABLED_SINGLELINE", " disabled=\"disabled\"");
				}
				$tpl->parseCurrentBlock();
			} else {
				if (!$this->getSingleline()) {
					$tpl->setCurrentBlock('multiline');
					$tpl->setVariable("PROPERTY_VALUE", ilUtil::prepareFormOutput($value->getAnswertext()));
					$tpl->setVariable("MULTILINE_ID", $this->getPostVar() . "[answer][$i]");
					$tpl->setVariable("MULTILINE_ROW_NUMBER", $i);
					$tpl->setVariable("MULTILINE_POST_VAR", $this->getPostVar());
					if ($this->getDisabled()) {
						$tpl->setVariable("DISABLED_MULTILINE", " disabled=\"disabled\"");
					}
					$tpl->parseCurrentBlock();
				}
			}

			if ($this->getAllowMove()) {
				$tpl->setCurrentBlock("move");
				$tpl->setVariable("CMD_UP", "cmd[up" . $this->getFieldId() . "][$i]");
				$tpl->setVariable("CMD_DOWN", "cmd[down" . $this->getFieldId() . "][$i]");
				$tpl->setVariable("ID", $this->getPostVar() . "[$i]");
				$tpl->parseCurrentBlock();
			}

			$class = "";
			$tpl->setCurrentBlock("row");
			if ($i == 0) {
				$class .= " first";
			}
			if ($i == count($this->params) - 1) {
				$class .= " last";
			}
			$tpl->setVariable("POST_VAR", $this->getPostVar());
			$tpl->setVariable("ROW_NUMBER", $i);
			$tpl->setVariable("ID", $this->getPostVar() . "[answer][$i]");
			$tpl->setVariable("CMD_ADD", "cmd[add" . $this->getFieldId() . "][$i]");
			$tpl->setVariable("CMD_REMOVE", "cmd[remove" . $this->getFieldId() . "][$i]");
			if ($this->getDisabled()) {
				$tpl->setVariable("DISABLED_POINTS", " disabled=\"disabled\"");
			}
			$tpl->parseCurrentBlock();
			$i ++;
		}

		$tpl->setVariable("ELEMENT_ID", $this->getPostVar());
		$tpl->setVariable("TEXT_YES", $lng->txt('yes'));
		$tpl->setVariable("TEXT_NO", $lng->txt('no'));
		$tpl->setVariable("DELETE_IMAGE_HEADER", $lng->txt('delete_image_header'));
		$tpl->setVariable("DELETE_IMAGE_QUESTION", $lng->txt('delete_image_question'));

		// $tpl->setVariable("ANSWER_TEXT", $lng->txt('answer_text'));
		$tpl->setVariable("ANSWER_TEXT", $pl->txt('paramvalues'));
		$tpl->setVariable("COMMANDS_TEXT", $lng->txt('actions'));

		$a_tpl->setCurrentBlock("prop_generic");
		$a_tpl->setVariable("PROP_GENERIC", $tpl->get());
		$a_tpl->parseCurrentBlock();

		global $tpl;
		include_once "./Services/YUI/classes/class.ilYuiUtil.php";
		ilYuiUtil::initDomEvent();
		$pl = ilassProgQuestionPlugin::getInstance();
		$plPath = $pl->_getDirectory(IL_COMP_MODULE, "TestQuestionPool", "qst", ilassProgQuestionPlugin::PLUGIN_NAME);
		$tpl->addJavascript($plPath . "/js/parameterTable.js");
		//$tpl->addJavascript ( "./Modules/TestQuestionPool/templates/default/singlechoicewizard.js" );
		$tpl->addJavascript("./Services/Form/js/ServiceFormWizardInput.js");
	}
}
