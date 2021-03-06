<?php
/**
 * ILIAS open source
 *
 * Copyright (c) 1998-2016 ILIAS open source, University of Köln / Basel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

// @TODO: Fehler bei nichtausgefuellten Parametern beim Kompilieren (nur Speichern: Fehlermeldung, aber funzt)
include_once "./Modules/TestQuestionPool/classes/class.assQuestionGUI.php";
include_once "./Modules/Test/classes/inc.AssessmentConstants.php";
// include_once "./Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/ilProgrammingTextAreaInputGUI.php";

/**
 * The assProgQuestionGUI class encapsulates the GUI representation
 * for E-Klausuren der Universitaet zu Koeln.
 *
 * @author            Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 * @author            Sebastian Koch <koch@informatik.uni-koeln.de>
 *
 * @version           $Id: class.assProgQuestionGUI.php 1318 2010-03-03 10:19:37Z hschottm $
 * @ingroup           ModulesTestQuestionPool
 * @ilctrl_iscalledby assProgQuestionGUI: ilObjQuestionPoolGUI, ilObjTestGUI, ilQuestionEditGUI, ilTestExpressPageObjectGUI
 *
 */
class assProgQuestionGUI extends assQuestionGUI {

	const CMD_EDIT_QUESTION = 'editQuestion';
	const CMD_SUGGESTEDSOLUTION = 'suggestedsolution';
	const CMD_ASSESSMENT = 'assessment';
	const CMD_COMPILE_TEACHER_CODE = 'compileTeacherCode';
	const CMD_RUN_TEACHER_CODE = 'runTeacherCode';
	const CMD_PREVIEW = 'preview';
	
	private $newUnitId;

	/** @var ilassProgQuestionPlugin plugin object */
	var $plugin = NULL;

	/** @var assProgQuestion question object */
	var $object = NULL;

	private $quest_types = array(
		"function_original",
		"testng"
	);


	/**
	 * assProgQuestionGUI constructor
	 *
	 * @param integer $id The database id of a single choice question object
	 * @access public
	 */
	function __construct($id = - 1) {
		parent::__construct();
		include_once "./Services/Component/classes/class.ilPlugin.php";
		$pl = ilassProgQuestionPlugin::getInstance();
		$pl->includeClass("class.assProgQuestion.php");
		$this->plugin = $pl;
		$this->object = new assProgQuestion ();
		$this->newUnitId = NULL;
		if ($id >= 0) {
			$this->object->loadFromDb($id);
		}
	}


	function getCommand($cmd) { return $cmd; }


	/**
	 * Evaluates a posted edit form and writes the form data in the question object.
	 * Called when question is saved.
	 *
	 * {@inheritdoc}
	 *
	 * @see assQuestionGUI::writePostData()
	 *
	 * @return integer A positive value, if one of the required fields wasn't set, else 0
	 */
	function writePostData($always = false) {
		$hasErrors = (!$always) ? $this->editQuestion(true) : false;
		if (!$hasErrors) {
			$this->object->setTitle($_POST ["title"]);
			$this->object->setAuthor($_POST ["author"]);
			$this->object->setComment($_POST ["comment"]);
			// include_once "./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php";
			$this->object->setProgQuestionType($this->quest_types [$_POST ["quest_type"]]);
			$this->object->setQuestion($_POST ["question"]);
			$this->object->setEstimatedWorkingTime($_POST ["Estimated"] ["hh"], $_POST ["Estimated"] ["mm"], $_POST ["Estimated"] ["ss"]);
			$this->object->setPoints($_POST ["points"]);

			// NEU
			$this->object->setSolution($_POST ["solution"]);
			$this->object->setTestCode($_POST ["test_code"]);

			if ($_POST ['structure'] == 'iterative' || $_POST ['structure'] == 'iterativenorecursive') {
				$this->object->setCheckIterative(true);
			} else {
				$this->object->setCheckIterative(false);
			}

			if ($_POST ['structure'] == 'iterativenorecursive') {
				$this->object->setForbidRecursive(true);
			} else {
				$this->object->setForbidRecursive(false);
			}
			if ($_POST ['structure'] == 'recursive' || $_POST ['structure'] == 'recursivenoiterative') {
				$this->object->setCheckRecursive(true);
			} else {
				$this->object->setCheckRecursive(false);
			}

			if ($_POST ['structure'] == 'recursivenoiterative') {
				$this->object->setForbidIterative(true);
			} else {
				$this->object->setForbidIterative(false);
			}

			$this->writeParamSpecificPostData();

			return 0;
		} else {
			return 1;
		}
	}


