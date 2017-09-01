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
class assProgQuestionDBConnection {
	
	/**
	 * Gibt den Namen der Tabelle mit den zusaetzlichen Daten zurueck, die fuer die Programmierfrage benoetigt werden.
	 *
	 * @return mixed the name(s) of the additional tables (array or string)
	 * @access private
	 */
	static function getFunctionQuestionTableName() {
		return "il_qpl_qst_prog_quest";
	}
	
	/**
	 * Gibt den Namen der Tabelle mit den Antworten der Fragen zurueck
	 *
	 * @return string The answer table name
	 * @access private
	 */
	static function getParameterTableName() {
		return "il_qpl_qst_prog_params";
	}
	
	/**
	 *
	 * @param unknown $prog_question        	
	 * @param unknown $active_id        	
	 * @param unknown $pass        	
	 */
	public static function loadStudentSolution($prog_question, $active_id, $pass) {
		$result = $ilDB->queryF ( "SELECT * FROM tst_solutions WHERE active_fi = %s AND question_fi = %s AND pass = %s", array (
				'integer',
				'integer',
				'integer' 
		), array (
				$active_id,
				$prog_question->getId (),
				$pass 
		) );
		$params = array ();
		$points = array ();
		$studentcode = "";
		while ( $data = $ilDB->fetchAssoc ( $result ) ) {
			if ($data ['value1'] == 'progquest_studentsolution') {
				$studentcode = $data ['value2'];
			}
			if ($data ['value1'] == 'progquest_studentparams') {
				$params = $data ['value2'];
				
			}
		}
	}
	
	/**
	 * Save data from the assProgQuestion plugin in the question table.
	 *
	 * @param unknown $prog_question        	
	 */
	public static function saveAssProgQuestion($prog_question) {
		global $ilDB;
		// save additional data
		// Musterloesung
		$affectedRows = $ilDB->manipulateF ( "DELETE FROM " . self::getFunctionQuestionTableName () . " WHERE question_fi = %s", array (
				"integer" 
		), array (
				$prog_question->getId () 
		) );
		
		// $affectedRows = $ilDB->manipulateF ( "INSERT INTO " . self::getFunctionQuestionTableName () . " (question_fi, solution, check_recursive, check_iterative, forbid_recursive, forbid_iterative, quest_type, test_code)" . " VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", array (
		// 'integer',
		// 'text',
		// 'integer',
		// 'integer',
		// 'integer',
		// 'integer',
		// 'text',
		// 'clob'
		// ), array (
		// $prog_question->getId (),
		// $prog_question->getSolution (),
		// ( int ) $prog_question->getCheckRecursive (),
		// ( int ) $prog_question->getCheckIterative (),
		// ( int ) $prog_question->getForbidRecursive (),
		// ( int ) $prog_question->getForbidIterative (),
		// ( string ) $prog_question->getProgQuestionType (),
		// ( string ) $prog_question->getTestCode ()
		// ) );
		$affectedRows = $ilDB->insert ( self::getFunctionQuestionTableName (), array (
				"question_fi" => array (
						'integer',
						$prog_question->getId () 
				),
				"solution" => array (
						'text',
						$prog_question->getSolution () 
				),
				"check_recursive" => array (
						'integer',
						( int ) $prog_question->getCheckRecursive () 
				),
				"check_iterative" => array (
						'integer',
						( int ) $prog_question->getCheckIterative () 
				),
				"forbid_recursive" => array (
						'integer',
						( int ) $prog_question->getForbidRecursive () 
				),
				"forbid_iterative" => array (
						'integer',
						( int ) $prog_question->getForbidIterative () 
				),
				"quest_type" => array (
						'text',
						( string ) $prog_question->getProgQuestionType () 
				),
				"test_code" => array (
						'clob',
						( string ) $prog_question->getTestCode () 
				) 
		) );
	}
	
