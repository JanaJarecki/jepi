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
include_once "./Modules/TestQuestionPool/classes/class.assQuestion.php";
include_once "./Modules/Test/classes/inc.AssessmentConstants.php";

/**
 * Klasse fuer E-Klausur des Instituts fuer Informatik der Uni Koeln
 *
 * @author  Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 * @author  Sebastian Koch <koch@informatik.uni-koeln.de>
 *
 * @version $Id: class.assProgQuestion.php 2197 2010-07-28 08:30:12Z hschottm $
 * @ingroup ModulesTestQuestionPool
 *
 */
class assProgQuestion extends assQuestion {

	private $plugin;

	// authored parameters of the question
	private $prog_question_type = "";
	private $solution = "";
	private $test_code = "";
	private $test_parameterset = array();
	private $rating_system_response = array();
	private $check_recursive = false;
	private $check_iterative = false;
	private $forbid_recursive = false;
	private $forbid_iterative = false;

	// student answers
	private $answer = "";
	private $student_values = array();


    /**
	 * assProgQuestion Konstruktor
	 *
	 * Der Konstruktor erwartet verschiedene Argumente und erstellt dann eine Instanz der Programmierfrage.
	 *
	 * @param string  $title
	 *            A title string to describe the question
	 * @param string  $comment
	 *            A comment string to describe the question
	 * @param string  $author
	 *            A string containing the name of the questions author
	 * @param integer $owner
	 *            A numerical ID to identify the owner/creator
	 * @param string  $question
	 *            The question string of the single choice question
	 *
	 * @access public
	 * @see    assQuestion:assQuestion()
	 */
	function __construct($title = "", $comment = "", $author = "", $owner = - 1, $question = "") {
		parent::__construct($title, $comment, $author, $owner, $question);
		// $this->plugin = null;
		$this->getPlugin();
	}

	/**
	 * Liefert das Plugin-Objekt zurueck.
	 *
	 * @return object The plugin object
	 */
	public function getPlugin() {
		if ($this->plugin == NULL) {
			include_once "./Services/Component/classes/class.ilPlugin.php";
			$this->plugin = ilassProgQuestionPlugin::getInstance();
		}

		return $this->plugin;
	}

    /**
     * Gibt wahr zurueck, wenn die Frage fertig zum beantworten ist.
     *
     * @return boolean True, if the single choice question is complete for use, otherwise false
     * @access public
     */
    function isComplete() {
        if (($this->title) and ($this->author) and ($this->question) and ($this->getMaximumPoints() > 0)) {
            return true;
        } else {
            return false;
        }
    }

    /** {@inheritdoc} */
    function getQuestionType() { return ilassProgQuestionPlugin::PLUGIN_NAME; }

    /** {@inheritdoc} */
    function getMaximumPoints() { return $this->points; }

    /** {@inheritdoc} */
    protected function reworkWorkingData($active_id, $pass, $obligationsAnswered, $authorized) { }

    public function getProgQuestionType() { return $this->prog_question_type; }
    public function setProgQuestionType($type) { $this->prog_question_type = ( string )$type; }

    public function getSolution() { return $this->solution; }
    public function setSolution($code) { $this->solution = $code; }

    public function getTestCode() { return $this->test_code; }
    public function setTestCode($code) { $this->test_code = $code; }

    public function getCheckRecursive() { return $this->check_recursive; }
    public function setCheckRecursive($r) { $this->check_recursive = ( boolean )$r; }

    public function getCheckIterative() { return $this->check_iterative; }
    public function setCheckIterative($i) { $this->check_iterative = ( boolean )$i; }

    public function getForbidIterative() { return $this->forbid_iterative; }
    public function setForbidIterative($i) { $this->forbid_iterative = ( boolean )$i; }

    public function getForbidRecursive() { return $this->forbid_recursive; }
    public function setForbidRecursive($r) { $this->forbid_recursive = ( boolean )$r; }