	/**
	 * selbsterstellte Funktion, also ProgQuestion spezifisch???
	 * Funktion taucht im Beispiel-Plugin nicht auf
	 *
	 * @param string $always
	 */
	public function writeParamSpecificPostData($always = true) {
		// Delete all existing answers and create new answers from the form data
		$this->object->flushParams();
		foreach ($_POST ['choice'] ['answer'] as $index => $answer) {
			$name = $_POST ['choice'] ['name'] [$index];
			$params = $answer;
			$points = $_POST ['choice'] ['points'] [$index];
			$this->object->addTestParameterset($name, $params, $points, $index);
		}
	}


	// wird von editQuestion() aufgerufen
	function getSelfAssessmentEditingMode() {
		return $this->object->getSelfAssessmentEditingMode();
	}


	// wird von editQuestion() aufgerufen
	function getDefaultNrOfTries() {
		return 1;
	}


	/**
	 * Creates the HTML of the authoring view where new questions are created or existing ones edited.
	 */
	function editQuestion() {
		global $lng;
		$this->getQuestionTemplate();

		include_once("./Services/Form/classes/class.ilPropertyFormGUI.php");
		$form = new ilPropertyFormGUI ();

		$form->setFormAction($this->ctrl->getFormAction($this));
		$form->setTitle($this->outQuestionType());
		$form->setMultipart(false);
		$form->setTableWidth("100%");
		$form->setId("assprogquestionquestion");

		$save = ((strcmp($this->ctrl->getCmd(), "save") == 0) || (strcmp($this->ctrl->getCmd(), "saveEdit") == 0)) ? true : false;
		$testMode = $this->getSelfAssessmentEditingMode();
		$question = $this->object;
		$plugin = $question->getPlugin();

		$plugin->includeClass("class.assProgQuestionGUIComponentFactory.php");
		$factory = new assProgQuestionGUIComponentFactory ($plugin, $this->lng);

		$form->addItem($factory->title($question->getTitle()));

		if ($testMode) {
			$form->addItem($factory->hiddenAuthor($question->getAuthor()));
		} else {
			$form->addItem($factory->author($question->getAuthor()));
			$form->addItem($factory->description($question->getComment()));
		}

		$questionText = $question->prepareTextareaOutput($question->getQuestion());
		$form->addItem($factory->question($questionText, $question->getId()));

		if ($testMode) {
			$form->addItem($factory->numberOfTries($question->getNrOfTries(), $this->getDefaultNrOfTries()));
		} else {
			$estimatedWorkingTime = $question->getEstimatedWorkingTime();
			$form->addItem($factory->duration($estimatedWorkingTime));
		}

		$form->addItem($factory->maximumPoints($question->getPoints()));
		$form->addItem($factory->questionType($question->getProgQuestionType(), $this->quest_types));
		$form->addItem($factory->solution($question->getSolution()));
		$form->addItem($factory->testCodeField($question->getTestCode()));

		// @note move buttons closer to code
		$form->addCommandButton(self::CMD_COMPILE_TEACHER_CODE, $plugin->txt("compile"));
		$form->addCommandButton(self::CMD_RUN_TEACHER_CODE, $plugin->txt("run"));

		$form->addItem($factory->methodTypeRequirements($question->getCheckRecursive(), $question->getForbidRecursive(), $question->getCheckIterative(), $question->getForbidIterative()));
		$form->addItem($factory->parameters($question->getTestParameterset(), $question->getSelfAssessmentEditingMode()));

		if ($question->getId()) {
			$form->addItem($factory->hiddenId($question->getId()));
		}

		// backmatter

		$this->addQuestionFormCommandButtons($form);

		// #10
		$errors = false;

		if ($save) {
			$form->setValuesByPost();
			$errors = !$form->checkInput();
			//$form->setValuesByPost (); // again, because checkInput now performs the whole stripSlashes handling and we need this if we don't want to have duplication of backslashes
			if ($errors) {
				$checkonly = false;
			}
		}

		// #11
		if (!$checkonly) {
			$this->tpl->setVariable("QUESTION_DATA", $form->getHTML());
		}

		return $errors;
	}


