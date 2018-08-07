<#1>
<?php
// Create the database entry when the database does not yet exists.
//   1. Ask for the entry of the plugin in `qpl_qst_type`.
//   2. If the entry for the plugin does not exists:
//      a) Get the maximum for the questin_type_id and increment by 1 as our new id.
//      a) Create the database entry for the plugin in `qpl_qst_type`.
// (1)
$res = $ilDB->queryF ( "SELECT * FROM qpl_qst_type WHERE type_tag = %s", array (
		'text' 
), array (
		'assProgQuestion' 
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
			'assProgQuestion',
			1 
	) );
}
?>
<#2>
<?php
// $ilDB->createTable('il_qpl_qst_mo_question',
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

// $ilDB->addPrimaryKey('il_qpl_qst_mo_question', array('question_fi'));
// $ilDB->addIndex('il_qpl_qst_mo_question',array('mo_id'),'i1');
?>
<#3>
<?php
// Umbenennung alter Tabellen.
// if ($ilDB->tableExists("il_qpl_qst_mathematikonline_question"))
// {
// $ilDB->manipulate("RENAME TABLE `il_qpl_qst_mathematikonline_question` TO `il_qpl_qst_mo_question`");
// $ilDB->addIndex('il_qpl_qst_mo_question',array('mo_id'),'i1');
// }
?>
<#4>
<?php
// Erstellung der zusaetzlich benoetigten Tabellen
// question_fi ist in MO wohl die zugehoerige ID der Frage. Warum heisst das so?
$ilDB->createTable ( 'il_qpl_qst_prog_quest', array (
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
$ilDB->addPrimaryKey ( 'il_qpl_qst_prog_quest', array (
		'question_fi' 
) );
?>
<#5>
<?php>
// +answer_id, +question_fi, +answertext, +points, ?aorder, -imagefile, ?tstamp
$ilDB->createTable ( 'il_qpl_qst_prog_params', array (
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
$ilDB->addPrimaryKey ( 'il_qpl_qst_prog_params', array (
		'answer_id' 
) );
?>
<#6>
<?php
$ilDB->createSequence("il_qpl_qst_prog_params");
?>
<#7>
<?php 
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'check_recursive', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'check_iterative', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
?>
<#8>
<?php 
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'forbid_recursive', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'forbid_iterative', array (
		'type' => 'integer',
		'length' => 1,
		'default' => 0,
		'notnull' => true
));
?>
<#9>
<?php 
$ilDB->createTable ( 'il_qpl_qst_prog_config', array (
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
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'quest_type', array (
		'type' => 'text',
		'length' => 64,
		'default' => 'function_original',
		'notnull' => true
));
?>
<#11>
<?php 
$ilDB->addTableColumn('il_qpl_qst_prog_quest', 'test_code', array (
		'type' => 'clob'
));
?>
<#12>
<?php 
$ilDB->addTableColumn('il_qpl_qst_prog_params', 'paramset_name', array (
		'type' => 'text',
				'length' => 4000,
				'default' => '',
				'notnull' => true 
));
?>