    /** Clears the array of test parameters. */
    public function flushParams() { $this->test_parameterset = array(); }

    /**
     * Returns the array of test parameters.
     * @return array
     */
    public function getTestParameterset() {
        return $this->test_parameterset;
    }

    /**
     * Adds new test parameters.
     *
     * @param string $answertext
     * @param real   $points
     * @param number $order
     * @param string $answerimage
     */
    function addTestParameterset($name = "", $answertext = "", $points = 0.0, $order = 0) {
        $this->getPlugin()->includeClass("class.assProgQuestionParameters.php");
        if (array_key_exists($order, $this->test_parameterset)) {
            // insert answer
            $answer = new assProgQuestionParameters ($name, $answertext, $points, $order);
            $newchoices = array();
            for ($i = 0; $i < $order; $i ++) {
                array_push($newchoices, $this->test_parameterset [$i]);
            }
            array_push($newchoices, $answer);
            for ($i = $order; $i < count($this->test_parameterset); $i ++) {
                $changed = $this->test_parameterset [$i];
                $changed->setOrder($i + 1);
                array_push($newchoices, $changed);
            }
            $this->test_parameterset = $newchoices;
        } else {
            // add answer
            $answer = new assProgQuestionParameters ($name, $answertext, $points, count($this->answers));
            array_push($this->test_parameterset, $answer);
        }
    }


    /**
     * Save the question data to the database.
     */
    public function saveToDb($original_id = "") {
        $this->getPlugin()->includeClass("class.assProgQuestionDBConnection.php");
        $this->saveQuestionDataToDb($original_id);
        assProgQuestionDBConnection::saveassProgQuestion($this);
        assProgQuestionDBConnection::saveParamsToDb($this);
        parent::saveToDb($original_id);
    }


    /**
     * Loads the question data from the database.
     *
     * @param integer $question_id
     *            A unique key which defines the multiple choice test in the database
     */
    public function loadFromDb($question_id) {
        $this->getPlugin()->includeClass("class.assProgQuestionDBConnection.php");
        $validQuestion = assProgQuestionDBConnection::loadAssQuestion($this, $question_id);
        if ($validQuestion) {
            $validProgQuestion = assProgQuestionDBConnection::loadAssProgQuestion($this, $question_id);
            if ($validProgQuestion) {
                assProgQuestionDBConnection::loadParams($this, $question_id);
                parent::loadFromDb($question_id);
            }
        }
    }


    /**
     * Duplicates the question.
     *
     * {@inheritdoc}
     *
     * @see assQuestion::duplicate()
     */
    function duplicate($for_test = true, $title = "", $author = "", $owner = "", $testObjId = NULL) {
        if ($this->id <= 0) {
            // The question has not been saved. It cannot be duplicated
            return;
        }
        // duplicate the question in database
        $this_id = $this->getId();
        $thisObjId = $this_id;
        $clone = $this;
        include_once("./Modules/TestQuestionPool/classes/class.assQuestion.php");
        $original_id = assQuestion::_getOriginalId($this->id);
        $clone->id = - 1;
        if ($title) {
            $clone->setTitle($title);
        }

        if ($author) {
            $clone->setAuthor($author);
        }
        if ($owner) {
            $clone->setOwner($owner);
        }

        if ($for_test) {
            $clone->saveToDb($original_id);
        } else {
            $clone->saveToDb();
        }

        // copy question page content
        $clone->copyPageOfQuestion($this_id);

        // copy XHTML media objects
        $clone->copyXHTMLMediaObjectsOfQuestion($this_id);

        $clone->onDuplicate($thisObjId, $this_id, $clone->getObjId(), $clone->getId());

        return $clone->id;
    }