	/**
	 * Compiles the code from the authoring view.
	 * The feedback is displayed to the author.
	 */
	function compileTeacherCode() {
		$this->writePostData(true);
		$this->object->saveToDb();

		// evaluate the code
		$this->object->getPlugin()->includeClass("class.assProgQuestionEvalConnection.php");
		$type = $this->object->getProgQuestionType();
		switch ($type) {
			case "function_original":
				$result = assProgQuestionEvalConnection::compileCode($this->object, NULL, 'teacher');
				break;
			case "testng":
				$result = assProgQuestionEvalConnection::compileTestNG($this->object);
				break;
			default:
				$result['type'] = 'failure';
				$result['message'] = "ERROR: Backend does not know how to handle the question type: " . $type;
		}
		$this->handleResult($result);
	}


	/**
	 * Compiles and runs the author code with the given parameters.
	 * The feedback is presented to the author. In particular the return values are shown, or the error that occured.
	 */
	function runTeacherCode() {
		// Erst mal speichern
		$this->writePostData(true);
		$this->object->saveToDb();

		// evaluate the code
		$this->object->getPlugin()->includeClass("class.assProgQuestionEvalConnection.php");
		$type = $this->object->getProgQuestionType();
		switch ($type) {
			case "function_original":
				$result = assProgQuestionEvalConnection::runCode($this->object, NULL, 'teacher');
				break;
			case "testng":
				$result = assProgQuestionEvalConnection::runTestNG($this->object);
				break;
			default:
				$result['message'] = "ERROR: Backend does not know how to handle the question type: " . $type;
		}
		$this->handleResult($result);
	}


	/**
	 * The function parses the returned xml and renders the information to the authoring view.
	 *
	 * @param (string=>string) $result
	 */
	function handleResult($result) {
		// handle the result or error
		$type = $result ['type'];
		switch ($type) {
			case 'success' :
				ilUtil::sendSuccess(nl2br(htmlspecialchars($result ['message']))); // . "\n\n" . $result ['paramsreturn']
				break;
			case 'failure' :
			case 'compile error' :
				ilUtil::sendFailure(nl2br(htmlspecialchars($result ['message'] . "\n\n" . $result ['diagnostics'])));
				break;
			default :
				ilUtil::sendInfo(nl2br(htmlspecialchars($result ['message'])));
		}

		// update the displayed question
		$this->editQuestion();
	}


	/**
	 * Looks if all required fields have a value.
	 *
	 * @return boolean
	 */
	function checkInput() {
		$cmd = $this->ctrl->getCmd();

		if ((!$_POST ["quest_type"]) or (!$_POST ["title"]) or (!$_POST ["author"]) or (!$_POST ["question"]) or (!strlen($_POST ["points"]))) {
			$this->addErrorMessage($this->lng->txt("fill_out_all_required_fields"));

			return false;
		}

		return true;
	}


	/**
	 *
	 * Renders the test output of the question.
	 * {@inheritdoc}
	 *
	 * @see assQuestionGUI::outQuestionForTest()
	 */
	function getTestOutput($active_id, $pass = NULL, $is_postponed = false, $use_post_solutions = false, $show_feedback = false) {
		$test_output = $this->renderStudentView($active_id, $pass, $is_postponed, $use_post_solutions, $show_feedback);

		return $test_output;
	}


