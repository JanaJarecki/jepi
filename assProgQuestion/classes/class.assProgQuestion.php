<?php
/*
 * +----------------------------------------------------------------------------+ | ILIAS open source | +----------------------------------------------------------------------------+ | Copyright (c) 1998-2001 ILIAS open source, University of Cologne | | | | This program is free software; you can redistribute it and/or | | modify it under the terms of the GNU General Public License | | as published by the Free Software Foundation; either version 2 | | of the License, or (at your option) any later version. | | | | This program is distributed in the hope that it will be useful, | | but WITHOUT ANY WARRANTY; without even the implied warranty of | | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the | | GNU General Public License for more details. | | | | You should have received a copy of the GNU General Public License | | along with this program; if not, write to the Free Software | | Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. | +----------------------------------------------------------------------------+
 */
include_once "./Modules/TestQuestionPool/classes/class.assQuestion.php";
include_once "./Modules/Test/classes/inc.AssessmentConstants.php";

/**
 * Klasse fuer E-Klausur des Instituts fuer Informatik der Uni Koeln
 *
 * @author Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 * @author Sebastian Koch <koch@informatik.uni-koeln.de>
 *        
 * @version $Id: class.assProgQuestion.php 2197 2010-07-28 08:30:12Z hschottm $
 *          @ingroup ModulesTestQuestionPool
 *         
 */
class assProgQuestion extends assQuestion {
	private $plugin;
	
	// neu
	private $test_parameterset = array ();
	private $solution = "";
	private $rating_system_response = "";
	private $check_recursive = false;
	private $check_iterative = false;
	private $forbid_recursive = false;
	private $forbid_iterative = false;
	
	/**
	 * assProgQuestion Konstruktor
	 *
	 * Der Konstruktor erwartet verschiedene Argumente und erstellt dann eine Instanz der Programmierfrage.
	 *
	 * @param string $title
	 *        	A title string to describe the question
	 * @param string $comment
	 *        	A comment string to describe the question
	 * @param string $author
	 *        	A string containing the name of the questions author
	 * @param integer $owner
	 *        	A numerical ID to identify the owner/creator
	 * @param string $question
	 *        	The question string of the single choice question
	 * @access public
	 * @see assQuestion:assQuestion()
	 */
	function __construct($title = "", $comment = "", $author = "", $owner = -1, $question = "") {
		parent::__construct ( $title, $comment, $author, $owner, $question );
		// $this->plugin = null;
		$this->getPlugin ();
	}
	