    /**
	 * Get the points for the provided solution.
	 * The solution is added to the DB and then sent to the rating system. The points
	 * are then
	 *
	 * @param integer $user_id
	 *            The database ID of the learner
	 * @param integer $test_id
	 *            The database Id of the test containing the question
	 * @param boolean $returndetails
	 *            (deprecated !!)
	 *
	 * @access public
	 */
	function calculateReachedPoints($active_id, $pass = NULL, $authorizedSolution = true, $returndetails = false) {
		global $ilDB;

		$reachedpoints = 0;
		// $found_values = array ();
		if (is_null($pass)) {
			$pass = $this->getSolutionMaxPass($active_id);
		}
		$result = $ilDB->queryF("SELECT * FROM tst_solutions WHERE active_fi = %s AND question_fi = %s AND pass = %s", array(
			'integer',
			'integer',
			'integer'
		), array(
			$active_id,
			$this->getId(),
			$pass
		));
		while ($data = $ilDB->fetchAssoc($result)) {
			if ($data ['value1'] == 'progquest_studentsolution') {
				$studentcode = $data ['value2'];
				$points = array();
				//$params = array ();
				foreach ($this->getTestParameterset() as $paramObject) {
					//$params [] = $paramObject->getParams ();
					$points [] = $paramObject->getPoints();
				}

				$this->getPlugin()->includeClass("class.assProgQuestionEvalConnection.php");
				$reachedpoints = 0;
				switch ($this->getProgQuestionType()) {
					case "function_original" :
						$ratingsystemresult = assProgQuestionEvalConnection::compareCode($this, $studentcode);
						if (isset ($ratingsystemresult ['points'])) {
							$fulfillsRec = !$this->getCheckRecursive() || $ratingsystemresult ['recursive'];
							$followNotRec = !$this->getForbidRecursive() || !$ratingsystemresult ['recursive'];
							$fulfillsIter = !$this->getCheckIterative() || $ratingsystemresult ['iterative'];
							$followNotIter = !$this->getForbidIterative() || !$ratingsystemresult ['iterative'];
							if ($fulfillsRec && $followNotRec && $fulfillsIter && $followNotIter) {
								$reachedpoints = $ratingsystemresult ['points'];
							}
						}
						break;
					case "testng" :
						$ratingsystemresult = assProgQuestionEvalConnection::runTestNG($this, $studentcode);
						if (isset ($ratingsystemresult ['points'])) {
							$reachedpoints = $ratingsystemresult ['points'];
						}
						break;
				}
				break;
			}
		}

		return min($reachedpoints, $this->getMaximumPoints());
	}


    /**
     *
     * @param string $code
     */
    private function handleStudentCode($code) {
        $result = array();
        $action = $_POST ['cmd'] ['handleQuestionAction'];
        $params = $_POST ['prog_params'];
        $type = $this->getProgQuestionType();

        $this->getPlugin()->includeClass("class.assProgQuestionEvalConnection.php");
        switch ($action) {
            case "Compile" :
                switch ($type) {
                    case "function_original" :
                        $result = assProgQuestionEvalConnection::compileCode($this, $code, 'student');
                        break;
                    case "testng" :
                        $result = assProgQuestionEvalConnection::compileTestNG($this, $code);
                        break;
                }
                break;
            case "Run" :
                switch ($type) {
                    case "function_original" :
                        $result = assProgQuestionEvalConnection::runCode($this, $code, 'student', array(
                            $params
                        ));
                        break;
                    case "testng" :
                        $result = assProgQuestionEvalConnection::runStudentTestNG($this, $code, array(
                            $params
                        ));
                        break;
                }
                break;
            case "Feedback" :
                switch ($type) {
                    case "function_original" :
                        $result = assProgQuestionEvalConnection::feedbackCode($this, $code, 'student', array(
                            $params
                        ));
                        break;
                    case "testng" :
                        $result = assProgQuestionEvalConnection::feedbackStudentTestNG($this, $code, array(
                            $params
                        ));
                        break;
                }
                break;
        }

        return $result;
    }