	/**
	 *
	 * {@inheritdoc}
	 *
	 * @see assQuestionGUI::getSolutionOutput()
	 */
	function getSolutionOutput($active_id, $pass = NULL, $graphicalOutput = false, $result_output = false, $show_question_only = true, $show_feedback = false, $show_correct_solution = false, $show_manual_scoring = false, $show_question_text = true) {
		$template = $this->plugin->getTemplate("tpl.il_as_qpl_progquestion_output_solution.html");

		$maxPoints = $this->object->getMaximumPoints();

		if ($show_question_text == true) {
			$questiontext = $this->object->getQuestion();
			$template->setCurrentBlock("questiontext");
			$template->setVariable("QUESTIONTEXT", $this->object->prepareTextareaOutput($questiontext, true));
			$template->parseCurrentBlock();
		}

		// If an active id is given we print the solution for a student.
		if ($active_id > 0) {
			// get the answers of the user for the active pass or from the last pass if allowed
			$studentSolutions = $this->object->getSolutionValues($active_id, $pass);
			$points = $this->object->getReachedPoints($active_id, $pass);

			if ($graphicalOutput) {
				if ($points == $maxPoints) {
					$template->setCurrentBlock("icon_ok");
					$template->setVariable("ICON_OK", ilUtil::getImagePath("icon_ok.png"));
					$template->setVariable("TEXT_OK", $this->lng->txt("answer_is_right"));
					$template->parseCurrentBlock();
				} else {
					$template->setCurrentBlock("icon_ok");
					$template->setVariable("ICON_NOT_OK", ilUtil::getImagePath("icon_not_ok.png"));
					$template->setVariable("TEXT_NOT_OK", $this->lng->txt("answer_is_wrong"));
					$template->parseCurrentBlock();
				}
			}

			foreach (array_reverse($studentSolutions, true) as $idx => $studentSolution) {

				if ($studentSolution["value1"] == "progquest_studentsolution") {
					$studentSolution ["value1"] = isset ($studentSolution ["value1"]) ? $studentSolution ["value1"] : "";
					$studentSolution ["value2"] = isset ($studentSolution ["value2"]) ? $studentSolution ["value2"] : "";
					if ($pass == NULL) {
						$studentSolution ["points"] = isset ($studentSolution ["points"]) ? $studentSolution ["points"] : "0";
					} else {
						$studentSolution ["points"] = $points;
					}

					$template->setCurrentBlock("points");
					$template->setVariable("VALUE_1", $this->plugin->txt('solutionoutput_label_solution'));
					$template->setVariable("VALUE_2", $idx + 1);

					$template->setVariable("LABEL_POINTS", $this->plugin->txt('solutionoutput_label_points'));
					$template->setVariable("POINTS", $studentSolution ["points"]);
					$template->parseCurrentBlock();

					$template->setCurrentBlock("answer");
					$template->setVariable("SOLUTION", $studentSolution ["value2"]);
					$template->setVariable("ID", 'cm' . $idx);
					$template->parseCurrentBlock();
				}
			}
		}

		// show the correct solution if requested
		if ($show_correct_solution) {
			$template->setCurrentBlock("answer_title");
			$template->setVariable("VALUE_1", $this->plugin->txt('solutionoutput_label_solution'));
			$template->parseCurrentBlock();

			$template->setCurrentBlock("answer");
			$template->setVariable("SOLUTION", $this->object->getSolution());
			$template->setVariable("ID", 'cm' . mt_rand());
			$template->parseCurrentBlock();

			$template->setCurrentBlock("answer_title");
			$template->setVariable("VALUE_1", $this->plugin->txt('testCode'));
			$template->parseCurrentBlock();

			$template->setCurrentBlock("answer");
			$template->setVariable("SOLUTION", $this->object->getTestCode());
			$template->setVariable("ID", 'cm' . mt_rand());
			$template->parseCurrentBlock();

			$template->setCurrentBlock("points");
			$template->setVariable("LABEL_POINTS", $this->plugin->txt('solutionoutput_label_maxpoints'));
			$template->setVariable("POINTS", $maxPoints);
			$template->parseCurrentBlock();
		}

		$questionoutput = $template->get();

		$solutiontemplate = new ilTemplate ("tpl.il_as_tst_solution_output.html", true, true, "Modules/TestQuestionPool");
		$solutiontemplate->setVariable("SOLUTION_OUTPUT", $questionoutput);

		$feedback = ($show_feedback) ? $this->getGenericFeedbackOutput($active_id, $pass) : "";
		if (strlen($feedback)) {
			$solutiontemplate->setVariable("FEEDBACK", $this->object->prepareTextareaOutput($feedback, true));
		}

		$solutionoutput = $solutiontemplate->get();
		if (!$show_question_only) {
			// get page object output
			$solutionoutput = $this->getILIASPage($solutionoutput);
		}

		return $solutionoutput;
	}


	/**
	 *
	 * {@inheritdoc}
	 *
	 * @see assQuestionGUI::getPreview()
	 */
	function getPreview($show_question_only = false, $showInlineFeedback = false) {
		$pl = $this->object->getPlugin();
		$template = $pl->getTemplate("tpl.il_as_qpl_progquestion_output.html");
		$questiontext = $this->object->getQuestion();
		$template->setVariable("QUESTIONTEXT", $this->object->prepareTextareaOutput($questiontext, true));
		$template->setVariable("SOLUTION_ON_WORK", $this->object->getSolution());
		$template->setVariable("ID", 'cm' . mt_rand());
		$questionoutput = $template->get();
		if (!$show_question_only) {
			// get page object output
			$questionoutput = $this->getILIASPage($questionoutput);
		}

		return $questionoutput;
	}


