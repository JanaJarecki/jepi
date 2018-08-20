<?php

include_once "./Modules/TestQuestionPool/classes/export/qti12/class.assQuestionExport.php";

/**
 * 
 * Export class to export the JEPI question type.
 * @author Andreas Morel-Forster
 *
 */
class assProgQuestionExport extends assQuestionExport {

	
	/**
	 * 
	 * Write label and value in QTI xml format.
	 * @param ilXmlWriter $writer XML writer.
	 * @param string $label Label of the value.
	 * @param string $value Value to write.
	 */
	private static function writeMetaDataField($writer, $label,$value) {
			$writer->xmlStartTag("qtimetadatafield");
			$writer->xmlElement("fieldlabel", NULL, $label);
			$writer->xmlElement("fieldentry", NULL, $value);
			$writer->xmlEndTag("qtimetadatafield");
	}
	
	/**
	 * 
	 * Write a boolean flag in QTI xml format.
	 * @param ilXmlWriter $writer XML writer.
	 * @param string $label Label of the flag.
	 * @param boolean $flag Boolean flag.
	 */
	private static function writeBooleanFlag($writer,$label,$flag) {
		self::writeMetaDataField($writer,$label,strval((int)$flag));
	}
	
	/**
	 * 
	 * Write a set of test parameters as QTI xml representation.
	 * @param ilXmlWriter $writer XML writer.
	 * @param array[assProgQuestionParameters] $parameter_set Set of parameters.
	 */
	private static function writeTestParameters($writer,$parameter_set) {
		self::writeMetaDataField($writer, "NUMTESTPARAMSETS", strval(count($parameter_set)));
		foreach( $parameter_set as $key => $parameters ) {
			self::writeMetaDataField($writer,"TESTPARAMETER_".strval($key),$parameters->getParams());
			self::writeMetaDataField($writer,"TESTPARAMETERPOINTS_".strval($key),strval($parameters->getPoints()));
			self::writeMetaDataField($writer,"TESTPARAMETERORDER_".strval($key),strval($parameters->getOrder()));
		}
	}
	
	private static function writeEstimatedWorkingTime($writer,$time) {
		$duration = sprintf("P0Y0M0DT%dH%dM%dS", $time["h"], $time["m"], $time["s"]);
		$writer->xmlElement("duration", NULL, $duration);
	}
	
	/**
	 * Returns a QTI xml representation of the question.
	 *
	 * @return string The QTI xml representation of the question
	 * @access public
	 */
	function toXML($a_include_header = true, $a_include_binary = true, $a_shuffle = false, $test_output = false, $force_image_references = false) {
		global $ilias;

		include_once("./Services/Xml/classes/class.ilXmlWriter.php");
		$writer = new ilXmlWriter;
		$writer->xmlHeader();
		$writer->xmlStartTag("questestinterop");
		$attrs = array(
			"ident" => "il_" . IL_INST_ID . "_qst_" . $this->object->getId(),
			"title" => $this->object->getTitle(),
			"maxattempts" => $this->object->getNrOfTries()
		);
		$writer->xmlStartTag("item", $attrs);
		
		$writer->xmlElement("qticomment", NULL, $this->object->getComment());
		self::writeEstimatedWorkingTime($writer, $this->object->getEstimatedWorkingTime());
		
		$writer->xmlStartTag("itemmetadata");
		$writer->xmlStartTag("qtimetadata");

		self::writeMetaDataField($writer, "ILIAS_VERSION", $ilias->getSetting("ilias_version"));
		self::writeMetaDataField($writer, "QUESTIONTYPE", $this->object->getQuestionType());
		self::writeMetaDataField($writer, "AUTHOR", $this->object->getAuthor());
		self::writeMetaDataField($writer, "POINTS", $this->object->getPoints());
		self::writeMetaDataField($writer, "SOLUTION", $this->object->getSolution());
		
		// write question specific fields
		self::writeTestParameters($writer,$this->object->getTestParameterSet());
		self::writeBooleanFlag($writer, "CHECKITERATIVE", $this->object->getCheckIterative());
		self::writeBooleanFlag($writer, "CHECKRECURSIVE", $this->object->getCheckRecursive());
		self::writeBooleanFlag($writer, "FORBIDITERATIVE", $this->object->getForbidIterative());
		self::writeBooleanFlag($writer, "FORBIDRECURSIVE", $this->object->getForbidRecursive());


		// additional content editing information
		$this->addAdditionalContentEditingModeInformation($writer);
		$this->addGeneralMetadata($writer);

		$writer->xmlEndTag("qtimetadata");
		$writer->xmlEndTag("itemmetadata");

		// PART I: qti presentation
		$attrs = array(
			"label" => $this->object->getTitle()
		);
		$writer->xmlStartTag("presentation", $attrs);
		// add flow to presentation
		$writer->xmlStartTag("flow");
		// add material with question text to presentation
		$this->object->addQTIMaterial($writer, $this->object->getQuestion());

		$writer->xmlEndTag("flow");
		$writer->xmlEndTag("presentation");

		// PART III: qti itemfeedback
		$feedback_allcorrect = $this->object->feedbackOBJ->getGenericFeedbackExportPresentation($this->object->getId(), true);

		$feedback_onenotcorrect = $this->object->feedbackOBJ->getGenericFeedbackExportPresentation($this->object->getId(), false);

		$attrs = array(
			"ident" => "Correct",
			"view" => "All"
		);
		$writer->xmlStartTag("itemfeedback", $attrs);
		// qti flow_mat
		$writer->xmlStartTag("flow_mat");
		$writer->xmlStartTag("material");
		$writer->xmlElement("mattext");
		$writer->xmlEndTag("material");
		$writer->xmlEndTag("flow_mat");
		$writer->xmlEndTag("itemfeedback");
		if (strlen($feedback_allcorrect)) {
			$attrs = array(
				"ident" => "response_allcorrect",
				"view" => "All"
			);
			$writer->xmlStartTag("itemfeedback", $attrs);
			// qti flow_mat
			$writer->xmlStartTag("flow_mat");
			$this->object->addQTIMaterial($writer, $feedback_allcorrect);
			$writer->xmlEndTag("flow_mat");
			$writer->xmlEndTag("itemfeedback");
		}
		if (strlen($feedback_onenotcorrect)) {
			$attrs = array(
				"ident" => "response_onenotcorrect",
				"view" => "All"
			);
			$writer->xmlStartTag("itemfeedback", $attrs);
			// qti flow_mat
			$writer->xmlStartTag("flow_mat");
			$this->object->addQTIMaterial($writer, $feedback_onenotcorrect);
			$writer->xmlEndTag("flow_mat");
			$writer->xmlEndTag("itemfeedback");
		}

		$writer->xmlEndTag("item");
		$writer->xmlEndTag("questestinterop");

		$xml = $writer->xmlDumpMem(false);
		if (!$a_include_header) {
			$pos = strpos($xml, "?>");
			$xml = substr($xml, $pos + 2);
		}

		return $xml;
	}
}