	/**
	 * Liefert das Plugin-Objekt zurueck.
	 *
	 * @return object The plugin object
	 */
	public function getPlugin() {
		if ($this->plugin == null) {
			include_once "./Services/Component/classes/class.ilPlugin.php";
			$this->plugin = ilPlugin::getPluginObject ( IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion" );
		}
		return $this->plugin;
	}
	
	/**
	 * Loescht alle Parameter im TestParameterset.
	 */
	public function flushParams() {
		$this->test_parameterset = array ();
	}
	
	/**
	 * Gibt wahr zurueck, wenn die Frage fertig zum beantworten ist.
	 *
	 * @return boolean True, if the single choice question is complete for use, otherwise false
	 * @access public
	 */
	function isComplete() {
		if (($this->title) and ($this->author) and ($this->question) and ($this->getMaximumPoints () > 0)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Speichert ein assProgQuestion Objekt in die Datenbank.
	 * 
	 * @access public
	 */
	function saveToDb($original_id = "") {
		global $ilDB, $ilLog;
		
		$this->saveQuestionDataToDb ( $original_id );
		
		// save additional data
		// Musterloesung
		$affectedRows = $ilDB->manipulateF ( "DELETE FROM " . $this->getAdditionalTableName () . " WHERE question_fi = %s", array (
				"integer" 
		), array (
				$this->getId () 
		) );
		
		$affectedRows = $ilDB->manipulateF ( "INSERT INTO " . $this->getAdditionalTableName () . " (question_fi, solution, check_recursive, check_iterative, forbid_recursive, forbid_iterative) VALUES (%s, %s, %s, %s, %s, %s)", array (
				'integer',
				'text',
				'integer',
				'integer',
				'integer',
				'integer' 
		), array (
				$this->getId (),
				$this->getSolution (),
				( int ) $this->getCheckRecursive (),
				( int ) $this->getCheckIterative (),
				( int ) $this->getForbidRecursive (),
				( int ) $this->getForbidIterative () 
		)
		 );
		// Parameter werden in die Datenbank gespeichert
		$this->saveParamsToDb ();
		
		parent::saveToDb ( $original_id );
	}
	
	/**
	 * Speichert die Parameter in die Datenbank.
	 */
	public function saveParamsToDb() {
		/**
		 * @var ilDB $ilDB
		 */
		global $ilDB;
		// if (!$this->isSingleline)
		// {
		// ilUtil::delDir( $this->getImagePath() );
		// }
		$ilDB->manipulateF ( "DELETE FROM " . $this->getAnswerTableName () . " WHERE question_fi = %s", array (
				'integer' 
		), array (
				$this->getId () 
		) );
		
		foreach ( $this->test_parameterset as $key => $answer_obj ) {
			/**
			 * @var ASS_AnswerMultipleResponseImage $answer_obj
			 */
			$answer_obj = $this->test_parameterset [$key]; // TODO bringt das was? sieht redundant aus.
			if ($answer_obj->getAnswertext () != NULL) {
				$next_id = $ilDB->nextId ( $this->getAnswerTableName () );
				// answer_id,question_fi,params,points,aorder
				$ilDB->manipulateF ( "INSERT INTO " . $this->getAnswerTableName () . " (answer_id, question_fi, params, points, aorder) VALUES (%s, %s, %s, %s, %s)", array (
						'integer',
						'integer',
						'text',
						'float',
						'integer' 
				), array (
						$next_id,
						$this->getId (),
						// ilRTE::_replaceMediaObjectImageSrc( ($answer_obj->getAnswertext()!=NULL?$answer_obj->getAnswertext():""), 0 ),
						$answer_obj->getAnswertext (),
						$answer_obj->getPoints (),
						$answer_obj->getOrder () 
				) );
			}
		}
		// $this->rebuildThumbnails();
	}
	
	/**
	 * Laedt ein assProgQuestion Objekt, inklusive der Musterloesung, aus der Datenbank.
	 *
	 * @param object $db
	 *        	A pear DB object
	 * @param integer $question_id
	 *        	A unique key which defines the multiple choice test in the database
	 * @access public
	 */
	function loadFromDb($question_id) {
		global $ilDB;
		
		// load the basic question data
		$result = $ilDB->queryF ( "SELECT qpl_questions.* FROM qpl_questions WHERE question_id = %s", array (
				'integer' 
		), array (
				$question_id 
		) );
		if ($result->numRows () == 1) {
			$data = $ilDB->fetchAssoc ( $result );
			$this->setId ( $question_id );
			$this->setTitle ( $data ["title"] );
			$this->setComment ( $data ["description"] );
			$this->setSuggestedSolution ( $data ["solution_hint"] );
			$this->setOriginalId ( $data ["original_id"] );
			$this->setObjId ( $data ["obj_fi"] );
			$this->setAuthor ( $data ["author"] );
			$this->setOwner ( $data ["owner"] );
			$this->setPoints ( $data ["points"] );
			
			include_once ("./Services/RTE/classes/class.ilRTE.php");
			$this->setQuestion ( ilRTE::_replaceMediaObjectImageSrc ( $data ["question_text"], 1 ) );
			$this->setEstimatedWorkingTime ( substr ( $data ["working_time"], 0, 2 ), substr ( $data ["working_time"], 3, 2 ), substr ( $data ["working_time"], 6, 2 ) );
			
			// ProgQuestion spezifisch -> Musterloesung und Parameter
			// load additional data
			// Solution
			$result = $ilDB->queryF ( "SELECT * FROM " . $this->getAdditionalTableName () . " WHERE question_fi = %s", array (
					'integer' 
			), array (
					$question_id 
			) );
			if ($result->numRows () == 1) {
				$data = $ilDB->fetchAssoc ( $result );
				$this->setSolution ( $data ['solution'] );
				$this->setCheckIterative ( $data ['check_iterative'] );
				$this->setCheckRecursive ( $data ['check_recursive'] );
				$this->setForbidIterative ( $data ['forbid_iterative'] );
				$this->setForbidRecursive ( $data ['forbid_recursive'] );
			}
			// Params
			$result = $ilDB->queryF ( "SELECT * FROM " . $this->getAnswerTableName () . " WHERE question_fi = %s", array (
					'integer' 
			), array (
					$question_id 
			) );
			if ($result->numRows () > 0) {
				include_once "./Modules/TestQuestionPool/classes/class.assAnswerBinaryStateImage.php";
				while ( $data = $ilDB->fetchAssoc ( $result ) ) {
					// $data = $ilDB->fetchAssoc($result);
					$this->addTestParameterset ( $data ["params"], $data ["points"], $data ["aorder"] );
					// array_push($this->test_parameterset, new ASS_AnswerBinaryStateImage($data["answertext"], $data["points"], $data["aorder"], 1, $data["imagefile"]));
				}
			}
		}
		
		// loads additional stuff like suggested solutions
		parent::loadFromDb ( $question_id );
	}
	
	/**
	 * Dupliziert eine Frage.
	 * Wird beim Kopieren von Fragen aufgerufen.
	 *
	 * @access public
	 */
	function duplicate($for_test = true, $title = "", $author = "", $owner = "", $testObjId= NULL) {
		if ($this->id <= 0) {
			// The question has not been saved. It cannot be duplicated
			return;
		}
		// duplicate the question in database
		$this_id = $this->getId ();
		$clone = $this;
		include_once ("./Modules/TestQuestionPool/classes/class.assQuestion.php");
		$original_id = assQuestion::_getOriginalId ( $this->id );
		$clone->id = - 1;
		if ($title) {
			$clone->setTitle ( $title );
		}
		
		if ($author) {
			$clone->setAuthor ( $author );
		}
		if ($owner) {
			$clone->setOwner ( $owner );
		}
		
		if ($for_test) {
			$clone->saveToDb ( $original_id );
		} else {
			$clone->saveToDb ();
		}
		
		// copy question page content
		$clone->copyPageOfQuestion ( $this_id );
		
		// copy XHTML media objects
		$clone->copyXHTMLMediaObjectsOfQuestion ( $this_id );
		
		$clone->onDuplicate ( $thisObjId, $this_id, $clone->getObjId (), $clone->getId () );
		
		return $clone->id;
	}
	
	/**
	 * Gibt die maximale Punktzahl zurueck, die ein Student erreichen kann
	 *
	 * @access public
	 * @see $points
	 */
	function getMaximumPoints() {
		return $this->points;
	}
	
	/**
	 * Berechnet die erreichten Punkte des Studenten.
	 * Dazu wird die Loesung des Studenten an das Bewertungssystem geschickt,
	 * welches die Punkte berechnet und diese dann zurueckgibt.
	 *
	 * @param integer $user_id
	 *        	The database ID of the learner
	 * @param integer $test_id
	 *        	The database Id of the test containing the question
	 * @param boolean $returndetails
	 *        	(deprecated !!)
	 * @access public
	 */
	function calculateReachedPoints($active_id, $pass = NULL, $returndetails = FALSE) {
		global $ilDB;
		
		$reachedpoints = 0;
		// $found_values = array ();
		if (is_null ( $pass )) {
			$pass = $this->getSolutionMaxPass ( $active_id );
		}
		$result = $ilDB->queryF ( "SELECT * FROM tst_solutions WHERE active_fi = %s AND question_fi = %s AND pass = %s", array (
				'integer',
				'integer',
				'integer' 
		), array (
				$active_id,
				$this->getId (),
				$pass 
		) );
		while ( $data = $ilDB->fetchAssoc ( $result ) ) { // TODO was tun bei mehreren results??
		                                             // Programmcode finden - data1, data2 entspricht key, value
			if ($data ['value1'] == 'progquest_studentsolution') {
				$params = array ();
				$points = array ();
				$studentcode = $data ['value2'];
				foreach ( $this->getTestParameterset () as $paramObject ) {
					$params [] = $paramObject->getAnswertext ();
					$points [] = $paramObject->getPoints ();
				}
				
				$this->getPlugin ()->includeClass ( "class.ilAssProgQuestionRatingSystemConnector.php" );
				$connector = new ilAssProgQuestionRatingSystemConnector ( $this->getPlugin () );
				// sende Programmcode an das Bewertungssystem zum Kompilieren
				// TODO Inputvalidierung
				if ($this->getCheckRecursive () || $this->getCheckIterative () || $this->getForbidRecursive () || $this->getForbidIterative ())
					$checkStructure = true;
				else
					$checkStructure = false;
				$ratingsystemresult = $connector->compile ( $studentcode, $params, 'compare', $this->solution, $points, $checkStructure );
				if (isset ( $ratingsystemresult ['points'] )) {
					// Vergebe Punkte nicht, wenn Rekursion gefordert ist und keine gemacht wurde
					if ((! $this->getCheckRecursive () || $ratingsystemresult ['recursive']) && (! $this->getForbidRecursive () || ! $ratingsystemresult ['recursive']) && (! $this->getCheckIterative () || $ratingsystemresult ['iterative']) && (! $this->getForbidIterative () || ! $ratingsystemresult ['iterative'])) {
						$reachedpoints = $ratingsystemresult ['points'];
					} else
						$reachedpoints = 0;
				}
			}
		}
		return min ( $reachedpoints, $this->getMaximumPoints () );
	}
	
	/**
	 * Calculate the reached points for a submitted user input
	 *
	 * @param mixed user input (scalar, object or array)
	 * 
	 * @todo implement this new required function
	 * // This function is not pre-defined in assQuestion but must be present in a question type for "check" 
	 * // functionality on a question pools "preview" screen. It gets a posted user solution and calculates 
	 * // the reached points for it. The actual data type of solution is not defined but must match the type 
	 * // returned by getSolutionSubmit().
	 */
	public function calculateReachedPointsforSolution($solution) {
	}
	
	/**
	 * Speichert die Eingabe des Studenten in der Datenbank
	 *
	 * @param integer $test_id
	 *        	The database id of the test containing this question
	 * @return boolean Indicates the save status (true if saved successful, false otherwise)
	 * @access public
	 * @see $answers
	 */
	function saveWorkingData($active_id, $pass = NULL) {
		global $ilDB;
		global $ilUser;
		
		include_once "./Services/Utilities/classes/class.ilStr.php";
		if (is_null ( $pass )) {
			include_once "./Modules/Test/classes/class.ilObjTest.php";
			$pass = ilObjTest::_getPass ( $active_id );
		}
		
		// this is question type specific data
		$affectedRows = $ilDB->manipulateF ( "DELETE FROM tst_solutions WHERE active_fi = %s AND question_fi = %s AND pass = %s", array (
				'integer',
				'integer',
				'integer' 
		), array (
				$active_id,
				$this->getId (),
				$pass 
		) );
		$text = ilUtil::stripSlashes ( $_POST ["prog_area"], FALSE );
		
		$entered_values = 0;
		if (strlen ( $text )) {
			// Pruefe ob "kompilieren"-Button gedrueckt wurde
			// Dadurch dass ohne Knopfdrucken ein leerer Wert gespeichert wird,
			// ist rating_system_response trotz speichern in DB ein temporaerer Wert.
			if ($_POST ['cmd'] ['handleQuestionAction']) {
				// Hier muss das Bewertungssystem aufgerufen werden
				$this->getPlugin ()->includeClass ( "class.ilAssProgQuestionRatingSystemConnector.php" );
				$connector = new ilAssProgQuestionRatingSystemConnector ( $this->getPlugin () );
				// sende Programmcode an das Bewertungssystem zum Kompilieren
				// TODO Inputvalidierung
				$result = $connector->compile ( $text, array (
						$_POST ['prog_params'] 
				) );
				// TODO NIEMALS NIE Serialisieren, das kann sehr boese werden wenn daten manipuliert wurden!
				// $this->rating_system_response = serialize($result);
				$this->rating_system_response = $result;
			} else
				$this->rating_system_response = array ();
			
			$time = time ();
			
			$points = null;
			$this->saveWorkingDataValue ( $active_id, $pass, 'progquest_studentsolution', trim ( $text ), $points, $time );
			if (is_array ( $this->rating_system_response )) {
				foreach ( $this->rating_system_response as $key => $value ) {
					$this->saveWorkingDataValue ( $active_id, $pass, 'progquest_ratingsystemresponse_' . $key, $value, null, $time );
				}
			}
			// $next_id = $ilDB->nextId('tst_solutions');
			// $affectedRows = $ilDB->insert("tst_solutions", array(
			// "solution_id" => array("integer", $next_id),
			// "active_fi" => array("integer", $active_id),
			// "question_fi" => array("integer", $this->getId()),
			// "value1" => array("clob", trim($text)),
			// "value2" => array("clob", $this->rating_system_response),
			// "pass" => array("integer", $pass),
			// "tstamp" => array("integer", time())
			// ));
			$entered_values ++;
		}
		if ($entered_values) {
			include_once ("./Modules/Test/classes/class.ilObjAssessmentFolder.php");
			if (ilObjAssessmentFolder::_enabledAssessmentLogging ()) {
				$this->logAction ( $this->lng->txtlng ( "assessment", "log_user_entered_values", ilObjAssessmentFolder::_getLogLanguage () ), $active_id, $this->getId () );
			}
		} else {
			include_once ("./Modules/Test/classes/class.ilObjAssessmentFolder.php");
			if (ilObjAssessmentFolder::_enabledAssessmentLogging ()) {
				$this->logAction ( $this->lng->txtlng ( "assessment", "log_user_not_entered_values", ilObjAssessmentFolder::_getLogLanguage () ), $active_id, $this->getId () );
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
	 * @param string $key        	
	 * @param string $value        	
	 * @param float $points        	
	 * @param integer $time        	
	 */
	private function saveWorkingDataValue($active_id, $pass, $key, $value, $points, $time) {
		global $ilDB;
		
		// Hanging multiple request for a question from the same user may result in
		// unpredictable order of DELETE and INSERT and thus points written twice.
		// Using replace ethod instead of insert is not possible because value1 is clob.
		// Current workaround: allow values to be stored twice and detect them
		// in calculateReachedPoints.
		
		$query = "DELETE FROM tst_solutions" . " WHERE active_fi = " . $ilDB->quote ( $active_id, "integer" ) . " AND pass = " . $ilDB->quote ( $pass, "integer" ) . " AND question_fi = " . $ilDB->quote ( $this->getId (), "integer" ) . " AND value1 = " . $ilDB->quote ( $key, "text" );
		
		$ilDB->manipulate ( $query );
		
		$next_id = $ilDB->nextId ( 'tst_solutions' );
		$ilDB->insert ( "tst_solutions", array (
				"solution_id" => array (
						"integer",
						$next_id 
				),
				"active_fi" => array (
						"integer",
						$active_id 
				),
				"pass" => array (
						"integer",
						$pass 
				),
				"question_fi" => array (
						"integer",
						$this->getId () 
				),
				"points" => array (
						"float",
						$points 
				),
				"value1" => array (
						"clob",
						$key 
				),
				"value2" => array (
						"clob",
						$value 
				),
				"tstamp" => array (
						"integer",
						$time 
				) 
		) );
	}
	
	/**
	 * Bearbeitet die schon gespeichert Daten, wenn noetig.
	 *
	 * Diese Funktion wird benoetigt, bleibt aber normalerweise leer.
	 *
	 * @abstract
	 *
	 * @access protected
	 * @param integer $active_id        	
	 * @param integer $pass        	
	 * @param boolean $obligationsAnswered        	
	 */
	protected function reworkWorkingData($active_id, $pass, $obligationsAnswered) {
		// normally nothing need to be reworked
		// METHODE WIRD BENOETIGT! (abstrakte Methode in Oberklasse)
	}
	
	/**
	 * Gibt den Fragetypen der Frage zurueck
	 *
	 * @return integer The question type of the question
	 * @access public
	 */
	function getQuestionType() {
		return "assProgQuestion";
	}
	
	/**
	 * Gibt den Namen der Tabelle mit den zusaetzlichen Daten zurueck, die fuer die Programmierfrage benoetigt werden.
	 *
	 * @return mixed the name(s) of the additional tables (array or string)
	 * @access public
	 */
	function getAdditionalTableName() {
		return "il_qpl_qst_prog_quest";
	}
	
	/**
	 * Gibt den Namen der Tabelle mit den Antworten der Fragen zurueck
	 *
	 * @return string The answer table name
	 * @access public
	 */
	function getAnswerTableName() {
		return "il_qpl_qst_prog_params";
	}
	
	/**
	 * Erstellt eine Frage aus einer QTI Datei.
	 * Erhaelt Parameter von einem QTI Parser und erstellt ein ILIAS Fragenobjekt.
	 *
	 * @param object $item
	 *        	The QTI item object
	 * @param integer $questionpool_id
	 *        	The id of the parent questionpool
	 * @param integer $tst_id
	 *        	The id of the parent test if the question is part of a test
	 * @param object $tst_object
	 *        	A reference to the parent test object
	 * @param integer $question_counter
	 *        	A reference to a question counter to count the questions of an imported question pool
	 * @param array $import_mapping
	 *        	An array containing references to included ILIAS objects
	 * @access public
	 */
	function fromXML(&$item, &$questionpool_id, &$tst_id, &$tst_object, &$question_counter, &$import_mapping) {
		$this->getPlugin ()->includeClass ( "import/qti12/class.assProgQuestionImport.php" );
		$import = new assProgQuestionImport ( $this );
		$import->fromXML ( $item, $questionpool_id, $tst_id, $tst_object, $question_counter, $import_mapping );
	}
	
	/**
	 * Returns a QTI xml representation of the question and sets the internal
	 * domxml variable with the DOM XML representation of the QTI xml representation!
	 *
	 * @return string The QTI xml representation of the question
	 * @access public
	 */
	function toXML($a_include_header = true, $a_include_binary = true, $a_shuffle = false, $test_output = false, $force_image_references = false) {
		$this->getPlugin ()->includeClass ( "export/qti12/class.assProgQuestionExport.php" );
		$export = new assProgQuestionExport ( $this );
		return $export->toXML ( $a_include_header, $a_include_binary, $a_shuffle, $test_output, $force_image_references );
	}
	
	/**
	 * Setzt die Loesung
	 */
	public function setSolution($s) {
		$this->solution = $s;
	}
	
	/**
	 * Gibt die Loesung der Programmierfrage zurueck.
	 */
	public function getSolution() {
		return $this->solution;
	}
	
	/**
	 * Get the submitted user input as a serializable value 
     *
     * @return mixed user input (scalar, object or array)
     * 
	 * @todo implement this new function (do we need functionality?)
	 * // This function is not pre-defined in assQuestion but must be present in a question type for "check"
	 * // functionality on a question pools "preview" screen. It should provide the posted user input in a
     * // data structure that can be stored in the user session. The actual data type is not defined  but must be
     * // known by the corresponding function calculateReachedPointsforSolution().
	 */
	public function getSolutionSubmit() {
		
	}
	
	/**
	 * Setzt, ob auf Rekursion geprueft werden soll.
	 *
	 * @param boolean $r        	
	 */
	public function setCheckRecursive($r) {
		$this->check_recursive = ( boolean ) $r;
	}
	
	/**
	 * Setzt, ob auf Iteration geprueft werden soll.
	 *
	 * @param boolean $i        	
	 */
	public function setCheckIterative($i) {
		$this->check_iterative = ( boolean ) $i;
	}
	
	/**
	 * Gibt zurueck, ob der Code auf Rekursion geprueft wird.
	 *
	 * @return boolean
	 */
	public function getCheckRecursive() {
		return $this->check_recursive;
	}
	
	/**
	 * Gibt zurueck, ob der Code auf Iteration geprueft wird.
	 *
	 * @return boolean
	 */
	public function getCheckIterative() {
		return $this->check_iterative;
	}
	
	/**
	 * Setzt, ob Rekursion verboten werden soll.
	 *
	 * @param boolean $r        	
	 */
	public function setForbidRecursive($r) {
		$this->forbid_recursive = ( boolean ) $r;
	}
	
	/**
	 * Setzt, ob Iteration verboten werden soll.
	 *
	 * @param boolean $i        	
	 */
	public function setForbidIterative($i) {
		$this->forbid_iterative = ( boolean ) $i;
	}
	
	/**
	 * Gibt zurueck, ob Rekursion verboten wurde.
	 *
	 * @return boolean
	 */
	public function getForbidRecursive() {
		return $this->forbid_recursive;
	}
	
	/**
	 * Gibt zurueck, ob Iteration verboten wurde.
	 *
	 * @return boolean
	 */
	public function getForbidIterative() {
		return $this->forbid_iterative;
	}
	
	/**
	 * Fuegt ein neues Set von Testparametern hinzu
	 */
	function addTestParameterset($answertext = "", $points = 0.0, $order = 0, $answerimage = "") {
		include_once "./Modules/TestQuestionPool/classes/class.assAnswerBinaryStateImage.php";
		if (array_key_exists ( $order, $this->test_parameterset )) {
			// insert answer
			$answer = new ASS_AnswerBinaryStateImage ( $answertext, $points, $order, 1, $answerimage );
			$newchoices = array ();
			for($i = 0; $i < $order; $i ++) {
				array_push ( $newchoices, $this->test_parameterset [$i] );
			}
			array_push ( $newchoices, $answer );
			for($i = $order; $i < count ( $this->test_parameterset ); $i ++) {
				$changed = $this->test_parameterset [$i];
				$changed->setOrder ( $i + 1 );
				array_push ( $newchoices, $changed );
			}
			$this->test_parameterset = $newchoices;
		} else {
			// add answer
			$answer = new ASS_AnswerBinaryStateImage ( $answertext, $points, count ( $this->answers ), 1, $answerimage );
			array_push ( $this->test_parameterset, $answer );
		}
	}
	
	/**
	 * Die Anzahl der Testparametersets wird zurueck gegeben.
	 *
	 * @return Die Anzahl der Testparametersets als String.
	 */
	function getNumberOfTestParametersets() {
		$number = "";
		$number .= strval ( count ( $this->test_parameterset ) );
		
		return $number;
	}
	
	/**
	 * Die Testparameter werden fuer den Export in einen String gepackt.
	 *
	 * @param unkown $i        	
	 * @return Die Testparameter als String.
	 */
	function getTestParameterToXML($i) {
		$parameter;
		
		$parameter .= $this->test_parameterset [$i]->getAnswertext ();
		
		return $parameter;
	}
	
	/**
	 * Die Punkte fuer die Testparameter werden fuer den Export in einen String gepackt.
	 *
	 * @param unknown $i        	
	 * @return Die Punkte fuer die Testparameter als String.
	 */
	function getTestParameterPointsToXML($i) {
		$points;
		
		$points .= strval ( $this->test_parameterset [$i]->getPoints () );
		
		return $points;
	}
	
	/**
	 * Die Order der Testparameter wird fuer den Export in einen String gepackt.
	 *
	 * @param unknown $i        	
	 * @return Die Order der Testparameter als String.
	 */
	function getTestParameterOrderToXML($i) {
		$order;
		
		$order .= strval ( $this->test_parameterset [$i]->getOrder () );
		
		return $order;
	}
	
	/**
	 * Der Imagepfad der Testparameter wird fuer den Export in einen String gepackt.
	 *
	 * @param unknown $i        	
	 * @return Der Imagepfad der Testparameter als String.
	 */
	function getTestParameterImageToXML($i) {
		$image;
		
		$image .= $this->test_parameterset [$i]->getImage ();
		
		return image;
	}
	
	/**
	 * Gibt das Set von Testparametern zurueck.
	 */
	function &getTestParameterset() {
		return $this->test_parameterset;
	}
	
	/**
	 * Gibt die Antwort des Bewertungssystems zurueck
	 */
	function getRatingSystemResponse() {
		return $rating_system_response;
	}
	
	/**
	 * Erstellt eine Excel Datei mit den kumulierten Loesungen der Fragen.
	 *
	 * @access public
	 * @see assQuestion::setExportDetailsXLS()
	 */
	public function setExportDetailsXLS(&$worksheet, $startrow, $active_id, $pass, &$format_title, &$format_bold) {
		global $lng;
		
		include_once ("./Services/Excel/classes/class.ilExcelUtils.php");
		$solutions = $this->getSolutionValues ( $active_id, $pass );
		
		if (is_array ( $solutions )) {
			foreach ( $solutions as $solution ) {
				if ($solution ['value1'] == 'progquest_studentsolution') {
					// $value1 = isset ( $solution ["value1"] ) ? $solution ["value1"] : "";
					$value2 = isset ( $solution ["value2"] ) ? $solution ["value2"] : "";
					$points = isset ( $solution ["points"] ) ? $solution ["points"] : "0";
				}
			}
		}
		$points = $this->getReachedPoints ( $active_id, $pass ); // Da wir in DB die Points selber gar nicht haben, so abgreifen
		
		$worksheet->writeString ( $startrow, 0, ilExcelUtils::_convert_text ( $this->plugin->txt ( $this->getQuestionType () ) ), $format_title );
		$worksheet->writeString ( $startrow, 1, ilExcelUtils::_convert_text ( $this->getTitle () ), $format_title );
		$i = 1;
		
		// now provide a result string and write it to excel
		// it is also possible to write multiple rows
		// $worksheet->writeString($startrow + $i, 0, ilExcelUtils::_convert_text($this->plugin->txt("label_value1")), $format_bold);
		// $worksheet->write($startrow + $i, 1, ilExcelUtils::_convert_text($value1));
		// $i++;
		
		$worksheet->writeString ( $startrow + $i, 0, ilExcelUtils::_convert_text ( $this->plugin->txt ( "xls_label_solution" ) ), $format_bold );
		$worksheet->write ( $startrow + $i, 1, ilExcelUtils::_convert_text ( $value2 ) );
		$i ++;
		
		$worksheet->writeString ( $startrow + $i, 0, ilExcelUtils::_convert_text ( $this->plugin->txt ( "xls_label_points" ) ), $format_bold );
		$worksheet->write ( $startrow + $i, 1, ilExcelUtils::_convert_text ( $points ) );
		$i ++;
		
		return $startrow + $i + 1;
	}
}

?>
