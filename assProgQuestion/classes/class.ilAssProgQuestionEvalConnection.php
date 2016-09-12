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

/**
 * This class is the connection to the evaluation system.
 *
 * The workflow is to construct an instance of this class. Then evaluate is
 * called with the nescessary Parameters. The response is then the return
 * value of the function evaluateIt().
 */
class ilAssProgQuestionEvalConnection {
	private $plugin;
	private $question;
	private $action;
	private $student_code;
	private $student_parameters;
	private $student_code_id = 0;
	private $teacher_code_id = 0;
	private $check_method_type = false;
	private $xml;
	
	/**
	 * Compile the soltuion and the test scenario.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @return array()
	 */
	static function compileTestNG($question, $code = null) {
		$action = "compilestudenttestng";
		if ($code == null) {
			$code = $question->getSolution ();
			$action = "compiletestng";
		}
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, $action );
		$evaluator->createTestNGXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseTestNGResponse' 
		) );
	}
	
	/**
	 * Evaluate a solution against a test-case scenario.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param array() $params        	
	 * @return array()
	 */
	static function runTestNG($question, $code = null) {
		if ($code == null) {
			$code = $question->getSolution ();
		}
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, "runtestng", array () );
		$evaluator->createTestNGXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseTestNGResponse' 
		) );
	}
	
	/**
	 * Evaluate a solution against a test-case scenario.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param array() $params        	
	 * @return array()
	 */
	static function runStudentTestNG($question, $code, $params) {
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, "runstudenttestng", $params );
		$evaluator->createTestNGXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseTestNGResponse' 
		) );
	}
	
	/**
	 * Evaluate a solution against a test-case scenario.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param array() $params        	
	 * @return array()
	 */
	static function feedbackStudentTestNG($question, $code, $params) {
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, "feedbackstudenttestng", $params );
		$evaluator->createTestNGXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseTestNGResponse' 
		) );
	}
	
	/**
	 * Run a solution that should consists of one single function.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param string $owner        	
	 * @param array $params        	
	 * @return array()
	 */
	static function runCode($question, $code = null, $owner, $params = array()) {
		$action = 'run' . $owner;
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, $action, $params );
		$evaluator->createFunctionXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseFunctionResponse' 
		) );
	}
	
	/**
	 * Run a solution that should consists of one single function.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param string $owner        	
	 * @param array $params        	
	 * @return array()
	 */
	static function feedbackCode($question, $code = null, $owner, $params = array()) {
		$action = 'feedback' . $owner;
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, $action, $params );
		$evaluator->createFunctionXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseFunctionResponse' 
		) );
	}
	
	/**
	 * Compiles a function and returns the verdict of the compiler.
	 *
	 * @param unknown $question        	
	 * @param unknown $code        	
	 * @param unknown $owner        	
	 * @return array()
	 */
	static function compileCode($question, $code = null, $owner) {
		$action = 'compile' . $owner;
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, $action );
		$evaluator->createFunctionXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseFunctionResponse' 
		) );
	}
	
	/**
	 * Compare the code of a passed in function with the solution based on the parameters.
	 *
	 * @param unknown $question        	
	 * @param unknown $code        	
	 * @return array()
	 */
	static function compareCode($question, $code) {
		$action = 'compare';
		$evaluator = new ilAssProgQuestionEvalConnection ( $question, $code, $action );
		$evaluator->createFunctionXML ();
		return $evaluator->evaluateIt ( array (
				$evaluator,
				'parseFunctionResponse' 
		) );
	}
	
	/**
	 * Constructs an evaluator and sets some private variables that are used in many functions.
	 * These variables are set only for convenience.
	 *
	 * @param assProgQuestion $question        	
	 * @param string $code        	
	 * @param string $action
	 *        	@params array() $params
	 */
	function __construct($question, $code = null, $action = 'run', $params = array()) {
		$this->plugin = $question->getPlugin ();
		$this->question = $question;
		$this->action = $action;
		$this->student_code = $code;
		$this->student_parameters = $params;
		$this->check_method_type = $this->question->getCheckRecursive () || $this->question->getCheckIterative () || $this->question->getForbidRecursive () || $this->question->getForbidIterative ();
	}
	
	/**
	 * Evaluates the question and returns the result.
	 *
	 * To evaluate the question the question parameters and the answer are packed
	 * into an XML. The XML is then send to the evaluation server. The function
	 * then returns either the parsed answer or the corresponding failure message.
	 *
	 * @return array()
	 */
	function evaluateIt(callable $result_parser) {
		$this->plugin->includeClass ( "class.assProgQuestionConfig.php" );
		$config = assProgQuestionConfig::_getStoredSettings ();
		$address = $config ['ratingsystem_address'];
		
		$resultxml = $this->sendMessage ( $this->xml, $address, $errno, $errstr );
		
		if ($resultxml === FALSE) {
			$result ['type'] = 'failure';
			$result ['message'] = $this->plugin->txt ( 'connectionfail' ) . "\n\n $errstr ($errno)";
		} elseif (empty ( $resultxml )) {
			$result ['type'] = 'failure';
			$result ['message'] = $this->plugin->txt ( 'emptyresponse' );
		} else {
			$result = $result_parser ( $resultxml );
		}
		
		return $result;
	}
	
	/**
	 * Creates the XML request when the solution is about functions.
	 * This creates the XML request for function_original type question. The XML is sended later to
	 * the evaluation server.
	 *
	 * @return mixed
	 */
	private function createFunctionXML() {
		$this->student_code_id = 0;
		$this->teacher_code_id = 0;
		
		$xml = new SimpleXMLElement ( '<?xml version="1.0" encoding="utf-8" ?><progquestion/>' );
		
		$xml->addChild ( 'type', 'function_original' );
		
		$xml->addChild ( 'action', $this->action );
		$teacher = $xml->addChild ( 'teacher' );
		$this->addCode ( $teacher, 'method', $this->teacher_code_id ++, $this->question->getSolution (), $this->check_method_type );
		$this->addTestParameterGroups ( $teacher, $this->question->getTestParameterset () );
		
		$student = $xml->addChild ( 'student' );
		$this->addCode ( $student, 'method', $this->student_code_id ++, $this->student_code, $this->check_method_type );
		$this->addStudentParameterGroups ( $student, $this->student_parameters );
		
		$this->xml = $xml->asXML ();
	}
	
	/**
	 * Creates the XML request when code should be evaluated against a test.
	 *
	 * @return mixed
	 */
	private function createTestNGXML() {
		$this->student_code_id = 0;
		$this->teacher_code_id = 0;
		
		$xml = new SimpleXMLElement ( '<?xml version="1.0" encoding="utf-8" ?><progquestion/>' );
		
		$xml->addChild ( 'type', 'testng' );
		
		$xml->addChild ( 'action', $this->action );
		
		$solution = $xml->addChild ( 'solution' );
		$this->addCode ( $solution, 'class', $this->student_code_id ++, $this->student_code, false );
		if ($this->action == "runstudenttestng") {
			$this->addStudentParameterGroups ( $solution, $this->student_parameters );
		}
		
		$test = $xml->addChild ( 'testgroup' );
		$this->addCode ( $test, 'testcode', $this->teacher_code_id ++, $this->question->getTestCode (), false );
		$this->addTests ( $test, $this->question->getTestParameterset () );
		
		$this->xml = $xml->asXML ();
	}
	
	/**
	 * Adds the code element to the passed xml element.
	 *
	 * @param SimpleXMLElement $xml        	
	 * @param string $type        	
	 * @param int $id        	
	 * @param string $code        	
	 * @param bool $check_method_type        	
	 */
	private function addCode($xml, $type, $id, $code, $check_method_type) {
		$codeXML = $xml->addChild ( 'code' );
		$codeXML->addAttribute ( 'type', $type );
		$codeXML->addAttribute ( 'id', $id );
		$codeXML->addAttribute ( 'checkmethodtype', ($check_method_type ? "true" : "false") );
		$codeXML [0] = $code;
	}
	
	/**
	 *
	 * @param SimpleXMLElement $xml        	
	 * @param array[] $paramgroups        	
	 */
	private function addTests($xml, $paramgroups) {
		foreach ( $paramgroups as $pid => $paramObject ) {
			$points = $paramObject->getPoints ();
			$description = $paramObject->getName ();
			$testName = $paramObject->getParams ();
			
			$xmlparamgroup = $xml->addChild ( 'test' );
			$xmlparamgroup->addAttribute ( 'id', $pid );
			$xmlparamgroup->addAttribute ( 'name', $testName );
			$xmlparamgroup->addAttribute ( 'description', $description );
			if ($points != NULL)
				$xmlparamgroup->addAttribute ( 'points', $points );
		}
	}
	
	/**
	 *
	 * @param SimpleXMLElement $xml        	
	 * @param array[] $paramgroups        	
	 */
	private function addTestParameterGroups($xml, $paramgroups) {
		foreach ( $paramgroups as $pid => $paramObject ) {
			$points = $paramObject->getPoints ();
			$paramgroup = $paramObject->getParams ();
			$desc = $paramObject->getName ();
			
			$xmlparamgroup = $xml->addChild ( 'paramgroup' );
			$xmlparamgroup->addAttribute ( 'id', $pid );
			$xmlparamgroup->addAttribute ( 'description', $desc );
			
			$this->addTestParameters ( $xmlparamgroup, $paramgroup );
			
			if ($points != NULL)
				$xmlparamgroup->addAttribute ( 'points', $points );
		}
	}
	
	/**
	 *
	 * @param SimpleXMLElement $xml        	
	 * @param array[] $paramgroups        	
	 */
	private function addStudentParameterGroups($xml, $paramgroups) {
		foreach ( $paramgroups as $pid => $paramgroup ) {
			
			$xmlparamgroup = $xml->addChild ( 'paramgroup' );
			$xmlparamgroup->addAttribute ( 'id', $pid );
			
			$this->addTestParameters ( $xmlparamgroup, $paramgroup );
		}
	}
	
	/**
	 *
	 * @param SimpleXMLElement $xml        	
	 * @param array[] $params        	
	 */
	private function addTestParameters($xml, $params) {
		$paramsarray = explode ( ';', $params );
		foreach ( $paramsarray as $paramsid => $ps ) {
			$xmlparams = $xml->addChild ( 'params' );
			$xmlparams->addAttribute ( 'id', $paramsid ++ );
			$param = explode ( ',', $ps );
			foreach ( $param as $p ) {
				$xmlparams->addChild ( 'param', str_replace ( '&', '&amp;', $p ) );
			}
		}
	}
	
	/**
	 * Stellt die Verbindung zum Bewertungssystem her und sendet ein gegebenes XML-Dokument
	 *
	 * @param String $message
	 *        	Die Anforderung an das System (XML-Dokument)
	 * @param String $address
	 *        	Adresse des Systems
	 * @param
	 *        	int &$errno Bei Verbindungsfehler Fehlernummer
	 * @param
	 *        	String &$errstr Bei Verbindungsfehler Fehlermeldung
	 * @return boolean string bei Verbindungsfehler, String Rueckgabe des Bewertungssystems (XML-Dokument)
	 *        
	 * @author Matthias Lohmann
	 */
	private function sendMessage($message, $address, &$errno, &$errstr) {
		$connection = stream_socket_client ( $address, $errno, $errstr, 1 );
		
		if (! $connection) {
			return false;
		}
		
		fwrite ( $connection, $message );
		
		while ( ! feof ( $connection ) ) {
			$result .= fgets ( $connection, 1024 );
		}
		
		fclose ( $connection );
		
		return $result;
	}
	private function parseTestNGResponse($response) {
		$xml = new SimpleXMLElement ( $response );
		if ($xml->error) {
			$result ['type'] = 'failure';
			$result ['message'] = $this->plugin->txt ( 'errorresponse' ) . "\n" . $this->parseErrors ( $xml );
			return $result;
		}
		
		switch ($this->action) {
			case "compiletestng" :
				return $this->parseCompileTestNGResponse ( $xml );
				break;
			case "compilestudenttestng" :
				return $this->parseCompileTestNGResponse ( $xml );
				break;
			case "runtestng" :
				return $this->parseRunTestNGResponse ( $xml );
				break;
			case "runstudenttestng" :
				return $this->parseStudentTestNGResponse ( $xml );
				break;
			case "feedbackstudenttestng" :
				return $this->parseRunTestNGResponse ( $xml );
				break;
			default :
				$result ['type'] = 'failure';
				$result ['message'] = 'FAILURE: Could not parse response from evaluation server.';
		}
		
		return $result;
	}
	private function parseCompileTestNGResponse($xml) {
		$response = $xml->compiletestng;
		$diagnostics = $response->diagnostics;
		if ($diagnostics === null || count ( $diagnostics->children () ) == 0) {
			$result ['type'] = "success";
			$result ['message'] = $this->plugin->txt ( "compilesSuccessfully" );
		} else {
			$result ['type'] = "failure";
			$testDiag = $diagnostics->xpath ( 'diagnostic[@id=1]' ) [0];
			if ($testDiag->line) {
				$result ['message'] = $this->plugin->txt ( "compileErrorAtLine" ) . $testDiag->line . "\n\t" . $testDiag->message;
			} else {
				$result ['message'] = "\t" . $testDiag->message;
			}
		}
		return $result;
	}
	private function parseRunTestNGResponse($xml) {
		$result = $this->parseCompileTestNGResponse ( $xml );
		if ($result ['type'] == "failure")
			return $result;
		
		$response = $xml->runtestng;
		$tests = $response->tests;
		$reachedTotalPoints = true;
		foreach ( $tests->xpath ( "test" ) as $test ) {
			$att = $test->attributes ();
			if ($att ["passed"] == "true") {
				$result ['message'] .= "\n ".$this->plugin->txt("passedtest")." [" . $att ["description"] . "].";
				$result ['points'] += $att ["reachedPoints"];
			} elseif ($att ["passedPartially"] == "true") {
				$result ['message'] .= "\n".$this->plugin->txt("partialtest")." [" . $att ["description"] . "].";
				$reachedTotalPoints = false;
			} else {
				$result ['message'] .= "\n".$this->plugin->txt("failedtest")." [" . $att ["description"] . "] ";
				$reachedTotalPoints = false;
			}
		}
		if (! $reachedTotalPoints) {
			$result ['type'] = "warning";
		}
		return $result;
	}
	private function parseStudentTestNGResponse($xml) {
		$result = $this->parseCompileTestNGResponse ( $xml );
		if ($result ['type'] == "failure")
			return $result;
		
		$message = '';
		$runresults = $xml->runstudenttestng->runresults;
		if (! ($runresults === null) && (count ( $runresults->children () ) > 0)) {
			$groups = $runresults->xpath ( "paramgroup" );
			foreach ( $groups as $group ) {
				$params = $group->xpath ( "params" );
				foreach ( $params as $param ) {
					$att = $param->attributes ();
					$message .= "\nRUN WITH PARAMETERS" . $att ["params"] . ":\n" . $param;
				}
			}
		}
		$result ['message'] .= $message;
		return $result;
	}
	private function parseErrors($xml) {
		$message = '';
		$errors = $xml->error;
		foreach ( $errors->xpath ( "error" ) as $error ) {
			$message .= "=== [ERROR DURING EXECUTION] ===\n" . $error;
		}
		return $message;
	}
	
	/**
	 *
	 * @param unknown $response        	
	 * @param unknown $errno        	
	 * @param unknown $errstr        	
	 * @return string
	 */
	private function parseFunctionResponse($response) {
		$xml = new SimpleXMLElement ( $response );
		if ($xml->error) {
			$result ['type'] = 'failure';
			$result ['message'] = $this->plugin->txt ( 'errorresponse' ) . "\n" . $xml->error;
			return $result;
		}
		
		switch ($this->action) {
			case "compileteacher" :
				return $this->parseCompileResponse ( $xml );
				break;
			case "compilestudent" :
				return $this->parseCompileResponse ( $xml );
				break;
			case "runteacher" :
				return $this->parseRunResponse ( $xml );
				break;
			case "runstudent" :
				return $this->parseRunResponse ( $xml );
				break;
			case "feedbackstudent" :
				return $this->parseFeedbackResponse ( $xml );
				break;
			case "compare" :
				return $this->parseCompareResponse ( $xml );
				break;
			default :
				$result ['type'] = 'failure';
				$result ['message'] = "Could not parse response action: " . $this->action;
		}
		
		return $result;
	}
	
	/**
	 *
	 * @param unknown $xml        	
	 * @return string
	 */
	private function parseCompileResponse($xml) {
		$response = $xml->compilemethod;
		$diagnostics = $response->diagnostics;
		if ($diagnostics === null || count ( $diagnostics->children () ) == 0) {
			$result ['type'] = "success";
			$result ['message'] = $this->plugin->txt ( "compilesSuccessfully" );
		} else {
			$tdiagnose = $diagnostics->xpath ( 'diagnostic[@id=1]' ) [0];
			$result ['type'] = "failure";
			$result ['message'] = $this->plugin->txt ( "compileErrorAtLine" ) . $tdiagnose->line . "\n\t" . $tdiagnose->message;
		}
		return $result;
	}
	
	/**
	 *
	 * @param unknown $xml        	
	 * @return string
	 */
	private function parseRunResponse($xml) {
		$response = $xml->runmethod;
		$diagnostics = $response->diagnostics;
		if ($diagnostics === null || count ( $diagnostics->children () ) == 0) {
			$message = $this->plugin->txt ( "compilesSuccessfully" );
			$result = $this->parseMethodTypeDiagnostic ( $response );
			$feedback = $this->generateMethodTypeFeedback ( $result );
			if ($feedback == "") {
				$result ['type'] = "success";
			} else {
				$result ['type'] = "warning";
				$message .= $feedback;
			}
			
			$message .= "\n" . $this->plugin->txt ( "runningMethodWithParametersLeadTo" );
			$runresults = $response->runresults;
			if (! ($runresults === null) && (count ( $runresults->children () ) > 0)) {
				$groups = $runresults->xpath ( "paramgroup" );
				foreach ( $groups as $group ) {
					$params = $group->xpath ( "params" );
					foreach ( $params as $param ) {
						$att = $param->attributes ();
						if ($att["error"] ) {
							$message .= "\n" . $att ["params"] . " => " . $param;
							$result ['type'] = "warning";
						} else {
							$message .= "\n" . $att ["params"] . " => " . $att ["value"];
						}
					}
				}
			}
			
			$result ['message'] = $message;
		} else {
			$tdiagnose = $diagnostics->xpath ( 'diagnostic[@id=1]' ) [0];
			$result ['type'] = "failure";
			$result ['message'] = $this->plugin->txt ( "compileErrorAtLine" ) . $tdiagnose->line . "\n\t" . $tdiagnose->message;
		}
		return $result;
	}
	
	/**
	 *
	 * @param unknown $xml        	
	 * @return string
	 */
	private function parseFeedbackResponse($xml) {
		
		$compareresults = $xml->comparemethods->compareresults;

		$result ['type'] = "success";
		$result ['message'] = "";
		if (! ($compareresults === null) && (count ( $compareresults->children () ) > 0)) {
			$groups = $compareresults->xpath ( "paramgroup" );
			foreach ( $groups as $group ) {
				$att = $group->attributes ();
				$passed = $att ["equals"];
				if ($passed == "false") {
					$message .= "\n".$this->plugin->txt("failedtest")." [" . $att ["name"] . "].";
					if ($result ['type'] == "success" || $result['type'] == null) {
						$result ['type'] = "warning";
					}
				} else {
					$message .= "\n".$this->plugin->txt("passedtest")." [" . $att ["name"] . "].";
					if ( $result['type'] == null) {
						$result ['type'] = "success";
					}
				}
			}
			$result ['message' ] .= $message;
		} else {
			$tdiagnose = $diagnostics->xpath ( 'diagnostic[@id=1]' ) [0];
			$result ['type'] = "failure";
			$result ['message'] = $this->plugin->txt ( "compileErrorAtLine" ) . $tdiagnose->line . "\n\t" . $tdiagnose->message;
		}
		return $result;
	}
	
	/**
	 *
	 * @param unknown $xml        	
	 * @return number|string
	 */
	private function parseCompareResponse($xml) {
		$response = $xml->comparemethods;
		$diagnostics = $response->diagnostics;
		if ($diagnostics === null || count ( $diagnostics->children () ) == 0) { // we rely only on children && count($diagnostics->attributes())==0 ) {
			$result ['type'] = "success";
			$result ['message'] = "";
			$result ['iterative'] = false;
			$result ['recursive'] = false;
			$result = array_merge ( $result, $this->parseMethodTypeDiagnostic ( $response ) );
			$feedback = $this->generateMethodTypeFeedback ( $result );
			// TODO: add feedback to message displayed to user
			
			$message = $this->plugin->txt ( "compilesSuccessfully" );
			
			$message .= "\n" . $this->plugin->txt ( "runningMethodWithParametersLeadTo" );
			
			$runresults = $response->compareresults;
			$result ['points'] = 0;
			
			if (! ($runresults === null) && (count ( $runresults->children () ) > 0)) {
				$groups = $runresults->xpath ( "paramgroup" );
				foreach ( $groups as $group ) {
					$points = $group->attributes () ["reachedPoints"];
					$result ['points'] += $points;
				}
			}
			$result ['message'] .= $message;
		} else {
			$tdiagnose = $diagnostics->xpath ( 'diagnostic[@id=0]' ) [0];
			$result ['type'] = "failure";
			$result ['message'] = $this->plugin->txt ( "compileErrorAtLine" ) . $tdiagnose->line . "\n\t" . $tdiagnose->message;
			$result ['points'] = 0;
		}
		return $result;
	}
	
	/**
	 *
	 * @param array() $result        	
	 */
	private function generateMethodTypeFeedback($result) {
		$feedback = "";
		$loop = $result ['iterative'];
		$recursive = $result ['recursive'];
		if ($this->question->getCheckIterative ()) {
			if (! is_null ( $loop ) && ! $loop) {
				$feedback .= "\n" . $this->plugin->txt ( "iterationReuiredNotFound" );
			}
		}
		if ($this->question->getForbidIterative ()) {
			if (! is_null ( $loop ) && $loop) {
				$feedback .= "\n" . $this->plugin->txt ( "iterationFoundNotAllowed" );
			}
		}
		if ($this->question->getCheckRecursive ()) {
			if (! is_null ( $recursive ) && ! $recursive) {
				$feedback .= "\n" . $this->plugin->txt ( "recursionReuiredNotFound" );
			}
		}
		if ($this->question->getForbidRecursive ()) {
			if (! is_null ( $recursive ) && $recursive) {
				$feedback .= "\n" . $this->plugin->txt ( "recursionFoundNotAllowed" );
			}
		}
		return $feedback;
	}
	
	/**
	 *
	 * @todo use language pack
	 * @param unknown $element        	
	 * @return string
	 */
	private function parseMethodTypeDiagnostic($element) {
		$result = array ();
		$diagnose = $element->methodtypediagnostics;
		if (! ($diagnose === null) && (count ( $diagnose->children () ) > 0)) {
			$result ['iterative'] = $diagnose->loop == "true";
			$result ['recursive'] = $diagnose->recursive == "true";
		}
		return $result;
	}
	
	/**
	 *
	 * @param unknown $xml        	
	 * @return number|boolean
	 */
	private function parseOldResponse($xml) {
		
		// TODO mehrere Codes moeglich!! Idee: Immer fuer Code 1 pruefen -> bei Erstellung auf Code 1 achten
		$compilable = ( string ) $xml->code->compileable;
		if ($compilable == 'false') {
			$result ['type'] = 'failure';
			$result ['message'] = $this->plugin->txt ( 'studcodedoesnotcompile' );
		} elseif ($compilable == 'true') {
			$result ['type'] = 'success';
			$result ['message'] = $this->plugin->txt ( 'studcodecompiles' );
		}
		
		// TODO wir koennen mehrere Codes haben! Richtigen finden / Iterieren!!!
		if ($xml->code->diagnostics->diagnostic) {
			foreach ( $xml->code->diagnostics->diagnostic as $diagnostic ) {
				$result ['diagnostics'] .= $diagnostic->message . "\n";
			}
		}
		
		if ($xml->code->paramgroup) {
			foreach ( $xml->code->paramgroup as $paramgroup ) {
				$pgid = ( string ) $paramgroup ['paramgroupid'];
				foreach ( $paramgroup->paramsreturn as $paramsreturn ) {
					$pid = $paramsreturn ['paramsid'];
					$param_val = $xml->paramgroup [( int ) $pgid]->params [( int ) $pid]->param;
					$ret_val = $paramsreturn->return;
					$result ['paramsreturn'] .= $param_val . ' => ' . $ret_val . "\n";
				}
				$result ['paramsreturn'] .= "\n";
			}
		}
		
		// Punkte bei Vgl
		
		$result ['points'] = 0;
		foreach ( $xml->code as $xmlcode ) {
			if ($xmlcode ['codeid'] == '1') {
				
				// Ist Code rekursiv?
				$recursive = ( string ) $xml->code->methodtype->recursive;
				$result ['recursive'] = ($recursive == 'true' ? true : false);
				$iterative = ( string ) $xml->code->methodtype->iterative; // Fehlt noch im BWS
				$result ['iterative'] = ($iterative == 'true' ? true : false);
				
				foreach ( $xmlcode->paramgroup as $xmlparamgroup ) {
					$result ['points'] += $xmlparamgroup->reachedpoints;
				}
			}
		}
		
		// $result['message'] .= $xml->asXML();
		// get completed xml document
		// $dom = new DOMDocument ( "1.0" );
		// $dom->preserveWhiteSpace = false;
		// $dom->formatOutput = true;
		// $dom->loadXML ( $xml->asXML () );
		// $result ['message'] .= "\n" . $dom->saveXML ();
		
		return $result;
	}
}
?>