	/**
	 * Save parameters to the database
	 *
	 * @param assProgQuestion $prog_questions        	
	 */
	public static function saveParamsToDb($prog_question) {
		global $ilDB;
		
		$ilDB->manipulateF ( "DELETE FROM " . self::getParameterTableName () . " WHERE question_fi = %s", array (
				'integer' 
		), array (
				$prog_question->getId () 
		) );
		
		foreach ( $prog_question->getTestParameterSet () as $key => $answer_obj ) {
			/**
			 *
			 * @var ASS_AnswerMultipleResponseImage $answer_obj
			 */
			// $answer_obj = $prog_question->getTestParameterSet()[$key]; // TODO bringt das was? sieht redundant aus.
			if ($answer_obj->getParams () != NULL) {
				$next_id = $ilDB->nextId ( self::getParameterTableName () );
				// answer_id,question_fi,params,points,aorder
				$ilDB->manipulateF ( "INSERT INTO " . self::getParameterTableName () . " (answer_id, question_fi, paramset_name, params, points, aorder) VALUES (%s, %s, %s, %s, %s, %s)", array (
						'integer',
						'integer',
						'text',
						'text',
						'float',
						'integer' 
				), array (
						$next_id,
						$prog_question->getId (),
						$answer_obj->getName (),
						$answer_obj->getParams (),
						$answer_obj->getPoints (),
						$answer_obj->getOrder () 
				) );
			}
		}
	}
	
	/**
	 * Load the basic data from the question.
	 */
	public static function loadAssQuestion($prog_question, $question_id) {
		global $ilDB;
		
		$result = $ilDB->queryF ( "SELECT qpl_questions.* FROM qpl_questions WHERE question_id = %s", array (
				'integer' 
		), array (
				$question_id 
		) );
		if ($result->numRows () == 1) {
			$data = $ilDB->fetchAssoc ( $result );
			$prog_question->setId ( $question_id );
			$prog_question->setTitle ( $data ["title"] );
			$prog_question->setComment ( $data ["description"] );
			$prog_question->setSuggestedSolution ( $data ["solution_hint"] );
			$prog_question->setOriginalId ( $data ["original_id"] );
			$prog_question->setObjId ( $data ["obj_fi"] );
			$prog_question->setAuthor ( $data ["author"] );
			$prog_question->setOwner ( $data ["owner"] );
			$prog_question->setPoints ( $data ["points"] );
			
			include_once ("./Services/RTE/classes/class.ilRTE.php");
			$prog_question->setQuestion ( ilRTE::_replaceMediaObjectImageSrc ( $data ["question_text"], 1 ) );
			$prog_question->setEstimatedWorkingTime ( substr ( $data ["working_time"], 0, 2 ), substr ( $data ["working_time"], 3, 2 ), substr ( $data ["working_time"], 6, 2 ) );
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Load question specific data.
	 */
	public static function loadAssProgQuestion($prog_question, $question_id) {
		global $ilDB;
		// ProgQuestion spezifisch -> Musterloesung und Parameter
		// load additional data
		// Solution
		$result = $ilDB->queryF ( "SELECT * FROM " . self::getFunctionQuestionTableName () . " WHERE question_fi = %s", array (
				'integer' 
		), array (
				$question_id 
		) );
		if ($result->numRows () == 1) {
			$data = $ilDB->fetchAssoc ( $result );
			$prog_question->setSolution ( $data ['solution'] );
			$prog_question->setCheckIterative ( $data ['check_iterative'] );
			$prog_question->setCheckRecursive ( $data ['check_recursive'] );
			$prog_question->setForbidIterative ( $data ['forbid_iterative'] );
			$prog_question->setForbidRecursive ( $data ['forbid_recursive'] );
			$prog_question->setProgQuestionType ( $data ['quest_type'] );
			$prog_question->setTestCode ( $data ['test_code'] );
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Load parameters sets for question.
	 */
	public static function loadParams($prog_question, $question_id) {
		global $ilDB;
		
		$result = $ilDB->queryF ( "SELECT * FROM " . self::getParameterTableName () . " WHERE question_fi = %s", array (
				'integer' 
		), array (
				$question_id 
		) );
		if ($result->numRows () > 0) {
			include_once "./Modules/TestQuestionPool/classes/class.assAnswerBinaryStateImage.php";
			while ( $data = $ilDB->fetchAssoc ( $result ) ) {
				// $data = $ilDB->fetchAssoc($result);
				$prog_question->addTestParameterset ( $data["paramset_name"], $data ["params"], $data ["points"], $data ["aorder"] );
				// array_push($this->test_parameterset, new ASS_AnswerBinaryStateImage($data["answertext"], $data["points"], $data["aorder"], 1, $data["imagefile"]));
			}
			return true;
		} else {
			return false;
		}
	}
}