	/**
	 * Generates the student view of the question during a test.
	 *
	 * 1. If previous results should be shown, look if some results are in the database.
	 * 2. Generte the output for the question.
	 *
	 * @param unknown $active_id
	 * @param unknown $pass
	 * @param string  $is_postponed
	 * @param string  $use_post_solutions
	 * @param string  $show_feedback
	 *
	 * @return mixed
	 */
	function renderStudentView($active_id, $pass = NULL, $is_postponed = false, $use_post_solutions = false, $show_feedback = false) {
		$user_solution = "";
		$user_params = "";

		if ($active_id != NULL) {
			$solutions = NULL;
			include_once "./Modules/Test/classes/class.ilObjTest.php";
			if (!ilObjTest::_getUsePreviousAnswers($active_id, true)) {
				if (is_null($pass)) {
					$pass = ilObjTest::_getPass($active_id);
				}
			}
			$solutions = &$this->object->getSolutionValues($active_id, $pass);
			$rating_system_response = array();
			foreach ($solutions as $idx => $solution_value) {
				if ($solution_value ['value1'] == 'progquest_studentsolution') {
					$user_solution = $solution_value ['value2'];
				} elseif (strpos($solution_value ['value1'], 'progquest_ratingsystemresponse_') === 0) {
					$rating_system_response [substr($solution_value ['value1'], 31)] = $solution_value ['value2'];
				} elseif ($solution_value ['value1'] == 'progquest_studentparams') {
					$user_params = $solution_value['value2'];
				}
			}
		}

		// generate the question output
		$pl = $this->object->getPlugin();
		$template = $pl->getTemplate("tpl.il_as_qpl_progquestion_output.html");

		$questiontext = $this->object->getQuestion();
		$template->setVariable("QUESTIONTEXT", $this->object->prepareTextareaOutput($questiontext, true));

		$template->setVariable("SOLUTION_ON_WORK", ilUtil::prepareFormOutput($user_solution));
		$template->setVariable("ID", "cm" . mt_rand());
		$template->setVariable("READ_ONLY", "false");

		$template->setVariable("CMD_COMPILE", 'handleQuestionAction');
		$template->setVariable("TEXT_COMPILE", $pl->txt("studcompile"));

		$template->setVariable("CMD_RUN", 'handleQuestionAction');
		$template->setVariable("TEXT_RUN", $pl->txt("run"));

		$template->setVariable("STUD_PARAMS_INPUT", $user_params);

		$template->setVariable("CMD_FEEDBACK", 'handleQuestionAction');
		$template->setVariable("TEXT_FEEDBACK", $pl->txt("feedback"));

		if ($this->object->getProgQuestionType() == "function_original") {
			$template->setVariable("TEXT_PARAMS", $pl->txt("studparams"));
		} else {
			$template->setVariable("TEXT_PARAMS", $pl->txt("studmain"));
		}

		if ($rating_system_response) {
			// @TODO: Doppelung mit Edit-Formular, eigene Methode?
			switch ($rating_system_response ['type']) {
				case 'success' :
					ilUtil::sendSuccess(nl2br(htmlspecialchars($rating_system_response ['message']))); // . "\n\n" . $rating_system_response ['paramsreturn']
					break;
				case 'failure' :
					ilUtil::sendFailure(nl2br(htmlspecialchars($rating_system_response ['message']))); // . "\n\n" . $rating_system_response ['diagnostics']
					break;
				case 'warning' :
					ilUtil::sendQuestion(nl2br(htmlspecialchars($rating_system_response ['message'])));
					break;
				default :
					ilUtil::sendInfo(nl2br(htmlspecialchars($rating_system_response ['message'])));
			}
		}

		$questionoutput = $template->get();
		$pageoutput = $this->outQuestionPage("", $is_postponed, $active_id, $questionoutput);
		include_once "./Services/YUI/classes/class.ilYuiUtil.php";
		ilYuiUtil::initDomEvent();

		return $pageoutput;
	}


