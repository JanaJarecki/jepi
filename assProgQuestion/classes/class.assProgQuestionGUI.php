<?php
/*
 * +----------------------------------------------------------------------------+ | ILIAS open source | +----------------------------------------------------------------------------+ | Copyright (c) 1998-2001 ILIAS open source, University of Cologne | | | | This program is free software; you can redistribute it and/or | | modify it under the terms of the GNU General Public License | | as published by the Free Software Foundation; either version 2 | | of the License, or (at your option) any later version. | | | | This program is distributed in the hope that it will be useful, | | but WITHOUT ANY WARRANTY; without even the implied warranty of | | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the | | GNU General Public License for more details. | | | | You should have received a copy of the GNU General Public License | | along with this program; if not, write to the Free Software | | Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. | +----------------------------------------------------------------------------+
 */

// TODO Fehler bei nichtausgefuellten Parametern beim Kompilieren (nur Speichern: Fehlermeldung, aber funzt)
include_once "./Modules/TestQuestionPool/classes/class.assQuestionGUI.php";
include_once "./Modules/Test/classes/inc.AssessmentConstants.php";
// include_once "./Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/ilProgrammingTextAreaInputGUI.php";

/**
 * The assProgQuestionGUI class encapsulates the GUI representation
 * for E-Klausuren der Universitaet zu Koeln.
 *
 * @author Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 * @author Sebastian Koch <koch@informatik.uni-koeln.de>
 *        
 * @version $Id: class.assProgQuestionGUI.php 1318 2010-03-03 10:19:37Z hschottm $
 *          @ingroup ModulesTestQuestionPool
 *          @ilctrl_iscalledby assProgQuestionGUI: ilObjQuestionPoolGUI, ilObjTestGUI, ilQuestionEditGUI, ilTestExpressPageObjectGUI
 *         
 */
class assProgQuestionGUI extends assQuestionGUI {
	private $newUnitId;
	
	/**
	 *
	 * @var ilassProgQuestionPlugin plugin object
	 */
	var $plugin = null;
	
	/**
	 *
	 * @var assProgQuestion question object
	 */
	var $object = null;
	
