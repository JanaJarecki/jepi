<?php
/*
 * +-----------------------------------------------------------------------------+ | ILIAS open source | +-----------------------------------------------------------------------------+ | Copyright (c) 1998-2007 ILIAS open source, University of Cologne | | | | This program is free software; you can redistribute it and/or | | modify it under the terms of the GNU General Public License | | as published by the Free Software Foundation; either version 2 | | of the License, or (at your option) any later version. | | | | This program is distributed in the hope that it will be useful, | | but WITHOUT ANY WARRANTY; without even the implied warranty of | | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the | | GNU General Public License for more details. | | | | You should have received a copy of the GNU General Public License | | along with this program; if not, write to the Free Software | | Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. | +-----------------------------------------------------------------------------+
 */
include_once("./Services/Form/classes/class.ilTextAreaInputGUI.php");

/**
 * This class represents a text area property in a property form.
 *
 * @author     Alex Killing <alex.killing@gmx.de>, Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 * @version    $Id: class.ilProgrammingTextAreaInputGUI.php 1318 2010-03-03 10:19:37Z hschottm $
 * @ingroup    ServicesForm
 *
 */
class assProgQuestionCodeArea extends ilTextAreaInputGUI {

	/**
	 * Insert property html
	 *
	 * @return int
	 */
	function insert($a_tpl) {
		$pl = ilassProgQuestionPlugin::getInstance();
		$ttpl = $pl->getTemplate("tpl.prop_programmingarea.html");

		// disabled rte
		if ($this->getUseRte() && $this->getDisabled()) {
			$ttpl->setCurrentBlock("disabled_rte");
			$ttpl->setVariable("DR_VAL", $this->getValue());
			$ttpl->parseCurrentBlock();
		} else {
			if ($this->getUseRte()) {
				include_once "./Services/RTE/classes/class.ilRTE.php";
				$rtestring = ilRTE::_getRTEClassname();
				include_once "./Services/RTE/classes/class.$rtestring.php";
				/**
				 * @var ilRTE $rte
				 */
				$rte = new $rtestring ();

				// @TODO: Check this.
				$rte->addPlugin("emotions");
				foreach ($this->plugins as $plugin) {
					if (strlen($plugin)) {
						$rte->addPlugin($plugin);
					}
				}
				foreach ($this->removeplugins as $plugin) {
					if (strlen($plugin)) {
						$rte->removePlugin($plugin);
					}
				}

				foreach ($this->buttons as $button) {
					if (strlen($button)) {
						$rte->addButton($button);
					}
				}

				$rte->disableButtons($this->getDisabledButtons());

				if ($this->getRTERootBlockElement() !== NULL) {
					$rte->setRTERootBlockElement($this->getRTERootBlockElement());
				}

				if (count($this->rteSupport) >= 3) {
					$rte->addRTESupport($this->rteSupport ["obj_id"], $this->rteSupport ["obj_type"], $this->rteSupport ["module"], true, $this->rteSupport ['cfg_template']);
				} else {
					$rte->addCustomRTESupport(0, "", $this->getRteTags());
				}

				$ttpl->touchBlock("prop_ta_w");
				//$ttpl->setCurrentBlock("prop_textarea");
				$ttpl->setVariable("ROWS", $this->getRows());
			} else {
				$ttpl->touchBlock("no_rteditor");

				if ($this->getCols() > 5) {
					$ttpl->setCurrentBlock("prop_ta_c");
					$ttpl->setVariable("COLS", $this->getCols());
					$ttpl->parseCurrentBlock();
				} else {
					$ttpl->touchBlock("prop_ta_w");
				}

				//$ttpl->setCurrentBlock("prop_textarea"); // TODO: block not found, even though plugin template?
				$ttpl->setVariable("ROWS", $this->getRows());
			}
			if (!$this->getDisabled()) {
				$ttpl->setVariable("POST_VAR", $this->getPostVar());
			}
			$ttpl->setVariable("ID", $this->getFieldId());
			if ($this->getDisabled()) {
				$ttpl->setVariable('DISABLED', 'disabled="disabled" ');
			}
			$ttpl->setVariable("PROPERTY_VALUE", ilUtil::prepareFormOutput($this->getValue()));
			$ttpl->parseCurrentBlock();
		}

		if ($this->getDisabled()) {
			$ttpl->setVariable("HIDDEN_INPUT", $this->getHiddenTag($this->getPostVar(), $this->getValue()));
		}

		$a_tpl->setCurrentBlock("prop_generic");
		$a_tpl->setVariable("PROP_GENERIC", $ttpl->get());
		$a_tpl->parseCurrentBlock();
	}


	/**
	 * Check input, strip slashes etc. set alert, if input is not ok.
	 * Wir reimplementieren das, damit Code nicht durch < abgeschnitten wird
	 *
	 * @return    boolean        Input ok, true/false
	 */
	function checkInput() {
		global $lng;
		include_once("./Services/AdvancedEditing/classes/class.ilObjAdvancedEditing.php");

		if ($this->usePurifier() && $this->getPurifier()) {
			$_POST[$this->getPostVar()] = ilUtil::stripOnlySlashes($_POST[$this->getPostVar()]);
			$_POST[$this->getPostVar()] = $this->getPurifier()->purify($_POST[$this->getPostVar()]);
		} else {
			$allowed = $this->getRteTagString();
			if ($this->plugins["latex"] == "latex" && !is_int(strpos($allowed, "<span>"))) {
				$allowed .= "<span>";
			}
			$_POST[$this->getPostVar()] = ($this->getUseRte()
				|| !$this->getUseTagsForRteOnly()) ? ilUtil::stripSlashes($_POST[$this->getPostVar()], false, $allowed) // false - kein HTML entfernen
				: ilUtil::stripSlashes($_POST[$this->getPostVar()], false); // s.o.
		}

		if ($this->getRequired() && trim($_POST[$this->getPostVar()]) == "") {
			$this->setAlert($lng->txt("msg_input_is_required"));

			return false;
		}

		return $this->checkSubItemsInput();
	}
}