	/**
	 * Speichert die Eingabe des Studenten in der Datenbank
	 *
	 * @param integer $test_id
	 *            The database id of the test containing this question
	 *
	 * @return boolean Indicates the save status (true if saved successful, false otherwise)
	 * @access public
	 * @see    $answers
	 */
	/**
	 *
	 * {@inheritdoc}
	 *
	 * @see assQuestion::saveWorkingData()
	 */
	function saveWorkingData($active_id, $pass = NULL, $authorized = NULL) {
		global $ilDB;
		global $ilUser;

		include_once "./Services/Utilities/classes/class.ilStr.php";
		if (is_null($pass)) {
			include_once "./Modules/Test/classes/class.ilObjTest.php";
			$pass = ilObjTest::_getPass($active_id);
		}

		// this is question type specific data
		$affectedRows = $ilDB->manipulateF("DELETE FROM tst_solutions WHERE active_fi = %s AND question_fi = %s AND pass = %s", array(
			'integer',
			'integer',
			'integer'
		), array(
			$active_id,
			$this->getId(),
			$pass
		));
		$code = ilUtil::stripSlashes($_POST ["prog_area"], false);
		if (strlen($code)) {
			$time = time();

			$this->saveWorkingDataValue($active_id, $pass, 'progquest_studentsolution', trim($code), NULL, $time);
			$this->saveWorkingDataValue($active_id, $pass, 'progquest_studentparams', trim($_POST ["prog_params"]), NULL, $time);

			$this->rating_system_response = $this->handleStudentCode($code);
			if (is_array($this->rating_system_response)) {
				foreach ($this->rating_system_response as $key => $value) {
					$this->saveWorkingDataValue($active_id, $pass, 'progquest_ratingsystemresponse_' . $key, $value, NULL, $time);
				}
			}

			include_once("./Modules/Test/classes/class.ilObjAssessmentFolder.php");
			if (ilObjAssessmentFolder::_enabledAssessmentLogging()) {
				$this->logAction($this->lng->txtlng("assessment", "log_user_entered_values", ilObjAssessmentFolder::_getLogLanguage()), $active_id, $this->getId());
			}
		} else {
			include_once("./Modules/Test/classes/class.ilObjAssessmentFolder.php");
			if (ilObjAssessmentFolder::_enabledAssessmentLogging()) {
				$this->logAction($this->lng->txtlng("assessment", "log_user_not_entered_values", ilObjAssessmentFolder::_getLogLanguage()), $active_id, $this->getId());
			}
		}

		return true;
	}


	/**
	 * Speichert einen Wert der Arbeitsdateien.
	 *
	 * Aus dem GPL-lizensiertem "Accounting Question Plugin" uebernommen.
	 *
	 * @param integer $active_id
	 * @param integer $pass
	 * @param string  $key
	 * @param string  $value
	 * @param float   $points
	 * @param integer $time
	 */
	private function saveWorkingDataValue($active_id, $pass, $key, $value, $points, $time) {
		global $ilDB;

		// Hanging multiple request for a question from the same user may result in
		// unpredictable order of DELETE and INSERT and thus points written twice.
		// Using replace ethod instead of insert is not possible because value1 is clob.
		// Current workaround: allow values to be stored twice and detect them
		// in calculateReachedPoints.

		$query = "DELETE FROM tst_solutions" . " WHERE active_fi = " . $ilDB->quote($active_id, "integer") . " AND pass = "
			. $ilDB->quote($pass, "integer") . " AND question_fi = " . $ilDB->quote($this->getId(), "integer") . " AND value1 = "
			. $ilDB->quote($key, "text");

		$ilDB->manipulate($query);

		$next_id = $ilDB->nextId('tst_solutions');
		$ilDB->insert("tst_solutions", array(
			"solution_id" => array(
				"integer",
				$next_id
			),
			"active_fi" => array(
				"integer",
				$active_id
			),
			"pass" => array(
				"integer",
				$pass
			),
			"question_fi" => array(
				"integer",
				$this->getId()
			),
			"points" => array(
				"float",
				$points
			),
			"value1" => array(
				"clob",
				$key
			),
			"value2" => array(
				"clob",
				$value
			),
			"tstamp" => array(
				"integer",
				$time
			)
		));
	}