	/**
	 * assProgQuestionGUI constructor
	 *
	 * The constructor takes possible arguments an creates an instance of the assProgQuestionGUI object.
	 *
	 * check!
	 *
	 * @param integer $id
	 *        	The database id of a single choice question object
	 * @access public
	 */
	function __construct($id = -1) {
		parent::__construct ();
		include_once "./Services/Component/classes/class.ilPlugin.php";
		$pl = ilPlugin::getPluginObject ( IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion" );
		$pl->includeClass ( "class.assProgQuestion.php" );
		$this->plugin = $pl;
		$this->object = new assProgQuestion ();
		$this->newUnitId = null;
		if ($id >= 0) {
			$this->object->loadFromDb ( $id );
		}
	}
	function getCommand($cmd) {
		return $cmd;
	}
	
	/**
	 * Evaluates a posted edit form and writes the form data in the question object.
	 * Called when question is saved.
	 *
	 * check!
	 * Die Funktion schreibt die Eingaben aus dem Formular in ein ProgQuestion-Objekt
	 *
	 * @return integer A positive value, if one of the required fields wasn't set, else 0
	 */
	function writePostData($always = false) {
		$hasErrors = (! $always) ? $this->editQuestion ( true ) : false;
		if (! $hasErrors) {
			$this->object->setTitle ( $_POST ["title"] );
			$this->object->setAuthor ( $_POST ["author"] );
			$this->object->setComment ( $_POST ["comment"] );
			include_once "./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php";
			$questiontext = $_POST ["question"];
			$this->object->setQuestion ( $questiontext );
			$this->object->setEstimatedWorkingTime ( $_POST ["Estimated"] ["hh"], $_POST ["Estimated"] ["mm"], $_POST ["Estimated"] ["ss"] );
			$this->object->setPoints ( $_POST ["points"] );
			
			// NEU
			$this->object->setSolution ( $_POST ["solution"] );
			if ($_POST ['structure'] == 'iterative' || $_POST ['structure'] == 'iterativenorecursive') {
				$this->object->setCheckIterative ( true );
			} else {
				$this->object->setCheckIterative ( false );
			}
			
			if ($_POST ['structure'] == 'iterativenorecursive') {
				$this->object->setForbidRecursive ( true );
			} else {
				$this->object->setForbidRecursive ( false );
			}		
			if ($_POST ['structure'] == 'recursive' || $_POST ['structure'] == 'recursivenoiterative') {
				$this->object->setCheckRecursive ( true );
			} else {
				$this->object->setCheckRecursive ( false );
			}
			
			if ( $_POST ['structure'] == 'recursivenoiterative' ) {
				$this->object->setForbidIterative(true);
			} else {
				$this->object->setForbidIterative(false);
			}
			
			$this->writeParamSpecificPostData ();
			
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
		$this->object->flushParams ();
		foreach ( $_POST ['choice'] ['answer'] as $index => $answer ) {
			$answertext = $answer;
			$this->object->addTestParameterset ( $answertext, $_POST ['choice'] ['points'] [$index], $index );
		}
	}
	
	// wird von editQuestion() aufgerufen
	function getSelfAssessmentEditingMode() {
		return $this->object->getSelfAssessmentEditingMode ();
	}
	
	// wird von editQuestion() aufgerufen
	function getDefaultNrOfTries() {
		return 1;
	}
	
	/**
	 * Creates an output of the edit form for the question
	 *
	 * Methode wird beim Erstellen der Frage aufgerufen
	 *
	 * check!
	 * Funktion stimmt mit Beispiel-Plugin ueberein + ProgQuestion spezifische Aenderungen
	 *
	 * @access public
	 */
	function editQuestion() {
		// #1
		global $lng;
		$save = ((strcmp ( $this->ctrl->getCmd (), "save" ) == 0) || (strcmp ( $this->ctrl->getCmd (), "saveEdit" ) == 0)) ? TRUE : FALSE;
		$this->getQuestionTemplate ();
		
		// #2
		include_once ("./Services/Form/classes/class.ilPropertyFormGUI.php");
		$form = new ilPropertyFormGUI ();
		
		// $form->addCommandButton("save","Save");
		// $form->addCommandButton("saveReturn","SaveR");
		$form->setFormAction ( $this->ctrl->getFormAction ( $this ) );
		$form->setTitle ( $this->outQuestionType () );
		$form->setMultipart ( FALSE );
		$form->setTableWidth ( "100%" );
		$form->setId ( "assprogquestionquestion" );
		
		// title
		$title = new ilTextInputGUI ( $this->lng->txt ( "title" ), "title" );
		$title->setValue ( $this->object->getTitle () );
		$title->setRequired ( TRUE );
		$form->addItem ( $title );
		
		// #3
		if (! $this->getSelfAssessmentEditingMode ()) {
			// author
			$author = new ilTextInputGUI ( $this->lng->txt ( "author" ), "author" );
			$author->setValue ( $this->object->getAuthor () );
			$author->setRequired ( TRUE );
			$form->addItem ( $author );
			
			// description
			$description = new ilTextInputGUI ( $this->lng->txt ( "description" ), "comment" );
			$description->setValue ( $this->object->getComment () );
			$description->setRequired ( FALSE );
			$form->addItem ( $description );
		} else {
			// author as hidden field
			$hi = new ilHiddenInputGUI ( "author" );
			$author = ilUtil::prepareFormOutput ( $this->object->getAuthor () );
			if (trim ( $author ) == "") {
				$author = "-";
			}
			$hi->setValue ( $author );
			$form->addItem ( $hi );
		}
		
		// #4
		
		// #5
		// questiontext
		// $this->object->getPlugin()->includeClass("class.ilMOTextAreaInputGUI.php");
		// $question = new ilMOTextAreaInputGUI($this->lng->txt("question"), "question");
		$question = new ilTextAreaInputGUI ( $this->lng->txt ( "question" ), "question" );
		$question->setValue ( $this->object->prepareTextareaOutput ( $this->object->getQuestion () ) );
		$question->setRequired ( TRUE );
		$question->setRows ( 10 );
		$question->setCols ( 80 );
		// if (!$this->getSelfAssessmentEditingMode())
		// {
		$question->setUseRte ( TRUE );
		include_once "./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php";
		$tags = ilObjAdvancedEditing::_getUsedHTMLTags ( "assessment" );
		array_push ( $tags, 'input' );
		array_push ( $tags, 'select' );
		array_push ( $tags, 'option' );
		array_push ( $tags, 'button' );
		$question->setRteTags ( $tags );
		$question->addPlugin ( "latex" );
		$question->addButton ( "latex" );
		$question->addButton ( "pastelatex" );
		$question->setRTESupport ( $this->object->getId (), "qpl", "assessment" );
		// }
		$form->addItem ( $question );
		
		// #6
		if (! $this->getSelfAssessmentEditingMode ()) {
			// duration
			$duration = new ilDurationInputGUI ( $this->lng->txt ( "working_time" ), "Estimated" );
			$duration->setShowHours ( TRUE );
			$duration->setShowMinutes ( TRUE );
			$duration->setShowSeconds ( TRUE );
			$ewt = $this->object->getEstimatedWorkingTime ();
			$duration->setHours ( $ewt ["h"] );
			$duration->setMinutes ( $ewt ["m"] );
			$duration->setSeconds ( $ewt ["s"] );
			$duration->setRequired ( FALSE );
			$form->addItem ( $duration );
		} else {
			// number of tries
			if (strlen ( $this->object->getNrOfTries () )) {
				$nr_tries = $this->object->getNrOfTries ();
			} else {
				$nr_tries = $this->getDefaultNrOfTries ();
			}
			if ($nr_tries <= 0) {
				$nr_tries = 1;
			}
			$ni = new ilNumberInputGUI ( $this->lng->txt ( "qst_nr_of_tries" ), "nr_of_tries" );
			$ni->setValue ( $nr_tries );
			$ni->setMinValue ( 1 );
			$ni->setSize ( 5 );
			$ni->setMaxLength ( 5 );
			$ni->setRequired ( true );
			$form->addItem ( $ni );
		}
		
		// #7
		// points
		$points = new ilNumberInputGUI ( $this->lng->txt ( "points" ), "points" );
		$points->setValue ( $this->object->getPoints () );
		$points->setRequired ( TRUE );
		$points->setSize ( 3 );
		$points->setMinValue ( 0.0 );
		$form->addItem ( $points );
		
		// #7.1
		// *neu* Die Musterloesung
		$this->object->getPlugin ()->includeClass ( "class.ilProgrammingTextAreaInputGUI.php" );
		$solution = new ilProgrammingTextAreaInputGUI ( $this->object->getPlugin ()->txt ( "solution" ), "solution" );
		// $solution->setValue($this->object->prepareTextareaOutput($this->object->getSolution()));
		$solution->setValue ( $this->object->getSolution () );
		// $solution->setRequired(true);
		$solution->setRequired ( true );
		$solution->setRows ( 10 );
		$solution->setCols ( 80 );
		$solution->setInfo ( $this->object->getPlugin ()->txt ( "solutioninfo" ) );
		$form->addItem ( $solution );
		
		// Fuege Button hinzu, 1. Parameter ist Methode die nach Abschicken aufgerufen wird, 2. Parameter ist der Text
		// Mit dieser Methode landen die Buttons immer ganz unten neben Speichern
		$form->addCommandButton ( "compile", $this->object->getPlugin ()->txt ( "compile" ) );
		
		// *neu* Iterativ/Rekursiv?
		$radio_prop = new ilRadioGroupInputGUI ( $this->object->getPlugin ()->txt ( 'codestructure' ), "structure" );
		// $radio_prop->setInfo($this->object->getPlugin()->txt('structureinfo'));
		
		$op = new ilRadioOption ( $this->object->getPlugin ()->txt ( 'nostructure' ), "none", $this->object->getPlugin ()->txt ( 'nostructureinfo' ) );
		$radio_prop->addOption ( $op );
		

		$op = new ilRadioOption ( $this->object->getPlugin ()->txt ( 'recursive' ), "recursive", $this->object->getPlugin ()->txt ( 'recursiveinfo' ) );
		
		// nest text input in first option
		// $text_prop = new ilTextInputGUI("Text Input", "ti2");
		// $text_prop->setInfo("This is the info text of subitem 'Text Input' of Option 1.");
		// $op->addSubItem($text_prop);
		
		$radio_prop->addOption ( $op );
		
		$op = new ilRadioOption ( $this->object->getPlugin ()->txt ( 'recursivenoiterative' ), "recursivenoiterative", $this->object->getPlugin ()->txt ( 'recursivenoiterativeinfo' ) );
		$radio_prop->addOption ( $op );
		
		$op = new ilRadioOption ( $this->object->getPlugin ()->txt ( 'iterative' ), "iterative", $this->object->getPlugin ()->txt ( 'iterativeinfo' ) );
		$radio_prop->addOption ( $op );
		
		$op = new ilRadioOption ( $this->object->getPlugin ()->txt ( 'iterativenorecursive' ), "iterativenorecursive", $this->object->getPlugin ()->txt ( 'iterativenorecursiveinfo' ) );
		$radio_prop->addOption ( $op );
		
		// $cb_prop = new ilCheckboxInputGUI("Checkbox", "cbox2");
		if ($this->object->getCheckRecursive ()) {
			$radio_prop->setValue ( $this->object->getForbidIterative()?"recursivenoiterative":"recursive" );
		} elseif ($this->object->getCheckIterative ()) {
			$radio_prop->setValue ( $this->object->getForbidRecursive()?"iterativenorecursive":"iterative" );
		} else {
			$radio_prop->setValue ( "none" );
		}
		
		// $cb_prop->setChecked(true);
		// $cb_prop->setInfo("bla!");
		// $op->addSubItem($cb_prop);
		
		
		$form->addItem ( $radio_prop );
		
		// *neu* zu testende Parameter, beliebig viele Felder mit einzelner Punktzahl sollte moeglich sein
		// siehe Multiple Choice
		// include_once "./Modules/TestQuestionPool/classes/class.ilProgQuestionParaInputGUI.php";
		// include_once "./Customizing/global/plugin/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.ilProgQuestionParaInputGUI.php";
		include_once 'class.ilProgQuestionParaInputGUI.php';
		// $choices = new ilProgQuestionParaInputGUI($this->lng->txt( "answers" ), "choice");
		$choices = new ilProgQuestionParaInputGUI ( $this->object->getPlugin ()->txt ( "testparams" ), "choice" );
		// $choices->setRequired( true );
		$choices->setRequired ( false );
		$choices->setQuestionObject ( $this->object );
		$choices->setSingleline ( true );
		$choices->setAllowMove ( true );
		$choices->setInfo ( $this->object->getPlugin ()->txt ( "paraminfo" ) );
		if ($this->object->getSelfAssessmentEditingMode ()) {
			$choices->setSize ( 40 );
			$choices->setMaxLength ( 800 );
		}
		if (count ( $this->object->getTestParameterset () ) < 1)
			$this->object->addTestParameterset ( "", 0, 0 );
		$choices->setValues ( $this->object->getTestParameterset () );
		$form->addItem ( $choices );
		
		// #8
		if ($this->object->getId ()) {
			$hidden = new ilHiddenInputGUI ( "", "ID" );
			$hidden->setValue ( $this->object->getId () );
			$form->addItem ( $hidden );
		}
		
		// #9
		$this->addQuestionFormCommandButtons ( $form );
		
		// #10
		$errors = false;
		
		if ($save) {
			$form->setValuesByPost ();
			$errors = ! $form->checkInput ();
			$form->setValuesByPost (); // again, because checkInput now performs the whole stripSlashes handling and we need this if we don't want to have duplication of backslashes
			if ($errors)
				$checkonly = false;
		}
		
		// #11
		if (! $checkonly)
			$this->tpl->setVariable ( "QUESTION_DATA", $form->getHTML () );
		return $errors;
	}
	
	/**
	 * selbsterstellte Funktion -> ProgQuestion spezifisch
	 * taucht nicht im Beispiel-Plugin auf
	 *
	 * NEU: Compilieren des eingegebenen Codes
	 * Name muss mit Angabe des CommandButtons im Formular uebereinstimmen
	 * 
	 * @author Matthias Lohmann
	 */
	function compile() {
		// Erst mal speichern
		$this->writePostData ( true );
		$this->object->saveToDb ();
		
		// binde den Connector zum Bewertungssystem ein
		$this->object->getPlugin ()->includeClass ( "class.ilAssProgQuestionRatingSystemConnector.php" );
		$connector = new ilAssProgQuestionRatingSystemConnector ($this->object->getPlugin());
		// Parameter flach machen
		$params = array ();
		foreach ( $this->object->getTestParameterset () as $paramObject ) {
			// $params .= $paramObject->getAnswertext() .';';
			$params [] = $paramObject->getAnswertext ();
		}
		// sende Programmcode an das Bewertungssystem zum Kompilieren
		$result = $connector->compile ( $this->object->getSolution (), $params );
		
		// Ergebnis mitteilen
		$type = $result ['type'];
		switch ($type) {
			case 'success' :
				ilUtil::sendSuccess ( nl2br ( htmlspecialchars ( $result ['message'] . "\n\n" . $result ['paramsreturn'] ) ) );
				break;
			case 'failure' :
				ilUtil::sendFailure ( nl2br ( htmlspecialchars ( $result ['message'] . "\n\n" . $result ['diagnostics'] ) ) );
				break;
			default :
				ilUtil::sendInfo ( nl2br ( htmlspecialchars ( $result ['message'] ) ) );
		}
		// Ruft wieder das Formular auf
		$this->editQuestion ();
	}
	
	/**
	 * Ueberprueft, ob alle Felder beim Eingeben der Frage durch den Fragensteller ausgefuellt wurden.
	 *
	 * selbsterstelle Funktion???
	 * taucht nicht im Beispiel-Plugin auf
	 */
	function checkInput() {
		$cmd = $this->ctrl->getCmd ();
		
		if ((! $_POST ["title"]) or (! $_POST ["author"]) or (! $_POST ["question"]) or (! strlen ( $_POST ["points"] ))) {
			$this->addErrorMessage ( $this->lng->txt ( "fill_out_all_required_fields" ) );
			return FALSE;
		}
		
		return TRUE;
	}
	
	/**
	 * check!
	 *
	 * Wird beim Starten des Tests von Studenten zur Beantwortung als erstes aufgerufen und zeigt die Frage im Test-Modus
	 */
	function outQuestionForTest($formaction, $active_id, $pass = NULL, $is_postponed = FALSE, $use_post_solutions = FALSE, $show_feedback = FALSE) {
		$test_output = $this->getTestOutput ( $active_id, $pass, $is_postponed, $use_post_solutions, $show_feedback );
		$this->tpl->setVariable ( "QUESTION_OUTPUT", $test_output );
		$this->tpl->setVariable ( "FORMACTION", $formaction );
	}
	
	/**
	 * Get the question solution output
	 * 
	 * @param integer $active_id
	 *        	The active user id
	 * @param integer $pass
	 *        	The test pass
	 * @param boolean $graphicalOutput
	 *        	Show visual feedback for right/wrong answers
	 * @param boolean $result_output
	 *        	Show the reached points for parts of the question
	 * @param boolean $show_question_only
	 *        	Show the question without the ILIAS content around
	 * @param boolean $show_feedback
	 *        	Show the question feedback
	 * @param boolean $show_correct_solution
	 *        	Show the correct solution instead of the user solution
	 * @param boolean $show_manual_scoring
	 *        	Show specific information for the manual scoring output
	 * @return The solution output of the question as HTML code
	 */
	function getSolutionOutput($active_id, $pass = NULL, $graphicalOutput = FALSE, $result_output = FALSE, $show_question_only = TRUE, $show_feedback = FALSE, $show_correct_solution = FALSE, $show_manual_scoring = FALSE, $show_question_text = TRUE) {
		if ($show_correct_solution) {
			// get the contents of a correct solution
			// adapt this to your structure of answers
			$solutions = array (
					array (
							// "value1" => $this->plugin->txt("any_text"),
							// "value2" => $this->plugin->txt("any_text"),
							"value1" => 'progquest_studentsolution',
							"value2" => $this->object->getSolution (),
							"points" => $this->object->getMaximumPoints () 
					) 
			);
		} elseif ($active_id > 0) {
			// get the answers of the user for the active pass or from the last pass if allowed
			$solutions = $this->object->getSolutionValues ( $active_id, $pass );
		} else {
			// get empty contents
			// adapt this to your structure of answers
			$solutions = array (
					array (
							"value1" => "progquest_studentsolution",
							"value2" => "",
							"points" => "0" 
					) 
			);
		}
		
		// loop through the saved values of more records exist
		// the last record wins
		// adapt this to your structure of answers
		foreach ( $solutions as $solution ) {
			if ($solution ['value1'] == 'progquest_studentsolution') {
				$value1 = isset ( $solution ["value1"] ) ? $solution ["value1"] : "";
				$value2 = isset ( $solution ["value2"] ) ? $solution ["value2"] : "";
				$points = isset ( $solution ["points"] ) ? $solution ["points"] : "0";
			}
		}
		
		// get the solution template
		$template = $this->plugin->getTemplate ( "tpl.il_as_qpl_progquestion_output_solution.html" );
		
		if ($active_id > 0 and $graphicalOutput) {
			$points = $this->object->getReachedPoints ( $active_id, $pass ); // Da wir in DB die Points selber gar nicht haben, so abgreifen
			// output of ok/not ok icons for user entered solutions
			// in this example we have ony one relevant input field (points)
			// so we just need to tet the icon beneath this field
			// question types with partial answers may have a more complex output
			if ($this->object->getReachedPoints ( $active_id, $pass ) == $this->object->getMaximumPoints ()) {
				$template->setCurrentBlock ( "icon_ok" );
				$template->setVariable ( "ICON_OK", ilUtil::getImagePath ( "icon_ok.png" ) );
				$template->setVariable ( "TEXT_OK", $this->lng->txt ( "answer_is_right" ) );
				$template->parseCurrentBlock ();
			} else {
				$template->setCurrentBlock ( "icon_ok" );
				$template->setVariable ( "ICON_NOT_OK", ilUtil::getImagePath ( "icon_not_ok.png" ) );
				$template->setVariable ( "TEXT_NOT_OK", $this->lng->txt ( "answer_is_wrong" ) );
				$template->parseCurrentBlock ();
			}
		}
		
		// fill the template variables
		// adapt this to your structure of answers
		// $template->setVariable("LABEL_VALUE1", $this->plugin->txt('label_value1'));
		$template->setVariable ( "LABEL_VALUE2", $this->plugin->txt ( 'solutionoutput_label_solution' ) );
		$template->setVariable ( "LABEL_POINTS", $this->plugin->txt ( 'solutionoutput_label_points' ) );
		
		// $template->setVariable("VALUE1", empty($value1) ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : ilUtil::prepareFormOutput($value1));
		$template->setVariable ( "VALUE2", empty ( $value2 ) ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : nl2br(ilUtil::prepareFormOutput ( $value2 ) ));
		$template->setVariable ( "POINTS", empty ( $points ) ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : ilUtil::prepareFormOutput ( $points ) );
		
		// TODO this should in most cases ensure different IDs for different elements, but it is not guaranteed
		$template->setVariable ( "ID", 'cm' . mt_rand () );
		
		$questiontext = $this->object->getQuestion ();
		if ($show_question_text == true) {
			$template->setVariable ( "QUESTIONTEXT", $this->object->prepareTextareaOutput ( $questiontext, TRUE ) );
		}
		
		$template->setVariable ( "QUESTIONTEXT", $this->object->prepareTextareaOutput ( $questiontext, TRUE ) );
		
		$questionoutput = $template->get ();
		
		$solutiontemplate = new ilTemplate ( "tpl.il_as_tst_solution_output.html", TRUE, TRUE, "Modules/TestQuestionPool" );
		$solutiontemplate->setVariable ( "SOLUTION_OUTPUT", $questionoutput );
		
		$feedback = ($show_feedback) ? $this->getGenericFeedbackOutput ( $active_id, $pass ) : "";
		if (strlen ( $feedback ))
			$solutiontemplate->setVariable ( "FEEDBACK", $this->object->prepareTextareaOutput ( $feedback, true ) );
		
		$solutionoutput = $solutiontemplate->get ();
		if (! $show_question_only) {
			// get page object output
			$solutionoutput = $this->getILIASPage ( $solutionoutput );
		}
		return $solutionoutput;
	}
	
	/**
	 * check!
	 * Erstellt die Ausgabe fuer die Fragen-Vorschau
	 *
	 * @param string $show_question_only        	
	 * @return unknown
	 */
	function getPreview($show_question_only = FALSE, $showInlineFeedback = false) {
		$pl = $this->object->getPlugin ();
		$template = $pl->getTemplate ( "tpl.il_as_qpl_progquestion_output.html" );
		$template->setVariable ( "QUESTIONTEXT", $this->object->prepareTextareaOutput ( $this->object->getQuestion (), TRUE ) );
		$questionoutput = $template->get ();
		if (! $show_question_only) {
			// get page object output
			$questionoutput = $this->getILIASPage ( $questionoutput );
		}
		return $questionoutput;
	}
	
	/**
	 * check!
	 * Erstellt die HTML Ausgabe der Frage fuer den Test
	 *
	 * Wird von outQuestionForTest aufgerufen beim Starten des Tests aus Studentensicht
	 */
	function getTestOutput($active_id, $pass = NULL, $is_postponed = FALSE, $use_post_solutions = FALSE, $show_feedback = FALSE) {
		// get the solution of the user for the active pass or from the last pass if allowed
		$user_solution = "";
		if ($active_id) {
			$solutions = NULL;
			include_once "./Modules/Test/classes/class.ilObjTest.php";
			if (! ilObjTest::_getUsePreviousAnswers ( $active_id, true )) {
				if (is_null ( $pass ))
					$pass = ilObjTest::_getPass ( $active_id );
			}
			$solutions = & $this->object->getSolutionValues ( $active_id, $pass );
			$rating_system_response = array ();
			foreach ( $solutions as $idx => $solution_value ) {
				if ($solution_value ['value1'] == 'progquest_studentsolution') {
					$user_solution = $solution_value ['value2'];
				} elseif (strpos ( $solution_value ['value1'], 'progquest_ratingsystemresponse_' ) === 0) {
					$rating_system_response [substr ( $solution_value ['value1'], 31 )] = $solution_value ['value2'];
				}
				
				// $user_solution = $solution_value["value1"];
				// $rating_system_response = unserialize($solution_value["value2"]);
			}
		}
		
		// generate the question output
		$pl = $this->object->getPlugin ();
		$template = $pl->getTemplate ( "tpl.il_as_qpl_progquestion_output.html" );
		
		$template->setVariable ( "SOLUTION_ON_WORK", ilUtil::prepareFormOutput ( $user_solution ) );
		$questiontext = $this->object->getQuestion ();
		$template->setVariable ( "QUESTIONTEXT", $this->object->prepareTextareaOutput ( $questiontext, TRUE ) );
		// Setting these variables seems to trigger display of the containing block, too
		$template->setVariable ( "CMD_COMPILE", 'handleQuestionAction' );
		$template->setVariable ( "TEXT_COMPILE", $pl->txt ( "studcompile" ) );
		$template->setVariable ( "TEXT_PARAMS", $pl->txt ( "studparams" ) );
		
		if ($rating_system_response) {
			// TODO Doppelung mit Edit-Formular, eigene Methode?
			switch ($rating_system_response ['type']) {
				case 'success' :
					ilUtil::sendSuccess ( nl2br ( htmlspecialchars ( $rating_system_response ['message'] . "\n\n" . $rating_system_response ['paramsreturn'] ) ) );
					break;
				case 'failure' :
					ilUtil::sendFailure ( nl2br ( htmlspecialchars ( $rating_system_response ['message'] . "\n\n" . $rating_system_response ['diagnostics'] ) ) );
					break;
				default :
					ilUtil::sendInfo ( nl2br ( htmlspecialchars ( $rating_system_response ['message'] ) ) );
			}
			// $template->setVariable("RATING_SYSTEM_RESPONSE", $this->object->getRatingSystemResponse());
		}
		
		$questionoutput = $template->get ();
		$pageoutput = $this->outQuestionPage ( "", $is_postponed, $active_id, $questionoutput );
		include_once "./Services/YUI/classes/class.ilYuiUtil.php";
		ilYuiUtil::initDomEvent ();
		return $pageoutput;
	}
	
	/**
	 * Sets the ILIAS tabs for this question type
	 * aufgerufen von ilObjTestGUI und ilObjQuestionPoolGUI
	 *
	 * check!
	 * Funktion exakt identisch zu der gleichen Funktion im Beispiel-Plugin
	 *
	 * @access public
	 */
	function setQuestionTabs() {
		global $rbacsystem, $ilTabs;
		
		$this->ctrl->setParameterByClass ( "ilAssQuestionPageGUI", "q_id", $_GET ["q_id"] );
		include_once "./Modules/TestQuestionPool/classes/class.assQuestion.php";
		$q_type = $this->object->getQuestionType ();
		
		if (strlen ( $q_type )) {
			$classname = $q_type . "GUI";
			$this->ctrl->setParameterByClass ( strtolower ( $classname ), "sel_question_types", $q_type );
			$this->ctrl->setParameterByClass ( strtolower ( $classname ), "q_id", $_GET ["q_id"] );
		}
		
		if ($_GET ["q_id"]) {
			
			if ($rbacsystem->checkAccess ( 'write', $_GET ["ref_id"] )) {
				// edit page
				$ilTabs->addTarget ( "edit_content", $this->ctrl->getLinkTargetByClass ( "ilAssQuestionPageGUI", "edit" ), array (
						"edit",
						"insert",
						"exec_pg" 
				), "", "", $force_active );
			}
			
			// preview page
			$ilTabs->addTarget ( "preview", $this->ctrl->getLinkTargetByClass ( "ilAssQuestionPageGUI", "preview" ), array (
					"preview" 
			), "ilAssQuestionPageGUI", "", $force_active );
		}
		
		$force_active = false;
		if ($rbacsystem->checkAccess ( 'write', $_GET ["ref_id"] )) {
			$url = "";
			if ($classname)
				$url = $this->ctrl->getLinkTargetByClass ( $classname, "editQuestion" );
			$commands = $_POST ["cmd"];
			if (is_array ( $commands )) {
				foreach ( $commands as $key => $value ) {
					// TODO da stand vorher am Ende des Matchingstrings noch /* war das absicht? (PHP-Fehler!)
					if (preg_match ( "/^suggestrange_.*/", $key, $matches )) {
						$force_active = true;
					}
				}
			}
			// edit question properties
			$ilTabs->addTarget ( "edit_properties", $url, array (
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
			), $classname, "", $force_active );
		}
		
		// add tab for question feedback within common class assQuestionGUI
		$this->addTab_QuestionFeedback ( $ilTabs );
		
		// add tab for question hint within common class assQuestionGUI
		$this->addTab_QuestionHints ( $ilTabs );
		
		if ($_GET ["q_id"]) {
			$ilTabs->addTarget ( "solution_hint", $this->ctrl->getLinkTargetByClass ( $classname, "suggestedsolution" ), array (
					"suggestedsolution",
					"saveSuggestedSolution",
					"outSolutionExplorer",
					"cancel",
					"addSuggestedSolution",
					"cancelExplorer",
					"linkChilds",
					"removeSuggestedSolution" 
			), $classname, "" );
		}
		
		// Assessment of questions sub menu entry
		if ($_GET ["q_id"]) {
			$ilTabs->addTarget ( "statistics", $this->ctrl->getLinkTargetByClass ( $classname, "assessment" ), array (
					"assessment" 
			), $classname, "" );
		}
		
		if (($_GET ["calling_test"] > 0) || ($_GET ["test_ref_id"] > 0)) {
			$ref_id = $_GET ["calling_test"];
			if (strlen ( $ref_id ) == 0)
				$ref_id = $_GET ["test_ref_id"];
			$ilTabs->setBackTarget ( $this->lng->txt ( "backtocallingtest" ), "ilias.php?baseClass=ilObjTestGUI&cmd=questions&ref_id=$ref_id" );
		} else {
			$ilTabs->setBackTarget ( $this->lng->txt ( "qpl" ), $this->ctrl->getLinkTargetByClass ( "ilobjquestionpoolgui", "questions" ) );
		}
	}
	
	/**
	 * Returns the answer specific feedback for the question
	 *
	 * check!
	 *
	 * @param integer $active_id
	 *        	Active ID of the user
	 * @param integer $pass
	 *        	Active pass
	 * @return string HTML Code with the answer specific feedback
	 * @access public
	 */
	public function getSpecificFeedbackOutput($active_id, $pass) {
		// METHODE WIRD BENOETIGT! (abstrakte Methode in Oberklasse)
		// im Beispiel-Plugin wird tatsaechlich etwas zurueckgegeben ...
		return '';
	}
}
?>
