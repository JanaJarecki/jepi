<#1>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.ilassProgQuestionPlugin.php";
// Create the database entry when the database does not yet exists.
//   1. Ask for the entry of the plugin in `qpl_qst_type`.
//   2. If the entry for the plugin does not exists:
//      a) Get the maximum for the questin_type_id and increment by 1 as our new id.
//      a) Create the database entry for the plugin in `qpl_qst_type`.
// (1)
$res = $ilDB->queryF ( "SELECT * FROM qpl_qst_type WHERE type_tag = %s", array (
		'text' 
), array (
	ilassProgQuestionPlugin::PLUGIN_NAME 
) );
// (2)
if ($res->numRows () == 0) {
	// (a)
	$res = $ilDB->query ( "SELECT MAX(question_type_id) maxid FROM qpl_qst_type" );
	$data = $ilDB->fetchAssoc ( $res );
	$max = $data ["maxid"] + 1;
	// (b)
	$affectedRows = $ilDB->manipulateF ( "INSERT INTO qpl_qst_type (question_type_id, type_tag, plugin) VALUES (%s, %s, %s)", array (
			"integer",
			"text",
			"integer" 
	), array (
			$max,
			ilassProgQuestionPlugin::PLUGIN_NAME,
			1 
	) );
}
?>
<#2>
<?php
//require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstMoQuestion.php";
// $ilDB->createTable(assProgQplQstMoQuestion::TABLE_NAME,
// array(
// 'question_fi' =>
// array(
// 'type' => 'integer',
// 'length' => 4,
// 'default' => 0,
// 'notnull' => true
// ),
// 'mo_id' =>
// array(
// 'type' => 'integer',
// 'length' => 4,
// 'default' => 0,
// 'notnull' => true
// ),
// 'mo_variant' =>
// array(
// 'type' => 'integer',
// 'length' => 4,
// 'default' => null,
// 'notnull' => false
// )
// ),
// true
// );

// $ilDB->addPrimaryKey(assProgQplQstMoQuestion::TABLE_NAME, array('question_fi'));
// $ilDB->addIndex(assProgQplQstMoQuestion::TABLE_NAME,array('mo_id'),'i1');
?>
<#3>
<?php
// Umbenennung alter Tabellen.
//require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstMathematikOnlineQuestion.php";
//require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstMoQuestion.php";
// if ($ilDB->tableExists(assProgQplQstMathematikOnlineQuestion::TABLE_NAME))
// {
// $ilDB->manipulate("RENAME TABLE `".assProgQplQstMathematikOnlineQuestion::TABLE_NAME."` TO `".assProgQplQstMoQuestion::TABLE_NAME."`");
// $ilDB->addIndex(assProgQplQstMoQuestion::TABLE_NAME,array('mo_id'),'i1');
// }
?>
<#4>
<?php
// Erstellung der zusaetzlich benoetigten Tabellen
// question_fi ist in MO wohl die zugehoerige ID der Frage. Warum heisst das so?
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgQuest.php";
$ilDB->createTable ( assProgQplQstProgQuest::TABLE_NAME, array (
		'question_fi' => array (
				'type' => 'integer',
				'length' => 4,
				'default' => 0,
				'notnull' => true 
		),
		'solution' => array (
				'type' => 'text',
				'length' => 4000,
				'default' => '',
				'notnull' => true 
		) 
), true );
$ilDB->addPrimaryKey ( assProgQplQstProgQuest::TABLE_NAME, array (
		'question_fi' 
) );
?>
<#5>
<?php
// +answer_id, +question_fi, +answertext, +points, ?aorder, -imagefile, ?tstamp
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgParams.php";
$ilDB->createTable ( assProgQplQstProgParams::TABLE_NAME, array (
		'answer_id' => array (
				'type' => 'integer',
				'length' => 4,
				'default' => 0,
				'notnull' => true 
		),
		'question_fi' => array (
				'type' => 'integer',
				'length' => 4,
				'default' => 0,
				'notnull' => true 
		),
		'params' => array (
				'type' => 'text',
				'length' => 4000,
				'default' => '',
				'notnull' => true 
		),
		'points' => array (
				'type' => 'float',
				'default' => 0,
				'notnull' => true 
		),
		'aorder' => array (
				'type' => 'integer',
				'length' => 4,
				'default' => 0,
				'notnull' => true 
		) 
), true );
$ilDB->addPrimaryKey ( assProgQplQstProgParams::TABLE_NAME, array (
		'answer_id' 
) );
?>
<#6>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgParams.php";
$ilDB->createSequence(assProgQplQstProgParams::TABLE_NAME);
?>
<#7>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgQuest.php";
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'check_recursive', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'check_iterative', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
?>
<#8>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgQuest.php";
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'forbid_recursive', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'forbid_iterative', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
?>
<#9>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgConfig.php";
$ilDB->createTable ( assProgQplQstProgConfig::TABLE_NAME, array (
		'name' => array (
				'type' => 'text',
				'length' => 30,
				'default' => 0,
				'notnull' => true 
		),
		'value' => array (
				'type' => 'text',
				'length' => 4000,
				'default' => '',
				'notnull' => true 
		) 
), true );
?>
<#10>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgQuest.php";
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'quest_type', array (
		'type' => 'text',
		'length' => 64,
		'default' => 'function_original',
		'notnull' => true
));
?>
<#11>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgQuest.php";
$ilDB->addTableColumn(assProgQplQstProgQuest::TABLE_NAME, 'test_code', array (
		'type' => 'clob'
));
?>
<#12>
<?php
require_once "Customizing/global/plugins/Modules/TestQuestionPool/Questions/assProgQuestion/classes/class.assProgQplQstProgParams.php";
$ilDB->addTableColumn(assProgQplQstProgParams::TABLE_NAME, 'paramset_name', array (
		'type' => 'text',
				'length' => 4000,
				'default' => '',
				'notnull' => true 
));
?>
