<?php
class Account extends Data { 
    static $validates_numericality_of = array(
        array('amount', 'greater_than' => 0, 'message' => 'Insufficient funds')
    );
    //relations to other Data objects not yet defined (has_many, has_one, belongs_to and so on)
    //static $has_many = array(array('Transfer'));
}
?>