    /**
     * Erstellt eine Frage aus einer QTI Datei.
     * Erhaelt Parameter von einem QTI Parser und erstellt ein ILIAS Fragenobjekt.
     *
     * @param object  $item
     *            The QTI item object
     * @param integer $questionpool_id
     *            The id of the parent questionpool
     * @param integer $tst_id
     *            The id of the parent test if the question is part of a test
     * @param object  $tst_object
     *            A reference to the parent test object
     * @param integer $question_counter
     *            A reference to a question counter to count the questions of an imported question pool
     * @param array   $import_mapping
     *            An array containing references to included ILIAS objects
     *
     * @access public
     */
    function fromXML(&$item, &$questionpool_id, &$tst_id, &$tst_object, &$question_counter, &$import_mapping) {
        $this->getPlugin()->includeClass("import/qti12/class.assProgQuestionImport.php");
        $import = new assProgQuestionImport ($this);
        $import->fromXML($item, $questionpool_id, $tst_id, $tst_object, $question_counter, $import_mapping);
    }


    /**
     * Returns a QTI xml representation of the question and sets the internal
     * domxml variable with the DOM XML representation of the QTI xml representation!
     *
     * @return string The QTI xml representation of the question
     * @access public
     */
    function toXML($a_include_header = true, $a_include_binary = true, $a_shuffle = false, $test_output = false, $force_image_references = false) {
        $this->getPlugin()->includeClass("export/qti12/class.assProgQuestionExport.php");
        $export = new assProgQuestionExport($this);
        return $export->toXML($a_include_header, $a_include_binary, $a_shuffle, $test_output, $force_image_references);
    }


	/**
	 * Erstellt eine Excel Datei mit den kumulierten Loesungen der Fragen.
	 *
	 * @access public
	 * @see    assQuestion::setExportDetailsXLS()
	 *
	 * TODO: ILIAS 5.3 support
	 */
	public function setExportDetailsXLS($worksheet, $startrow, $active_id, $pass) {
		
		parent::setExportDetailsXLS($worksheet, $startrow, $active_id, $pass);
				
		$format_title = ""; // Temp fix
		$format_bold = ""; // Temp fix
		$solutions = $this->getSolutionValues($active_id, $pass);

		if (is_array($solutions)) {
			foreach ($solutions as $solution) {
				if ($solution ['value1'] == 'progquest_studentsolution') {
					// $value1 = isset ( $solution ["value1"] ) ? $solution ["value1"] : "";
					$value2 = isset ($solution ["value2"]) ? $solution ["value2"] : "";
					$points = isset ($solution ["points"]) ? $solution ["points"] : "0";
				}
			}
		}
		$points = $this->getReachedPoints($active_id, $pass); // Da wir in DB die Points selber gar nicht haben, so abgreifen

		$worksheet->setCell($startrow, 0, $this->plugin->txt($this->getQuestionType()), $format_title);
		$worksheet->setCell($startrow, 1, $this->getTitle(), $format_title);
		$i = 1;

		// now provide a result string and write it to excel
		// it is also possible to write multiple rows
		// $worksheet->setCell($startrow + $i, 0, $this->plugin->txt("label_value1"), $format_bold);
		// $worksheet->setCell($startrow + $i, 1, $value1);
		// $i++;

		$worksheet->setCell($startrow + $i, 0, $this->plugin->txt("xls_label_solution"), $format_bold);
		$worksheet->setCell($startrow + $i, 1, $value2);
		$i ++;

		$worksheet->setCell($startrow + $i, 0, $this->plugin->txt("xls_label_points"), $format_bold);
		$worksheet->setCell($startrow + $i, 1, $points);
		$i ++;

		return $startrow + $i + 1;
	}
}
