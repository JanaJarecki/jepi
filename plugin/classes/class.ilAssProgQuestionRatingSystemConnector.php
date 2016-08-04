<?php
/*
 * +----------------------------------------------------------------------------+ | ILIAS open source | +----------------------------------------------------------------------------+ | Copyright (c) 1998-2001 ILIAS open source, University of Cologne | | | | This program is free software; you can redistribute it and/or | | modify it under the terms of the GNU General Public License | | as published by the Free Software Foundation; either version 2 | | of the License, or (at your option) any later version. | | | | This program is distributed in the hope that it will be useful, | | but WITHOUT ANY WARRANTY; without even the implied warranty of | | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the | | GNU General Public License for more details. | | | | You should have received a copy of the GNU General Public License | | along with this program; if not, write to the Free Software | | Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. | +----------------------------------------------------------------------------+
 */

/**
 * This class connects to the JAVA rating system.
 *
 * @author Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 *        
 */
class ilAssProgQuestionRatingSystemConnector {
	private $plugin_object;
	
	function __construct($plugin) {
		$this->plugin_object = $plugin;
	}
	
	/**
	 * Sendet Code zur Kompilierung und Ausfuehrung ans Bewertungssystem
	 *
	 * @param string $code        	
	 * @param string $params        	
	 * @param string $address        	
	 * @return array
	 */
	function compile($code, $params = NULL, $action = 'run', $solutioncode = NULL, $points = NULL, $checkStructure=false) {

		$this->plugin_object->includeClass ( "class.assProgQuestionConfig.php" );
		$config = assProgQuestionConfig::_getStoredSettings ();
		$address = $config['ratingsystem_address'];
		//echo $address; flush();
		
		// Pluginobjekt wird benoetigt um auf die Sprachdateien zuzugreifen
		$pl = ilPlugin::getPluginObject ( IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion" );
		
		// Bereite das zu sendende XML-Dokument vor
		$paramsbyid = array();
		$xml = $this->createXML ( $code, $params, $action, $solutioncode, $points, $checkStructure, $paramsbyid );
		
		// Sende an Bewertungssystem
		$resultxml = $this->sendMessage ( $xml, $address, $errno, $errstr );
		if ($resultxml === FALSE) {
			$result ['type'] = 'failure';
			$result ['message'] = $pl->txt ( 'connectionfail' ) . "\n\n $errstr ($errno)";
		} else {
			$result = $this->parseResponse ( $resultxml , $paramsbyid);
		}
		
		// if ($action == 'compare') {
// 		echo $xml.$resultxml . $result ['message'];
// 		flush ();
		// }
		return $result;
	}
	
	/**
	 * Erstellt eine XML-Anforderung fuer das Bewertungssystem
	 *
	 * @param String $code        	
	 * @param String[] $params        	
	 * @param string $action        	
	 */
	private function createXML($code, $params, $action = 'run', $solutioncode = '', $points = NULL, $checkStructure=false, &$paramsbyid=array()) {
		$xml = new SimpleXMLElement ( '<?xml version="1.0" encoding="utf-8" ?><progquestion/>' );
		$xml->addChild ( 'action', $action );
		if ($checkStructure) $xml->addChild ('methodtype', 'true');
		else $xml->addChild ('methodtype', 'false');
		$xml->addChild ( 'element' ); // 'element' separiert verschiedene codes von lerner und lehrer
		$xml->element->addChild ( 'code' );
		$xml->element->code = $code; // text nicht bei addChild hinzufuegen, weil das & nicht escaped!
		$xml->element->addAttribute ( 'type', 'method' );
		$xml->element->addAttribute ( 'id', '1' );
		$xml->element->addAttribute ( 'origin', 'student' );
		
		if (! empty ( $solutioncode )) {
			$solxml = $xml->addChild ( 'element' );
			$solxml->addChild ( 'code' );
			$solxml->code = $solutioncode; // text nicht bei addChild hinzufuegen, weil das & nicht escaped!
			$solxml->addAttribute ( 'type', 'method' );
			$solxml->addAttribute ( 'id', '2' );
			$solxml->addAttribute ( 'origin', 'teacher' );
		}
		
		if (is_array ( $params )) { // Haben wir keine Parameter uebergeben, dann auch keine paramgroups anlegen
			foreach ( $params as $pid => $paramgroup ) {
				$xmlparamgroup = $xml->addChild ( 'paramgroup' );
				$xmlparamgroup->addAttribute ( 'id', $pid );
				if ($points != NULL)
					$xmlparamgroup->addAttribute ( 'points', $points [$pid] );
					// Parameter aufteilen. TODO: das sollte in Zukunft direkt anders gespeichert werden
				$paramsarray = explode ( ';', $paramgroup );
				$paramsid = 1;
				foreach ( $paramsarray as $ps ) {
					if (! empty ( $ps )) { // TODO was ist mit parameterlosen methoden?
						$xmlparams = $xmlparamgroup->addChild ( 'params' );
						
						$paramsbyid[$pid][$paramsid] = $ps;
						
						$xmlparams->addAttribute ( 'id', $paramsid ++ );
						$param = explode ( ',', $ps );
						foreach ( $param as $p ) {
							$xmlparam = $xmlparams->addChild ( 'param', str_replace ( '&', '&amp;', $p ) );
						}
					}
				}
			}
		}
		return $xml->asXML ();
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
		// Pluginobjekt wird benoetigt um auf die Sprachdateien zuzugreifen
		// $pl = ilPlugin::getPluginObject ( IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion" );
		$result = '';
		
		// Verbindung herstellen
		$fp = stream_socket_client ( $address, $errno, $errstr, 1 );
		// stream_set_timeout ( $fp, 1 ); // TODO pruefe ob vernuenftig. derzeit nur da um bei bewertungssystemproblemen irgendwann auch weiterzumachen.
		if (! $fp) { // Verbindungsfehler
			return false;
		} else { // Verbindung erfolgreich
		         // Daten senden
			fwrite ( $fp, $message );
			// Antwort lesen
			while ( ! feof ( $fp ) ) {
				$result .= fgets ( $fp, 1024 );
			}
			fclose ( $fp );
		}
		
		// echo "Sende an $address: <pre>" . htmlspecialchars ( $message ) . "</pre>";
		// echo "Antwort: <pre>" . htmlspecialchars ( $result ) . "</pre>";
		// flush ();
		
		return $result;
	}
	/**
	 *
	 * @param unknown $response        	
	 * @param unknown $errno        	
	 * @param unknown $errstr        	
	 * @return string
	 */
	private function parseResponse($response, $paramsbyid=array()) {
		// Pluginobjekt wird benoetigt um auf die Sprachdateien zuzugreifen
		$pl = ilPlugin::getPluginObject ( IL_COMP_MODULE, "TestQuestionPool", "qst", "assProgQuestion" );
		
		if (empty ( $response )) {
			$result ['type'] = 'failure';
			$result ['message'] = $pl->txt ( 'emptyresponse' );
			return $result;
		}
		
		$xml = new SimpleXMLElement ( $response );
		if ($xml->error) {
			$result ['type'] = 'failure';
			$result ['message'] = $pl->txt ( 'errorresponse' ) . "\n" . $xml->error;
			return $result;		}
		// TODO mehrere Codes moeglich!! Idee: Immer fuer Code 1 pruefen -> bei Erstellung auf Code 1 achten
		$compilable = ( string ) $xml->code->compileable;
		if ($compilable == 'false') {
			$result ['type'] = 'failure';
			$result ['message'] = $pl->txt ( 'studcodedoesnotcompile' );
		} elseif ($compilable == 'true') {
			$result ['type'] = 'success';
			$result ['message'] = $pl->txt ( 'studcodecompiles' );
		}
		
		// TODO wir koennen mehrere Codes haben! Richtigen finden / Iterieren!!!
		if ($xml->code->diagnostics->diagnostic) {
			foreach ( $xml->code->diagnostics->diagnostic as $diagnostic ) {
				$result ['diagnostics'] .= $diagnostic->message . "\n";
			}
		}
		
		if ($xml->code->paramgroup) {
			foreach ( $xml->code->paramgroup as $paramgroup ) {
				$pgid = (string)$paramgroup['paramgroupid'];
				foreach ( $paramgroup->paramsreturn as $paramsreturn ) {
					$pid = (string)$paramsreturn['paramsid'];
					$result ['paramsreturn'] .= $paramsbyid[$pgid][$pid] . ' => ' . $paramsreturn->return . "\n";
				}
				$result['paramsreturn'] .= "\n";
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
// 		$dom = new DOMDocument ( "1.0" );
// 		$dom->preserveWhiteSpace = false;
// 		$dom->formatOutput = true;
// 		$dom->loadXML ( $xml->asXML () );
// 		$result ['message'] .= "\n" . $dom->saveXML ();
		
		return $result;
	}
}
?>