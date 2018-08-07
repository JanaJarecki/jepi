<?php

include_once "./Modules/TestQuestionPool/classes/import/qti12/class.assQuestionImport.php";

/**
 * Class for accounting question import
 *
 * @author     Fred Neumann <fred.neumann@fim.unierlangen.de>
 * @version    $Id: $
 * @ingroup    ModulesTestQuestionPool
 */
class assProgQuestionImport extends assQuestionImport {

	/**
	 * Creates a question from a QTI file
	 *
	 * Receives parameters from a QTI parser and creates a valid ILIAS question object
	 *
	 * @param object  $item             The QTI item object
	 * @param integer $questionpool_id  The id of the parent questionpool
	 * @param integer $tst_id           The id of the parent test if the question is part of a test
	 * @param object  $tst_object       A reference to the parent test object
	 * @param integer $question_counter A reference to a question counter to count the questions of an imported question pool
	 * @param array   $import_mapping   An array containing references to included ILIAS objects
	 *
	 * @access public
	 */
	function fromXML(&$item, $questionpool_id, &$tst_id, &$tst_object, &$question_counter, &$import_mapping) {
		global $ilUser, $ilLog;

		// empty session variable for imported xhtml mobs
		unset($_SESSION["import_mob_xhtml"]);
		$presentation = $item->getPresentation();
		$duration = $item->getDuration();
		$now = getdate();
		$created = sprintf("%04d%02d%02d%02d%02d%02d", $now['year'], $now['mon'], $now['mday'], $now['hours'], $now['minutes'], $now['seconds']);

		// get the generic feedbach
		$feedbacksgeneric = array();
		if (isset($item->itemfeedback)) {
			foreach ($item->itemfeedback as $ifb) {
				if (strcmp($ifb->getIdent(), "response_allcorrect") == 0) {
					// found a feedback for the identifier
					if (count($ifb->material)) {
						foreach ($ifb->material as $material) {
							$feedbacksgeneric[1] = $material;
						}
					}
					if ((count($ifb->flow_mat) > 0)) {
						foreach ($ifb->flow_mat as $fmat) {
							if (count($fmat->material)) {
								foreach ($fmat->material as $material) {
									$feedbacksgeneric[1] = $material;
								}
							}
						}
					}
				} else {
					if (strcmp($ifb->getIdent(), "response_onenotcorrect") == 0) {
						// found a feedback for the identifier
						if (count($ifb->material)) {
							foreach ($ifb->material as $material) {
								$feedbacksgeneric[0] = $material;
							}
						}
						if ((count($ifb->flow_mat) > 0)) {
							foreach ($ifb->flow_mat as $fmat) {
								if (count($fmat->material)) {
									foreach ($fmat->material as $material) {
										$feedbacksgeneric[0] = $material;
									}
								}
							}
						}
					}
				}
			}
		}

		// set question properties
		//Die Grundinformationen der Frage werden importiert
		$this->addGeneralMetadata($item);
		$this->object->setTitle($item->getTitle());
		$this->object->setNrOfTries($item->getMaxattempts());
		$this->object->setComment($item->getComment());
		$this->object->setAuthor($item->getAuthor());
		$this->object->setOwner($ilUser->getId());
		$this->object->setQuestion($this->object->QTIMaterialToString($item->getQuestiontext()));
		$this->object->setObjId($questionpool_id);
		$this->object->setEstimatedWorkingTime($duration["h"], $duration["m"], $duration["s"]);
		$this->object->setPoints($item->getMetadataEntry("POINTS"));
		//NEW - Die Musterloesung wird importiert
		$this->object->setSolution($item->getMetadataEntry("SOLUTION"));

		//NEW - Die Paramter werden importiert
		//$number_paramsets = intval(($item->getMetadataEntry("NUMTESTPARAMSETS")));

		for ($i = 0; $i < intval($item->getMetadataEntry("NUMTESTPARAMSETS")); $i ++) {
			$label_name_params = "TESTPARAMETER_";
			$label_name_params .= strval($i);

			$label_name_points = "TESTPARAMETERPOINTS_";
			$label_name_points .= strval($i);

			$label_name_order = "TESTPARAMETERORDER_";
			$label_name_order .= strval($i);

			$label_name_image = "TESTPARAMETERIMAGE_";
			$label_name_image .= strval($i);

			$params = $item->getMetadataEntry($label_name_params);
			$points = floatval($item->getMetadataEntry($label_name_points));
			$order = intval($item->getMetadataEntry($label_name_order));
			$image = $item->getMetadataEntry($label_name_image);

			$this->object->addTestParameterset($params, $points, $order, $image);
		}

		//NEW - checkIterative wird importiert
		if (intval($item->getMetadataEntry("CHECKITERATIVE")) == 1) {
			$this->object->setCheckIterative(true);
		} else {
			$this->object->setCheckIterative(false);
		}

		//NEW - checkRecursive wird importiert
		if (intval($item->getMetadataEntry("CHECKRECURSIVE")) == 1) {
			$this->object->setCheckRecursive(true);
		} else {
			$this->object->setCheckRecursive(false);
		}

		//NEW - forbidIterative wird importiert
		if (intval($item->getMetadataEntry("FORBIDITERATIVE")) == 1) {
			$this->object->setForbidIterative(true);
		} else {
			$this->object->setForbidIterative(false);
		}

		//NEW - forbidRecursive wird importiert
		if (intval($item->getMetadataEntry("FORBIDRECURSIVE")) == 1) {
			$this->object->setForbidRecursive(true);
		} else {
			$this->object->setForbidRecursive(false);
		}

		// additional content editing mode information
		$this->object->setAdditionalContentEditingMode($this->fetchAdditionalContentEditingModeInformation($item));

		// first save the question to get a new question id
		$this->object->saveToDb();

		// convert the generic feedback
		foreach ($feedbacksgeneric as $correctness => $material) {
			$m = $this->object->QTIMaterialToString($material);
			$feedbacksgeneric[$correctness] = $m;
		}

		// handle the import of media objects in XHTML code
		$questiontext = $this->object->getQuestion();
		if (is_array($_SESSION["import_mob_xhtml"])) {
			include_once "./Services/MediaObjects/classes/class.ilObjMediaObject.php";
			include_once "./Services/RTE/classes/class.ilRTE.php";
			foreach ($_SESSION["import_mob_xhtml"] as $mob) {
				if ($tst_id > 0) {
					$importfile = $this->getTstImportArchivDirectory() . '/' . $mob["uri"];
				} else {
					$importfile = $this->getQplImportArchivDirectory() . '/' . $mob["uri"];
				}
				global $ilLog;
				$ilLog->write($importfile);

				$media_object =& ilObjMediaObject::_saveTempFileAsMediaObject(basename($importfile), $importfile, false);
				ilObjMediaObject::_saveUsage($media_object->getId(), "qpl:html", $this->object->getId());

				// images in question text
				$questiontext = str_replace("src=\"" . $mob["mob"] . "\"", "src=\"" . "il_" . IL_INST_ID . "_mob_" . $media_object->getId()
					. "\"", $questiontext);

				// images in feedback
				foreach ($feedbacksgeneric as $correctness => $material) {
					$feedbacksgeneric[$correctness] = str_replace("src=\"" . $mob["mob"] . "\"", "src=\"" . "il_" . IL_INST_ID . "_mob_"
						. $media_object->getId() . "\"", $material);
				}
			}
		}

		$this->object->setQuestion(ilRTE::_replaceMediaObjectImageSrc($questiontext, 1));
		foreach ($feedbacksgeneric as $correctness => $material) {
			$this->object->feedbackOBJ->importGenericFeedback($this->object->getId(), $correctness, ilRTE::_replaceMediaObjectImageSrc($material, 1));
		}

		// Now save the question again
		$this->object->saveToDb();

		// import mapping for tests
		if ($tst_id > 0) {
			$q_1_id = $this->object->getId();
			$question_id = $this->object->duplicate(true, NULL, NULL, NULL, $tst_id);
			$tst_object->questions[$question_counter ++] = $question_id;
			$import_mapping[$item->getIdent()] = array( "pool" => $q_1_id, "test" => $question_id );
		} else {
			$import_mapping[$item->getIdent()] = array( "pool" => $this->object->getId(), "test" => 0 );
		}
	}
}
