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

class assProgQuestionGUIComponentFactory {

	private $plugin;
	private $lng;


	private function txt($txt) {
		return $this->plugin->txt($txt);
	}


	private function lng($txt) {
		return $this->lng->txt($txt);
	}


	public function __construct($plugin, $lng) {
		$this->plugin = $plugin;
		$this->lng = $lng;
	}


	public function title($title) {
		$elem = new ilTextInputGUI ($this->lng("title"), "title");
		$elem->setValue($title);
		$elem->setRequired(true);

		return $elem;
	}


	public function hiddenAuthor($name) {
		$hidden = new ilHiddenInputGUI ("author");
		$author = ilUtil::prepareFormOutput($name);
		if (trim($author) == "") {
			$author = "-";
		}
		$hidden->setValue($author);

		return $hidden;
	}


	public function author($name) {
		$author = new ilTextInputGUI ($this->lng("author"), "author");
		$author->setValue($name);
		$author->setRequired(true);

		return $author;
	}


	public function description($desc) {
		$description = new ilTextInputGUI ($this->lng("description"), "comment");
		$description->setValue($desc);
		$description->setRequired(false);

		return $description;
	}


	public function question($text, $id) {
		include_once "./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php";
		$tags = ilObjAdvancedEditing::_getUsedHTMLTags("assessment");
		array_push($tags, 'input');
		array_push($tags, 'select');
		array_push($tags, 'option');
		array_push($tags, 'button');
		$question = new ilTextAreaInputGUI ($this->lng("question"), "question");
		$question->setValue($text);
		$question->setRequired(true);
		$question->setRows(10);
		$question->setCols(80);
		$question->setUseRte(true);
		$question->setRteTags($tags);
		$question->addPlugin("latex");
		$question->addButton("latex");
		$question->addButton("pastelatex");
		$question->setRTESupport($id, "qpl", "assessment");

		return $question;
	}


	public function duration($ewt) {
		$duration = new ilDurationInputGUI ($this->lng("working_time"), "Estimated");
		$duration->setShowHours(true);
		$duration->setShowMinutes(true);
		$duration->setShowSeconds(true);
		$duration->setHours($ewt ["h"]);
		$duration->setMinutes($ewt ["m"]);
		$duration->setSeconds($ewt ["s"]);
		$duration->setRequired(false);

		return $duration;
	}


	public function numberOfTries($nbrOfTries, $default = 0) {
		if (strlen($nbrOfTries)) {
			$nr_tries = $nbrOfTries;
		} else {
			$nr_tries = $default;
		}
		if ($nr_tries <= 0) {
			$nr_tries = 1;
		}
		$ni = new ilNumberInputGUI ($this->lng("qst_nr_of_tries"), "nr_of_tries");
		$ni->setValue($nr_tries);
		$ni->setMinValue(1);
		$ni->setSize(5);
		$ni->setMaxLength(5);
		$ni->setRequired(true);
	}


	public function maximumPoints($N) {
		$points = new ilNumberInputGUI ($this->lng("points"), "points");
		$points->setValue($N);
		$points->setRequired(true);
		$points->setSize(3);
		$points->setMinValue(0.0);

		return $points;
	}


	public function solution($text) {
		$this->plugin->includeClass("class.assProgQuestionCodeArea.php");
		$solution = new assProgQuestionCodeArea ($this->txt("solution"), "solution");
		$solution->setValue($text);
		$solution->setRequired(true);
		$solution->setRows(10);
		$solution->setCols(80);
		$solution->setInfo($this->txt("solutioninfo"));

		return $solution;
	}


	public function testCodeField($code = "") {
        $this->plugin->includeClass("class.assProgQuestionCodeArea.php");
		$testCode = new assProgQuestionCodeArea ($this->txt("testCode"), "test_code");
		$testCode->setValue($code);
		$testCode->setRequired(true);
		$testCode->setRows(10);
		$testCode->setCols(80);
		$testCode->setInfo($this->txt("testCodeInfo"));

		return $testCode;
	}


	public function questionType($question_type, $quest_types) {
		$quest_names = array();
		foreach ($quest_types as $qt) {
			$quest_names [] = $this->txt($qt);
		}
		$assProgQuestionTypeOptionName = $this->txt('quest_type');
		$assProgQuestionType = new ilSelectInputGUI ($assProgQuestionTypeOptionName, 'quest_type');
		$assProgQuestionType->setOptions($quest_names);
		$assProgQuestionType->setRequired(true);
		$assProgQuestionType->setValue(array_search($question_type, $quest_types));

		return $assProgQuestionType;
	}


	public function methodTypeRequirements($recYes, $recNo, $iterYes, $iterNo) {

		$none = new ilRadioOption ($this->txt('nostructure'), "none", $this->txt('nostructureinfo'));
		$rec = new ilRadioOption ($this->txt('recursive'), "recursive", $this->txt('recursiveinfo'));
		$recNotIter = new ilRadioOption ($this->txt('recursivenoiterative'), "recursivenoiterative", $this->txt('recursivenoiterativeinfo'));
		$iter = new ilRadioOption ($this->txt('iterative'), "iterative", $this->txt('iterativeinfo'));
		$iterNotRec = new ilRadioOption ($this->txt('iterativenorecursive'), "iterativenorecursive", $this->txt('iterativenorecursiveinfo'));

		$radio_prop = new ilRadioGroupInputGUI ($this->txt('codestructure'), "structure");
		$radio_prop->addOption($none);
		$radio_prop->addOption($rec);
		$radio_prop->addOption($recNotIter);
		$radio_prop->addOption($iter);
		$radio_prop->addOption($iterNotRec);

		if ($recYes) {
			$radio_prop->setValue($iterNo ? "recursivenoiterative" : "recursive");
		} elseif ($iterYes) {
			$radio_prop->setValue($recNo ? "iterativenorecursive" : "iterative");
		} else {
			$radio_prop->setValue("none");
		}

		return $radio_prop;
	}


	public function parameters($parameterSet, $testSituation) {
		include_once 'class.assProgQuestionParametersInputGUI.php';

		$this->plugin->includeClass("class.assProgQuestionParameters.php");
		$choices = new assProgQuestionParametersInputGUI ($this->txt("testparams"), "choice");
		$choices->setRequired(false);
		//$choices->setQuestionObject ( $question );
		$choices->setSingleline(true);
		$choices->setAllowMove(true);
		$choices->setInfo($this->txt("paraminfo"));
		if ($testSituation) {
			$choices->setSize(40);
			$choices->setMaxLength(800);
		}
		$params = array();
		if (count($parameterSet) < 1) {
			array_push($params, new assProgQuestionParameters("", "", 0, 1));
		} else {
			$params = $parameterSet;
		}
		$choices->setValues($params);

		return $choices;
	}


	public function hiddenId($id) {
		$hidden = new ilHiddenInputGUI ("", "ID");
		$hidden->setValue($id);

		return $hidden;
	}
}
