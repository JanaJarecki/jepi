<?php
/* Copyright (c) 1998-2013 ILIAS open source, Extended GPL, see docs/LICENSE */

require_once './Modules/TestQuestionPool/classes/class.assAnswerBinaryState.php';

/**
 * Class for parameters of the assProgQuestion question.
 *
 * assProgQuestionParameters is a class for answers with a binary state
 * indicator (checked/unchecked, set/unset) and an image file
 *
 */
class assProgQuestionParameters
{
	protected $name;
	protected $params;
	protected $order;
	protected $state;
	protected $id;
	protected $points;

	/**
	 * assProgQuestionParameters constructor
	 *
	 * The constructor takes possible arguments an creates an instance of the ASS_AnswerBinaryStateImage object.
	 *
	 * @param string  $answertext A string defining the answer text
	 * @param double  $points     The number of points given for the selected answer
	 * @param integer $order      A nonnegative value representing a possible display or sort order
	 * @param integer $state      A integer value indicating the state of the answer
	 * @param string  $a_image    The image filename
	 * @param integer $id         The database id of the answer
	 *
	 * @return ASS_AnswerBinaryStateImage
	 */
	public function __construct( $name = "", $params = "", $points = 0.0, $order = 0, $state = 0, $id = -1 )
	{
		$this->name = $name;
		$this->params = $params;
		$this->points = $points;
		$this->order = $order;
		$this->state = $state;
		$this->id = $id;
	}
	
	public function getName() { return $this->name; }
	public function getparams() { return $this->params; }
	public function getPoints() { return $this->points; }
	public function getOrder() { return $this->order; }
	public function setOrder($val) { $this->order = $val; }
	public function getState() { return $this->state; }
	public function getId() { return $this->id; }
	
}