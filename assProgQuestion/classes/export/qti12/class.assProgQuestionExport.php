<?php

include_once "./Modules/TestQuestionPool/classes/export/qti12/class.assQuestionExport.php";

/**
* Example question export
*
* @author	Fred Neumann <fred.neumann@fau.de>
* @version	$Id:  $
* @ingroup ModulesTestQuestionPool
*/
class assProgQuestionExport extends assQuestionExport
{
	/**
	* Returns a QTI xml representation of the question
	*
	* @return string The QTI xml representation of the question
	* @access public
	*/
	function toXML($a_include_header = true, $a_include_binary = true, $a_shuffle = false, $test_output = false, $force_image_references = false)
	{
		global $ilias;
		
		include_once("./Services/Xml/classes/class.ilXmlWriter.php");
		$a_xml_writer = new ilXmlWriter;
		// set xml header
		$a_xml_writer->xmlHeader();
		$a_xml_writer->xmlStartTag("questestinterop");
		$attrs = array(
			"ident" => "il_".IL_INST_ID."_qst_".$this->object->getId(),
			"title" => $this->object->getTitle(),
			"maxattempts" => $this->object->getNrOfTries()
		);
		$a_xml_writer->xmlStartTag("item", $attrs);
		// add question description
		$a_xml_writer->xmlElement("qticomment", NULL, $this->object->getComment());
		// add estimated working time
		$workingtime = $this->object->getEstimatedWorkingTime();
		$duration = sprintf("P0Y0M0DT%dH%dM%dS", $workingtime["h"], $workingtime["m"], $workingtime["s"]);
		$a_xml_writer->xmlElement("duration", NULL, $duration);
		// add ILIAS specific metadata
		$a_xml_writer->xmlStartTag("itemmetadata");
		$a_xml_writer->xmlStartTag("qtimetadata");
		
		//Die Ilias-Version wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "ILIAS_VERSION");
		$a_xml_writer->xmlElement("fieldentry", NULL, $ilias->getSetting("ilias_version"));
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		//Der Fragetyp wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "QUESTIONTYPE");
		$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getQuestionType());
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		//Der Autor der Frage wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "AUTHOR");
		$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getAuthor());
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		//Die Maximalpunktzahl wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "POINTS");
		$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getPoints());
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		//NEW - Die Musterloesung wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "SOLUTION");
		$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getSolution());
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		//NEW - Die Anzahl der Testparametersets wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "NUMTESTPARAMSETS");
		$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getNumberOfTestParametersets());
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		
		for($i = 0; $i < intval($this->object->getNumberOfTestparametersets()); $i++){
			//NEW - Die Testparameter werden exportiert
			$a_xml_writer->xmlStartTag("qtimetadatafield");
			
			$label_name_params = "TESTPARAMETER_";
			$label_name_params .= strval($i);
			
			$a_xml_writer->xmlElement("fieldlabel", NULL, $label_name_params);
			$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getTestParameterToXML($i));
			$a_xml_writer->xmlEndTag("qtimetadatafield");
			//NEW - Die Punkte fuer die Testparameter werden exportiert
			$a_xml_writer->xmlStartTag("qtimetadatafield");
			
			$label_name_points = "TESTPARAMETERPOINTS_";
			$label_name_points .= strval($i);
			
			$a_xml_writer->xmlElement("fieldlabel", NULL, $label_name_points);
			$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getTestParameterPointsToXML($i));
			$a_xml_writer->xmlEndTag("qtimetadatafield");
			//NEW - Die Order fuer die Testparameter wird exportiert
			$a_xml_writer->xmlStartTag("qtimetadatafield");
				
			$label_name_order = "TESTPARAMETERORDER_";
			$label_name_order .= strval($i);
				
			$a_xml_writer->xmlElement("fieldlabel", NULL, $label_name_order);
			$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getTestParameterOrderToXML($i));
			$a_xml_writer->xmlEndTag("qtimetadatafield");
			//NEW - Der Imagepfad fuer die Testparameter wird exportiert
			$a_xml_writer->xmlStartTag("qtimetadatafield");
			
			$label_name_image = "TESTPARAMETERIMAGE_";
			$label_name_image .= strval($i);
			
			$a_xml_writer->xmlElement("fieldlabel", NULL, $label_name_image);
			$a_xml_writer->xmlElement("fieldentry", NULL, $this->object->getTestParameterImageToXML($i));
			$a_xml_writer->xmlEndTag("qtimetadatafield");
		}
		
		//NEW - checkIterative wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "CHECKITERATIVE");
		
		if($this->object->getCheckIterative()){
			$checkIterative = "1";
		}
		else{
			$checkIterative = "0";
		}
		
		$a_xml_writer->xmlElement("fieldentry", NULL, $checkIterative);
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		
		//NEW - checkRecursive wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "CHECKRECURSIVE");
		
		if($this->object->getCheckRecursive()){
			$checkRecursive = "1";
		}
		else{
			$checkRecursive = "0";
		}
		
		$a_xml_writer->xmlElement("fieldentry", NULL, $checkRecursive);
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		
		//NEW - forbidIterative wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "FORBIDITERATIVE");
		
		if($this->object->getForbidIterative()){
			$forbidIterative = "1";
		}
		else{
			$forbidIterative = "0";
		}
		
		$a_xml_writer->xmlElement("fieldentry", NULL, $forbidIterative);
		$a_xml_writer->xmlEndTag("qtimetadatafield");
		
		//NEW - forbidRecursive wird exportiert
		$a_xml_writer->xmlStartTag("qtimetadatafield");
		$a_xml_writer->xmlElement("fieldlabel", NULL, "FORBIDRECURSIVE");
		
		if($this->object->getForbidRecursive()){
			$forbidRecursive = "1";
		}
		else{
			$forbidRecursive = "0";
		}
		
		$a_xml_writer->xmlElement("fieldentry", NULL, $forbidRecursive);
		$a_xml_writer->xmlEndTag("qtimetadatafield");

		// additional content editing information
		$this->addAdditionalContentEditingModeInformation($a_xml_writer);
		$this->addGeneralMetadata($a_xml_writer);

		$a_xml_writer->xmlEndTag("qtimetadata");
		$a_xml_writer->xmlEndTag("itemmetadata");

		// PART I: qti presentation
		$attrs = array(
			"label" => $this->object->getTitle()
		);
		$a_xml_writer->xmlStartTag("presentation", $attrs);
		// add flow to presentation
		$a_xml_writer->xmlStartTag("flow");
		// add material with question text to presentation
		$this->object->addQTIMaterial($a_xml_writer, $this->object->getQuestion());

		$a_xml_writer->xmlEndTag("flow");
		$a_xml_writer->xmlEndTag("presentation");


		// PART III: qti itemfeedback
		$feedback_allcorrect = $this->object->feedbackOBJ->getGenericFeedbackExportPresentation(
			$this->object->getId(), true
		);

		$feedback_onenotcorrect = $this->object->feedbackOBJ->getGenericFeedbackExportPresentation(
			$this->object->getId(), false
		);

		$attrs = array(
			"ident" => "Correct",
			"view" => "All"
		);
		$a_xml_writer->xmlStartTag("itemfeedback", $attrs);
		// qti flow_mat
		$a_xml_writer->xmlStartTag("flow_mat");
		$a_xml_writer->xmlStartTag("material");
		$a_xml_writer->xmlElement("mattext");
		$a_xml_writer->xmlEndTag("material");
		$a_xml_writer->xmlEndTag("flow_mat");
		$a_xml_writer->xmlEndTag("itemfeedback");
		if (strlen($feedback_allcorrect))
		{
			$attrs = array(
				"ident" => "response_allcorrect",
				"view" => "All"
			);
			$a_xml_writer->xmlStartTag("itemfeedback", $attrs);
			// qti flow_mat
			$a_xml_writer->xmlStartTag("flow_mat");
			$this->object->addQTIMaterial($a_xml_writer, $feedback_allcorrect);
			$a_xml_writer->xmlEndTag("flow_mat");
			$a_xml_writer->xmlEndTag("itemfeedback");
		}
		if (strlen($feedback_onenotcorrect))
		{
			$attrs = array(
				"ident" => "response_onenotcorrect",
				"view" => "All"
			);
			$a_xml_writer->xmlStartTag("itemfeedback", $attrs);
			// qti flow_mat
			$a_xml_writer->xmlStartTag("flow_mat");
			$this->object->addQTIMaterial($a_xml_writer, $feedback_onenotcorrect);
			$a_xml_writer->xmlEndTag("flow_mat");
			$a_xml_writer->xmlEndTag("itemfeedback");
		}

		$a_xml_writer->xmlEndTag("item");
		$a_xml_writer->xmlEndTag("questestinterop");

		$xml = $a_xml_writer->xmlDumpMem(FALSE);
		if (!$a_include_header)
		{
			$pos = strpos($xml, "?>");
			$xml = substr($xml, $pos + 2);
		}
		return $xml;
	}
}

?>