	/**
	 *
	 * {@inheritdoc}
	 *
	 * @see assQuestionGUI::setQuestionTabs()
	 */
	function setQuestionTabs() {
		global $rbacsystem, $ilTabs;

		$this->ctrl->setParameterByClass(ilAssQuestionPageGUI::class, "q_id", $_GET ["q_id"]);
		include_once "./Modules/TestQuestionPool/classes/class.assQuestion.php";
		$q_type = $this->object->getQuestionType();

		if (strlen($q_type)) {
			$classname = $q_type . "GUI";
			$this->ctrl->setParameterByClass(strtolower($classname), "sel_question_types", $q_type);
			$this->ctrl->setParameterByClass(strtolower($classname), "q_id", $_GET ["q_id"]);
		}

		if ($_GET ["q_id"]) {

			if ($rbacsystem->checkAccess('write', $_GET ["ref_id"])) {
				$force_active = false;
				// edit page
				$ilTabs->addTarget("edit_content", $this->ctrl->getLinkTargetByClass(ilAssQuestionPageGUI::class, "edit"), array(
					"edit",
					"insert",
					"exec_pg"
				), "", "", $force_active); // TODO: Translate
			}

			// preview page
			$ilTabs->addTarget("preview", $this->ctrl->getLinkTargetByClass(ilAssQuestionPageGUI::class, self::CMD_PREVIEW), array(
				self::CMD_PREVIEW
			), ilAssQuestionPageGUI::class, "", $force_active); // TODO: Translate
		}

		$force_active = false;
		if ($rbacsystem->checkAccess('write', $_GET ["ref_id"])) {
			$url = "";
			if ($classname) {
				$url = $this->ctrl->getLinkTargetByClass($classname, self::CMD_EDIT_QUESTION);
			}
			$commands = $_POST ["cmd"];
			if (is_array($commands)) {
				foreach ($commands as $key => $value) {
					// @TODO: da stand vorher am Ende des Matchingstrings noch /* war das absicht? (PHP-Fehler!)
					if (preg_match("/^suggestrange_.*/", $key, $matches)) {
						$force_active = true;
					}
				}
			}
			// edit question properties
			$ilTabs->addTarget("edit_properties", $url, array(
				"editQuestion",
				"save",
				"cancel",
				"addSuggestedSolution",
				"cancelExplorer",
				"linkChilds",
				"removeSuggestedSolution",
				"parseQuestion",
				"saveEdit",
				"suggestRange"
			), $classname, "", $force_active); // TODO: Translate
		}

		// add tab for question feedback within common class assQuestionGUI
		$this->addTab_QuestionFeedback($ilTabs);

		// add tab for question hint within common class assQuestionGUI
		$this->addTab_QuestionHints($ilTabs);

		if ($_GET ["q_id"]) {
			$ilTabs->addTarget("solution_hint", $this->ctrl->getLinkTargetByClass($classname, self::CMD_SUGGESTEDSOLUTION), array(
				"suggestedsolution",
				"saveSuggestedSolution",
				"outSolutionExplorer",
				"cancel",
				"addSuggestedSolution",
				"cancelExplorer",
				"linkChilds",
				"removeSuggestedSolution"
			), $classname, ""); // TODO: Translate
		}

		// Assessment of questions sub menu entry
		if ($_GET ["q_id"]) {
			$ilTabs->addTarget("statistics", $this->ctrl->getLinkTargetByClass($classname, self::CMD_ASSESSMENT), array(
				"assessment"
			), $classname, ""); // TODO: Translate
		}

		if (($_GET ["calling_test"] > 0) || ($_GET ["test_ref_id"] > 0)) {
			$ref_id = $_GET ["calling_test"];
			if (strlen($ref_id) == 0) {
				$ref_id = $_GET ["test_ref_id"];
			}
			$ilTabs->setBackTarget($this->lng->txt("backtocallingtest"), "ilias.php?baseClass=" . ilObjTestGUI::class
				. "&cmd=questions&ref_id=$ref_id");
		} else {
			$ilTabs->setBackTarget($this->lng->txt("qpl"), $this->ctrl->getLinkTargetByClass(ilObjQuestionPoolGUI::class, "questions"));
		}
	}


	/**
	 * Returns the answer specific feedback for the question.
	 *
	 * {@inheritdoc}
	 *
	 * @see    assQuestionGUI::getSpecificFeedbackOutput()
	 *
	 * @param integer $active_id
	 *            Active ID of the user
	 * @param integer $pass
	 *            Active pass
	 *
	 * @return string HTML Code with the answer specific feedback
	 * @access public
	 */
	public function getSpecificFeedbackOutput($active_id, $pass) {
		// METHODE WIRD BENOETIGT! (abstrakte Methode in Oberklasse)
		// im Beispiel-Plugin wird tatsaechlich etwas zurueckgegeben ...
		return '';
	}
}
