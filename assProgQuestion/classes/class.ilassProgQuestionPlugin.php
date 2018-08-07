<?php
include_once "./Modules/TestQuestionPool/classes/class.ilQuestionsPlugin.php";
//require_once __DIR__ . "/class.assProgQplQstMathematikOnlineQuestion.php";
//require_once __DIR__ . "/class.assProgQplQstMoQuestion.php";
require_once __DIR__ . "/class.assProgQplQstProgConfig.php";
require_once __DIR__ . "/class.assProgQplQstProgParams.php";
require_once __DIR__ . "/class.assProgQplQstProgQuest.php";

/**
 * Die Basisklasse des Programmierfragenplugins.
 *
 * @author Matthias Lohmann <lohmann@informatik.uni-koeln.de>
 */
class ilassProgQuestionPlugin extends ilQuestionsPlugin {

	const PLUGIN_ID = 'progquestion';
	const PLUGIN_NAME = 'assProgQuestion';
	/**
	 * @var ilassProgQuestionPlugin
	 */
	protected static $instance;


	/**
	 * @return ilassProgQuestionPlugin
	 */
	public static function getInstance() {
		if (!isset(self::$instance)) {
			self::$instance = new self();
		}

		return self::$instance;
	}


	/**
	 * @var ilDB
	 */
	protected $db;


	/**
	 *
	 */
	public function __construct() {
		parent::__construct();

		global $DIC;

		$this->db = $DIC->database();
	}


	/**
	 * @return string
	 */
	public final function getPluginName() {
		return self::PLUGIN_NAME;
	}


	/**
	 * @return string
	 */
	public final function getQuestionType() {
		return self::PLUGIN_NAME;
	}


	/**
	 * @return string
	 */
	public final function getQuestionTypeTranslation() {
		return $this->txt($this->getQuestionType());
	}


	/**
	 * @return bool
	 */
	protected function beforeUninstall() {
		//$this->db->dropTable(assProgQplQstMathematikOnlineQuestion::TABLE_NAME, false);
		//$this->db->dropTable(assProgQplQstMoQuestion::TABLE_NAME, false);
		$this->db->dropTable(assProgQplQstProgConfig::TABLE_NAME, false);
		$this->db->dropTable(assProgQplQstProgParams::TABLE_NAME, false);
		$this->db->dropTable(assProgQplQstProgQuest::TABLE_NAME, false);

		return true;
	}
}
